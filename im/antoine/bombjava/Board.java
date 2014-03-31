package im.antoine.bombjava;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.ArrayList;

public final class Board {

  private final Cell[][] cells;

  Board(JsonReader reader) throws IOException {
    ArrayList<ArrayList<Cell>> cellArrL = new ArrayList<ArrayList<Cell>>();

    reader.beginArray();
    while (reader.hasNext()) {
      ArrayList<Cell> row = new ArrayList<Cell>();

      reader.beginArray();
      while (reader.hasNext()) {
        row.add(new Cell(reader));
      }
      reader.endArray();

      cellArrL.add(row);
    }
    reader.endArray();

    Cell[][] cells = new Cell[cellArrL.size()][cellArrL.get(0).size()];
    for (int x = 0; x < cells.length; x++) {
      for (int y = 0; y < cells[0].length; y++) {
        cells[x][y] = cellArrL.get(x).get(y);
      }
    }
    this.cells = cells;
  }

  public Cell[][] getCells() {
    return this.cells;
  }

  public int getWidth() {
    return cells.length;
  }

  public int getHeight() {
    return cells[0].length;
  }


  public static enum GameObject {
    // Land
    Wall("Wall"), Ground("Ground"), Rock("Rock"),
    // Danger!
    Flame("Flame"), Bomb("Bomb"),
    // Powerups
    BombPowerUp("BombPU"), RadiusPowerUp("RadiusPU"),
    // Client
    Player("p");

    private String type;

    GameObject(String name) {
      this.type = name;
    }

    public static GameObject fromString(String text) {
      if (text == null) {
        return null;
      }
      for (GameObject go : GameObject.values()) {
        if (text.startsWith(go.type)) {
          return go;
        }
      }
      return null;
    }
  }

  public final static class Cell {

    public final int        x;
    public final int        y;
    public final GameObject object;
    public final String     content;

    private Cell(JsonReader reader) throws IOException {
      int x = -1;
      int y = -2;
      GameObject object = null;
      String content = "";

      reader.beginObject();
      while (reader.hasNext()) {

        String field = reader.nextName();
        if (field.equals("X")) {
          x = reader.nextInt();
        } else if (field.equals("Y")) {
          y = reader.nextInt();
        } else if (field.equals("Name")) {
          content = reader.nextString();
          object = GameObject.fromString(content);
        }

      }
      reader.endObject();

      this.x = x;
      this.y = y;
      this.object = object;
      this.content = content;
    }
  }
}
