package sample;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.awt.geom.Point2D;

public class Player {

    public Point2D position;
    public double angle;
    public Direction direction = Direction.Straight;
    private int id;
    public int collisionValue;
    Paint color;

    public Player(int id) {
        this.id = id;
        collisionValue = id * 666666;
        color = Color.hsb(id * 150,1,1);
    }

    public enum Direction
    {
        Left, Right, Straight
    }

}
