package controllers;

import btl_ltm_n3.Main;

public class HomeController {
    public void handleLogout() {
        try {
            Main.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleStartGame() {
        try {
            Main.setRoot("startgame");
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
