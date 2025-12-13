package org.example.fuzzy_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class FuzzyFinderApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FuzzyFinderApp.class.getResource("dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        stage.setTitle("Fuzzy Finder");
        // stage.setFullScreen(true);
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("fzf_icon.png")));
        stage.show();
    }
}
