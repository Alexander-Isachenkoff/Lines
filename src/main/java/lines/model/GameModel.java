package lines.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;
import java.util.function.Consumer;

public class GameModel {
    private static final int GRID_SIZE = 8;
    private static final int MIN_LINE_LENGTH = 5;
    private static final int BALLS_FOR_TURN = 3;
    private static final int START_BALLS = 5;
    private final Set<Ball> balls = new HashSet<>();
    private final Set<Ball> nextBalls = new HashSet<>();
    private final Random random = new Random();
    private final SimpleIntegerProperty scoreProperty = new SimpleIntegerProperty(-1);
    private final SimpleIntegerProperty recordProperty = new SimpleIntegerProperty(-1);
    private final ReadOnlyStringWrapper textScoreProperty = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper textRecordProperty = new ReadOnlyStringWrapper();
    private Consumer<Ball> onBallAdded = number -> {
    };
    private Consumer<Ball> onBallRemoved = number -> {
    };
    private Consumer<Ball> onNextBallAdded = number -> {
    };
    private Consumer<Ball> onNextBallRemoved = number -> {
    };
    private Consumer<Ball> onBallMoved = number -> {
    };
    private Consumer<Integer> onNewRecord = value -> {
    };

    public GameModel() {
        scoreProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > recordProperty.get()) {
                recordProperty.set(newValue.intValue());
            }
        });
        recordProperty.addListener((observable, oldValue, newValue) -> {
            onNewRecord.accept(newValue.intValue());
        });
        scoreProperty.addListener((observable, oldValue, newValue) -> {
            textScoreProperty.set(String.valueOf(newValue));
        });
        recordProperty.addListener((observable, oldValue, newValue) -> {
            textRecordProperty.set(String.valueOf(newValue));
        });
    }

    private Ball generateBall() {
        int col;
        int row;
        do {
            col = random.nextInt(GRID_SIZE);
            row = random.nextInt(GRID_SIZE);
        } while (get(col, row).isPresent());
        BallColor color = BallColor.values()[random.nextInt(BallColor.values().length)];
        return new Ball(col, row, color);
    }

    public void restart() {
        scoreProperty.set(0);
        nextBalls.forEach(onNextBallRemoved);
        nextBalls.clear();
        removeAllBalls();
        for (int i = 0; i < START_BALLS; i++) {
            addBall(generateBall());
        }
        generateNextBalls();
    }

    private void generateNextBalls() {
        for (int i = 0; i < BALLS_FOR_TURN; i++) {
            nextBalls.add(generateBall());
        }
        nextBalls.forEach(onNextBallAdded);
    }

    private void spawnBalls() {
        nextBalls.forEach(onNextBallRemoved);
        for (Ball nextBall : nextBalls) {
            if (get(nextBall.getCol(), nextBall.getRow()).isPresent()) {
                addBall(generateBall());
            } else {
                addBall(nextBall);
            }
        }
        nextBalls.clear();
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

    private void removeAllBalls() {
        new HashSet<>(balls).forEach(this::removeBall);
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

    public void setOnNextBallAdded(Consumer<Ball> onNextBallAdded) {
        this.onNextBallAdded = onNextBallAdded;
    }

    public void setOnNextBallRemoved(Consumer<Ball> onNextBallRemoved) {
        this.onNextBallRemoved = onNextBallRemoved;
    }

    public void move(Ball ball, int col, int row) {
        ball.setCol(col);
        ball.setRow(row);
        onBallMoved.accept(ball);
        removeLines();
        spawnBalls();
        generateNextBalls();
    }

    public boolean canMove(Ball ball, int colTo, int rowTo) {
        Graph<Place> graph = new Graph<>();
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE; row++) {
                if (!get(col, row).isPresent()) {
                    Place place = new Place(col, row);
                    graph.getElements().add(place);
                    graph.getRelations().put(place, neighboursPlaces(col, row));
                }
            }
        }
        Place from = new Place(ball.getCol(), ball.getRow());
        graph.getElements().add(from);
        graph.getRelations().put(from, neighboursPlaces(ball.getCol(), ball.getRow()));

        Optional<Place> optionalPlace = graph.getElements().stream()
                .filter(place -> place.col == colTo)
                .filter(place -> place.row == rowTo)
                .findFirst();

        if (optionalPlace.isPresent()) {
            Place to = optionalPlace.get();
            return graph.hasPath(from, to);
        } else {
            return false;
        }
    }

    private Set<Place> neighboursPlaces(int col, int row) {
        Set<Place> neighbours = new HashSet<>();
        if (col > 0) {
            if (!get(col - 1, row).isPresent()) {
                neighbours.add(new Place(col - 1, row));
            }
        }
        if (col < GRID_SIZE - 1) {
            if (!get(col + 1, row).isPresent()) {
                neighbours.add(new Place(col + 1, row));
            }
        }
        if (row > 0) {
            if (!get(col, row - 1).isPresent()) {
                neighbours.add(new Place(col, row - 1));
            }
        }
        if (row < GRID_SIZE - 1) {
            if (!get(col, row + 1).isPresent()) {
                neighbours.add(new Place(col, row + 1));
            }
        }
        return neighbours;
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
        addScore(ballsToRemove.size());
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

    private void addScore(int score) {
        scoreProperty.set(scoreProperty.get() + score);
    }

    public ReadOnlyStringProperty textScoreProperty() {
        return textScoreProperty.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty textRecordProperty() {
        return textRecordProperty.getReadOnlyProperty();
    }

    public void setRecord(int record) {
        recordProperty.set(record);
    }

    public void setOnNewRecord(Consumer<Integer> onNewRecord) {
        this.onNewRecord = onNewRecord;
    }

    private static class Place {
        private final int col;
        private final int row;

        public Place(int col, int row) {
            this.col = col;
            this.row = row;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Place place = (Place) o;

            if (col != place.col) return false;
            return row == place.row;
        }

        @Override
        public int hashCode() {
            int result = col;
            result = 31 * result + row;
            return result;
        }
    }

}
