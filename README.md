# Matacka :snake:

## Description
It is a game for 2-12 players, including at least one human player (others can be computer-contolled). Game area is a rectangular bitmap. Players are moving around the game area with different-coloured "snakes". A snake dies after going into the wall or hitting a snake.

## Scoring system
After death of a player every still living player is awarded points. After *nth* collision every living player gets *n* points. When only one player is left, a new round begins. The game goal is to collect a maximum number of points (equal to *p\*(p+1)*, where *p* is the number of players), and then win a round. When any of the players achieves the goal, the game ends.

## Movement
All snakes are moving at the same speed. The angular speed is constant throughout the whole game. The linear speed is lower in the beginning each round (resulting in a smaller turn radius) so that the players have a greater chance to survive an unfortunate spawn. The linear speed starts at 30% of the base value and gradually rises to 100% in the first 90 frames (1.5 seconds). After that time, when both angular and linear speed aren't changing anymore, the snakes have a constant turning radius, which means that during the turn they move along a circumference of a circle with a certain radius.Each player has a position (x,y coordinates) on the map and the `angle` of current movement. These values are updated each frame.
```java
  Player p = players.get(i);
  double px = p.getX() + speed * Math.cos(p.angle);
  double py = p.getY() + speed * Math.sin(p.angle);
  if (p.direction.equals(Player.Direction.Right))
      p.angle += angularSpeed;
  else if (p.direction.equals(Player.Direction.Left))
      p.angle -= angularSpeed;
```
```java
  p.setPosition(px, py);
```
## Controls
In each frame a player can make one of 3 moves: turn right/left or go straight. In the beginning of the game each human player is assigned two buttons responsible for the snake turns. The computer-controlled players use the same interface, but set their `Direction` variable without using buttons.

## GUI
In the beginning of the game the user can choose the total number of players and the number of human players. During the game a scoreboard is displayed, allowing players to track their progress.

## AI algorithm
The algorithm tries to find the longest path with no collision. It works asynchronously for each player, modifying the variable representing the first move decision in a found sequence. It is restarted with new parameters after every frame. Algorithm assumes that other players will go straight, right and left at the same time. Because each computer-controlled player is assigned a new thread there may be a performance drop (due to starvation) when the number of computer-controlled players is greater or equal to the number of available CPU threads (one thread is used for the main application).
