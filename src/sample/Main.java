package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends Application {

    private final int width = 1200;     //width  of game area
    private final int height = 800;     //height of game area
    private final double lineWidth = 3; //width of players' trails
    private double speed;                       //players' real speed (lower than max at the beginning of a round)
    private final double maxSpeed = 2;          //players' regular speed
    private final double angularSpeed = 0.03;   //players' angular speed
                                                //players' minimal turn radius is speed/angularSpeed
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> humanPlayers = new ArrayList<>();
    private ArrayList<ComputerPlayer> computerPlayers = new ArrayList<>();
    private int[][] collisionMatrix = new int[width][height];
    private ArrayList<KeyCode> keys = new ArrayList<KeyCode>() {{
        add(KeyCode.LEFT);  add(KeyCode.RIGHT);
        add(KeyCode.Q);     add(KeyCode.E);
        add(KeyCode.DIGIT7);add(KeyCode.DIGIT9);
        add(KeyCode.Z);     add(KeyCode.C);
        add(KeyCode.COMMA); add(KeyCode.PERIOD);
    }};
    private Random random = new Random();
    private List<Integer> activePlayers = new LinkedList<>();   //list of ids of active(still moving) players in a round
    private double chance = 3e-3;                               //chance that trail drawing will be suppressed in a frame

    @Override
    public void start(Stage theStage)
    {
        theStage.setTitle( "Matacka" );
        HBox gameWindow = new HBox();
        StackPane root = new StackPane();
        root.getChildren().add(gameWindow);
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
        Pane gameArea = new Pane(canvas);
        gameArea.setMaxWidth(width);
        gameArea.setMaxHeight(height);
        Pane borderLine = new Pane();
        borderLine.setPrefSize(4,height);
        VBox scoreboard = new VBox();
        borderLine.setStyle("-fx-background-color: #E0F0FF");
        gameWindow.setStyle("-fx-background-color: #FFFFFF");
        gameWindow.getChildren().addAll(gameArea, borderLine, scoreboard);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(lineWidth);

        setPlayers();

        for (Player p : players)
        {
            scoreboard.getChildren().add(p.getDisplay());
            gameArea.getChildren().add(p.getHead());
        }


        theStage.show();

        AnimationTimer animator = new AnimationTimer()
        {
            @Override
            public void handle(long arg0)
            {
                if(activePlayers.isEmpty())
                {
                    for (int i = 0; i < width; i++)
                        for (int j = 0; j < height; j++)
                            collisionMatrix[i][j] = 0;
                    collisionMatrix = new int[width][height];
                    gc.clearRect(0,0,width,height);
                    activePlayers = IntStream.rangeClosed(0, players.size() - 1).boxed().collect(Collectors.toList());
                    for (Player p : players)
                    {
                        p.angle = random.nextDouble() * 2 * Math.PI;
                        p.setPosition(100 + random.nextInt(width - 200), 100 + random.nextInt(height - 200));
                    }
                    speed = 1e-1;
                }
                if(speed < maxSpeed) speed += 0.01;
                Collections.shuffle(activePlayers);
                LinkedList<Integer> unlucky = new LinkedList<>();
                for (int i : activePlayers)
                {
                    Player p = players.get(i);
                    double px = p.getX() + speed * Math.cos(p.angle);
                    double py = p.getY() + speed * Math.sin(p.angle);
                    if (p.direction.equals(Player.Direction.Right))
                        p.angle += angularSpeed;
                    if (p.direction.equals(Player.Direction.Left))
                        p.angle -= angularSpeed;
                    p.collisionValue++;

                    if (random.nextDouble() < chance)
                        p.hole = 10;
                    if (p.hole > 0)
                        p.hole--;
                    else
                    {
                        gc.setStroke(p.color);
                        gc.strokeLine(p.getX(), p.getY(), px, py);
                        if(collide(px, py, p.collisionValue))
                            unlucky.push(i);
                    }
                    p.setPosition(px, py);

                }
                for (int i : unlucky)
                {
                    players.get(i).addPoints(unlucky.size() - 1);
                    activePlayers.remove(Integer.valueOf(i));
                }
                for (int i : activePlayers)
                {
                    players.get(i).addPoints(unlucky.size());
                }
            }
        };

        animator.start();

    }

    private void setPlayers()
    {
        players.clear();
        humanPlayers.clear();
        int total = 2;
        int humans = 1;
        List<Integer> choices;
        Optional<Integer> result;
        ChoiceDialog<Integer> dialog;
        choices = IntStream.rangeClosed(2, 12).boxed().collect(Collectors.toList());
        dialog = new ChoiceDialog<>(2, choices);
        dialog.setTitle("Game settings");
        dialog.setHeaderText("Choose the total number of players");
        dialog.setContentText("Total players:");
        result = dialog.showAndWait();
        if (result.isPresent())
            total = result.get();
        choices = IntStream.rangeClosed(0, total).boxed().collect(Collectors.toList());
        dialog = new ChoiceDialog<>(1, choices);
        dialog.setTitle("Game settings");
        dialog.setHeaderText("Choose the number of human players");
        dialog.setContentText("Human players:");
        result = dialog.showAndWait();
        if (result.isPresent())
            humans = result.get();

        for (int i = 0; i < humans; i++)
        {
            Player p = new Player(i);
            players.add(p);
            humanPlayers.add(p);
        }
        for (int i = humans; i < total; i++)
        {
            ComputerPlayer p = new ComputerPlayer(i);
            players.add(p);
            computerPlayers.add(p);
        }
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
                if(collisionMatrix[i][j] > 0 && Math.abs(collisionMatrix[i][j] - value) > 0xf)
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
