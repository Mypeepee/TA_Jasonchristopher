package com.perpustakaan.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Konfigurasi Database (Sesuaikan jika XAMPP-mu ada passwordnya)
    private static final String URL = "jdbc:mysql://localhost:3306/db_perpustakaan";
    private static final String USER = "root";
    private static final String PASSWORD = "01082003Jason"; // Default XAMPP biasanya kosong

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // 1. Register Driver (Opsional di JDBC baru, tapi bagus untuk memastikan)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Buat Koneksi
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Koneksi Gagal! Cek apakah XAMPP sudah nyala?");
            e.printStackTrace();
        }
        return connection;
    }
}