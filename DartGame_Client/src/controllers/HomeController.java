package controllers;

import btl_ltm_n3.Main;
import static btl_ltm_n3.Main.socketHandler;

public class HomeController {
    public void handleLogout() {
        try {
            socketHandler.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleStartGame() {
        try {
            Main.setRoot("choosemode");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleClickStartGameWithBot() {
        try {
            Main.setRoot("startgamewithbot");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleRanking() {
        try {
            Main.setRoot("ranking");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
