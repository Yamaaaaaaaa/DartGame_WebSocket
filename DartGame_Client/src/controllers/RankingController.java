package controllers;

import btl_ltm_n3.Main;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class RankingController {
    @FXML private BorderPane rootPane;

    @FXML
    public void initialize() {
        var bgImage = new javafx.scene.image.Image(getClass().getResource("/images/background.jpg").toExternalForm());
        var bg = new javafx.scene.layout.BackgroundImage(
            bgImage,
            javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
            javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
            javafx.scene.layout.BackgroundPosition.CENTER,
            new javafx.scene.layout.BackgroundSize(100, 100, true, true, true, false)
        );
        rootPane.setBackground(new javafx.scene.layout.Background(bg));
    }

    public void handleBack() {
        try {
            Main.setRoot("home");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
