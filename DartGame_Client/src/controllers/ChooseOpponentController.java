package controllers;

import btl_ltm_n3.Main;
import static btl_ltm_n3.Main.socketHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import models.User;

import java.util.List;

public class ChooseOpponentController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> colId;

    @FXML
    private TableColumn<User, String> colUsername;

    @FXML
    private TableColumn<User, String> colStatus;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Text text = new Text(status);
                    text.setFill(status.equalsIgnoreCase("online") ? Color.GREEN : Color.RED);
                    setGraphic(text);
                }
            }
        });

        // Load lần đầu
        updateUserTable(Main.listOnlineUser);
    }

    // Hàm public để update bảng
    public void updateUserTable(List<User> users) {
        Platform.runLater(() -> {
            userTable.getItems().setAll(users);
        });
    }

    @FXML
    private void handleReload() {
        updateUserTable(Main.listOnlineUser);
    }

    @FXML
    private void handleBack() {
        try {
            Main.setRoot("home");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfirm() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Opponent chosen: " + selected.getUsername());
            
            // Gửi lời mời Đối thủ => Server: Check + mời
            socketHandler.inviteToPlay(selected.getUsername());
        } else {
            System.out.println("No opponent selected!");
        }
    }
}