package lines.model;

import java.util.*;
import java.util.function.Consumer;

public class GameModel {
    private static final int GRID_SIZE = 8;
    private static final int MIN_LINE_LENGTH = 5;
    private static final int BALLS_FOR_TURN = 3;
    private final Set<Ball> balls = new HashSet<>();
    private final Random random = new Random();
    private Consumer<Ball> onBallAdded = number -> {
    };
    private Consumer<Ball> onBallRemoved = number -> {
    };
    private Consumer<Ball> onBallMoved = number -> {
    };

    public void generateBall() {
        int col;
        int row;
        do {
            col = random.nextInt(GRID_SIZE);
            row = random.nextInt(GRID_SIZE);
        } while (get(col, row).isPresent());
        BallColor color = BallColor.values()[random.nextInt(BallColor.values().length)];
        Ball ball = new Ball(col, row, color);
        addBall(ball);
    }

    private Optional<Ball> get(int col, int row) {
        return balls.stream()
                .filter(ball -> ball.getCol() == col)
                .filter(ball -> ball.getRow() == row)
                .findFirst();
    }

    private void addBall(Ball ball) {
        balls.add(ball);
        onBallAdded.accept(ball);
        removeLines();
    }

    private void removeBall(Ball ball) {
        balls.remove(ball);
        onBallRemoved.accept(ball);
    }

    public void setOnBallAdded(Consumer<Ball> onBallAdded) {
        this.onBallAdded = onBallAdded;
    }

    public void setOnBallRemoved(Consumer<Ball> onBallRemoved) {
        this.onBallRemoved = onBallRemoved;
    }

    public void setOnBallMoved(Consumer<Ball> onBallMoved) {
        this.onBallMoved = onBallMoved;
    }

    public void move(Ball ball, int col, int row) {
        ball.setCol(col);
        ball.setRow(row);
        onBallMoved.accept(ball);
        removeLines();
        for (int i = 0; i < BALLS_FOR_TURN; i++) {
            generateBall();
        }
    }

    private void removeLines() {
        Set<Ball> ballsToRemove = new HashSet<>();
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE; row++) {
                ballsToRemove.addAll(findHLine(col, row));
                ballsToRemove.addAll(findVLine(col, row));
            }
        }
        for (Ball ball : ballsToRemove) {
            removeBall(ball);
        }
    }

    private Set<Ball> findHLine(int col, int row) {
        Optional<Ball> optionalBall = get(col, row);
        if (!optionalBall.isPresent()) {
            return Collections.emptySet();
        }
        BallColor color = optionalBall.get().getColor();
        Set<Ball> lineBalls = new HashSet<>();
        lineBalls.add(optionalBall.get());
        for (int i = col; i < GRID_SIZE; i++) {
            Optional<Ball> nextOptionalBall = get(i, row);
            if (nextOptionalBall.isPresent()) {
                Ball nextBall = nextOptionalBall.get();
                if (nextBall.getColor() == color) {
                    lineBalls.add(nextBall);
                    continue;
                }
            }
            break;
        }
        return (lineBalls.size() >= MIN_LINE_LENGTH) ? lineBalls : Collections.emptySet();
    }

    private Set<Ball> findVLine(int col, int row) {
        Optional<Ball> optionalBall = get(col, row);
        if (!optionalBall.isPresent()) {
            return Collections.emptySet();
        }
        BallColor color = optionalBall.get().getColor();
        Set<Ball> lineBalls = new HashSet<>();
        lineBalls.add(optionalBall.get());
        for (int i = row; i < GRID_SIZE; i++) {
            Optional<Ball> nextOptionalBall = get(col, i);
            if (nextOptionalBall.isPresent()) {
                Ball nextBall = nextOptionalBall.get();
                if (nextBall.getColor() == color) {
                    lineBalls.add(nextBall);
                    continue;
                }
            }
            break;
        }
        return (lineBalls.size() >= MIN_LINE_LENGTH) ? lineBalls : Collections.emptySet();
    }

}