/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package btl_ltm_n3;
import controllers.SocketHandler;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        scene = new Scene(loader.load(), 800, 800);
        stage.setScene(scene);
        stage.setTitle("JavaFX Login/Register Demo");
        stage.show();

        // Tạo kết nối Socket
        socketHandler = new SocketHandler();
        socketHandler.connect("localhost", 99); // Tạm thời gọi ở đây, nao có thể làm 1 cái UI để điển Host - Port
    }
    
    public static void setRoot(String fxml) {
        Platform.runLater(() -> { // đảm bảo chạy trên JavaFX Application Thread
            try {
                scene.setRoot(loadFXML(fxml));
            } catch (IOException e) {
                System.out.println("Không tìm thấy file FXML: " + fxml);
                e.printStackTrace();
            }
        });
    }

    private static Parent loadFXML(String fxml) throws IOException {
        // file fxml nằm trong /views/
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }
 
    public static void main(String[] args) {
        // TODO code application logic here
         launch(args);
    }
    
}


