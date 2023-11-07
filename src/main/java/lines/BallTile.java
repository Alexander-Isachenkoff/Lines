package lines;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lines.model.Ball;

public class BallTile extends StackPane {
    private final Ball ball;
    private final Circle circle;
    private ParallelTransition jumpTransition;

    public BallTile(Ball ball) {
        this.ball = ball;
        setAlignment(Pos.CENTER);
        circle = new Circle(GameProperties.GRID_SIZE * 0.3, ball.getColor().getColor());
        getChildren().add(circle);
        getStyleClass().add("ball-tile");
    }

    public Ball getBall() {
        return ball;
    }

    public void jump() {
        TranslateTransition tt = new TranslateTransition(new Duration(400), circle);
        tt.setToY(-0.2 * GameProperties.GRID_SIZE);

        ScaleTransition st = new ScaleTransition(new Duration(400), circle);
        double factor = 0.05;
        st.setFromX(1 + factor);
        st.setToX(1 - factor);
        st.setFromY(1 - factor);
        st.setToY(1 + factor);

        jumpTransition = new ParallelTransition(tt, st);

        jumpTransition.setCycleCount(2);
        jumpTransition.setAutoReverse(true);
        jumpTransition.setOnFinished(event -> jump());
        jumpTransition.play();
    }

    public void stopJump() {
        jumpTransition.stop();
        circle.setScaleX(1);
        circle.setScaleY(1);
        circle.setTranslateX(0);
        circle.setTranslateY(0);
    }

}
