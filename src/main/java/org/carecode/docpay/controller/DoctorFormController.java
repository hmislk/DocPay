package org.carecode.docpay.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.carecode.docpay.model.Doctor;

public class DoctorFormController {
    @FXML private TextField txtName;
    @FXML private TextField txtSpeciality;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtDetails;
    @FXML private CheckBox chkActive;
    @FXML private Label lblError;

    private Stage dialogStage;
    private boolean saved = false;
    private Doctor doctor;
    private Doctor original;

    @FXML
    public void initialize() {
        // nothing
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            txtName.setText(doctor.getName());
            txtSpeciality.setText(doctor.getSpeciality());
            txtPhone.setText(doctor.getPhone());
            txtEmail.setText(doctor.getEmail());
            txtDetails.setText(doctor.getDetails());
            chkActive.setSelected(doctor.isActive());
        }
    }

    public Doctor getDoctor() { return doctor; }

    public void setOriginal(Doctor original) { this.original = original; }

    public boolean isSaved() { return saved; }

    @FXML
    public void onSave() {
        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        if (name.isEmpty()) {
            lblError.setText("Name is required");
            txtName.requestFocus();
            return;
        }
        lblError.setText("");
        doctor.setName(name);
        doctor.setSpeciality(trimOrNull(txtSpeciality.getText()));
        doctor.setPhone(trimOrNull(txtPhone.getText()));
        doctor.setEmail(trimOrNull(txtEmail.getText()));
        doctor.setDetails(trimOrNull(txtDetails.getText()));
        doctor.setActive(chkActive.isSelected());
        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    public void onCancel() {
        saved = false;
        if (dialogStage != null) dialogStage.close();
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

