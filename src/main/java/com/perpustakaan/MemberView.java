package com.perpustakaan;

import com.perpustakaan.util.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MemberView {

    private Stage stage;
    private int idMember;
    private String namaMember;

    // Container agar bisa di-refresh isinya
    private FlowPane bookGrid;
    private VBox historyContainer;

    public void show(Stage loginStage, int id, String nama) {
        this.idMember = id;
        this.namaMember = nama;

        loginStage.close();
        stage = new Stage();
        stage.setTitle("Perpustakaan Digital - Halaman Anggota");
        stage.setMaximized(true);

        // Layout Utama: BorderPane
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f8;");

        // --- 1. HEADER ATAS ---
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 0, 5, 10, 0);");
        header.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("📚 Library App");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblUser = new Label("Hi, " + namaMember);
        lblUser.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnLogout = new Button("Keluar");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnLogout.setOnAction(e -> {
            stage.close();
            try { new Main().start(new Stage()); } catch (Exception ex) {}
        });

        header.getChildren().addAll(logo, spacer, lblUser, btnLogout);
        root.setTop(header);

        // --- 2. CENTER: KATALOG BUKU ---
        ScrollPane scrollCatalog = new ScrollPane();
        scrollCatalog.setFitToWidth(true);
        scrollCatalog.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        bookGrid = new FlowPane();
        bookGrid.setPadding(new Insets(30));
        bookGrid.setHgap(20);
        bookGrid.setVgap(20);
        bookGrid.setAlignment(Pos.TOP_LEFT);

        scrollCatalog.setContent(bookGrid);
        root.setCenter(scrollCatalog);

        // --- 3. RIGHT: RIWAYAT PEMINJAMAN ---
        VBox rightPanel = new VBox(15);
        rightPanel.setPrefWidth(340); // Lebar sedikit ditambah biar muat tombol
        rightPanel.getStyleClass().add("history-panel");

        Label lblHistory = new Label("📖 Buku Saya");
        lblHistory.getStyleClass().add("history-title");
        lblHistory.setMaxWidth(Double.MAX_VALUE);

        historyContainer = new VBox(10);
        ScrollPane scrollHistory = new ScrollPane(historyContainer);
        scrollHistory.setFitToWidth(true);
        scrollHistory.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollHistory.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollHistory, Priority.ALWAYS);

        rightPanel.getChildren().addAll(lblHistory, scrollHistory);
        root.setRight(rightPanel);

        // --- LOAD DATA ---
        refreshBookCatalog();
        refreshLoanHistory();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    // ==========================================================
    // BAGIAN 1: KATALOG BUKU
    // ==========================================================
    private void refreshBookCatalog() {
        bookGrid.getChildren().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                int idBuku = rs.getInt("id_buku");
                String judul = rs.getString("judul");
                String penulis = rs.getString("penulis");
                String penerbit = rs.getString("penerbit");
                String desc = "Kategori: " + rs.getString("kategori") + "\nPenerbit: " + penerbit;
                int stok = rs.getInt("stok");
                String imgUrl = rs.getString("gambar_url");

                bookGrid.getChildren().add(createBookCard(idBuku, judul, penulis, desc, stok, imgUrl));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox createBookCard(int idBuku, String judul, String penulis, String desc, int stok, String imgFileName) {
        VBox card = new VBox(8);
        card.setPrefWidth(160);
        card.setPrefHeight(280);
        card.getStyleClass().add("book-card");

        card.setOnMouseClicked(e -> showBookDetailPopup(idBuku, judul, penulis, desc, stok, imgFileName));

        ImageView imgView = new ImageView();
        imgView.setFitWidth(120); imgView.setFitHeight(170); imgView.setPreserveRatio(true);
        loadLocalImage(imgView, imgFileName);

        StackPane imgContainer = new StackPane(imgView);
        imgContainer.setPrefHeight(170); imgContainer.setAlignment(Pos.CENTER);
        if (imgView.getImage() == null) imgContainer.getChildren().add(new Label("📘"));

        Label lblStok = new Label(stok > 0 ? "Stok: " + stok : "Habis");
        lblStok.getStyleClass().add(stok > 0 ? "stock-badge-green" : "stock-badge-red");
        HBox stockBox = new HBox(lblStok); stockBox.setAlignment(Pos.CENTER);

        Label lblJudul = new Label(judul);
        lblJudul.getStyleClass().add("card-title");
        lblJudul.setWrapText(true); lblJudul.setMaxHeight(40);

        card.getChildren().addAll(imgContainer, stockBox, lblJudul);
        return card;
    }

    // ==========================================================
    // BAGIAN 2: RIWAYAT & TOMBOL PENGEMBALIAN (UPDATE)
    // ==========================================================
    private void refreshLoanHistory() {
        historyContainer.getChildren().clear();

        // PENTING: Kita perlu id_loan dan id_buku untuk proses pengembalian
        String sql = "SELECT l.id_loan, l.id_buku, l.status, l.tanggal_pinjam, l.tanggal_kembali, b.judul " +
                "FROM loans l " +
                "JOIN books b ON l.id_buku = b.id_buku " +
                "WHERE l.id_member = ? " +
                "ORDER BY l.id_loan DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.idMember);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                int idLoan = rs.getInt("id_loan");
                int idBuku = rs.getInt("id_buku");
                String judul = rs.getString("judul");
                String status = rs.getString("status");
                String tglKembali = rs.getString("tanggal_kembali");

                historyContainer.getChildren().add(createHistoryCard(idLoan, idBuku, judul, status, tglKembali));
            }

            if(!hasData) {
                Label empty = new Label("Belum ada riwayat peminjaman.");
                empty.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 10;");
                historyContainer.getChildren().add(empty);
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox createHistoryCard(int idLoan, int idBuku, String judul, String status, String tglKembali) {
        VBox card = new VBox(8); // Jarak antar elemen diperbesar
        card.getStyleClass().add("loan-card");

        Label lblJudul = new Label(judul);
        lblJudul.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        lblJudul.setWrapText(true);

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label lblStatus = new Label(status.toUpperCase());
        Label lblDate = new Label();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if ("Dipinjam".equalsIgnoreCase(status)) {
            lblStatus.getStyleClass().add("status-active");
            lblDate.setText("Tempo: " + tglKembali);
            lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");

            // --- TOMBOL KEMBALIKAN (Hanya muncul jika dipinjam) ---
            Button btnReturn = new Button("Kembalikan");
            btnReturn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8; -fx-cursor: hand;");

            // Aksi Kembalikan
            btnReturn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Kembalikan buku ini?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        prosesPengembalian(idLoan, idBuku);
                    }
                });
            });

            statusBox.getChildren().addAll(lblStatus, spacer, btnReturn); // Ada tombol

        } else {
            lblStatus.getStyleClass().add("status-returned");
            lblDate.setText("Selesai");
            lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
            statusBox.getChildren().addAll(lblStatus, spacer, lblDate); // Tidak ada tombol
        }

        card.getChildren().addAll(lblJudul, statusBox);
        if("Dipinjam".equalsIgnoreCase(status)) {
            // Tambahkan tanggal di baris bawah jika status dipinjam, biar rapi
            card.getChildren().add(lblDate);
        }

        return card;
    }

    // --- LOGIKA PENGEMBALIAN BUKU ---
    private void prosesPengembalian(int idLoan, int idBuku) {
        String sqlUpdateLoan = "UPDATE loans SET status = 'Kembali' WHERE id_loan = ?";
        String sqlUpdateBook = "UPDATE books SET stok = stok + 1 WHERE id_buku = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Mulai Transaksi

            try {
                // 1. Ubah Status Peminjaman
                PreparedStatement stmtLoan = conn.prepareStatement(sqlUpdateLoan);
                stmtLoan.setInt(1, idLoan);
                stmtLoan.executeUpdate();

                // 2. Tambah Stok Buku
                PreparedStatement stmtBook = conn.prepareStatement(sqlUpdateBook);
                stmtBook.setInt(1, idBuku);
                stmtBook.executeUpdate();

                conn.commit(); // Simpan Permanen

                // Refresh UI (Katalog stok nambah, List kanan status berubah)
                refreshBookCatalog();
                refreshLoanHistory();

                // Info
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Buku berhasil dikembalikan! Stok telah diperbarui.");
                alert.showAndWait();

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal mengembalikan buku.");
                alert.showAndWait();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==========================================================
    // BAGIAN 3: DETAIL POPUP
    // ==========================================================
    private void showBookDetailPopup(int idBuku, String judul, String penulis, String desc, int stok, String imgFileName) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Detail Buku");

        HBox layout = new HBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(200); imgView.setFitHeight(300); imgView.setPreserveRatio(true);
        loadLocalImage(imgView, imgFileName);

        VBox details = new VBox(15);
        details.setPrefWidth(300);

        Label lblJudul = new Label(judul);
        lblJudul.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblJudul.setWrapText(true);

        Label lblPenulis = new Label("Penulis: " + penulis);
        Label lblStokInfo = new Label("Sisa Stok: " + stok);
        lblStokInfo.setStyle("-fx-font-weight: bold;");

        Button btnPinjam = new Button("PINJAM BUKU INI");
        btnPinjam.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");

        if (stok <= 0) {
            btnPinjam.setDisable(true);
            btnPinjam.setText("STOK HABIS");
            btnPinjam.setStyle("-fx-background-color: #bdc3c7;");
        }

        btnPinjam.setOnAction(e -> {
            if(prosesPeminjaman(idBuku)) {
                popup.close();
                refreshBookCatalog();
                refreshLoanHistory();
            }
        });

        details.getChildren().addAll(lblJudul, lblPenulis, new Separator(), new Label(desc), lblStokInfo, new Region(), btnPinjam);
        layout.getChildren().addAll(imgView, details);

        popup.setScene(new Scene(layout, 650, 450));
        popup.showAndWait();
    }

    private boolean prosesPeminjaman(int idBuku) {
        String sqlInsert = "INSERT INTO loans (id_member, id_buku, tanggal_pinjam, tanggal_kembali, status) VALUES (?, ?, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), 'Dipinjam')";
        String sqlUpdate = "UPDATE books SET stok = stok - 1 WHERE id_buku = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement stmtLoan = conn.prepareStatement(sqlInsert);
                stmtLoan.setInt(1, this.idMember);
                stmtLoan.setInt(2, idBuku);
                stmtLoan.executeUpdate();

                PreparedStatement stmtStock = conn.prepareStatement(sqlUpdate);
                stmtStock.setInt(1, idBuku);
                stmtStock.executeUpdate();

                conn.commit();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sukses");
                alert.setHeaderText(null);
                alert.setContentText("Berhasil meminjam! Cek sidebar kanan.");
                alert.showAndWait();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void loadLocalImage(ImageView imgView, String fileName) {
        try {
            if (fileName != null && !fileName.isEmpty()) {
                var is = getClass().getResourceAsStream("/images/" + fileName);
                if (is != null) imgView.setImage(new Image(is));
            }
        } catch (Exception e) {}
    }
}