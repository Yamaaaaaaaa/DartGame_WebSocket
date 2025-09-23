/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package btl_ltm_n3;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Scene scene = new Scene(loader.load(), 800, 800);
        stage.setScene(scene);
        stage.setTitle("JavaFX Login/Register Demo");
        stage.show();

        // Kết nối socket trong luồng riêng
        new Thread(Main::connectServer).start();
    }
    
    public static void setRoot(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/" + fxml + ".fxml"));
        Scene scene = new Scene(loader.load(), 800, 800);
        primaryStage.setScene(scene);
    }
    public static Socket socket;

    public static void connectServer(){
         // TODO code application logic here
            try {
                socket = new Socket("localhost", 99);
                System.out.println("Thành công kết nối đến server");

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                Scanner sc = new Scanner(System.in);
                String message;

                while(true){
                    System.out.println("Client: ");
                    message = sc.nextLine();
                    writer.println(message);
                    writer.flush();
                    
                    message = reader.readLine();
                    System.out.println("Server: " + message);
                }
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    public static void main(String[] args) {
        // TODO code application logic here
         launch(args);
    }
    
}


