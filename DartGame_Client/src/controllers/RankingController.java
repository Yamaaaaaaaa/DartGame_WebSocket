package controllers;

import btl_ltm_n3.Main;

public class RankingController {
    public void handleBack() {
        try {
            Main.setRoot("home");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
