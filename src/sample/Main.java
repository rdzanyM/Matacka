package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.geom.Point2D;

public class Main extends Application {

    final int width = 1000;
    final int height = 500;
    final double lineWidth = 3;
    double speed = 2;
    double angularSpeed = 0.03;
    Player p = new Player(true, 0);
    int[][] collisionMatrix = new int[width][height];

    @Override
    public void start(Stage theStage) throws Exception{
        theStage.setTitle( "Canvas" );

        Group root = new Group();
        Scene theScene = new Scene( root );
        theStage.setScene( theScene );

        theScene.setOnKeyReleased(event -> {
            switch (event.getCode())
            {
                case LEFT:
                case RIGHT:
                    p.direction = Player.Direction.Straight;
                    break;
            }
        });

        theScene.setOnKeyPressed(event -> {
            switch (event.getCode())
            {
                case LEFT:
                    p.direction = Player.Direction.Left;
                    break;
                case RIGHT:
                    p.direction = Player.Direction.Right;
                    break;
            }
        });

        Canvas canvas = new Canvas( width, height );
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill( Color.RED );
        gc.setStroke( Color.BLACK );
        gc.setLineWidth(lineWidth);
        p.angle = 0;
        p.position = new Point2D.Double(20,100);

        theStage.show();

        AnimationTimer animator = new AnimationTimer(){

            @Override
            public void handle(long arg0) {

                double px = p.position.getX() + speed * Math.cos(p.angle);
                double py = p.position.getY() + speed * Math.sin(p.angle);

                gc.strokeLine(p.position.getX(), p.position.getY(), p.position.getX(), p.position.getY());

                if(p.direction.equals(Player.Direction.Right))
                    p.angle += angularSpeed;
                if(p.direction.equals(Player.Direction.Left))
                    p.angle -= angularSpeed;

                p.position.setLocation(px, py);
                p.collisionValue++;

                if(collide(px, py, p.collisionValue))
                {
                    gc.setStroke(Color.RED);
                }
            }
        };

        animator.start();
    }

    public static void main(String[] args) {
        launch(args);
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
        for (int i = i1; i <= i2; i++) {
            for (int j = j1; j <= j2; j++) {
                if(collisionMatrix[i][j] > 0 && Math.abs(collisionMatrix[i][j] - value) > 8)
                    return true;
                collisionMatrix[i][j] = value;
            }
        }
        return false;
    }
}
