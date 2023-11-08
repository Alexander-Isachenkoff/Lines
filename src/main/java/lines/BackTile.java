package lines;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class BackTile extends StackPane {

    private static final double LINE_SIZE = GameProperties.GRID_SIZE * 0.4;
    private final int col;
    private final int row;
    private final Group cross = new Group();
    private final Group plus = new Group();

    public BackTile(int col, int row) {
        this.col = col;
        this.row = row;

        initCross();
        initPlus();

        getChildren().add(cross);
        getChildren().add(plus);
        setCrossVisible(false);
        setPlusVisible(false);

        getStyleClass().add("backtile");
    }

    private void initPlus() {
        Line line1 = new Line(0, LINE_SIZE / 2, LINE_SIZE, LINE_SIZE / 2);
        line1.setStroke(Color.LIMEGREEN);
        line1.setStrokeWidth(3);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);

        Line line2 = new Line(LINE_SIZE / 2, 0, LINE_SIZE / 2, LINE_SIZE);
        line2.setStroke(Color.LIMEGREEN);
        line2.setStrokeWidth(3);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);

        plus.getChildren().addAll(line1, line2);
    }

    private void initCross() {
        Line line1 = new Line(0, 0, LINE_SIZE, LINE_SIZE);
        line1.setStroke(Color.FIREBRICK);
        line1.setStrokeWidth(3);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);

        Line line2 = new Line(0, LINE_SIZE, LINE_SIZE, 0);
        line2.setStroke(Color.FIREBRICK);
        line2.setStrokeWidth(3);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);

        cross.getChildren().addAll(line1, line2);
    }

    public void setCrossVisible(boolean value) {
        cross.setVisible(value);
    }

    public void setPlusVisible(boolean value) {
        plus.setVisible(value);
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}
