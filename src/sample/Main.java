package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends Application {

    private int counter = 0;
    private int width;      //width  of game area
    private int height;     //height of game area
    private final double lineWidth = 3; //width of players' trails
    private double speed;                       //players' real speed (lower than max at the beginning of a round)
    private final double maxSpeed = 2;          //players' regular speed
    private final double angularSpeed = 0.033;  //players' angular speed
                                                //players' minimal turn radius is speed/angularSpeed
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> humanPlayers = new ArrayList<>();
    private ArrayList<ComputerPlayer> computerPlayers = new ArrayList<>();
    private int[][] collisionMatrix;
    private final Hashtable<KeyCode, Integer> keys = new Hashtable<KeyCode, Integer>()
    {{
        put(KeyCode.LEFT,   -1);    put(KeyCode.RIGHT,  1);
        put(KeyCode.DIGIT1, -2);    put(KeyCode.DIGIT2, 2);
        put(KeyCode.DIGIT9, -3);    put(KeyCode.DIGIT0, 3);
        put(KeyCode.DIGIT5, -4);    put(KeyCode.DIGIT6, 4);
        put(KeyCode.Z,      -5);    put(KeyCode.X,      5);
        put(KeyCode.COMMA,  -6);    put(KeyCode.PERIOD, 6);
        put(KeyCode.V,      -7);    put(KeyCode.B,      7);
        put(KeyCode.D,      -8);    put(KeyCode.F,      8);
        put(KeyCode.J,      -9);    put(KeyCode.K,      9);
        put(KeyCode.MINUS,  -10);   put(KeyCode.EQUALS, 10);
        put(KeyCode.DIGIT3, -11);   put(KeyCode.DIGIT4, 11);
        put(KeyCode.DIGIT7, -12);   put(KeyCode.DIGIT8, 12);
    }};
    private final ArrayList<String> controls = new ArrayList<String>()
    {{
        add("←"); add("→");
        add("1"); add("2");
        add("9"); add("0");
        add("5"); add("6");
        add("Z"); add("X");
        add(","); add(".");
        add("V"); add("B");
        add("D"); add("F");
        add("J"); add("K");
        add("-"); add("=");
        add("3"); add("4");
        add("7"); add("8");
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
            if(keys.containsKey(event.getCode()))
            {
                int i = keys.get(event.getCode());
                if(Math.abs(keys.get(event.getCode())) <= humanPlayers.size())
                {
                    if(i > 0 && humanPlayers.get(i - 1).direction == Player.Direction.Right)
                        humanPlayers.get(i - 1).direction = Player.Direction.Straight;
                    else if(i < 0 && humanPlayers.get(-(i + 1)).direction == Player.Direction.Left)
                        humanPlayers.get(-(i + 1)).direction = Player.Direction.Straight;
                }
            }
        });
        theScene.setOnKeyPressed(event -> {
            if(keys.containsKey(event.getCode()))
            {
                int i = keys.get(event.getCode());
                if(Math.abs(keys.get(event.getCode())) <= humanPlayers.size())
                {
                    humanPlayers.get(Math.abs(i) - 1).direction = (i > 0) ? Player.Direction.Right : Player.Direction.Left;
                }
            }
        });
        if (!setPlayers()) return;
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

        Label l = new Label("Get " + Player.limit + " points\nand win");
        l.setTextAlignment(TextAlignment.CENTER);
        l.setAlignment(Pos.CENTER);
        l.setPadding(new Insets(1,4,1,4));
        scoreboard.getChildren().add(l);
        for (Player p : players)
        {
            scoreboard.getChildren().add(p.getDisplay());
            gameArea.getChildren().add(p.getHead());
        }
        //theStage.setResizable(false);
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
                        p.pretender = p.getPoints() == Player.limit;
                    }
                    speed = 0.6;
                    counter = 0;
                }
                if(speed < maxSpeed) speed += 0.02;
                Collections.shuffle(activePlayers);
                LinkedList<Integer> unlucky = new LinkedList<>();
                if(activePlayers.size() == 1  && counter % 0xff == 0)
                    activePlayers.clear();
                for (int i : activePlayers)
                {
                    if(i >= humanPlayers.size())
                    {
                        ComputerPlayer p = computerPlayers.get(i - humanPlayers.size());
                        synchronized (p.threadID)
                        {
                            p.threadID.value++;
                            p.direction = p.computedDirection;
                        }
                        p.computeInitAsync(collisionMatrix, activePlayers.toArray(new Integer[0]));
                    }
                }
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
                int points = players.size() - activePlayers.size() + 1;
                for (int i : unlucky)
                {
                    players.get(i).addPoints((unlucky.size() - 1) * points);
                    activePlayers.remove(Integer.valueOf(i));
                }
                if (activePlayers.size() == 1 && players.get(activePlayers.get(0)).pretender)
                {
                    this.stop();
                    Platform.runLater(() ->
                    {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Game over");
                        alert.setHeaderText("Player " + (activePlayers.get(0) + 1) + " won.");
                        alert.setContentText("Thanks for playing!");
                        alert.showAndWait();
                        System.exit(0);
                    });
                }
                for (int i : activePlayers)
                {
                    players.get(i).addPoints(unlucky.size() * points);
                }
                counter++;
            }
        };

        animator.start();
    }

    private boolean setPlayers()
    {
        players.clear();
        humanPlayers.clear();
        int total, humans;
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
        else
            return false;
        choices = IntStream.rangeClosed(0, total).boxed().collect(Collectors.toList());
        dialog = new ChoiceDialog<>(1, choices);
        dialog.setTitle("Game settings");
        dialog.setHeaderText("Choose the number of human players");
        dialog.setContentText("Human players:");
        result = dialog.showAndWait();
        if (result.isPresent())
            humans = result.get();
        else
            return false;
        StringBuilder tip = new StringBuilder();
        for (int i = 0; i < humans; i ++)
        {
            tip.append("Player ").append(i + 1).append(" \tLeft: ").append(controls.get(2 * i)).append(" \tRight: ").append(controls.get(2 * i + 1)).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, tip + "\nPlayers' scores and colors will be displayed on the right side of the window.");
        alert.setTitle("Controls");
        alert.setHeaderText("Here are your controls");
        alert.showAndWait();
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
        width = 350 + 80 * total;
        height = 250 + 50 * total;
        collisionMatrix = new int[width][height];
        ComputerPlayer.width = width;
        ComputerPlayer.height = height;
        ComputerPlayer.speed = maxSpeed;
        ComputerPlayer.players = players;
        ComputerPlayer.lineWidth = lineWidth;
        ComputerPlayer.angularSpeed = angularSpeed;
        Player.limit = players.size() * (players.size() + 1);
        return true;
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
