package lines.model;

import javafx.scene.paint.Color;

public enum BallColor {

    BLUE(Color.DODGERBLUE),
    GRAY(Color.GRAY),
    GREEN(Color.LIMEGREEN),
    RED(Color.FIREBRICK),
    YELLOW(Color.GOLD);

    private final Color color;

    BallColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
