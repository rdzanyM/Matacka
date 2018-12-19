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

class Player {

    Paint color;    //trail color
    double angle;   //velocity angle
    int hole = 0;   //number of frames without drawing trail
    private int id;
    int collisionValue;
    private int points = 0;
    private Display display;    //how player is shown on the scoreboard
    Direction direction = Direction.Straight;
    private Point2D position = new Point2D.Double(0,0);
    private Circle head = new Circle(0,0,2, Color.BLACK);

    double getX()     { return position.getX(); }
    double getY()     { return position.getY(); }
    int getPoints()   { return points; }
    Circle getHead()  { return head; }
    Node getDisplay() { return display.root; }

    void addPoints(int p)
    {
        points += p;
        display.points.setText(Integer.toString(points));
    }

    void setPosition(double x, double y)
    {
        position.setLocation(x,y);
        head.setCenterX(x);
        head.setCenterY(y);
    }

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
        private Pane icon;
        private HBox root = new HBox();
        private Label points = new Label("0");

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


