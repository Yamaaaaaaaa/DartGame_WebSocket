package controllers;

import btl_ltm_n3.Main;
import database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import java.sql.*;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            showAlert("Đăng ký thành công!");
            Main.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Tên đăng nhập đã tồn tại!");
        }
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
