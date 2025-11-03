/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package btl_ltm_n3;
import controllers.ChooseOpponentController;
import controllers.SocketHandler;
import controllers.StartGameController;

import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import models.User;
/**
 *
 * @author kaita
 */


// Dart - CLIENT


public class Main extends Application {

    /**
     * @param args the command line arguments
     */
    private static Stage primaryStage;
    public static SocketHandler socketHandler;
    private static Scene scene;
    
    
    public static ArrayList<User> listOnlineUser = new ArrayList<>();
    
    // Lưu controller instance
    public static ChooseOpponentController chooseOpponentController;

    // Lưu instance controller
    public static StartGameController startGameController; 

    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        scene = new Scene(loader.load(), 800, 800);
        stage.setScene(scene);
        stage.setTitle("JavaFX Login/Register Demo");
        stage.show();

        // Gắn sự kiện khi người dùng tắt app
        stage.setOnCloseRequest(event -> {
            event.consume(); // Ngăn chặn tắt app ngay lập tức

            // Gọi hàm xác nhận
            handleExitConfirmation();
        });
        
        // Tạo kết nối Socket
        socketHandler = new SocketHandler();
        socketHandler.connect("localhost", 99); // Tạm thời gọi ở đây, nao có thể làm 1 cái UI để điển Host - Port
    }
    
    private void handleExitConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc muốn thoát ứng dụng không?");
        alert.setContentText("Chọn OK để thoát, Cancel để ở lại.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                socketHandler.logout();
                                
                Platform.exit(); // Tắt ứng dụng (cái này sẽ giúp tắt luôn cả tiến trình chạy)
                System.exit(0);
            } else {
                System.out.println("Người dùng chọn: Ở lại ứng dụng");
            }
        });
    }
 
      // ?Load scene và lưu controller nếu cần
    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/" + fxml + ".fxml"));
        Parent root = loader.load();

        // Lưu controller nếu là chooseOpponent
        if (fxml.equals("chooseOpponent")) {
            chooseOpponentController = loader.getController();
        }

        // Nếu load StartGame thì lấy controller
        if (fxml.equals("startgame")) {
            startGameController = loader.getController();
        }
        scene.setRoot(root);
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
         launch(args);
    }
    
}


