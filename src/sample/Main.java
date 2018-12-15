package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends Application {

    private final int width = 1000;
    private final int height = 600;
    private final double lineWidth = 3;
    private double speed = 2;
    private double angularSpeed = 0.03;
    private ArrayList<Player> humanPlayers = new ArrayList<>();
    private int[][] collisionMatrix = new int[width][height];
    private ArrayList<KeyCode> keys = new ArrayList<KeyCode>() {{
        add(KeyCode.LEFT);  add(KeyCode.RIGHT);
        add(KeyCode.Q);     add(KeyCode.E);
        add(KeyCode.DIGIT7);add(KeyCode.DIGIT9);
        add(KeyCode.Z);     add(KeyCode.C);
        add(KeyCode.COMMA); add(KeyCode.PERIOD);
    }};
    private Random random = new Random();
    private List<Integer> activePlayers = new LinkedList<>();
    private double chance = 3e-3;

    @Override
    public void start(Stage theStage)
    {
        theStage.setTitle( "Matacka" );
        HBox gameArea = new HBox();
        StackPane root = new StackPane();
        root.getChildren().add(gameArea);
        Scene theScene = new Scene(root);
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
        pane.setStyle("-fx-background-color: #E0F0FF");
        gameArea.setStyle("-fx-background-color: #FFFFFF");
        gameArea.getChildren().addAll(canvas, pane, scoreboard);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(lineWidth);

        setPlayers();

        for (Player p : humanPlayers)
        {
            scoreboard.getChildren().add(p.getDisplay());
            p.angle = random.nextDouble() * 2 * Math.PI;
            p.position = new Point2D.Double(100 + random.nextInt(width - 200), 100 + random.nextInt(height - 200));
        }


        theStage.show();

        AnimationTimer animator = new AnimationTimer()
        {
            @Override
            public void handle(long arg0)
            {
                Collections.shuffle(activePlayers);
                LinkedList<Integer> unlucky = new LinkedList<>();
                for (int i : activePlayers)
                {
                    Player p = humanPlayers.get(i);
                    double px = p.position.getX() + speed * Math.cos(p.angle);
                    double py = p.position.getY() + speed * Math.sin(p.angle);
                    if(p.direction.equals(Player.Direction.Right))
                        p.angle += angularSpeed;
                    if(p.direction.equals(Player.Direction.Left))
                        p.angle -= angularSpeed;
                    p.collisionValue++;

                    if(random.nextDouble() < chance)
                        p.hole = 10;
                    if(p.hole > 0)
                        p.hole--;
                    else
                    {
                        gc.setStroke(p.color);
                        gc.strokeLine(p.position.getX(), p.position.getY(), px, py);
                        if(collide(px, py, p.collisionValue))
                            unlucky.push(i);
                    }

                    p.position.setLocation(px, py);

                }
                for (int i : unlucky)
                {
                    humanPlayers.get(i).addPoints(unlucky.size() - 1);
                    activePlayers.remove(Integer.valueOf(i));
                }
                for (int i : activePlayers)
                {
                    humanPlayers.get(i).addPoints(unlucky.size());
                }
            }
        };

        activePlayers = IntStream.rangeClosed(0, humanPlayers.size() - 1).boxed().collect(Collectors.toList());
        animator.start();

    }

    private void setPlayers()
    {
        humanPlayers.clear();
        int players = 2;
        List<Integer> choices = IntStream.rangeClosed(2, 12).boxed().collect(Collectors.toList());

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(2, choices);
        dialog.setTitle("Game settings");
        dialog.setHeaderText("Choose the number of players");
        dialog.setContentText("Total players:");

        Optional<Integer> result = dialog.showAndWait();
        if (result.isPresent())
            players = result.get();

        for (int i = 0; i < players; i++)
            humanPlayers.add(new Player(i));
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
