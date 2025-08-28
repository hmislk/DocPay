package org.carecode.docpay.controller;

import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.print.Printer;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.carecode.docpay.model.DoctorPayment;
import org.carecode.docpay.service.PaymentService;
import org.carecode.docpay.model.AppSettings;
import org.carecode.docpay.service.SettingsService;

import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PaymentPrintController {
    @FXML private StackPane previewArea;
    private PaymentNavigator navigator;
    private DoctorPayment payment;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setNavigator(PaymentNavigator navigator) { this.navigator = navigator; }

    public void showPayment(DoctorPayment payment) {
        this.payment = payment;
        previewArea.getChildren().setAll(buildReceiptNode(payment, false));
    }

    @FXML
    public void onPrint() {
        if (payment == null) return;
        Node toPrint = buildReceiptNode(payment, payment.getPrintCount() > 0);
        AppSettings s = SettingsService.get();
        Printer selected = null;
        if (s.getPrinterName() != null && !s.getPrinterName().isBlank()) {
            for (Printer p : Printer.getAllPrinters()) {
                if (p.getName().equalsIgnoreCase(s.getPrinterName())) { selected = p; break; }
            }
        }
        PrinterJob job = selected != null ? PrinterJob.createPrinterJob(selected) : PrinterJob.createPrinterJob();
        if (job == null) { new Alert(Alert.AlertType.ERROR, "No printer available").showAndWait(); return; }
        // Configure page layout
        PageOrientation orient = "LANDSCAPE".equalsIgnoreCase(s.getOrientation()) ? PageOrientation.LANDSCAPE : PageOrientation.PORTRAIT;
        Paper paper = Paper.A4;
        try {
            // Attempt to match a supported paper by width/height mm (approx tolerance)
            double targetWpt = mmToPoints(nvl(s.getPageWidthMm(), 210.0));
            double targetHpt = mmToPoints(nvl(s.getPageHeightMm(), 297.0));
            Printer usePrinter = job.getPrinter();
            double bestScore = Double.MAX_VALUE;
            for (Paper pp : usePrinter.getPrinterAttributes().getSupportedPapers()) {
                double dw = Math.abs(pp.getWidth() - targetWpt);
                double dh = Math.abs(pp.getHeight() - targetHpt);
                double score = dw + dh;
                if (score < bestScore) { bestScore = score; paper = pp; }
            }
        } catch (Exception ignore) {}
        double left = mmToPoints(nvl(s.getMarginLeftMm(), 12.0));
        double right = mmToPoints(nvl(s.getMarginRightMm(), 12.0));
        double top = mmToPoints(nvl(s.getMarginTopMm(), 12.0));
        double bottom = mmToPoints(nvl(s.getMarginBottomMm(), 12.0));
        PageLayout layout = job.getPrinter().createPageLayout(paper, orient, left, right, top, bottom);
        job.getJobSettings().setPageLayout(layout);
        // Scale content
        double scale = (s.getScalePercent() == null ? 100 : s.getScalePercent()) / 100.0;
        toPrint.setScaleX(scale);
        toPrint.setScaleY(scale);
        boolean ok = job.printPage(toPrint);
        if (ok) {
            job.endJob();
            try { PaymentService.incrementPrintCount(payment.getId()); } catch (Exception ignored) {}
        }
    }

    @FXML
    public void onNewPayment() {
        if (navigator != null) navigator.goToPrint(null); // We will interpret null as request to go to new payment form
    }

    private Node buildReceiptNode(DoctorPayment p, boolean duplicate) {
        AppSettings s = SettingsService.get();
        VBox box = new VBox(10);
        VBox header = new VBox(2);
        header.setAlignment(Pos.CENTER);
        if (s.getLogo() != null && s.getLogo().length > 0) {
            ImageView iv = new ImageView(new Image(new java.io.ByteArrayInputStream(s.getLogo())));
            iv.setPreserveRatio(true);
            iv.setFitWidth(220);
            header.getChildren().add(iv);
        }
        Text title = new Text(s.getHospitalName());
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        Text subtitle = new Text(s.getReceiptTitle());
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 12));
        Label dup = new Label(duplicate ? "DUPLICATE" : "");
        dup.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
        String when = dtf.format(p.getPaidAt().atZone(ZoneId.systemDefault()));
        String amt = p.getValue().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String addr = String.join(" ", safe(s.getAddressLine1()), safe(s.getAddressLine2())).trim();
        String contact = (safe(s.getPhone()) + (safe(s.getEmail()).isEmpty() ? "" : "  |  " + safe(s.getEmail()))).trim();

        header.getChildren().add(title);
        if (!addr.isEmpty()) header.getChildren().add(new Label(addr));
        if (!contact.isEmpty()) header.getChildren().add(new Label(contact));
        header.getChildren().add(subtitle);
        if (duplicate) header.getChildren().add(dup);
        box.getChildren().addAll(header, new Separator());
        box.getChildren().addAll(
                new Label("Receipt: " + p.getReceiptId()),
                new Label("Date: " + when),
                new Label("Doctor: " + p.getDoctor().getName()),
                new Label("Amount: Rs. " + amt),
                new Label("Description: " + (p.getDescription() == null ? "" : p.getDescription()))
        );
        if (!safe(s.getReceiptFooter1()).isEmpty() || !safe(s.getReceiptFooter2()).isEmpty()) {
            box.getChildren().add(new Separator());
            if (!safe(s.getReceiptFooter1()).isEmpty()) box.getChildren().add(new Label(s.getReceiptFooter1()));
            if (!safe(s.getReceiptFooter2()).isEmpty()) box.getChildren().add(new Label(s.getReceiptFooter2()));
        }
        BorderPane wrapper = new BorderPane(box);
        wrapper.setStyle("-fx-padding: 24; -fx-border-color: #ddd; -fx-border-width: 1; -fx-font-size: 12px;");
        return wrapper;
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
    private double mmToPoints(double mm) { return mm * 72.0 / 25.4; }
    private double nvl(Double v, double def) { return v == null ? def : v; }
}
