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

    public Point2D position;
    public double angle;
    public Direction direction = Direction.Straight;
    private int id;
    public int collisionValue;
    Paint color;
    private Display display;
    private int points = 0;

    public Node getDisplay() { return display.root; }

    public void addPoint()
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

    public Player(int id)
    {
        this.id = id;
        collisionValue = id * 666666;
        color = Color.hsb(id * 150,1,1);
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

        public Display(Paint color)
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


