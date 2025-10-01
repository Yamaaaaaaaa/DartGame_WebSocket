package controllers;

import btl_ltm_n3.Main;
import static btl_ltm_n3.Main.socketHandler;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class StartGameController implements Initializable {

    @FXML
    private ListView<String> chatList;

    @FXML
    private TextField chatInput;
    
    @FXML
    private Pane gamePane;
    
    @FXML
    private Pane scoreboardPane;
    
    @FXML
    private Button startButton;
    
    @FXML
    private Label instructionLabel;
    
    // Game variables
    private static final double BOARD_SIZE = 400;
    private static final double CENTER_X = 225; // Center of 450px width
    private static final double CENTER_Y = BOARD_SIZE / 2;
    private static final double RADIUS = BOARD_SIZE / 2.5;
    private static final int[] POINTS = {20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5};
    
    private Line lineX, lineY;
    private Timeline lineXAnimation, lineYAnimation;
    private boolean started = false;
    private boolean inXAxis = false;
    private boolean inYAxis = false;
    
    private int playerScore = 301;
    private int computerScore = 301;
    private int currentDart = 0; // Track current dart in turn (0, 1, 2)
    private int[] currentTurnScores = new int[3]; // Store scores for current turn
    private boolean isPlayerTurn = true;
    private boolean gameOver = false;
    
    private List<Circle> darts = new ArrayList<>();
    
    private Rectangle scoreBoard;
    private Text playerScoreText, computerScoreText;
    private Text[] playerTurnTexts = new Text[3];
    private Text[] computerTurnTexts = new Text[3];
    private Text winnerText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGame();
        setupButtonEffects();
    }

    private void setupButtonEffects() {
        startButton.setOnMouseEntered(e -> {
            startButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #5CBF60, #4CAF50); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 16px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 15, 0, 0, 8); " +
                "-fx-cursor: hand; " +
                "-fx-opacity: 1.0; " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;"
            );
        });
        startButton.setOnMouseExited(e -> {
            startButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 16px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5); " +
                "-fx-cursor: hand; " +
                "-fx-opacity: 0.95; " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"
            );
        });
    }

    private void setupGame() {
        gamePane.setPrefSize(450, 400);
        
        // Draw dartboard
        drawDartboard();
        
        createScoreboard();
        
        // Create moving lines
        lineX = new Line(0, 0, 0, BOARD_SIZE);
        lineX.setStroke(Color.ORANGE);
        lineX.setStrokeWidth(3);
        lineX.setVisible(false);
        gamePane.getChildren().add(lineX);
        
        lineY = new Line(0, 0, 450, 0); // Updated to match pane width
        lineY.setStroke(Color.ORANGE);
        lineY.setStrokeWidth(3);
        lineY.setVisible(false);
        gamePane.getChildren().add(lineY);
        
    }

    private void drawDartboard() {
        double angleStep = Math.PI * 2 / POINTS.length;
        
        // Draw main dartboard sections
        for (int i = 0; i < POINTS.length; i++) {
            Color singleColor = (i % 2 == 0) ? Color.BLACK : Color.WHITE;
            Color otherColor = (i % 2 == 0) ? Color.RED : Color.GREEN;
            
            // Single area (main area)
            Polygon single = createDartSection(i, angleStep, 0, RADIUS, singleColor);
            single.setUserData(POINTS[i]);
            gamePane.getChildren().add(single);
            
            // Double area (outer ring)
            Polygon doubleArea = createDartSection(i, angleStep, RADIUS - 20, RADIUS, otherColor);
            doubleArea.setUserData(POINTS[i] * 2);
            gamePane.getChildren().add(doubleArea);
            
            // Triple area (inner ring)
            Polygon tripleArea = createDartSection(i, angleStep, RADIUS/2, RADIUS/2 + 20, otherColor);
            tripleArea.setUserData(POINTS[i] * 3);
            gamePane.getChildren().add(tripleArea);
            
            // Add numbers
            double angle = angleStep * i - Math.PI / 2;
            double numberX = CENTER_X + (RADIUS + 30) * Math.cos(angle);
            double numberY = CENTER_Y + (RADIUS + 30) * Math.sin(angle);
            
            Text number = new Text(numberX - 10, numberY + 5, String.valueOf(POINTS[i]));
            number.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            number.setFill(Color.BLACK);
            gamePane.getChildren().add(number);
        }
        
        // Outer bull (25 points)
        Circle outerBull = new Circle(CENTER_X, CENTER_Y, 25, Color.GREEN);
        outerBull.setUserData(25);
        gamePane.getChildren().add(outerBull);
        
        // Bull's eye (50 points)
        Circle bullsEye = new Circle(CENTER_X, CENTER_Y, 12, Color.RED);
        bullsEye.setUserData(50);
        gamePane.getChildren().add(bullsEye);
    }
    
    private Polygon createDartSection(int section, double angleStep, double innerRadius, double outerRadius, Color color) {
        Polygon polygon = new Polygon();
        
        double startAngle = angleStep * section - angleStep/2 - Math.PI/2;
        double endAngle = angleStep * section + angleStep/2 - Math.PI/2;
        
        // Create points for the polygon
        if (innerRadius == 0) {
            // For center sections, start from center
            polygon.getPoints().addAll(new Double[]{
                CENTER_X, CENTER_Y
            });
        }
        
        // Add outer arc points
        for (double angle = startAngle; angle <= endAngle; angle += 0.1) {
            double x = CENTER_X + outerRadius * Math.cos(angle);
            double y = CENTER_Y + outerRadius * Math.sin(angle);
            polygon.getPoints().addAll(new Double[]{x, y});
        }
        
        // Add inner arc points (if not center section)
        if (innerRadius > 0) {
            for (double angle = endAngle; angle >= startAngle; angle -= 0.1) {
                double x = CENTER_X + innerRadius * Math.cos(angle);
                double y = CENTER_Y + innerRadius * Math.sin(angle);
                polygon.getPoints().addAll(new Double[]{x, y});
            }
        }
        
        polygon.setFill(color);
        polygon.setStroke(Color.BLACK);
        polygon.setStrokeWidth(1);
        
        return polygon;
    }

    private void startGame() {
        started = true;
        gameOver = false;
        playerScore = 301;
        computerScore = 301;
        currentDart = 0;
        isPlayerTurn = true;
        currentTurnScores = new int[3];
        darts.clear();
        
        updateScoreboard();
        startButton.setText("STOP X");
        startButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #FF6B6B, #FF5252); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5); " +
            "-fx-cursor: hand; " +
            "-fx-opacity: 0.95;"
        );
        instructionLabel.setText("Player's turn - Dart 1/3");
        startXAxis();
    }
    
    private void nextTurn() {
        currentDart++;
        
        if (currentDart >= 3) {
            // End of turn, switch players
            currentDart = 0;
            
            if (isPlayerTurn) {
                // Computer's turn - simulate computer play
                isPlayerTurn = false;
                simulateComputerTurn();
            } else {
                // Back to player's turn
                isPlayerTurn = true;
                clearTurnScores();
                if (!gameOver) {
                    instructionLabel.setText("Player's turn - Dart 1/3");
                    startXAxis();
                }
            }
        } else {
            // Continue current player's turn
            if (isPlayerTurn && !gameOver) {
                instructionLabel.setText("Player's turn - Dart " + (currentDart + 1) + "/3");
                startXAxis();
            }
        }
    }
    
    private void simulateComputerTurn() {
        instructionLabel.setText("Computer's turn...");
        
        Timeline computerDelay = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            for (int i = 0; i < 3; i++) {
                int computerDartScore = (int)(Math.random() * 60) + 1; // Random score 1-60
                currentTurnScores[i] = computerDartScore;
                computerTurnTexts[i].setText(String.valueOf(computerDartScore));
                
                // Check if computer can finish
                if (computerScore - computerDartScore == 0) {
                    computerScore = 0;
                    computerScoreText.setText("0");
                    endGame(false); // Computer wins
                    return;
                } else if (computerScore - computerDartScore > 0) {
                    computerScore -= computerDartScore;
                } else {
                    // Bust - reset turn scores
                    currentTurnScores[i] = 0;
                    computerTurnTexts[i].setText("0");
                    break;
                }
            }
            
            computerScoreText.setText(String.valueOf(computerScore));
            
            // Back to player
            isPlayerTurn = true;
            clearTurnScores();
            if (!gameOver) {
                instructionLabel.setText("Player's turn - Dart 1/3");
                startXAxis();
            }
        }));
        computerDelay.play();
    }
    
    private void clearTurnScores() {
        currentTurnScores = new int[3];
        for (int i = 0; i < 3; i++) {
            if (isPlayerTurn) {
                playerTurnTexts[i].setText("0");
            } else {
                computerTurnTexts[i].setText("0");
            }
        }
    }
    
    private void updateScoreboard() {
        playerScoreText.setText(String.valueOf(playerScore));
        computerScoreText.setText(String.valueOf(computerScore));
    }

    private void startXAxis() {
        inXAxis = true;
        lineX.setVisible(true);
        lineX.setStartX(0);
        lineX.setEndX(0);
        startButton.setText("STOP X");
        
        // Animate X line
        lineXAnimation = new Timeline(
            new KeyFrame(Duration.millis(50), e -> {
                double currentX = lineX.getStartX();
                currentX += 5;
                if (currentX > 450) currentX = 0; // Updated to match pane width
                lineX.setStartX(currentX);
                lineX.setEndX(currentX);
            })
        );
        lineXAnimation.setCycleCount(Timeline.INDEFINITE);
        lineXAnimation.play();
    }
    
    private void stopXAxis() {
        inXAxis = false;
        lineXAnimation.stop();
        startYAxis();
    }
    
    private void startYAxis() {
        inYAxis = true;
        lineY.setVisible(true);
        lineY.setStartY(0);
        lineY.setEndY(0);
        startButton.setText("THROW!");
        
        // Animate Y line
        lineYAnimation = new Timeline(
            new KeyFrame(Duration.millis(50), e -> {
                double currentY = lineY.getStartY();
                currentY += 5;
                if (currentY > BOARD_SIZE) currentY = 0;
                lineY.setStartY(currentY);
                lineY.setEndY(currentY);
            })
        );
        lineYAnimation.setCycleCount(Timeline.INDEFINITE);
        lineYAnimation.play();
    }
    
    private void stopYAxis() {
        inYAxis = false;
        lineYAnimation.stop();
        throwDart();
    }
    
    private void throwDart() {
        double dartX = lineX.getStartX();
        double dartY = lineY.getStartY();
        
        // Create dart visual
        Circle dart = new Circle(dartX, dartY, 3, Color.PURPLE);
        gamePane.getChildren().add(dart);
        darts.add(dart);
        
        // Hide lines
        lineX.setVisible(false);
        lineY.setVisible(false);
        
        // Check hit and calculate score
        int score = calculateScore(dartX, dartY);
        currentTurnScores[currentDart] = score;
        
        // Update turn score display
        if (isPlayerTurn) {
            playerTurnTexts[currentDart].setText(String.valueOf(score));
        }
        
        // Check for win or bust
        if (isPlayerTurn) {
            if (playerScore - score == 0) {
                playerScore = 0;
                updateScoreboard();
                endGame(true); // Player wins
                return;
            } else if (playerScore - score > 0) {
                playerScore -= score;
                updateScoreboard();
            } else {
                // Bust - end turn
                showScorePopup(dartX, dartY, 0, "BUST!");
                currentDart = 2; // Force end of turn
                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), e -> nextTurn()));
                delay.play();
                return;
            }
        }
        
        // Show score popup
        showScorePopup(dartX, dartY, score, score > 0 ? score + "!" : "MISS");
        
        // Continue to next dart after delay
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), e -> nextTurn()));
        delay.play();
    }
    
    private int calculateScore(double x, double y) {
        double distance = Math.sqrt(Math.pow(x - CENTER_X, 2) + Math.pow(y - CENTER_Y, 2));
        
        // Bull's eye (center red circle)
        if (distance <= 12) return 50;
        
        // Outer bull (green ring around center)
        if (distance <= 25) return 25;
        
        // Outside dartboard
        if (distance > RADIUS) return 0;
        
        // Calculate angle from center, starting from top (12 o'clock position)
        double angle = Math.atan2(y - CENTER_Y, x - CENTER_X);
        // Convert to 0-2π range and adjust so 20 is at top
        angle = angle + Math.PI/2;
        if (angle < 0) angle += 2 * Math.PI;
        if (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        
        // Each section is 18 degrees (π/10 radians)
        double sectionAngle = (2 * Math.PI) / POINTS.length;
        int section = (int) Math.round(angle / sectionAngle) % POINTS.length;
        
        int baseScore = POINTS[section];
        
        // Double ring (outer ring) - between radius-30 and radius-10
        if (distance >= RADIUS - 30 && distance <= RADIUS - 10) {
            return baseScore * 2;
        }
        // Triple ring (middle ring) - between radius/2-10 and radius/2+10  
        else if (distance >= RADIUS/2 - 10 && distance <= RADIUS/2 + 10) {
            return baseScore * 3;
        }
        // Single area (everything else in the dartboard)
        else {
            return baseScore;
        }
    }
    
    private void showScorePopup(double x, double y, int score, String message) {
        Text popup = new Text(x, y - 20, message);
        popup.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        popup.setFill(score > 0 ? Color.YELLOW : Color.RED);
        gamePane.getChildren().add(popup);
        
        // Remove popup after delay
        Timeline removePopup = new Timeline(new KeyFrame(Duration.seconds(0.8), e -> 
            gamePane.getChildren().remove(popup)));
        removePopup.play();
    }
    
    private void endGame(boolean playerWon) {
        started = false;
        gameOver = true;
        startButton.setText("START GAME");
        startButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5); " +
            "-fx-cursor: hand; " +
            "-fx-opacity: 0.95;"
        );
        
        String winner = playerWon ? "Player là người chiến thắng !!!" : "Computer là người chiến thắng !!!";
        instructionLabel.setText(winner + " - Click START GAME to play again");
        
        winnerText.setText(playerWon ? "Player là người chiến thắng !!!" : "Computer là người chiến thắng !!!");
        
        // Clear darts
        for (Circle dart : darts) {
            gamePane.getChildren().remove(dart);
        }
        darts.clear();
    }

    public void handleGameAction() {
        if (!started) {
            startGame();
        } else if (inXAxis) {
            stopXAxis();
        } else if (inYAxis) {
            stopYAxis();
        }
    }

    private void createScoreboard() {
        double boardWidth = 530; // Reduced from 980 to fit in 630px container
        double boardHeight = 80;
        double padding = 10;
        
        scoreBoard = new Rectangle(padding, padding, boardWidth, boardHeight);
        scoreBoard.setFill(Color.LIGHTGRAY);
        scoreBoard.setStroke(Color.BLACK);
        scoreBoard.setStrokeWidth(2);
        scoreboardPane.getChildren().add(scoreBoard);
        
        // Columns: Tên người chơi/Lượt (150px), 1 (60px), 2 (60px), 3 (60px), Điểm còn lại (120px), Người chiến thắng (160px)
        Line vLine1 = new Line(padding + 150, padding, padding + 150, padding + boardHeight);
        Line vLine2 = new Line(padding + 210, padding, padding + 210, padding + boardHeight);
        Line vLine3 = new Line(padding + 270, padding, padding + 270, padding + boardHeight);
        Line vLine4 = new Line(padding + 330, padding, padding + 330, padding + boardHeight);
        
        Line headerSeperator = new Line(padding, padding + 25, padding + boardWidth, padding + 25); 
        headerSeperator.setStroke(Color.BLACK);
        headerSeperator.setStrokeWidth(1.5);
        
        // Horizontal line to separate Player1 and Computer rows
        Line hLine = new Line(padding, padding + boardHeight/2 + 10, padding + boardWidth, padding + boardHeight/2 + 10); 
        
        vLine1.setStroke(Color.BLACK);
        vLine2.setStroke(Color.BLACK);
        vLine3.setStroke(Color.BLACK);
        vLine4.setStroke(Color.BLACK);
        hLine.setStroke(Color.BLACK);
        
        scoreboardPane.getChildren().addAll(vLine1, vLine2, vLine3, vLine4,headerSeperator ,hLine);
        
        Text headerName = new Text(padding + 30, padding + 15, "Tên người chơi/Lượt");
        Text header1 = new Text(padding + 180, padding + 15, "1");
        Text header2 = new Text(padding + 240, padding + 15, "2");
        Text header3 = new Text(padding + 300, padding + 15, "3");
        Text headerScore = new Text(padding + 390, padding + 15, "Điểm còn lại");
        
        headerName.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        header1.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        header2.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        header3.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        headerScore.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        
        scoreboardPane.getChildren().addAll(headerName, header1, header2, header3, headerScore);
        
        // Player rows
        Text player1Name = new Text(padding + 30, padding + 40, socketHandler.loginUser);
        Text computerName = new Text(padding + 30, padding + 70, socketHandler.competitor);
        
        player1Name.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        computerName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        scoreboardPane.getChildren().addAll(player1Name, computerName);
        
        playerScoreText = new Text(padding + 410, padding + 40, "301");
        playerScoreText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        playerScoreText.setFill(Color.BLACK);
        scoreboardPane.getChildren().add(playerScoreText);
        
        computerScoreText = new Text(padding + 410, padding + 70, "301");
        computerScoreText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        computerScoreText.setFill(Color.BLACK);
        scoreboardPane.getChildren().add(computerScoreText);
        
        for (int i = 0; i < 3; i++) {
            playerTurnTexts[i] = new Text(padding + 180 + (i * 60), padding + 40, "0");
            playerTurnTexts[i].setFont(Font.font("Arial", FontWeight.BOLD, 12));
            playerTurnTexts[i].setFill(Color.BLACK);
            scoreboardPane.getChildren().add(playerTurnTexts[i]);
            
            computerTurnTexts[i] = new Text(padding + 180 + (i * 60), padding + 70, "0");
            computerTurnTexts[i].setFont(Font.font("Arial", FontWeight.BOLD, 12));
            computerTurnTexts[i].setFill(Color.BLACK);
            scoreboardPane.getChildren().add(computerTurnTexts[i]);
        }
    }
    public void handleBack() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát game");
        alert.setHeaderText("Bạn có chắc chắn muốn thoát game không?");
        alert.setContentText("Hãy chọn hành động của bạn.");

        ButtonType buttonXacNhan = new ButtonType("Xác nhận");
        ButtonType buttonHuy = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonXacNhan, buttonHuy);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonXacNhan) {
                try {
                    Main.setRoot("home"); // Quay lại màn hình home
                    socketHandler.leaveGame();
                    socketHandler.setRoomIdPresent(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
            // Nếu bấm Hủy thì không làm gì
        });
    }


    public void handleSend() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            chatList.getItems().add("Bạn: " + msg);
            chatInput.clear();
        }
    }

    public void showWinnerDialog(String leaver) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kết thúc trận đấu");
        alert.setHeaderText(null);
        alert.setContentText("Người chơi " + leaver + " đã rời khỏi phòng.\nBạn là người chiến thắng!");

        ButtonType leaveBtn = new ButtonType("Rời khỏi phòng", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(leaveBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == leaveBtn) {
                try {
                    // Quay về màn home
                    Main.setRoot("home");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
