package lines.model;

public class Ball {
    private int col;
    private int row;
    private final BallColor color;

    public Ball(int col, int row, BallColor color) {
        this.col = col;
        this.row = row;
        this.color = color;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public BallColor getColor() {
        return color;
    }
}
