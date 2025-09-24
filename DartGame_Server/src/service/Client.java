/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import controller.UserController;
import static dartgame_server.DartGame_Server.clientManager;
import static dartgame_server.DartGame_Server.isShutDown;
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

    String loginUser;
    
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
                    case "LOGOUT":
                        onReceiveLogout();
                        break;  
                    case "EXIT":
                        running = false;
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




    // Get set
    public String getLoginUser() {
        return loginUser;
    }
}
