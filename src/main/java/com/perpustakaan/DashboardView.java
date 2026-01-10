package com.perpustakaan;

import com.perpustakaan.util.DatabaseConnection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DashboardView {

    private BorderPane rootLayout;
    private Stage stage;

    private String currentUser;
    private String currentRole;

    public void show(Stage loginStage, String userName, String userRole) {
        this.currentUser = userName;
        this.currentRole = userRole;

        // 1. Setup Stage
        loginStage.close();
        stage = new Stage();
        stage.setTitle("Sistem Perpustakaan Modern - Admin Panel");

        // 2. Layout Utama
        rootLayout = new BorderPane();

        // --- SIDEBAR ---
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(240);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 10, 20, 10));

        Label lblBrand = new Label("LIBRARY APP");
        lblBrand.getStyleClass().add("sidebar-title");
        lblBrand.setAlignment(Pos.CENTER);
        lblBrand.setMaxWidth(Double.MAX_VALUE);

        Button btnHome = createMenuBtn("📊  Dashboard");
        Button btnKatalog = createMenuBtn("📚  Katalog Buku");
        Button btnPinjam = createMenuBtn("🔄  Peminjaman");
        Button btnAnggota = createMenuBtn("👥  Data Anggota");

        Label spacer = new Label();
        spacer.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = createMenuBtn("🚪  Keluar");
        btnLogout.setStyle("-fx-text-fill: #e74c3c;");

        // Navigasi
        btnHome.setOnAction(e -> showHome());
        btnKatalog.setOnAction(e -> showKatalog());
        btnPinjam.setOnAction(e -> showPeminjaman());
        btnAnggota.setOnAction(e -> showAnggota());

        btnLogout.setOnAction(e -> {
            stage.close();
            try { new Main().start(new Stage()); } catch (Exception ex) { ex.printStackTrace(); }
        });

        sidebar.getChildren().addAll(lblBrand, new Label(""), btnHome, btnKatalog, btnPinjam, btnAnggota, spacer, btnLogout);
        rootLayout.setLeft(sidebar);

        showHome(); // Default Page

        Scene scene = new Scene(rootLayout, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    // --- HEADER ---
    private HBox createHeader(String titleText) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setSpacing(20);

        Label title = new Label(titleText);
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);
        Label lblName = new Label("Hi, " + currentUser);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        Label lblRole = new Label(currentRole.toUpperCase());
        lblRole.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        userInfo.getChildren().addAll(lblName, lblRole);
        header.getChildren().addAll(title, spacer, userInfo);
        return header;
    }

    // =================================================================================
    // PAGE 1: HOME (DASHBOARD)
    // =================================================================================
    private void showHome() {
        VBox homeContent = new VBox(25);
        homeContent.getChildren().add(createHeader("Dashboard Overview"));

        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER_LEFT);

        int totalBuku = getDataCount("SELECT COUNT(*) FROM books");
        int totalPinjam = getDataCount("SELECT COUNT(*) FROM loans WHERE status = 'Dipinjam'");
        int totalAnggota = getDataCount("SELECT COUNT(*) FROM members");

        statsContainer.getChildren().addAll(
                createCard("Total Buku", String.valueOf(totalBuku), "📚"),
                createCard("Sedang Dipinjam", String.valueOf(totalPinjam), "🔄"),
                createCard("Jumlah Anggota", String.valueOf(totalAnggota), "👥")
        );

        homeContent.getChildren().add(statsContainer);
        setContent(homeContent);
    }

    // =================================================================================
    // PAGE 2: KATALOG BUKU (GRID CARD)
    // =================================================================================
    private void showKatalog() {
        VBox content = new VBox(20);
        content.getChildren().add(createHeader("Katalog Buku"));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        FlowPane grid = new FlowPane();
        grid.setHgap(20); grid.setVgap(20);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.TOP_LEFT);

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                int id = rs.getInt("id_buku");
                String judul = rs.getString("judul");
                String penulis = rs.getString("penulis");
                int stok = rs.getInt("stok");
                String imgUrl = rs.getString("gambar_url");
                if (imgUrl == null) imgUrl = "";

                VBox card = createBookCard(id, judul, penulis, stok, imgUrl);
                grid.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
            grid.getChildren().add(new Label("Gagal memuat data buku."));
        }

        scrollPane.setContent(grid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        content.getChildren().add(scrollPane);

        setContent(content);
    }

    private VBox createBookCard(int id, String judul, String penulis, int stok, String imgFileName) {
        VBox card = new VBox(8);
        card.setPrefWidth(180); card.setPrefHeight(300);
        card.getStyleClass().add("book-card");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(130); imgView.setFitHeight(190); imgView.setPreserveRatio(true);

        boolean loadSuccess = false;
        if (imgFileName != null && !imgFileName.isEmpty()) {
            try {
                String path = "/images/" + imgFileName;
                var inputStream = getClass().getResourceAsStream(path);
                if (inputStream != null) {
                    Image img = new Image(inputStream);
                    imgView.setImage(img);
                    loadSuccess = true;
                }
            } catch (Exception e) {}
        }

        StackPane imgContainer = new StackPane();
        imgContainer.setPrefHeight(190); imgContainer.setAlignment(Pos.CENTER);

        if (loadSuccess) imgContainer.getChildren().add(imgView);
        else {
            Label placeholder = new Label("📖");
            placeholder.setStyle("-fx-font-size: 50px;");
            imgContainer.getChildren().add(placeholder);
        }

        Label lblStok = new Label(stok > 0 ? "Stok: " + stok : "Habis");
        lblStok.getStyleClass().add(stok > 0 ? "stock-badge-green" : "stock-badge-red");
        HBox stockContainer = new HBox(lblStok); stockContainer.setAlignment(Pos.CENTER);

        Label lblJudul = new Label(judul);
        lblJudul.getStyleClass().add("card-title");
        lblJudul.setWrapText(true); lblJudul.setMaxHeight(40); lblJudul.setMinHeight(40);

        Label lblPenulis = new Label(penulis);
        lblPenulis.getStyleClass().add("card-author");
        lblPenulis.setMaxWidth(160); lblPenulis.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imgContainer, stockContainer, lblJudul, lblPenulis);
        return card;
    }

    // =================================================================================
    // PAGE 3: DATA PEMINJAMAN (TABLE VIEW MODERN - SUDAH DIREVISI)
    // =================================================================================
    private void showPeminjaman() {
        VBox content = new VBox(20);
        content.getChildren().add(createHeader("Data Sirkulasi Peminjaman"));

        TableView<LoanModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Kolom 1: ID
        TableColumn<LoanModel, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idLoan"));
        colId.setMaxWidth(40);

        // Kolom 2: Anggota
        TableColumn<LoanModel, String> colMember = new TableColumn<>("Peminjam");
        colMember.setCellValueFactory(new PropertyValueFactory<>("namaMember"));

        // Kolom 3: Buku
        TableColumn<LoanModel, String> colBuku = new TableColumn<>("Judul Buku");
        colBuku.setCellValueFactory(new PropertyValueFactory<>("judulBuku"));

        // Kolom 4: Tanggal Pinjam
        TableColumn<LoanModel, String> colTglPinjam = new TableColumn<>("Tgl Pinjam");
        colTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colTglPinjam.setStyle("-fx-alignment: CENTER;");

        // Kolom 5: Tanggal Kembali (Jatuh Tempo) - FITUR BARU
        TableColumn<LoanModel, String> colTglKembali = new TableColumn<>("Jatuh Tempo");
        colTglKembali.setCellValueFactory(new PropertyValueFactory<>("tglKembali"));
        colTglKembali.setStyle("-fx-alignment: CENTER; -fx-text-fill: #e67e22;"); // Warna Oranye

        // Kolom 6: Durasi (Hitung Hari) - FITUR BARU
        TableColumn<LoanModel, String> colDurasi = new TableColumn<>("Durasi");
        colDurasi.setCellValueFactory(new PropertyValueFactory<>("durasi"));
        colDurasi.setStyle("-fx-alignment: CENTER;");

        // Custom Cell untuk Warnai Merah jika Telat (> 7 Hari)
        colDurasi.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Ambil baris data saat ini
                    LoanModel row = getTableView().getItems().get(getIndex());

                    // Cek: Jika Status masih Dipinjam DAN Durasi > 7 Hari
                    if (row.getStatus().equalsIgnoreCase("Dipinjam")) {
                        try {
                            // Ambil angka depan (misal "8 Hari" -> ambil 8)
                            int hari = Integer.parseInt(item.split(" ")[0]);
                            if (hari > 7) {
                                setStyle("-fx-alignment: CENTER; -fx-text-fill: red; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-alignment: CENTER; -fx-text-fill: #2c3e50;");
                            }
                        } catch (Exception e) { setStyle("-fx-alignment: CENTER;"); }
                    } else {
                        // Jika sudah kembali, warna abu biasa
                        setStyle("-fx-alignment: CENTER; -fx-text-fill: #95a5a6;");
                    }
                }
            }
        });

        // Kolom 7: Status (Badge)
        TableColumn<LoanModel, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(status.toUpperCase());
                    lbl.setPadding(new Insets(5, 12, 5, 12));
                    lbl.setStyle("-fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 10px;");

                    if ("Dipinjam".equalsIgnoreCase(status)) {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #ffeaa7; -fx-text-fill: #d35400;");
                    } else {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #c8f7c5; -fx-text-fill: #27ae60;");
                    }
                    setGraphic(lbl);
                }
            }
        });

        table.getColumns().addAll(colId, colMember, colBuku, colTglPinjam, colTglKembali, colDurasi, colStatus);

        // Load Data Database
        ObservableList<LoanModel> dataList = FXCollections.observableArrayList();
        String sql = "SELECT l.id_loan, m.nama_member, b.judul, l.tanggal_pinjam, l.tanggal_kembali, l.status " +
                "FROM loans l " +
                "JOIN members m ON l.id_member = m.id_member " +
                "JOIN books b ON l.id_buku = b.id_buku " +
                "ORDER BY l.id_loan DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Hitung Durasi (Hari)
                String tglPinjamStr = rs.getString("tanggal_pinjam");
                String tglKembaliStr = rs.getString("tanggal_kembali"); // Jatuh Tempo

                long daysDiff = 0;
                if (tglPinjamStr != null) {
                    LocalDate tglPinjam = LocalDate.parse(tglPinjamStr);
                    LocalDate today = LocalDate.now();

                    // Hitung selisih hari dari Tgl Pinjam s.d. Hari Ini
                    daysDiff = ChronoUnit.DAYS.between(tglPinjam, today);
                }

                String durasiText = daysDiff + " Hari";

                dataList.add(new LoanModel(
                        rs.getInt("id_loan"),
                        rs.getString("nama_member"),
                        rs.getString("judul"),
                        tglPinjamStr,
                        tglKembaliStr,
                        durasiText,
                        rs.getString("status")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }

        table.setItems(dataList);
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);

        setContent(content);
    }

    // =================================================================================
    // PAGE 4: DATA ANGGOTA (TABLE WITH AVATAR)
    // =================================================================================
    private void showAnggota() {
        VBox content = new VBox(20);
        content.getChildren().add(createHeader("Daftar Anggota Perpustakaan"));

        TableView<MemberModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 13px; -fx-background-color: white;");

        // Kolom 1: Avatar
        TableColumn<MemberModel, String> colAvatar = new TableColumn<>("Avatar");
        colAvatar.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colAvatar.setMaxWidth(60);
        colAvatar.setStyle("-fx-alignment: CENTER;");
        colAvatar.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String nama, boolean empty) {
                super.updateItem(nama, empty);
                if (empty || nama == null) {
                    setGraphic(null);
                } else {
                    StackPane avatar = new StackPane();
                    Circle circle = new Circle(18, Color.web("#3498db"));
                    String inisial = nama.substring(0, 1).toUpperCase();
                    Label lblInit = new Label(inisial);
                    lblInit.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    avatar.getChildren().addAll(circle, lblInit);
                    setGraphic(avatar);
                }
            }
        });

        // Kolom 2: Nama
        TableColumn<MemberModel, String> colNama = new TableColumn<>("Nama Lengkap");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                } else { setText(null); }
            }
        });

        TableColumn<MemberModel, String> colEmail = new TableColumn<>("Email Address");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<MemberModel, String> colTelp = new TableColumn<>("No. Telepon");
        colTelp.setCellValueFactory(new PropertyValueFactory<>("noTelp"));

        TableColumn<MemberModel, String> colAlamat = new TableColumn<>("Alamat");
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        table.getColumns().addAll(colAvatar, colNama, colEmail, colTelp, colAlamat);

        ObservableList<MemberModel> memberList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM members")) {

            while (rs.next()) {
                memberList.add(new MemberModel(
                        rs.getInt("id_member"),
                        rs.getString("nama_member"),
                        rs.getString("email"),
                        rs.getString("no_telp"),
                        rs.getString("alamat")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }

        table.setItems(memberList);
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);

        setContent(content);
    }

    // --- HELPERS UMUM ---
    private void setContent(javafx.scene.Node content) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.getStyleClass().add("content-area");
        container.getChildren().add(content);
        rootLayout.setCenter(container);
    }

    private Button createMenuBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.BASELINE_LEFT);
        btn.setPadding(new Insets(12, 15, 12, 15));
        return btn;
    }

    private VBox createCard(String title, String number, String icon) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");
        card.setMinWidth(220);
        card.setAlignment(Pos.CENTER_LEFT);

        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 24px;");
        Label lblNum = new Label(number);
        lblNum.getStyleClass().add("stat-number");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-label");

        card.getChildren().addAll(lblIcon, lblNum, lblTitle);
        return card;
    }

    private int getDataCount(String query) {
        int count = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // =================================================================================
    // INNER CLASSES FOR DATA MODELS (DTO)
    // =================================================================================

    public static class LoanModel {
        private final SimpleIntegerProperty idLoan;
        private final SimpleStringProperty namaMember;
        private final SimpleStringProperty judulBuku;
        private final SimpleStringProperty tglPinjam;
        // FITUR BARU: Tanggal Kembali & Durasi
        private final SimpleStringProperty tglKembali;
        private final SimpleStringProperty durasi;
        private final SimpleStringProperty status;

        public LoanModel(int id, String member, String buku, String tglP, String tglK, String dur, String stat) {
            this.idLoan = new SimpleIntegerProperty(id);
            this.namaMember = new SimpleStringProperty(member);
            this.judulBuku = new SimpleStringProperty(buku);
            this.tglPinjam = new SimpleStringProperty(tglP);
            this.tglKembali = new SimpleStringProperty(tglK);
            this.durasi = new SimpleStringProperty(dur);
            this.status = new SimpleStringProperty(stat);
        }

        public int getIdLoan() { return idLoan.get(); }
        public String getNamaMember() { return namaMember.get(); }
        public String getJudulBuku() { return judulBuku.get(); }
        public String getTglPinjam() { return tglPinjam.get(); }
        public String getTglKembali() { return tglKembali.get(); }
        public String getDurasi() { return durasi.get(); }
        public String getStatus() { return status.get(); }
    }

    public static class MemberModel {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty nama;
        private final SimpleStringProperty email;
        private final SimpleStringProperty noTelp;
        private final SimpleStringProperty alamat;

        public MemberModel(int id, String nama, String email, String telp, String alamat) {
            this.id = new SimpleIntegerProperty(id);
            this.nama = new SimpleStringProperty(nama);
            this.email = new SimpleStringProperty(email);
            this.noTelp = new SimpleStringProperty(telp);
            this.alamat = new SimpleStringProperty(alamat);
        }

        public int getId() { return id.get(); }
        public String getNama() { return nama.get(); }
        public String getEmail() { return email.get(); }
        public String getNoTelp() { return noTelp.get(); }
        public String getAlamat() { return alamat.get(); }
    }
}