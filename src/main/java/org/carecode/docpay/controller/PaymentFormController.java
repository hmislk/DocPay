package org.carecode.docpay.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.carecode.docpay.model.Doctor;
import org.carecode.docpay.model.DoctorPayment;
import org.carecode.docpay.service.PaymentService;
import org.carecode.docpay.util.JPAUtil;
import org.carecode.docpay.util.Session;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PaymentFormController {
    @FXML private ComboBox<Doctor> cmbDoctor;
    @FXML private TextField txtAmount;
    @FXML private DatePicker dpDate;
    @FXML private TextArea txtDescription;
    @FXML private Label lblError;

    private PaymentNavigator navigator;

    @FXML
    public void initialize() {
        dpDate.setValue(LocalDate.now());
        cmbDoctor.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Doctor d) { return d == null ? "" : d.getName(); }
            @Override public Doctor fromString(String s) { return null; }
        });
        loadDoctors();
    }

    private void loadDoctors() {
        EntityManager em = JPAUtil.em();
        try {
            List<Doctor> list = em.createQuery("select d from Doctor d where d.isActive=true order by d.name", Doctor.class).getResultList();
            cmbDoctor.setItems(FXCollections.observableArrayList(list));
        } finally { em.close(); }
    }

    public void setNavigator(PaymentNavigator navigator) { this.navigator = navigator; }

    @FXML
    public void onMakePayment() {
        lblError.setText("");
        Doctor doctor = cmbDoctor.getSelectionModel().getSelectedItem();
        if (doctor == null) { lblError.setText("Doctor is required"); cmbDoctor.requestFocus(); return; }
        String amtStr = txtAmount.getText() == null ? "" : txtAmount.getText().trim();
        BigDecimal amount;
        try { amount = new BigDecimal(amtStr); } catch (Exception ex) { lblError.setText("Invalid amount"); txtAmount.requestFocus(); return; }
        if (amount.signum() <= 0) { lblError.setText("Amount must be > 0"); txtAmount.requestFocus(); return; }
        LocalDate date = dpDate.getValue();
        String desc = txtDescription.getText();
        try {
            DoctorPayment payment = PaymentService.createPayment(doctor.getId(), desc, amount, date, Session.getCurrentUser());
            if (navigator != null) navigator.goToPrint(payment);
        } catch (Exception ex) {
            ex.printStackTrace();
            lblError.setText("Payment failed: " + ex.getMessage());
        }
    }
}

