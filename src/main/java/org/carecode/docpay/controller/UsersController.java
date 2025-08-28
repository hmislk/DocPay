package org.carecode.docpay.controller;

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
import javafx.scene.layout.VBox;
import org.carecode.docpay.model.User;
import org.carecode.docpay.util.BCryptUtil;
import org.carecode.docpay.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UsersController {
    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, User.Role> colRole;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TableColumn<User, java.time.Instant> colLastLogin;
    @FXML private TextField txtSearch;

    private final ObservableList<User> data = FXCollections.observableArrayList();
    private FilteredList<User> filtered;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        onRefresh();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActive.setCellFactory(tc -> new TableCell<>(){
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (Boolean.TRUE.equals(item) ? "Yes" : "No"));
            }
        });
        colLastLogin.setCellValueFactory(new PropertyValueFactory<>("lastLoginAt"));
        colLastLogin.setCellFactory(tc -> new TableCell<>(){
            @Override protected void updateItem(java.time.Instant v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setText(null); return; }
                if (v == null) { setText(""); return; }
                var z = v.atZone(ZoneId.systemDefault());
                setText(dtf.format(z));
            }
        });

        filtered = new FilteredList<>(data, u -> true);
        SortedList<User> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblUsers.comparatorProperty());
        tblUsers.setItems(sorted);
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, old, val) -> {
            String q = val == null ? "" : val.trim().toLowerCase();
            filtered.setPredicate(u -> q.isEmpty() ||
                    (u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) ||
                    (u.getName() != null && u.getName().toLowerCase().contains(q)));
        });
    }

    @FXML
    public void onRefresh() {
        EntityManager em = JPAUtil.em();
        try {
            List<User> list = em.createQuery("select u from User u order by u.username", User.class).getResultList();
            data.setAll(list);
        } finally { em.close(); }
    }

    @FXML
    public void onNew() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/UserForm.fxml"));
            Parent root = fx.load();
            UserFormController ctrl = fx.getController();
            ctrl.initForCreate();
            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setTitle("New User");
            dlg.setScene(new Scene(root));
            ctrl.setDialogStage(dlg);
            dlg.showAndWait();
            if (ctrl.isSaved()) {
                User u = ctrl.getUser();
                if (!isUsernameAvailable(u.getUsername(), null)) {
                    new Alert(Alert.AlertType.ERROR, "Username already exists").showAndWait();
                    return;
                }
                u.setPasswordHash(BCryptUtil.hash(ctrl.getNewPassword()));
                persist(u);
                onRefresh();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open form: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onEdit() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a user to edit.").showAndWait();
            return;
        }
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/UserForm.fxml"));
            Parent root = fx.load();
            UserFormController ctrl = fx.getController();
            ctrl.initForEdit(selected);
            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setTitle("Edit User");
            dlg.setScene(new Scene(root));
            ctrl.setDialogStage(dlg);
            dlg.showAndWait();
            if (ctrl.isSaved()) {
                User updated = ctrl.getUser();
                if (!isUsernameAvailable(updated.getUsername(), selected.getId())) {
                    new Alert(Alert.AlertType.ERROR, "Username already exists").showAndWait();
                    return;
                }
                selected.setUsername(updated.getUsername());
                selected.setName(updated.getName());
                selected.setRole(updated.getRole());
                selected.setActive(updated.isActive());
                if (ctrl.getNewPassword() != null && !ctrl.getNewPassword().isEmpty()) {
                    selected.setPasswordHash(BCryptUtil.hash(ctrl.getNewPassword()));
                }
                merge(selected);
                onRefresh();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open form: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onDeactivate() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a user to deactivate.").showAndWait();
            return;
        }
        if (!selected.isActive()) {
            new Alert(Alert.AlertType.INFORMATION, "User already inactive.").showAndWait();
            return;
        }
        if (selected.getRole() == User.Role.ADMIN) {
            new Alert(Alert.AlertType.WARNING, "Cannot deactivate an ADMIN user.").showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Deactivate \"" + selected.getUsername() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm Deactivate");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                selected.setActive(false);
                merge(selected);
                onRefresh();
            }
        });
    }

    @FXML
    public void onResetPassword() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a user to reset password.").showAndWait();
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        PasswordField pf1 = new PasswordField(); pf1.setPromptText("New password");
        PasswordField pf2 = new PasswordField(); pf2.setPromptText("Confirm password");
        VBox box = new VBox(8, pf1, pf2);
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? pf1.getText() + "\n" + pf2.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split("\n", 2);
            String p1 = parts.length > 0 ? parts[0] : "";
            String p2 = parts.length > 1 ? parts[1] : "";
            if (p1 == null || p1.isBlank() || !p1.equals(p2)) {
                new Alert(Alert.AlertType.ERROR, "Passwords do not match.").showAndWait();
                return;
            }
            selected.setPasswordHash(BCryptUtil.hash(p1));
            merge(selected);
        });
    }

    private boolean isUsernameAvailable(String username, Long excludeId) {
        EntityManager em = JPAUtil.em();
        try {
            String jpql = "select count(u) from User u where u.username = :u" + (excludeId != null ? " and u.id <> :id" : "");
            TypedQuery<Long> q = em.createQuery(jpql, Long.class);
            q.setParameter("u", username.toLowerCase());
            if (excludeId != null) q.setParameter("id", excludeId);
            return q.getSingleResult() == 0;
        } finally { em.close(); }
    }

    private void persist(User u) {
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    private void merge(User u) {
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            em.merge(u);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
}
