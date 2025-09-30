package controllers;

import btl_ltm_n3.Main;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ChooseModeController {

    @FXML
    private Button btnPlayWithUser;

    @FXML
    private Button btnPlayWithAI;

    @FXML
    private void initialize() {
        btnPlayWithUser.setOnAction(this::handlePlayWithUser);
        btnPlayWithAI.setOnAction(this::handlePlayWithAI);
    }

    private void handlePlayWithUser(ActionEvent event) {
        try {
            Main.setRoot("choosecomponent");
        } catch (IOException ex) {
            Logger.getLogger(ChooseModeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handlePlayWithAI(ActionEvent event) {
        try {
            Main.setRoot("startgame");
        } catch (IOException ex) {
            Logger.getLogger(ChooseModeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) btnPlayWithUser.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
