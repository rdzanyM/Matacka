package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.geom.Point2D;
import java.io.FileInputStream;

public class Main extends Application {

    double speed = 1;
    double angularSpeed = 0.01;
    Player p = new Player(true);

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

        Canvas canvas = new Canvas( 900, 500 );
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill( Color.RED );
        gc.setStroke( Color.BLACK );
        gc.setLineWidth(2);
        p.angle = 0;
        p.position = new Point2D.Double(20,100);

        theStage.show();

        AnimationTimer animator = new AnimationTimer(){

            @Override
            public void handle(long arg0) {

                // UPDATE
                /*ballX += xSpeed;

                if (ballX + ballRadius >= WIDTH)
                {
                    ballX = WIDTH - ballRadius;
                    xSpeed *= -1;
                }
                else if (ballX - ballRadius < 0)
                {
                    ballX = 0 + ballRadius;
                    xSpeed *= -1;
                }

                // RENDER
                circle.setCenterX(ballX);*/
                double px = p.position.getX() + speed * Math.cos(p.angle);
                double py = p.position.getY() + speed * Math.sin(p.angle);

                gc.strokeLine(p.position.getX(), p.position.getY(), p.position.getX(), p.position.getY());

                if(p.direction.equals(Player.Direction.Right))
                    p.angle += angularSpeed;
                if(p.direction.equals(Player.Direction.Left))
                    p.angle -= angularSpeed;

                p.position.setLocation(px, py);
            }
        };

        animator.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
