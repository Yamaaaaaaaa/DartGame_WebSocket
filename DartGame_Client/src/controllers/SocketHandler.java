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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kaita
 */
public class SocketHandler {
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;

    String loginUser = null; // lưu tài khoản đăng nhập hiện tại
    Thread listener = null;
    
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
                    case "EXIT":
                        running = false;
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

            Vector vheader = new Vector();
            vheader.add("User");

            Vector vdata = new Vector();
            if (userCount > 1) {
                for (int i = 3; i < userCount + 3; i++) {
                    String user = splitted[i];
                    if (!user.equals(loginUser) && !user.equals("null")) {
                        Vector vrow = new Vector();
                        vrow.add(user);
                        vdata.add(vrow);
                    }
                }
                
                System.out.println("LIST USER: vdata: " + vdata + " vheader: " + vheader);
            } else {
            }
            
        } else {
            System.out.println("Have some error!");
        }
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
}
