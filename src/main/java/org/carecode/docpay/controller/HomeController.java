package org.carecode.docpay.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.carecode.docpay.util.Session;

public class HomeController {
    @FXML private Label lblWelcome;
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        var user = Session.getCurrentUser();
        if (user != null) {
            lblWelcome.setText("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        } else {
            lblWelcome.setText("Welcome");
        }
        // Ensure dashboard is visible at start
        // (The FXML already includes dashboard content inside contentArea)
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    private void loadAndSet(String fxmlPath) {
        try {
            Node n = FXMLLoader.load(getClass().getResource(fxmlPath));
            setContent(n);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML public void showDashboard() {
        // Reset to dashboard placeholder from FXML by reloading Home? Instead, just clear children to show built-in dashboardPane
        // We can reload a small VBox dashboard here to keep it simple.
        try {
            javafx.scene.layout.VBox dashboard = new javafx.scene.layout.VBox(8);
            javafx.geometry.Insets pad = new javafx.geometry.Insets(30,30,30,30);
            dashboard.setPadding(pad);
            javafx.scene.control.Label t = new javafx.scene.control.Label("Dashboard");
            t.getStyleClass().add("title");
            javafx.scene.control.Label sub = new javafx.scene.control.Label("Choose a section from the left.");
            dashboard.getChildren().addAll(t, sub);
            setContent(dashboard);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML public void showDoctors() { loadAndSet("/fxml/Doctors.fxml"); }
    @FXML public void showUsers() { loadAndSet("/fxml/Users.fxml"); }
    @FXML public void showPayments() { loadAndSet("/fxml/Payments.fxml"); }
    @FXML public void showSettings() { loadAndSet("/fxml/Settings.fxml"); }

    @FXML public void onLogout() {
        try {
            Session.setCurrentUser(null);
            Node login = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene((javafx.scene.Parent) login);
            scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
            ((javafx.stage.Stage) lblWelcome.getScene().getWindow()).setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
