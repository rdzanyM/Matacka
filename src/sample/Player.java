package sample;

import java.awt.geom.Point2D;

public class Player {

    boolean isHuman;
    public Point2D position;
    public double angle;
    public Direction direction = Direction.Straight;
    private int id;
    public int collisionValue;

    public Player(boolean isHuman, int id) {
        this.isHuman = isHuman;
        this.id = id;
        collisionValue = id * 666666;
    }

    public enum Direction
    {
        Left, Right, Straight
    }

}
