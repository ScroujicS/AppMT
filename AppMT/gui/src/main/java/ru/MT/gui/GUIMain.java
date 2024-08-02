package ru.MT.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import ru.MT.logic.ApplicationMintrance;


import java.io.IOException;

public class GUIMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.getIcons().add(new Image(getClass().getResource("/icon.jpg").toExternalForm()));
        Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.setTitle("МИНТРАНС РФ. Матрица расстояний");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
