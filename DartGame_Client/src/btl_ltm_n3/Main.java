package btl_ltm_n3;

import controllers.ChooseOpponentController;
import controllers.SocketHandler;
import controllers.StartGameController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import models.User;

import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {

    private static Stage primaryStage;
    public static SocketHandler socketHandler;
    private static Scene scene;

    public static ArrayList<User> listOnlineUser = new ArrayList<>();

    public static ChooseOpponentController chooseOpponentController;
    public static StartGameController startGameController;

    // 🎵 Biến lưu trình phát nhạc
    private static MediaPlayer backgroundPlayer;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        scene = new Scene(loader.load(), 800, 800);
        stage.setScene(scene);
        stage.setTitle("Darts Game");
        stage.show();

        // Phát nhạc nền
        playBackgroundMusic();

        // Sự kiện đóng app
        stage.setOnCloseRequest(event -> {
            event.consume();
            handleExitConfirmation();
        });

        // Kết nối Socket
        socketHandler = new SocketHandler();
        socketHandler.connect("localhost", 99);
    }

    private void handleExitConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc muốn thoát ứng dụng không?");
        alert.setContentText("Chọn OK để thoát, Cancel để ở lại.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                socketHandler.logout();
                stopBackgroundMusic(); // 🧩 Dừng nhạc khi thoát
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/" + fxml + ".fxml"));
        Parent root = loader.load();

        if (fxml.equals("chooseOpponent")) {
            chooseOpponentController = loader.getController();
        }
        if (fxml.equals("startgame")) {
            startGameController = loader.getController();
        }

        scene.setRoot(root);
    }

    // ======================================================
    // NHẠC NỀN: HÀM BẬT / TẮT
    // ======================================================

    public static void playBackgroundMusic() {
        try {
            if (backgroundPlayer == null) {
                String path = Main.class.getResource("/musics/background_bb_0.mp3").toExternalForm();
                Media media = new Media(path);
                backgroundPlayer = new MediaPlayer(media);
                backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // lặp vô hạn
                backgroundPlayer.setVolume(0.3); // âm lượng vừa phải
            }
            backgroundPlayer.play();
        } catch (Exception e) {
            System.out.println("Không thể phát nhạc nền: " + e.getMessage());
        }
    }

    public static void stopBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
        }
    }

    public static void toggleBackgroundMusic() {
        if (backgroundPlayer == null) return;

        if (backgroundPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            backgroundPlayer.pause();
        } else {
            backgroundPlayer.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
