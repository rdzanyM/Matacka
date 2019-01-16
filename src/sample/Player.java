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
import java.util.*;

class Player {

    Paint color;    //trail color
    double angle;   //velocity angle
    int hole = 0;   //number of frames without drawing trail
    int id;
    int collisionValue;
    private int points = 0;
    private Display display;    //how player is shown on the scoreboard
    Direction direction = Direction.Straight;
    private Point2D position = new Point2D.Double(0,0);
    private Circle head = new Circle(0,0,2, Color.BLACK);
    static int limit;
    boolean pretender;

    double getX()     { return position.getX(); }
    double getY()     { return position.getY(); }
    int getPoints()   { return points; }
    Circle getHead()  { return head; }
    Node getDisplay() { return display.root; }

    void addPoints(int p)
    {
        if(points < limit)
        {
            points += p;
            if (points >= limit)
            {
                points = limit;
                display.points.setTextFill(Color.LIGHTGREEN);
            }
            display.points.setText(Integer.toString(points));
        }
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

class ComputerPlayer extends Player
{
    static int width;
    static int height;
    static double speed;
    static double lineWidth;
    static double angularSpeed;
    static ArrayList<Player> players;
    private int[][] othersMap;
    private int computedStep;
    private int computedDepth;
    Direction computedDirection = Direction.Straight;
    final ThreadID threadID = new ThreadID(0);

    ComputerPlayer(int id)
    {
        super(id);
    }

    private void computeInit(final int[][] map, Integer[] activePlayers)
    {
        int tid = threadID.value;
        List<SimplePlayer> others = new LinkedList<>();
        for (int i : activePlayers)
        {
            if(i != id)
            {
                Player p = players.get(i);
                others.add(new SimplePlayer(p.getX(), p.getY(), p.angle, Direction.Straight));
                others.add(new SimplePlayer(p.getX(), p.getY(), p.angle, Direction.Right));
                others.add(new SimplePlayer(p.getX(), p.getY(), p.angle, Direction.Left));
            }
        }
        synchronized (threadID)
        {
            computedDepth = 0;
            computedStep = 0;
            //int[][] mapCopy = Clone(map, width, height);
            othersMap = new int[width][height];
        }
        //compute(mapCopy, Direction.Straight, 1, Direction.Straight, getX(), getY(), angle, collisionValue, tid, others, 0);
        //compute(mapCopy, Direction.Right,    1, Direction.Right,    getX(), getY(), angle, collisionValue, tid, others, 0);
        //compute(mapCopy, Direction.Left,     1, Direction.Left,     getX(), getY(), angle, collisionValue, tid, others, 0);
        compute(map, Direction.Straight, 1, Direction.Straight, getX(), getY(), angle, collisionValue, tid, others, 0);
        compute(map, Direction.Right,    1, Direction.Right,    getX(), getY(), angle, collisionValue, tid, others, 0);
        compute(map, Direction.Left,     1, Direction.Left,     getX(), getY(), angle, collisionValue, tid, others, 0);
    }

    void computeInitAsync(final int[][] map, Integer[] activePlayers)
    {
        Thread thread = new Thread(() -> computeInit(map, activePlayers));
        thread.start();
    }

    private void compute(final int[][] map, Direction prev, int depth, Direction initial, double x, double y, double a, int cv, int tid, List<SimplePlayer> others, int step)
    {
        synchronized (threadID)
        {
            if (tid < threadID.value)
                return;
            if(depth > 16)
            {
                threadID.value++;
                return;
            }
            for (int i = 0; i < decisionGap(depth); i++)
            {
                step++;
                if(step > computedStep)
                {
                    for (SimplePlayer sp : others)
                    {
                        if(sp.alive)
                        {
                            sp.x += speed * Math.cos(sp.angle);
                            sp.y += speed * Math.sin(sp.angle);
                            switch (sp.direction)
                            {
                                case Left:
                                    sp.angle -= angularSpeed;
                                    break;
                                case Right:
                                    sp.angle += angularSpeed;
                                    break;
                            }
                            if (collide(sp.x, sp.y, -step, map, -1))
                                sp.alive = false;
                        }
                    }
                    computedStep = step;
                }
                x += speed * Math.cos(a);
                y += speed * Math.sin(a);
                switch (prev)
                {
                    case Left:
                        a -= angularSpeed;
                        break;
                    case Right:
                        a += angularSpeed;
                        break;
                }
                cv++;
                if (collide(x, y, cv, map, step)) return;
            }

            if (depth > computedDepth)
            {
                computedDepth = depth;
                computedDirection = initial;
            }
        }
        compute(map, Direction.Straight, depth + 1, initial, x, y, a, cv, tid, others, step);
        compute(map, Direction.Right,    depth + 1, initial, x, y, a, cv, tid, others, step);
        compute(map, Direction.Left,     depth + 1, initial, x, y, a, cv, tid, others, step);
    }

    private int decisionGap(int depth)
    {
        return depth * 4;
    }

    private int[][] Clone(int[][] array, int width, int height)
    {
        int[][] newArray = new int[width][height];
        for(int i = 0; i < width; i++)
            if (height >= 0)
                System.arraycopy(array[i], 0, newArray[i], 0, height);
        return newArray;
    }

    private boolean collide(double x, double y, int value, final int[][] map, int step)
    {
        int i1,i2,j1,j2;
        double r = lineWidth/2;
        i1 = (int)(x - r);
        i2 = (int)(x + r);
        j1 = (int)(y - r);
        j2 = (int)(y + r);
        if(i1 < 0 || j1 < 0 || i2 >= width || j2 >= height)
            return true;
        for (int i = i1; i <= i2; i++)
        {
            for (int j = j1; j <= j2; j++)
            {
                if(step < 0)
                {
                    if(map[i][j] == 0 && othersMap[i][j] == 0)
                        othersMap[i][j] = value;
                    else
                        return othersMap[i][j] > value;
                    return false;
                }
                if(map[i][j] > 0 && Math.abs(map[i][j] - value) > 0xf)//standard collision
                    return true;
                if(othersMap[i][j] < 0 && othersMap[i][j] >= -step)//possible collision with other player
                    return true;
            }
        }
        return false;
    }

    private class SimplePlayer
    {
        double x;
        double y;
        double angle;
        Direction direction;
        boolean alive = true;

        SimplePlayer(double x, double y, double angle, Direction direction)
        {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.direction = direction;
        }
    }

    class ThreadID
    {
        int value;
        ThreadID(int value)
        {
            this.value = value;
        }
    }
}


