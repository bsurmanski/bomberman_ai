package im.antoine.bombjava;

import com.google.gson.stream.JsonReader;

import java.io.IOException;

public final class PlayerState {

  private String  name;
  private boolean isAlive;
  private int     x;
  private int     y;
  private int     lastX;
  private int     lastY;
  private int     bombsPlaced;
  private int     maxBombs;
  private int     maxBombRadius;

  PlayerState() {
  }

  boolean setField(String fieldName, JsonReader reader) throws IOException {
    if (fieldName.equals("Name")) {
      name = reader.nextString();
    } else if (fieldName.equals("X")) {
      x = reader.nextInt();
    } else if (fieldName.equals("Y")) {
      y = reader.nextInt();
    } else if (fieldName.equals("LastX")) {
      lastX = reader.nextInt();
    } else if (fieldName.equals("LastY")) {
      lastY = reader.nextInt();
    } else if (fieldName.equals("Bombs")) {
      bombsPlaced = reader.nextInt();
    } else if (fieldName.equals("MaxBomb")) {
      maxBombs = reader.nextInt();
    } else if (fieldName.equals("MaxRadius")) {
      maxBombRadius = reader.nextInt();
    } else if (fieldName.equals("Alive")) {
      isAlive = reader.nextBoolean();
    } else {
      return false;
    }
    return true;
  }

  public String getName() {
    return name;
  }

  public boolean isAlive() {
    return isAlive;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getLastX() {
    return lastX;
  }

  public int getLastY() {
    return lastY;
  }

  public int getBombsPlaced() {
    return bombsPlaced;
  }

  public int getMaxBombs() {
    return maxBombs;
  }

  public int getMaxBombRadius() {
    return maxBombRadius;
  }
}
