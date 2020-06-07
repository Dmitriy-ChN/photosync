package edu.mgkit.exam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML("main_scene"));
        stage.setScene(scene);
        stage.show();
    }

    static Stage setScene(String fxml) throws IOException {
        Scene scene2 = new Scene(loadFXML(fxml));
        Stage stage2 = new Stage();
        stage2.setScene(scene2);
        stage2.show();
        return stage2;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}