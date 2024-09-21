package com.example._pill_20102669_datastruc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PillView.fxml"));
        Parent root = loader.load();

        PillController controller = loader.getController();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pill and Capsule Analyzer");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
