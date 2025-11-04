package controllers;

import btl_ltm_n3.Main;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * Controller cho bảng xếp hạng
 * Hiển thị top người chơi và thống kê cá nhân
 */
public class RankingController {
    @FXML private AnchorPane rootPane;
    @FXML private Label userRankLabel;
    @FXML private Label userStatsLabel;
    @FXML private HBox loadingBox;
    
    // TableView và các cột
    @FXML private TableView<PlayerRankData> leaderboardTable;
    @FXML private TableColumn<PlayerRankData, Integer> rankColumn;
    @FXML private TableColumn<PlayerRankData, String> usernameColumn;
    @FXML private TableColumn<PlayerRankData, Integer> scoreColumn;
    
    // Dữ liệu bảng xếp hạng
    private List<PlayerRankData> leaderboardList = new ArrayList<>();
    
    // Thông tin người dùng hiện tại
    private int currentUserRank = -1;
    private String currentUserStats = "";

    @FXML
    public void initialize() {
        // Set background
        var bgImage = new javafx.scene.image.Image(getClass().getResource("/images/background.jpg").toExternalForm());
        var bg = new javafx.scene.layout.BackgroundImage(
            bgImage,
            javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
            javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
            javafx.scene.layout.BackgroundPosition.CENTER,
            new javafx.scene.layout.BackgroundSize(100, 100, true, true, true, false)
        );
        rootPane.setBackground(new javafx.scene.layout.Background(bg));
        
        // Setup table columns với cell value factories
        setupTableColumns();
        
        // Lưu instance để SocketHandler có thể gọi
        Main.rankingController = this;
        
        // Tải dữ liệu bảng xếp hạng
        loadLeaderboardData();
    }
    
