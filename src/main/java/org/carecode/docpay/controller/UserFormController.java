package org.carecode.docpay.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.carecode.docpay.model.User;

public class UserFormController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtName;
    @FXML private ComboBox<User.Role> cmbRole;
    @FXML private CheckBox chkActive;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private Label lblError;

    private Stage dialogStage;
    private boolean saved = false;
    private User user;
    private boolean createMode = true;
    private String newPassword; // for controller consumer to hash & apply

    @FXML
    public void initialize() {
        cmbRole.setItems(FXCollections.observableArrayList(User.Role.values()));
    }

    public void initForCreate() {
        createMode = true;
        user = new User();
        user.setActive(true);
        cmbRole.getSelectionModel().select(User.Role.CASHIER);
        chkActive.setSelected(true);
        txtUsername.clear();
        txtName.clear();
        txtPassword.clear();
        txtConfirm.clear();
    }

    public void initForEdit(User original) {
        createMode = false;
        user = new User();
        user.setActive(original.isActive());
        user.setName(original.getName());
        user.setRole(original.getRole());
        user.setUsername(original.getUsername());
        // Bind to fields
        txtUsername.setText(user.getUsername());
        txtName.setText(user.getName());
        cmbRole.getSelectionModel().select(user.getRole());
        chkActive.setSelected(user.isActive());
    }

    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }
    public boolean isSaved() { return saved; }
    public User getUser() { return user; }
    public String getNewPassword() { return newPassword; }

    @FXML
    public void onSave() {
        String username = safe(txtUsername.getText()).toLowerCase();
        String name = safe(txtName.getText());
        User.Role role = cmbRole.getSelectionModel().getSelectedItem();
        boolean active = chkActive.isSelected();
        String pw1 = txtPassword.getText();
        String pw2 = txtConfirm.getText();

        if (username.isEmpty()) { lblError.setText("Username is required"); txtUsername.requestFocus(); return; }
        if (name.isEmpty()) { lblError.setText("Name is required"); txtName.requestFocus(); return; }
        if (role == null) { lblError.setText("Role is required"); cmbRole.requestFocus(); return; }
        if (createMode) {
            if (pw1 == null || pw1.isBlank() || !pw1.equals(pw2)) { lblError.setText("Passwords required and must match"); txtPassword.requestFocus(); return; }
            newPassword = pw1;
        } else {
            if ((pw1 != null && !pw1.isBlank()) || (pw2 != null && !pw2.isBlank())) {
                if (!safe(pw1).equals(safe(pw2))) { lblError.setText("Passwords do not match"); txtPassword.requestFocus(); return; }
                newPassword = pw1;
            }
        }
        lblError.setText("");
        user.setUsername(username);
        user.setName(name);
        user.setRole(role);
        user.setActive(active);
        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    public void onCancel() {
        saved = false;
        if (dialogStage != null) dialogStage.close();
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
