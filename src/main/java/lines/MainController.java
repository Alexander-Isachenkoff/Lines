package lines;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lines.model.Ball;
import lines.model.GameModel;

import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    private static final int GRID_SIZE = 80;
    private final GameModel gameModel = new GameModel();
    @FXML
    private Pane gamePane;

    private final SingleSelectionModel<BallTile> selectionModel = new SingleSelectionModel<BallTile>() {
        @Override
        protected BallTile getModelItem(int index) {
            return getBallTiles().get(0);
        }

        @Override
        protected int getItemCount() {
            return getBallTiles().size();
        }
    };

    @FXML
    private void initialize() {
        gamePane.setOnMouseClicked(event -> {
            int col = (int) Math.floor(event.getX() / GRID_SIZE);
            int row = (int) Math.floor(event.getY() / GRID_SIZE);
            BallTile selectedItem = selectionModel.getSelectedItem();
            if (selectedItem != null) {
                Ball ball = selectedItem.getBall();
                if (ball.getCol() != col || ball.getRow() != row) {
                    gameModel.move(ball, col, row);
                }
            }
        });

        gameModel.setOnBallAdded(ball -> {
            BallTile tile = new BallTile(ball);
            tile.setPrefSize(GRID_SIZE, GRID_SIZE);
            tile.setTranslateX(ball.getCol() * GRID_SIZE);
            tile.setTranslateY(ball.getRow() * GRID_SIZE);
            tile.setOnMouseClicked(event -> {
                if (selectionModel.getSelectedItem() == tile) {
                    selectionModel.select(null);
                } else {
                    selectionModel.select(tile);
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
        });

        gameModel.setOnBallMoved(ball -> {
            selectionModel.select(null);
            BallTile ballTile = getBallTile(ball);
            final int xDest = ball.getCol() * GRID_SIZE;
            final int yDest = ball.getRow() * GRID_SIZE;
            TranslateTransition tt = new TranslateTransition(new Duration(100), ballTile);
            tt.setToX(xDest);
            tt.setToY(yDest);
            tt.play();
        });

        gameModel.setOnBallRemoved(ball -> {
            BallTile ballTile = getBallTile(ball);
            ScaleTransition tt = new ScaleTransition(new Duration(200), ballTile);
            tt.setToX(0);
            tt.setToY(0);
            tt.setOnFinished(event -> {
                gamePane.getChildren().remove(ballTile);
            });
            tt.play();
        });

        gameModel.setOnNextBallAdded(ball -> {
            BallTile tile = new BallTile(ball);
            tile.setPrefSize(GRID_SIZE, GRID_SIZE);
            tile.setTranslateX(ball.getCol() * GRID_SIZE);
            tile.setTranslateY(ball.getRow() * GRID_SIZE);
            tile.setScaleX(0.25);
            tile.setScaleY(0.25);
            gamePane.getChildren().add(tile);
        });

        gameModel.setOnNextBallRemoved(ball -> {
            BallTile tile = getBallTile(ball);
            gamePane.getChildren().remove(tile);
        });

        selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.stopJump();
            }
            if (newValue != null) {
                newValue.jump();
            }
        });

        gameModel.restart();
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

}