    /**
     * Cấu hình các cột của TableView
     */
    private void setupTableColumns() {
        // Set resize policy để không có cột trống
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Cột Hạng
        rankColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().rank).asObject());
        rankColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Cột Tên người chơi
        usernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().username));
        usernameColumn.setStyle("-fx-alignment: CENTER_LEFT; -fx-font-size: 14px;");
        
        // Cột Điểm số
        scoreColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().score).asObject());
        scoreColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6B35;");
        
        // Tùy chỉnh style cho table
        leaderboardTable.setStyle(
            "-fx-background-color: rgba(255,255,255,0.9);" +
            "-fx-background-radius: 15;"
        );
    }
    
    /**
     * Tải dữ liệu bảng xếp hạng từ server
     */
    public void loadLeaderboardData() {
        if (Main.socketHandler != null) {
            // Hiển thị loading
            showLoading(true);
            
            // Lấy top 100 người chơi
            Main.socketHandler.getLeaderboard(100);
            
            // Lấy thứ hạng của người chơi hiện tại
            String currentUser = Main.socketHandler.loginUser;
            if (currentUser != null && !currentUser.isEmpty()) {
                Main.socketHandler.getUserRank(currentUser);
                Main.socketHandler.getUserStats(currentUser);
            }
            
            System.out.println(" Đang tải dữ liệu bảng xếp hạng...");
        } else {
            showError("Lỗi kết nối", "Không thể kết nối đến server.");
        }
    }
    
    /**
     * Hiển thị/ẩn loading indicator
     */
    private void showLoading(boolean show) {
        Platform.runLater(() -> {
            if (loadingBox != null) {
                loadingBox.setVisible(show);
                loadingBox.setManaged(show);
            }
        });
    }
    
    /**
     * Cập nhật bảng xếp hạng (được gọi từ SocketHandler)
     */
    public void updateLeaderboardTable() {
        leaderboardList.clear();
        
        // Parse dữ liệu từ Main.leaderboardData
        // Format đơn giản: userId|username|score
        int rank = 1;
        for (String playerData : Main.leaderboardData) {
            String[] parts = playerData.split("\\|");
            if (parts.length >= 3) {
                try {
                    int userId = Integer.parseInt(parts[0]);
                    String username = parts[1];
                    int score = Integer.parseInt(parts[2]);
                    
                    PlayerRankData player = new PlayerRankData(
                        rank++, userId, username, score
                    );
                    leaderboardList.add(player);
                } catch (NumberFormatException e) {
                    System.err.println("❌ Error parsing player data: " + playerData);
                }
            }
        }
        
        System.out.println("✅ Đã cập nhật bảng xếp hạng với " + leaderboardList.size() + " người chơi");
        
        // Cập nhật TableView trên UI thread
        Platform.runLater(() -> {
            ObservableList<PlayerRankData> data = FXCollections.observableArrayList(leaderboardList);
            leaderboardTable.setItems(data);
            
            // Tô màu cho top 3
            highlightTopPlayers();
            
            // Ẩn loading
            showLoading(false);
        });
        
        // Hiển thị trong console để debug
        displayLeaderboardInConsole();
    }
    
    /**
     * Tô màu nổi bật cho top 3 players
     */
    private void highlightTopPlayers() {
        leaderboardTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<PlayerRankData> row = new javafx.scene.control.TableRow<>();
            row.itemProperty().addListener((obs, oldPlayer, newPlayer) -> {
                if (newPlayer != null) {
                    String style = "";
                    switch (newPlayer.rank) {
                        case 1:
                            style = "-fx-background-color: rgba(255, 215, 0, 0.3);"; // Vàng
                            break;
                        case 2:
                            style = "-fx-background-color: rgba(192, 192, 192, 0.3);"; // Bạc
                            break;
                        case 3:
                            style = "-fx-background-color: rgba(205, 127, 50, 0.3);"; // Đồng
                            break;
                        default:
                            // Highlight người chơi hiện tại
                            if (Main.socketHandler != null && 
                                newPlayer.username.equals(Main.socketHandler.loginUser)) {
                                style = "-fx-background-color: rgba(100, 200, 255, 0.3);"; // Xanh
                            }
                    }
                    row.setStyle(style);
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });
    }
    
    /**
     * Cập nhật thứ hạng của người dùng (được gọi từ SocketHandler)
     */
    public void updateUserRank(int rank) {
        this.currentUserRank = rank;
        
        Platform.runLater(() -> {
            if (userRankLabel != null) {
                userRankLabel.setText("Hạng của bạn: #" + rank);
            }
        });
        
        System.out.println("✅ Hạng của bạn: #" + rank);
    }
    
    /**
     * Cập nhật thống kê của người dùng (được gọi từ SocketHandler)
     * Format đơn giản: username|score
     */
    public void updateUserStats(String statsData) {
        this.currentUserStats = statsData;
        String[] stats = statsData.split("\\|");
        
        if (stats.length >= 2) {
            String displayText = String.format(
                "Điểm số: %s",
                stats[1]
            );
            
            Platform.runLater(() -> {
                if (userStatsLabel != null) {
                    userStatsLabel.setText(displayText);
                }
            });
            
            System.out.println("✅ Thống kê: " + displayText);
        }
    }
    
    /**
     * Hiển thị bảng xếp hạng trong console (cho debug)
     */
    private void displayLeaderboardInConsole() {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║              BẢNG XẾP HẠNG                         ║");
        System.out.println("╠════════════════════════════════════════════════════╣");
        System.out.println("║ Hạng | Tên người chơi            | Điểm số       ║");
        System.out.println("╠════════════════════════════════════════════════════╣");
        
        for (PlayerRankData player : leaderboardList) {
            System.out.printf("║ %-4d | %-25s | %-13d ║%n",
                player.rank,
                player.username.length() > 25 ? player.username.substring(0, 22) + "..." : player.username,
                player.score
            );
        }
        
        System.out.println("╚════════════════════════════════════════════════════╝\n");
    }
    
    /**
     * Làm mới dữ liệu bảng xếp hạng
     */
    public void handleRefresh() {
        System.out.println("🔄 Đang làm mới bảng xếp hạng...");
        loadLeaderboardData();
    }
    
    /**
     * Quay về trang chủ
     */
    public void handleBack() {
        try {
            Main.setRoot("home");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể quay về trang chủ.");
        }
    }
    
    /**
     * Hiển thị lỗi
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Lấy dữ liệu bảng xếp hạng
     */
    public List<PlayerRankData> getLeaderboardList() {
        return leaderboardList;
    }
    
    /**
     * Lấy thứ hạng hiện tại của người dùng
     */
    public int getCurrentUserRank() {
        return currentUserRank;
    }
    
    /**
     * Class chứa dữ liệu của một người chơi trong bảng xếp hạng
     * Đơn giản chỉ có: rank, userId, username, score
     */
    public static class PlayerRankData {
        public int rank;
        public int userId;
        public String username;
        public int score;
        
        public PlayerRankData(int rank, int userId, String username, int score) {
            this.rank = rank;
            this.userId = userId;
            this.username = username;
            this.score = score;
        }
        
        @Override
        public String toString() {
            return String.format("#%d - %s (Điểm: %d)",
                rank, username, score);
        }
    }
}

