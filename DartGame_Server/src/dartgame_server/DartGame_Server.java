package dartgame_server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// DART - SERVER
public class DartGame_Server extends Application {
    public static boolean isShutDown = false;
    public static ServerSocket ss;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ServerView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("Dart Game Server");
            primaryStage.setScene(scene);
            primaryStage.show();

            // chạy server socket trên thread riêng
            new Thread(DartGame_Server::startServer).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startServer() {
        try {
            int port = 99;
            ss = new ServerSocket(port);
            System.out.println("Created Server at port " + port + ".");

            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    10,
                    100,
                    10,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(8)
            );

            while (!isShutDown) {
                try {
                    Socket s = ss.accept();
                    System.out.println("+ New Client connected: " + s);

                    executor.execute(() -> handleClient(s));

                } catch (Exception ex) {
                    System.out.println("ERROR: " + ex);
                    isShutDown = true;
                }
            }

            System.out.println("Shutting down executor...");
            executor.shutdownNow();
            ss.close();

        } catch (Exception ex) {
            System.out.println("ERROR: " + ex);
        }
    }

    private static void handleClient(Socket s) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream()); // auto flush
            Scanner sc = new Scanner(System.in);
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Client " + s.getInetAddress() + ": " + line);

                // Gửi phản hồi về client
                String message = sc.nextLine();
                out.println("Server: " + message);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + s);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
