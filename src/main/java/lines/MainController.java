package lines;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;
import lines.model.Ball;
import lines.model.GameModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    private static final String RECORD_FILE = "record.txt";
    private final GameModel gameModel = new GameModel();
    @FXML
    private TilePane tilePane;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label recordLabel;
    @FXML
    private Pane gamePane;
    private BallTile selectedTile;

    @FXML
    private void initialize() {
        for (int row = 0; row < GameModel.GRID_SIZE; row++) {
            for (int col = 0; col < GameModel.GRID_SIZE; col++) {
                BackTile backTile = new BackTile(col, row);
                tilePane.getChildren().add(backTile);

                backTile.setOnMouseEntered(event -> {
                    if (selectedTile != null) {
                        Ball ball = selectedTile.getBall();
                        boolean canMove = gameModel.canMove(ball, backTile.getCol(), backTile.getRow());
                        backTile.setCrossVisible(!canMove);
                        backTile.setPlusVisible(canMove);
                        backTile.setCursor(canMove ? Cursor.HAND : Cursor.DEFAULT);
                    }
                });

                backTile.setOnMouseExited(event -> {
                    backTile.setCrossVisible(false);
                    backTile.setPlusVisible(false);
                });
            }
        }

        scoreLabel.textProperty().bind(gameModel.textScoreProperty());
        recordLabel.textProperty().bind(gameModel.textRecordProperty());

        gamePane.setOnMouseClicked(event -> {
            int col = (int) Math.floor(event.getX() / GameProperties.GRID_SIZE);
            int row = (int) Math.floor(event.getY() / GameProperties.GRID_SIZE);
            if (selectedTile != null) {
                Ball ball = selectedTile.getBall();
                if (ball.getCol() != col || ball.getRow() != row) {
                    if (gameModel.canMove(ball, col, row)) {
                        gameModel.move(ball, col, row);
                    }
                }
            }
        });

        gameModel.setOnBallAdded(this::onBallAdded);
        gameModel.setOnBallMoved(this::onBallMoved);
        gameModel.setOnBallRemoved(this::onBallRemoved);
        gameModel.setOnNextBallAdded(this::onNextBallAdded);
        gameModel.setOnNextBallRemoved(this::onNextBallRemoved);
        gameModel.setOnGameOver(this::onGameOver);

        gameModel.setOnNewRecord(value -> {
            try {
                Files.write(Paths.get(RECORD_FILE), Collections.singletonList(String.valueOf(value)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        gameModel.setRecord(loadRecord());
        gameModel.restart();
    }

    private static int loadRecord() {
        int record = 0;
        try {
            Path path = Paths.get(RECORD_FILE);
            if (Files.exists(path)) {
                record = Integer.parseInt(Files.readAllLines(path).get(0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return record;
    }

    private void onNextBallRemoved(Ball ball) {
        BallTile tile = getBallTile(ball);
        gamePane.getChildren().remove(tile);
    }

    private void onBallMoved(Ball ball) {
        select(null);
        BallTile ballTile = getBallTile(ball);
        final int xDest = ball.getCol() * GameProperties.GRID_SIZE;
        final int yDest = ball.getRow() * GameProperties.GRID_SIZE;
        TranslateTransition tt = new TranslateTransition(new Duration(100), ballTile);
        tt.setToX(xDest);
        tt.setToY(yDest);
        tt.play();
    }

    private void onGameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Игра окончена");
        alert.setContentText("Счёт: " + gameModel.textScoreProperty().get());
        alert.showAndWait();
        gameModel.restart();
    }

    private void select(BallTile ballTile) {
        if (selectedTile != null) {
            selectedTile.stopJump();
        }
        if (ballTile != null) {
            ballTile.jump();
        }
        selectedTile = ballTile;
    }

    private void onBallAdded(Ball ball) {
        BallTile tile = new BallTile(ball);
        tile.setPrefSize(GameProperties.GRID_SIZE, GameProperties.GRID_SIZE);
        tile.setTranslateX(ball.getCol() * GameProperties.GRID_SIZE);
        tile.setTranslateY(ball.getRow() * GameProperties.GRID_SIZE);
        tile.setOnMouseClicked(event -> {
            if (selectedTile == tile) {
                select(null);
            } else {
                select(tile);
            }
        });
        tile.setOnMouseEntered(event -> {
            tile.setEffect(new ColorAdjust(0, 0, 0.3, 0));
        });
        tile.setOnMouseExited(event -> {
            tile.setEffect(null);
        });
        ScaleTransition tt = new ScaleTransition(new Duration(200), tile);
        tt.setFromX(0.25);
        tt.setFromY(0.25);
        tt.setToX(1);
        tt.setToY(1);
        tt.play();
        gamePane.getChildren().add(tile);
    }

    private List<BallTile> getBallTiles() {
        return gamePane.getChildren().stream()
                .filter(node -> node instanceof BallTile)
                .map(node -> (BallTile) node)
                .collect(Collectors.toList());
    }

    private BallTile getBallTile(Ball ball) {
        return getBallTiles().stream()
                .filter(ballTile -> ballTile.getBall() == ball)
                .findFirst()
                .orElse(null);
    }

    private void onBallRemoved(Ball ball) {
        BallTile ballTile = getBallTile(ball);
        ScaleTransition tt = new ScaleTransition(new Duration(200), ballTile);
        tt.setToX(0);
        tt.setToY(0);
        tt.setOnFinished(event -> {
            gamePane.getChildren().remove(ballTile);
        });
        tt.play();
    }

    private void onNextBallAdded(Ball ball) {
        BallTile tile = new BallTile(ball);
        tile.setPrefSize(GameProperties.GRID_SIZE, GameProperties.GRID_SIZE);
        tile.setTranslateX(ball.getCol() * GameProperties.GRID_SIZE);
        tile.setTranslateY(ball.getRow() * GameProperties.GRID_SIZE);
        tile.setScaleX(0.25);
        tile.setScaleY(0.25);
        gamePane.getChildren().add(tile);
    }

    @FXML
    private void onRestart() {
        gameModel.restart();
    }

}
