package controllers;

import btl_ltm_n3.Main;
import static btl_ltm_n3.Main.socketHandler;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        socketHandler.register(username, password);
    }

    public void goToLogin() {
        try {
            Main.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}
