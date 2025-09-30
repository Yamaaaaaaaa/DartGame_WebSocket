/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import controller.UserController;
import static dartgame_server.DartGame_Server.clientManager;
import static dartgame_server.DartGame_Server.isShutDown;
import static dartgame_server.DartGame_Server.roomManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author kaita
 */
public class Client implements Runnable{
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;
    
    // Mỗi luồng là 1 Client => Mỗi luồng sẽ lưu các thông tin sau:
    Room joinedRoom; 
    String loginUser;
    Client cCompetitor;
    
    public Client(Socket s) throws IOException {
        this.s = s;
        // obtaining input and output streams 
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
    }
      
    @Override
    public void run() {
        String received;
        boolean running = true;

        while (!isShutDown) {
            try {
                // receive the request from client
                received = dis.readUTF();

                System.out.println("Data Server Received: " + received);
                String type = received.split(";")[0];
               
                switch (type) {
                    case "LOGIN":
                        onReceiveLogin(received);
                        break;
                    case "REGISTER":
                        onReceiveRegister(received);
                        break;
                    case "GET_LIST_ONLINE":
                        onReceiveGetListOnline();
                        break;
//                    case "GET_INFO_USER":
//                        onReceiveGetInfoUser(received);
//                        break;
                    case "INVITE_TO_PLAY":
                        onReceiveInviteToPlay(received);
                        break;
                    case "ACCEPT_PLAY":
                        onReceiveAcceptPlay(received);
                        break;
                    case "NOT_ACCEPT_PLAY":
                        onReceiveNotAcceptPlay(received);
                        break;
                    case "LOGOUT":
                        onReceiveLogout();
                        break;  
                    case "EXIT":
                        running = false;
                        break;
                }

            } catch (IOException ex) {
                break;
            } 
//            catch (SQLException ex) {
//                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }

        try {
            // closing resources 
            this.s.close();
            this.dis.close();
            this.dos.close();
            System.out.println("- Client disconnected: " + s);

            // remove from clientManager
            clientManager.remove(this);

        } catch (IOException ex) {
            System.out.println("ERROR RUNNING: "+ ex);
        }
    }
      
    

    // send data fucntions
    public String sendData(String data) {
        try {
            this.dos.writeUTF(data);
            return "success";
        } catch (IOException e) {
            System.err.println("Send data failed!");
            return "failed;" + e.getMessage();
        }
    }
    
    private void onReceiveLogin(String received) {
        // get email / password from data
        String[] splitted = received.split(";");
        String username = splitted[1];
        String password = splitted[2];

        // check login
        String result = new UserController().login(username, password);

        if (result.split(";")[0].equals("success")) {
            // set login user
            this.loginUser = username;
        }
        
        // send result
        sendData("LOGIN" + ";" + result);
        onReceiveGetListOnline();
    }
    private void onReceiveRegister(String received) {
        // get email / password from data
        String[] splitted = received.split(";");
        String username = splitted[1];
        String password = splitted[2];

        // reigster
        String result = new UserController().register(username, password);

        // send result
        sendData("REGISTER" + ";" + result);
    }
    private void onReceiveLogout() {
        this.loginUser = null;
        // send result
        sendData("LOGOUT" + ";" + "success");
        onReceiveGetListOnline();
    }
    private void onReceiveGetListOnline() {
        String result = clientManager.getListUseOnline();
        
        // send result
        String msg = "GET_LIST_ONLINE" + ";" + result;
        clientManager.broadcast(msg);
    }
    private void onReceiveInviteToPlay(String received){
        // Check Status trước đã
        System.out.println("==================================");
        System.out.println("Client Invited String: " + received);

        String[] splitted = received.split(";");
        String hostname = splitted[1];
        String invitedname = splitted[2]; // Check tk dc moi
        
        String status = "";
        Client c = clientManager.find(invitedname);
        System.out.println("------Client Invited: " + c.getLoginUser());
        if (c == null) {
            status = "OFFLINE";
        } else {
            if (c.getJoinedRoom() == null) {
                status = "ONLINE";

                joinedRoom = roomManager.createRoom();
                System.out.println("------Create new Room: " + joinedRoom.getId());

                joinedRoom.addClient(this);
                cCompetitor = clientManager.find(invitedname);
                
                // Send Invitation to Invited Ussr:
                String msg = "INVITE_TO_PLAY;" + "success;" + hostname + ";" + invitedname + ";" + joinedRoom.getId();
                System.out.println("------Message invite: " + msg);
                clientManager.sendToAClient(invitedname, msg);
            } else {
                status = "INGAME";
            }
        }
        System.out.println("==================================");
    }
    private void onReceiveCheckStatusUser(String received) {
        String[] splitted = received.split(";");
        String username = splitted[1];
        
        String status = "";
        Client c = clientManager.find(username);
        if (c == null) {
            status = "OFFLINE";
        } else {
            if (c.getJoinedRoom() == null) {
                status = "ONLINE";
            } else {
                status = "INGAME";
            }
        }
        // send result
        sendData("CHECK_STATUS_USER" + ";" + username + ";" + status);
    }
    private void onReceiveAcceptPlay(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];
        String roomId = splitted[3];
        
        Room room = roomManager.find(roomId);
        joinedRoom = room;
        joinedRoom.addClient(this);
        
        cCompetitor = clientManager.find(userHost);
        
        // send result
        String msg = "ACCEPT_PLAY;" + "success;" + userHost + ";" + userInvited + ";" + joinedRoom.getId();
        clientManager.sendToAClient(userHost, msg);
    }     
    private void onReceiveNotAcceptPlay(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];
        String roomId = splitted[3];
         
        // Xóa thông tin room khỏi client
        clientManager.find(userHost).setJoinedRoom(null);
        clientManager.find(userInvited).setJoinedRoom(null);
        
        // Xóa room: 
        Room room = roomManager.find(roomId);
        roomManager.remove(room);
        
        // send res: 
        String msg = "NOT_ACCEPT_PLAY;" + "success;" + userHost + ";" + userInvited + ";" + room.getId();
        clientManager.sendToAClient(userHost, msg);
    } 
    
    // GET
    public Room getJoinedRoom(){ // TAm thoi de day da
        return null;
    }
    public String getLoginUser() {
        return loginUser;
    }
    public void setJoinedRoom(Room joinedRoom){ // TAm thoi de day da
        this.joinedRoom = joinedRoom;
    }
}
