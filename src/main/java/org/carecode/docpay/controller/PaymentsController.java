package org.carecode.docpay.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.carecode.docpay.model.DoctorPayment;

public class PaymentsController implements PaymentNavigator {
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        // nothing yet
    }

    private void setContent(Node n) { contentArea.getChildren().setAll(n); }

    @FXML public void showNewPayment() { loadForm(); }
    @FXML public void showSearch() { loadSearch(); }

    private void loadForm() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/PaymentForm.fxml"));
            Node n = fx.load();
            PaymentFormController ctrl = fx.getController();
            ctrl.setNavigator(this);
            setContent(n);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadSearch() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/PaymentSearch.fxml"));
            Node n = fx.load();
            PaymentSearchController ctrl = fx.getController();
            ctrl.setNavigator(this);
            setContent(n);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @Override
    public void goToPrint(DoctorPayment payment) {
        if (payment == null) { loadForm(); return; }
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/PaymentPrint.fxml"));
            Node n = fx.load();
            PaymentPrintController ctrl = fx.getController();
            ctrl.setNavigator(this);
            ctrl.showPayment(payment);
            setContent(n);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
