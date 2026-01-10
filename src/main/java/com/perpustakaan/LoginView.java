package com.perpustakaan;

import com.perpustakaan.util.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginView {

    public Parent getView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root-pane");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(420);
        card.setMaxHeight(550);

        // Header
        Label logoIcon = new Label("📚");
        logoIcon.setStyle("-fx-font-size: 48px; -fx-padding: 0 0 10 0;");
        Label titleLabel = new Label("Library System");
        titleLabel.getStyleClass().add("label-title");
        Label subTitleLabel = new Label("Sign in to continue");
        subTitleLabel.getStyleClass().add("label-subtitle");

        VBox headerBox = new VBox(5, logoIcon, titleLabel, subTitleLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 30, 0));

        // Form
        Label userLabel = new Label("USERNAME / EMAIL");
        userLabel.getStyleClass().add("label-small");
        TextField userField = new TextField();
        userField.setPromptText("Enter username or email");
        userField.getStyleClass().add("input-field");

        Label passLabel = new Label("PASSWORD");
        passLabel.getStyleClass().add("label-small");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.getStyleClass().add("input-field");

        Button btnLogin = new Button("Sign In");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setDefaultButton(true);

        btnLogin.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), btnLogin));

        card.getChildren().addAll(headerBox, userLabel, userField, passLabel, passField, new Label(), btnLogin);
        root.getChildren().add(card);
        return root;
    }

    private void handleLogin(String username, String password, Button sourceBtn) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Isi semua kolom!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            Stage currentStage = (Stage) sourceBtn.getScene().getWindow();

            // 1. CEK KE TABEL PETUGAS (USERS) DULU
            if (checkPetugas(conn, username, password, currentStage)) return;

            // 2. KALAU GAGAL, CEK KE TABEL ANGGOTA (MEMBERS)
            if (checkAnggota(conn, username, password, currentStage)) return;

            // 3. KALAU GAGAL SEMUA
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Akun tidak ditemukan atau password salah.");

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    // Login sebagai Petugas/Admin
    private boolean checkPetugas(Connection conn, String user, String pass, Stage stage) throws Exception {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, user);
        stmt.setString(2, pass);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String nama = rs.getString("nama_lengkap");
            String role = rs.getString("role");
            // Buka Dashboard Admin/Petugas
            new DashboardView().show(stage, nama, role);
            return true;
        }
        return false;
    }

    // Login sebagai Anggota
    private boolean checkAnggota(Connection conn, String email, String pass, Stage stage) throws Exception {
        // Anggota login pakai email & password
        String sql = "SELECT * FROM members WHERE email = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, email);
        stmt.setString(2, pass);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int idMember = rs.getInt("id_member");
            String nama = rs.getString("nama_member");
            // Buka Dashboard Khusus Member (Baru)
            new MemberView().show(stage, idMember, nama);
            return true;
        }
        return false;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}