package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class Main extends Application {

    final int width = 1000;
    final int height = 600;
    final double lineWidth = 3;
    double speed = 2;
    double angularSpeed = 0.03;
    ArrayList<Player> humanPlayers = new ArrayList<>();
    int[][] collisionMatrix = new int[width][height];
    ArrayList<KeyCode> keys = new ArrayList<KeyCode>() {{
        add(KeyCode.LEFT);
        add(KeyCode.RIGHT);
        add(KeyCode.Q);
        add(KeyCode.E);
        add(KeyCode.DIGIT7);
        add(KeyCode.DIGIT9);
        add(KeyCode.Z);
        add(KeyCode.C);
        add(KeyCode.COMMA);
        add(KeyCode.PERIOD);
    }};
    Random random = new Random();

    @Override
    public void start(Stage theStage) throws Exception{

        humanPlayers.add(new Player(0));
        humanPlayers.add(new Player(1));
        humanPlayers.add(new Player(2));
        humanPlayers.add(new Player(3));
        humanPlayers.add(new Player(4));

        theStage.setTitle( "Matacka" );

        HBox root = new HBox();
        Scene theScene = new Scene( root );
        theStage.setScene( theScene );

        theScene.setOnKeyReleased(event -> {
            for (int i = 0; i < humanPlayers.size() * 2; i++)
            {
                if(keys.get(i).equals(event.getCode()))
                {
                    humanPlayers.get(i / 2).direction = Player.Direction.Straight;
                    return;
                }
            }
        });
        theScene.setOnKeyPressed(event -> {
            for (int i = 0; i < humanPlayers.size() * 2; i++)
            {
                if(keys.get(i).equals(event.getCode()))
                {
                    humanPlayers.get(i / 2).direction = (i % 2 == 0) ? Player.Direction.Left : Player.Direction.Right;
                    return;
                }
            }
        });

        Canvas canvas = new Canvas( width, height );
        Pane pane = new Pane();
        pane.setPrefSize(4,height);
        VBox scoreboard = new VBox();
        for (Player p : humanPlayers)
        {
            scoreboard.getChildren().add(p.getDisplay());
            p.angle = random.nextDouble() * 2 * Math.PI;
            p.position = new Point2D.Double(100 + random.nextInt(width - 200), 100 + random.nextInt(height - 200));
        }
        pane.setStyle("-fx-background-color: #E0F0FF");
        root.setStyle("-fx-background-color: #FFFFFF");
        root.getChildren().addAll(canvas, pane, scoreboard);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill( Color.RED );
        gc.setStroke( Color.BLACK );
        gc.setLineWidth(lineWidth);

        theStage.show();

        AnimationTimer animator = new AnimationTimer()
        {
            @Override
            public void handle(long arg0)
            {
                for (Player p : humanPlayers)
                {
                    double px = p.position.getX() + speed * Math.cos(p.angle);
                    double py = p.position.getY() + speed * Math.sin(p.angle);

                    gc.setStroke(p.color);
                    gc.strokeLine(p.position.getX(), p.position.getY(), p.position.getX(), p.position.getY());

                    if(p.direction.equals(Player.Direction.Right))
                        p.angle += angularSpeed;
                    if(p.direction.equals(Player.Direction.Left))
                        p.angle -= angularSpeed;

                    p.position.setLocation(px, py);
                    p.collisionValue++;

                    if(collide(px, py, p.collisionValue))
                    {
                        p.color = Color.BLACK;
                        p.addPoint();
                    }
                }
            }
        };

        animator.start();
    }

    private boolean collide(double x, double y, int value)
    {
        int i1,i2,j1,j2;
        i1 = (int)(x - lineWidth/2);
        i2 = (int)(x + lineWidth/2);
        j1 = (int)(y - lineWidth/2);
        j2 = (int)(y + lineWidth/2);
        if(i1 < 0 || j1 < 0 || i2 >= width || j2 >= height)
            return true;
        for (int i = i1; i <= i2; i++)
        {
            for (int j = j1; j <= j2; j++)
            {
                if(collisionMatrix[i][j] > 0 && Math.abs(collisionMatrix[i][j] - value) > 8)
                    return true;
                collisionMatrix[i][j] = value;
            }
        }
        return false;
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
