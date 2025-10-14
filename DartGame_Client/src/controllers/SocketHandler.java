/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import btl_ltm_n3.Main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.User;

/**
 *
 * @author kaita
 */
public class SocketHandler {
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;

    public String loginUser = null; // lưu tài khoản đăng nhập hiện tại
    Thread listener = null;
    public String competitor = "";
    String roomIdPresent = null; // lưu room hiện tại mà người chơi đang ở 
    public boolean checkYouAreInvited = false;
    
    public String connect(String addr, int port){
        try {
            s = new Socket(addr, port);
            
            // obtaining input and output streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            // close old listener
            if (listener != null && listener.isAlive()) {
                listener.interrupt();
            }

            // listen to server
            listener = new Thread(this::listen);
            listener.start();

            // connect success
            return "success";
        } catch (IOException ex) {
             return "failed;" + ex.getMessage();
        }
    }
    
    private void listen(){
        boolean running = true;
        while(running){
            try {
                // Nhận Dữ liệu - Phản hồi - Request từ Server
                String received = dis.readUTF();
                System.out.println("RECEIVED: " + received);

                String type = received.split(";")[0];
                switch (type) {
                    case "LOGIN":
                        onReceiveLogin(received);
                        break;
                    case "REGISTER":
                        onReceiveRegister(received);
                        break;
                    case "LOGOUT":
                        onReceiveLogout(received);
                        break;
                    case "GET_LIST_ONLINE":
                        onReceiveGetListOnline(received);
                        break;
                    case "INVITE_TO_PLAY":
                        onReceiveInviteToPlay(received);
                        break;
                    case "ACCEPT_PLAY":
                        onReceiveAcceptPlay(received);
                        break;
                    case "NOT_ACCEPT_PLAY":
                        onReceiveNotAcceptPlay(received);
                        break;
                    case "LEAVE_TO_GAME":
                        onReceiveLeaveToGame(received);
                        break; 
                    case "TURN_THROW":
                        onReceiveTurnThrow(received);
                        break;
                    case "TURN_ROTATE":
                        onReceiveTurnRotate(received);
                        break;
                    case "CHAT_MESSAGE": // Ng gửi, ng nhận, roomId,  message
                        onReceiveChatMessage(received);
                        break; 
                    case "END_GAME":
                        onReceiveEndGame(received);
                        break;
                    case "EXIT":
                        running = false;
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
                running = false;    
            }
        }
    }
    
    
    // ------------------------------------------------------------------------
    // RECEIVE: 
    private void onReceiveLogin(String received) {
        // get status from data
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("failed")) {
            // hiển thị lỗi
            String failedMsg = splitted[2];
            System.out.println("Đăng nhập Lỗi, vui lòng kiểm tra lại tài khoản - mật khẩu: "+ failedMsg); // Cần UI: Text Cảnh Báo
        } else if (status.equals("success")) {
            // lưu user login
            this.loginUser = splitted[2];
            System.out.println("Đăng nhập thành công");
            try {
                Main.setRoot("home");
            } catch (Exception ex) {
                System.out.println("Trang không tồn tại, kiểm tra lại Router App"); // Cần UI: Trang Notfound
            }
        }
    }
    
