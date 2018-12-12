package sample;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import java.awt.geom.Point2D;

public class Player {

    Point2D position;
    double angle;
    Direction direction = Direction.Straight;
    private int id;
    int collisionValue;
    Paint color;
    private Display display;
    private int points = 0;

    Node getDisplay() { return display.root; }

    void addPoint()
    {
        points++;
        display.points.setText(Integer.toString(points));
    }

    public void setPoints(int points)
    {
        this.points = points;
        display.points.setText(Integer.toString(points));
    }

    public int getPoints() { return points; }

    Player(int id)
    {
        this.id = id;
        collisionValue = id * 666666;
        //noinspection IntegerDivisionInFloatingPointContext
        color = Color.hsb(id % 6 * 60 + id / 6 * 30,1,1);
        display = new Display(color);
    }

    public enum Direction
    {
        Left, Right, Straight
    }

    private class Display
    {
        private HBox root = new HBox();
        private Label points = new Label("0");
        private Pane icon;

        Display(Paint color)
        {
            Circle c = new Circle(20, 20, 12, color);
            icon = new Pane(c);
            icon.setPrefSize(40, 40);
            points.setPrefSize(40,40);
            points.setAlignment(Pos.CENTER);

            root.getChildren().addAll(icon, points);
        }
    }
}


