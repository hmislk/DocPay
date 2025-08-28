package org.carecode.docpay.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.carecode.docpay.model.Doctor;
import org.carecode.docpay.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.function.Predicate;

public class DoctorsController {
    @FXML private TableView<Doctor> tblDoctors;
    @FXML private TableColumn<Doctor, Long> colId;
    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colSpeciality;
    @FXML private TableColumn<Doctor, String> colPhone;
    @FXML private TableColumn<Doctor, String> colEmail;
    @FXML private TableColumn<Doctor, Boolean> colActive;
    @FXML private TextField txtSearch;

    private final ObservableList<Doctor> doctorData = FXCollections.observableArrayList();
    private FilteredList<Doctor> filtered;

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadDoctors();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSpeciality.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActive.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                setText(empty ? null : (Boolean.TRUE.equals(active) ? "Yes" : "No"));
            }
        });

        filtered = new FilteredList<>(doctorData, d -> true);
        SortedList<Doctor> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblDoctors.comparatorProperty());
        tblDoctors.setItems(sorted);

        // Double-click to edit
        tblDoctors.setRowFactory(tv -> {
            TableRow<Doctor> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    tblDoctors.getSelectionModel().select(row.getIndex());
                    onEdit();
                }
            });
            return row;
        });
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, old, val) -> {
            String q = val == null ? "" : val.trim().toLowerCase();
            Predicate<Doctor> p = d -> {
                if (q.isEmpty()) return true;
                return (d.getName() != null && d.getName().toLowerCase().contains(q))
                        || (d.getSpeciality() != null && d.getSpeciality().toLowerCase().contains(q))
                        || (d.getPhone() != null && d.getPhone().toLowerCase().contains(q))
                        || (d.getEmail() != null && d.getEmail().toLowerCase().contains(q));
            };
            filtered.setPredicate(p);
        });
    }

    private void loadDoctors() {
        EntityManager em = JPAUtil.em();
        try {
            TypedQuery<Doctor> q = em.createQuery("select d from Doctor d order by d.name", Doctor.class);
            List<Doctor> list = q.getResultList();
            doctorData.setAll(list);
        } finally {
            em.close();
        }
    }

    @FXML
    public void onRefresh() {
        loadDoctors();
    }

    @FXML
    public void onNew() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/DoctorForm.fxml"));
            Parent root = fx.load();
            DoctorFormController ctrl = fx.getController();
            ctrl.setDoctor(new Doctor());
            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setTitle("New Doctor");
            dlg.setScene(new Scene(root));
            ctrl.setDialogStage(dlg);
            dlg.showAndWait();
            if (ctrl.isSaved()) {
                saveDoctor(ctrl.getDoctor());
                loadDoctors();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open form: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onEdit() {
        Doctor selected = tblDoctors.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a doctor to edit.").showAndWait();
            return;
        }
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/DoctorForm.fxml"));
            Parent root = fx.load();
            DoctorFormController ctrl = fx.getController();
            // Work on a copy to avoid mutating table item if canceled
            Doctor copy = new Doctor();
            copy.setActive(selected.isActive());
            copy.setDetails(selected.getDetails());
            copy.setEmail(selected.getEmail());
            copy.setName(selected.getName());
            copy.setPhone(selected.getPhone());
            copy.setSpeciality(selected.getSpeciality());
            // Keep id in controller via original reference
            ctrl.setDoctor(copy);
            ctrl.setOriginal(selected);
            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setTitle("Edit Doctor");
            dlg.setScene(new Scene(root));
            ctrl.setDialogStage(dlg);
            dlg.showAndWait();
            if (ctrl.isSaved()) {
                // Apply updated fields back to selected
                selected.setActive(ctrl.getDoctor().isActive());
                selected.setDetails(ctrl.getDoctor().getDetails());
                selected.setEmail(ctrl.getDoctor().getEmail());
                selected.setName(ctrl.getDoctor().getName());
                selected.setPhone(ctrl.getDoctor().getPhone());
                selected.setSpeciality(ctrl.getDoctor().getSpeciality());
                updateDoctor(selected);
                loadDoctors();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open form: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onDeactivate() {
        Doctor selected = tblDoctors.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a doctor to deactivate.").showAndWait();
            return;
        }
        if (!selected.isActive()) {
            new Alert(Alert.AlertType.INFORMATION, "Doctor is already inactive.").showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Deactivate \"" + selected.getName() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm Deactivate");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                selected.setActive(false);
                updateDoctor(selected);
                loadDoctors();
            }
        });
    }

    private void saveDoctor(Doctor d) {
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            em.persist(d);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    private void updateDoctor(Doctor d) {
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            em.merge(d);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
}