    private void onReceiveRegister(String received) {
        // get status from data
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("failed")) {
            // hiển thị lỗi
            String failedMsg = splitted[2];
            System.out.println("Đăng kí bị lỗi, vui lòng kiểm tra lại: "+ failedMsg); // Cần UI: Text Cảnh Báo

        } else if (status.equals("success")) {
            try {
                 Main.setRoot("login");
            } catch (Exception ex) {
                System.out.println("Trang không tồn tại, kiểm tra lại Router App"); // Cần UI: Trang Notfound
            }
        }
    }
    
    private void onReceiveLogout(String received) {
        // get status from data
        String[] splitted = received.split(";");
        String status = splitted[1];
        
        if (status.equals("success")) {
            try {
                Main.setRoot("login");
            } catch (Exception ex) {
                System.out.println("Trang không tồn tại, kiểm tra lại Router App"); // Cần UI: Trang Notfound
            }
        }
    }
    private void onReceiveGetListOnline(String received) {
        // get status from data
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("success")) {
            int userCount = Integer.parseInt(splitted[2]);

            // Reset lại list
            Main.listOnlineUser.clear();

            if (userCount > 0) {
                for (int i = 3; i < userCount + 3; i++) {
                    String username = splitted[i];
                    if (!username.equals(loginUser) && !username.equals("null")) { // Ko tính người dùng đang online
                        // chỉ lưu username, id và status tạm thời để mặc định
                        User u = new User(i - 2, username, "online");
                        Main.listOnlineUser.add(u);
                    }
                }
            }

            // debug
            System.out.print("LIST USER ONLINE: ");
            for (User u : Main.listOnlineUser) {
                System.out.print(u.getUsername() + " ");
            }
            System.out.println();
            
            // Cập nhật bảng nếu controller đã load
            if (Main.chooseOpponentController != null) {
                Main.chooseOpponentController.updateUserTable(Main.listOnlineUser);
            }

        } else {
            System.out.println("Have some error!");
        }
    }
        
        //-- Invite Room + Accept + Reject
    private void onReceiveInviteToPlay(String received) {
        System.out.println("==================================");
        System.out.println("------INVITATION: " + received);

        String[] splitted = received.split(";");
        String status = splitted[1];
        
        if(status.equals("success")){
            String userHost = splitted[2];
            String userInvited = splitted[3];
            String roomId = splitted[4];
            System.out.println("Nhận được lời mời: "+ received);
            
            // Tạo Dialog mời người chơi vào trận (chắc để 1 cái log ở góc dưới thôi)
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NotificationDialog.fxml"));
                    Parent root = loader.load();

                    NotificationController controller = loader.getController();
                    controller.setMessage("Người chơi " + userHost + " mời bạn vào phòng " + roomId);
                    controller.setOnAccept(() -> {
                        System.out.println("Đồng ý tham gia phòng " + roomId);
                        try {
                            // TODO: gửi gói tin Accept lên server + Lưu Host + Invited User + Room ID vào
                            roomIdPresent = roomId;
                            this.competitor = userHost;
                            sendData("ACCEPT_PLAY;" + userHost + ";" + userInvited + ";" + roomId);
                            checkYouAreInvited = true;
                            Main.setRoot("startgame");
                        } catch (IOException ex) {
                            sendData("NOT_ACCEPT_PLAY;" + userHost + ";" + userInvited + ";" + roomId);
                            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    controller.setOnDecline(() -> {
                        System.out.println("Từ chối tham gia phòng " + roomId);
                        // TODO: gửi gói tin Decline lên server
                    });

                    Stage dialogStage = new Stage();
                    dialogStage.initStyle(StageStyle.UNDECORATED);
                    dialogStage.setAlwaysOnTop(true);

                    Scene scene = new Scene(root);
                    dialogStage.setScene(scene);

                    // hiển thị ở góc dưới phải màn hình
                    Screen screen = Screen.getPrimary();
                    javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                    dialogStage.setX(bounds.getMaxX() - 320); // 300 width + margin
                    dialogStage.setY(bounds.getMaxY() - 160); // 120 height + margin

                    dialogStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println("==================================");
    }
    private void onReceiveAcceptPlay(String received){
        String[] splitted = received.split(";");
        String status = splitted[1];
        
        if(status.equals("success")){
            String userHost = splitted[2];
            String userInvited = splitted[3];
            roomIdPresent = splitted[4];
            // Nhớ lưu dữ liệu của User Invited
            try {
                this.competitor = userInvited;
                System.out.println("Người chơi: " + userInvited + " đồng ý lời mời của bạn!");
                Main.setRoot("startgame");
            } catch (IOException ex) {
                Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void onReceiveNotAcceptPlay(String received){
        String[] splitted = received.split(";");
        String status = splitted[1];
        
        if(status.equals("success")){
            String userHost = splitted[2];
            String userInvited = splitted[3];
            System.out.println("Người chơi: " + userInvited + " từ chối lời mời của bạn!");
        }
    }
    private void onReceiveLeaveToGame(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        
        if(status.equals("success")){
            String userHost = splitted[2];
            String userInvited = splitted[3];
            Platform.runLater(() -> {
                if (Main.startGameController != null) {
                    Main.startGameController.showWinnerDialog(userHost);
                }
            });
        }
    }
    private void onReceiveTurnThrow(String received){
        String[] splitted = received.split(";");
        String userTurn = splitted[1];
        
        if(splitted.length == 9){
            String competitorName = splitted[2];
            String roomId = splitted[3];
            String aigle = splitted[4];
            String score1 = splitted[5];
            String score2 = splitted[6];
            String score3 = splitted[7];
            String scoreRemaining = splitted[8];
            
            Platform.runLater(() -> {
                Main.startGameController.updateCompetitorStatus(competitorName, roomId,aigle, score1, score2, score3, scoreRemaining);
            });
        }
        Platform.runLater(() -> {
            Main.startGameController.setTurn(userTurn);
        });
    }
    private void onReceiveTurnRotate(String received){
        String[] splitted = received.split(";");
        String userTurn = splitted[1];
        Platform.runLater(() -> {
            Main.startGameController.setTurnRotate(userTurn);
        });
    }
    private void onReceiveEndGame(String received){
        String[] splitted = received.split(";");
        String userName = splitted[1];
        String competitorName = splitted[2];
        String roomId = splitted[3];
        String winnerName = splitted[4];
        System.out.println("Trận đấu kết thúc, người chiến thắng là: " + winnerName);
        
         // Gọi UI hiển thị thông báo trên giao diện game
        Platform.runLater(() -> {
            if (Main.startGameController != null) {
                Main.startGameController.showWinnerDialogEndGame(winnerName);
            } else {
                System.out.println("⚠️ Không tìm thấy StartGameController để hiển thị kết quả!");
            }
        });
    }
    public void onReceiveChatMessage(String received){
        String[] splitted = received.split(";");
        String sendName = splitted[1];
        String competitorName = splitted[2];
        String roomId = splitted[3];
        String message = splitted[4];
                
        // set chat vào giao diện (check đúng room - compe tránh lỗi)
        if(sendName.equals(competitor) && roomId.equals(roomIdPresent) && Main.startGameController != null){
            Platform.runLater(() -> {
                Main.startGameController.addMessage(sendName + ": " + message);
            });
        }
        else System.out.println("Cõ lối xảy ra");
    }
    
    
    // ------------------------------------------------------------------------
    // SEND:
    public void sendData(String data) {
        try {
            System.out.println("Data Client Sended: "+ data);
            dos.writeUTF(data);
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class
                .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void login(String email, String password) {
        // prepare data
        String data = "LOGIN" + ";" + email + ";" + password;
        // send data
        sendData(data);
    }
    public void register(String email, String password) {
        // prepare data
        String data = "REGISTER" + ";" + email + ";" + password;
        // send data
        sendData(data);
    }
    public void logout() {
        // prepare data
        this.loginUser = null;
        sendData("LOGOUT");
    }
    public void getListOnline() {
        sendData("GET_LIST_ONLINE");
    }
    public void checkStatusUser(String username){
        sendData("CHECK_sTATUS_USER;" + username);
    }
    public void inviteToPlay(String opponentName){
        sendData("INVITE_TO_PLAY;" + loginUser + ";" + opponentName);
    }
    public void leaveGame(){
        sendData("LEAVE_TO_GAME;" + loginUser + ";" + competitor + ";" + roomIdPresent);
    }
    
    
    // Getter setter:

    public String getRoomIdPresent() {
        return roomIdPresent;
    }

    public void setRoomIdPresent(String roomIdPresent) {
        this.roomIdPresent = roomIdPresent;
    }
    
}
