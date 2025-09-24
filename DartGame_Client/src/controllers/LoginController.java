package controllers;

import btl_ltm_n3.Main;
import static btl_ltm_n3.Main.socketHandler;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.equals("")) {
            System.out.println("Invalid username");    
        } else if (password.equals("")) {
            System.out.println("Invalid password"); 
        } else {
            socketHandler.login(username, password);
        }
    }

    public void goToRegister() {
        try {
            Main.setRoot("register");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
