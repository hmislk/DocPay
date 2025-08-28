package org.carecode.docpay.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.image.Image;
import org.carecode.docpay.model.AppSettings;
import org.carecode.docpay.service.SettingsService;

public class SettingsController {
    @FXML private TextField txtHospitalName;
    @FXML private TextField txtAddr1;
    @FXML private TextField txtAddr2;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtReceiptTitle;
    @FXML private TextField txtFooter1;
    @FXML private TextField txtFooter2;
    @FXML private Label lblError;
    @FXML private ImageView imgLogo;
    @FXML private ComboBox<String> cmbPrinter;
    @FXML private ComboBox<String> cmbOrientation;
    @FXML private TextField txtPageW;
    @FXML private TextField txtPageH;
    @FXML private TextField txtMT;
    @FXML private TextField txtMR;
    @FXML private TextField txtMB;
    @FXML private TextField txtML;
    @FXML private TextField txtScale;

    private AppSettings settings;
    private byte[] logoBytes;

    @FXML
    public void initialize() {
        settings = SettingsService.get();
        loadPrinters();
        loadToForm();
    }

    private void loadToForm() {
        txtHospitalName.setText(settings.getHospitalName());
        txtAddr1.setText(settings.getAddressLine1());
        txtAddr2.setText(settings.getAddressLine2());
        txtPhone.setText(settings.getPhone());
        txtEmail.setText(settings.getEmail());
        txtReceiptTitle.setText(settings.getReceiptTitle());
        txtFooter1.setText(settings.getReceiptFooter1());
        txtFooter2.setText(settings.getReceiptFooter2());
        logoBytes = settings.getLogo();
        refreshLogoPreview();
        cmbPrinter.getItems().add(0, "(System Default)");
        if (settings.getPrinterName() != null && !settings.getPrinterName().isBlank()) {
            cmbPrinter.getSelectionModel().select(settings.getPrinterName());
        } else {
            cmbPrinter.getSelectionModel().select(0);
        }
        cmbOrientation.getItems().setAll("PORTRAIT", "LANDSCAPE");
        cmbOrientation.getSelectionModel().select(settings.getOrientation() == null ? "PORTRAIT" : settings.getOrientation());
        txtPageW.setText(fmt(settings.getPageWidthMm()));
        txtPageH.setText(fmt(settings.getPageHeightMm()));
        txtMT.setText(fmt(settings.getMarginTopMm()));
        txtMR.setText(fmt(settings.getMarginRightMm()));
        txtMB.setText(fmt(settings.getMarginBottomMm()));
        txtML.setText(fmt(settings.getMarginLeftMm()));
        txtScale.setText(String.valueOf(settings.getScalePercent() == null ? 100 : settings.getScalePercent()));
    }

    @FXML
    public void onSave() {
        String name = safe(txtHospitalName.getText());
        if (name.isEmpty()) { lblError.setText("Hospital name is required"); txtHospitalName.requestFocus(); return; }
        String title = safe(txtReceiptTitle.getText());
        if (title.isEmpty()) { lblError.setText("Receipt title is required"); txtReceiptTitle.requestFocus(); return; }
        lblError.setText("");
        settings.setHospitalName(name);
        settings.setAddressLine1(safe(txtAddr1.getText()));
        settings.setAddressLine2(safe(txtAddr2.getText()));
        settings.setPhone(safe(txtPhone.getText()));
        settings.setEmail(safe(txtEmail.getText()));
        settings.setReceiptTitle(title);
        settings.setReceiptFooter1(safe(txtFooter1.getText()));
        settings.setReceiptFooter2(safe(txtFooter2.getText()));
        settings.setLogo(logoBytes);
        String selPrinter = cmbPrinter.getSelectionModel().getSelectedItem();
        settings.setPrinterName(selPrinter != null && !selPrinter.equals("(System Default)") ? selPrinter : "");
        settings.setOrientation(cmbOrientation.getSelectionModel().getSelectedItem());
        settings.setPageWidthMm(parseD(txtPageW.getText(), 210));
        settings.setPageHeightMm(parseD(txtPageH.getText(), 297));
        settings.setMarginTopMm(parseD(txtMT.getText(), 12));
        settings.setMarginRightMm(parseD(txtMR.getText(), 12));
        settings.setMarginBottomMm(parseD(txtMB.getText(), 12));
        settings.setMarginLeftMm(parseD(txtML.getText(), 12));
        settings.setScalePercent(parseI(txtScale.getText(), 100));
        SettingsService.save(settings);
        new Alert(Alert.AlertType.INFORMATION, "Settings saved").showAndWait();
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private void refreshLogoPreview() {
        if (logoBytes != null && logoBytes.length > 0) {
            imgLogo.setImage(new Image(new java.io.ByteArrayInputStream(logoBytes)));
        } else {
            imgLogo.setImage(null);
        }
    }

    @FXML
    public void onChooseLogo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Logo Image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        java.io.File f = fc.showOpenDialog(txtHospitalName.getScene().getWindow());
        if (f != null) {
            try {
                logoBytes = java.nio.file.Files.readAllBytes(f.toPath());
                refreshLogoPreview();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to load image: " + ex.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    public void onClearLogo() {
        logoBytes = null;
        refreshLogoPreview();
    }

    private void loadPrinters() {
        cmbPrinter.getItems().clear();
        for (Printer p : Printer.getAllPrinters()) {
            cmbPrinter.getItems().add(p.getName());
        }
        if (cmbPrinter.getItems().isEmpty()) {
            cmbPrinter.getItems().add("(System Default)");
            cmbPrinter.getSelectionModel().select(0);
        }
    }

    private String fmt(Double v) { return v == null ? "" : String.format("%.2f", v); }
    private double parseD(String s, double def) { try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; } }
    private int parseI(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } }

    @FXML public void setPresetA4() { txtPageW.setText("210"); txtPageH.setText("297"); }
    @FXML public void setPreset80mm() { txtPageW.setText("80"); txtPageH.setText("200"); }
}
