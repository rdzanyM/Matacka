package sample;

import java.awt.geom.Point2D;

public class Player {

    boolean isHuman;
    public Point2D position;
    public double angle;
    public Direction direction = Direction.Straight;

    public Player(boolean isHuman) {
        this.isHuman = isHuman;
    }

    public enum Direction
    {
        Left, Right, Straight
    }

}
