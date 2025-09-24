/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.sql.Connection;
import database.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author kaita
 */
public class UserController {
    // 1 controller này quản lý 1 người dùng thôi
    
    //  SQL
    private final String INSERT_USER = "INSERT INTO users (username, password) VALUES (?, ?)";
    
    private final String CHECK_USER = "SELECT id from users WHERE username = ? limit 1";
    
    private final String LOGIN_USER = "SELECT username, password FROM users WHERE username=? AND password=?";
    
    private final String GET_INFO_USER = "SELECT username, password FROM users WHERE username=?";

    
    //  Instance
    private final Connection con;
    
    public UserController() {
        this.con = DBConnection.getInstance().getConnection();
    }
    
     public String register(String username, String password) {
    	//  Check user exit
        try {
            PreparedStatement p = con.prepareStatement(CHECK_USER);
            p.setString(1, username);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                System.out.println("Register failed");
                return "failed;" + "User Already Exit";
            } else {
                r.close();
                p.close();
                p = con.prepareStatement(INSERT_USER);
                System.out.println("Register Successful");
                p.setString(1, username);
                p.setString(2, password);
                p.executeUpdate();
                p.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "success;";
    }
  
    public String login(String username, String password) {
    	//  Check user exit
        try {
            PreparedStatement p = con.prepareStatement(LOGIN_USER);
            //  Login User 
            p.setString(1, username);
            p.setString(2, password);
            ResultSet r = p.executeQuery();
//            System.out.println("r: " + r.next());

            if (r.next()) {
                return "success;" + username;
            } else {
                return "failed;" + "Please enter the correct account password!";
            }
        } catch (SQLException e) {
            
        }
        return null;
    }
}
