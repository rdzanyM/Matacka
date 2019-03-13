# Matacka :snake:

## Description
It is a game for 2-12 players, including at least one human player (others can be computer-contolled). Game area is a rectangular bitmap. Players are moving around the game area with different-coloured "snakes". A snake dies after going into the wall or hitting a snake. The aim of the game is to collect a certain number of points awarded each round for surviving longer than other players.

## Movement
All snakes are moving at the same speed. The angular speed is constant throughout the whole game. The linear speed is lower in the beginning each round (resulting in a smaller turn radius) so that the players have a greater chance to survive an unfortunate spawn. The linear speed starts at 30% of the base value and gradually rises to 100% in the first 90 frames (1.5 seconds). After that time, when both angular and linear speed aren't changing anymore, the snakes have a constant turning radius, which means that during the turn they move along a circumference of a circle with a certain radius.Each player has a position (x,y coordinates) on the map and the `angle` of current movement. These values are updated each frame.
```java
  Player p = players.get(i);
  double px = p.getX() + speed * Math.cos(p.angle);
  double py = p.getY() + speed * Math.sin(p.angle);
  if (p.direction.equals(Player.Direction.Right))
      p.angle += angularSpeed;
  if (p.direction.equals(Player.Direction.Left))
      p.angle -= angularSpeed;
```
```java
  p.setPosition(px, py);
```
## Controls
In each frame a player can make one of 3 moves: turn right/left or go straight. Due to 
