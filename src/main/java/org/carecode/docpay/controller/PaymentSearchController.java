package org.carecode.docpay.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.carecode.docpay.model.Doctor;
import org.carecode.docpay.model.DoctorPayment;
import org.carecode.docpay.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentSearchController {
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private ComboBox<Doctor> cmbDoctor;
    @FXML private TableView<DoctorPayment> tblPayments;
    @FXML private TableColumn<DoctorPayment, String> colReceipt;
    @FXML private TableColumn<DoctorPayment, String> colDate;
    @FXML private TableColumn<DoctorPayment, String> colDoctor;
    @FXML private TableColumn<DoctorPayment, BigDecimal> colAmount;
    @FXML private TableColumn<DoctorPayment, Integer> colPrinted;

    private final ObservableList<DoctorPayment> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private PaymentNavigator navigator;

    @FXML
    public void initialize() {
        cmbDoctor.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Doctor d) { return d == null ? "" : d.getName(); }
            @Override public Doctor fromString(String s) { return null; }
        });
        loadDoctors();
        setupTable();
    }

    public void setNavigator(PaymentNavigator navigator) { this.navigator = navigator; }

    private void loadDoctors() {
        EntityManager em = JPAUtil.em();
        try {
            List<Doctor> list = em.createQuery("select d from Doctor d where d.isActive=true order by d.name", Doctor.class).getResultList();
            cmbDoctor.setItems(FXCollections.observableArrayList(list));
        } finally { em.close(); }
    }

    private void setupTable() {
        colReceipt.setCellValueFactory(new PropertyValueFactory<>("receiptId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paidAt"));
        colDate.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setText(null); return; }
                DoctorPayment p = getTableView().getItems().get(getIndex());
                var z = p.getPaidAt().atZone(ZoneId.systemDefault());
                setText(dtf.format(z));
            }
        });
        colDoctor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoctor().getName()));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("value"));
        colPrinted.setCellValueFactory(new PropertyValueFactory<>("printCount"));

        SortedList<DoctorPayment> sorted = new SortedList<>(data);
        sorted.comparatorProperty().bind(tblPayments.comparatorProperty());
        tblPayments.setItems(sorted);
    }

    @FXML public void onClear() { dpFrom.setValue(null); dpTo.setValue(null); cmbDoctor.getSelectionModel().clearSelection(); data.clear(); }

    @FXML
    public void onSearch() {
        EntityManager em = JPAUtil.em();
        try {
            StringBuilder jpql = new StringBuilder("select p from DoctorPayment p where 1=1");
            if (dpFrom.getValue() != null) jpql.append(" and p.paidAt >= :from");
            if (dpTo.getValue() != null) jpql.append(" and p.paidAt < :to");
            if (cmbDoctor.getSelectionModel().getSelectedItem() != null) jpql.append(" and p.doctor = :doc");
            jpql.append(" order by p.paidAt desc");
            TypedQuery<DoctorPayment> q = em.createQuery(jpql.toString(), DoctorPayment.class);
            if (dpFrom.getValue() != null) q.setParameter("from", dpFrom.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            if (dpTo.getValue() != null) q.setParameter("to", dpTo.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            if (cmbDoctor.getSelectionModel().getSelectedItem() != null) q.setParameter("doc", cmbDoctor.getSelectionModel().getSelectedItem());
            data.setAll(q.getResultList());
        } finally { em.close(); }
    }

    @FXML
    public void onPrint() {
        DoctorPayment p = tblPayments.getSelectionModel().getSelectedItem();
        if (p == null) { new Alert(Alert.AlertType.INFORMATION, "Select a payment to print.").showAndWait(); return; }
        if (navigator != null) navigator.goToPrint(p);
    }
}

