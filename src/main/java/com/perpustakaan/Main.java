package com.perpustakaan;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Panggil tampilan Login
        LoginView loginView = new LoginView();

        // Setup Scene (Ukuran Window awal)
        Scene scene = new Scene(loginView.getView(), 800, 600);

        // Hubungkan dengan CSS (Wajib biar modern)
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Sistem Perpustakaan Modern");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}