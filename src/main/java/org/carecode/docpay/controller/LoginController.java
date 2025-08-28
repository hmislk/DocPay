package org.carecode.docpay.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.carecode.docpay.model.User;
import org.carecode.docpay.util.BCryptUtil;
import org.carecode.docpay.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.Instant;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        try {
            ensureAdminOnFirstRun();
        } catch (Throwable t) {
            if (lblMessage != null) lblMessage.setText("DB init failed: " + t.getClass().getSimpleName());
            t.printStackTrace();
        }
    }

    private void ensureAdminOnFirstRun() {
        EntityManager em = JPAUtil.em();
        try {
            long count = em.createQuery("select count(u) from User u", Long.class).getSingleResult();
            if (count == 0 && lblMessage != null) {
                lblMessage.setText("Create admin: set username and password then Login.");
            }
        } catch (Exception ex) {
            if (lblMessage != null) lblMessage.setText("Database error. See console.");
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }

    @FXML
    public void onLogin(ActionEvent e) {
        String u = txtUsername.getText().trim().toLowerCase();
        String p = txtPassword.getText();
        if (u.isEmpty() || p.isEmpty()) { lblMessage.setText("Enter username and password"); return; }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            long count = em.createQuery("select count(u) from User u", Long.class).getSingleResult();
            if (count == 0) {
                User admin = new User();
                admin.setUsername(u);
                admin.setName("Administrator");
                admin.setRole(User.Role.ADMIN);
                admin.setPasswordHash(BCryptUtil.hash(p));
                admin.setCreatedAt(Instant.now());
                em.persist(admin);
                em.getTransaction().commit();
                lblMessage.setText("Admin created. Please login again.");
                txtPassword.clear();
                return;
            }

            TypedQuery<User> q = em.createQuery("select u from User u where u.username=:u and u.isActive=true", User.class);
            q.setParameter("u", u);
            User user;
            try { user = q.getSingleResult(); }
            catch (NoResultException ex) { lblMessage.setText("Invalid credentials"); return; }

            if (!BCryptUtil.check(p, user.getPasswordHash())) { lblMessage.setText("Invalid credentials"); return; }
            user.setLastLoginAt(Instant.now());
            em.merge(user);
            em.getTransaction().commit();

            // Navigate to Home view
            org.carecode.docpay.util.Session.setCurrentUser(user);
            try {
                javafx.scene.Parent home = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(home);
                scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
                ((javafx.stage.Stage) lblMessage.getScene().getWindow()).setScene(scene);
            } catch (Exception ex) {
                ex.printStackTrace();
                lblMessage.setText("Failed to open Home view");
            }
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
}
