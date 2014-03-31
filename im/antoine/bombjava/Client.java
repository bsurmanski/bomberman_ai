package im.antoine.bombjava;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

public class Client implements Closeable {

  interface Opener {

    public void open() throws IOException;

    public Reader getReader() throws IOException;

    public Writer getWriter() throws IOException;

    public void close() throws IOException;
  }

  private static final String UP       = "up\n";
  private static final String DOWN     = "down\n";
  private static final String LEFT     = "left\n";
  private static final String RIGHT    = "right\n";
  private static final String PUT_BOMB = "bomb\n";

  private Opener         opener;
  private JsonReader     jsonReader;
  private BufferedWriter textWriter;
  private boolean hasNext = false;

  public Client(final String hostname, final int port) {
    this.hasNext = true;
    this.opener = new Opener() {
      Socket conn;

      public void open() throws IOException {
        conn = new Socket(hostname, port);
      }

      public Reader getReader() throws IOException {
        return new InputStreamReader(conn.getInputStream());
      }

      public Writer getWriter() throws IOException {
        return new OutputStreamWriter(conn.getOutputStream());
      }

      public void close() throws IOException {
        conn.close();
      }
    };
  }

  Client(Opener opener) {
    this.hasNext = true;
    this.opener = opener;
  }

  public final void open() throws IOException {
    opener.open();

    jsonReader = new JsonReader(opener.getReader());
    jsonReader.setLenient(true);
    textWriter = new BufferedWriter(opener.getWriter());
  }

  public final void close() throws IOException {
    opener.close();
  }

  public final void goUp() throws IOException {
    textWriter.write(UP);
    textWriter.flush();
  }

  public final void goDown() throws IOException {
    textWriter.write(DOWN);
    textWriter.flush();
  }

  public final void goLeft() throws IOException {
    textWriter.write(LEFT);
    textWriter.flush();
  }

  public final void goRight() throws IOException {
    textWriter.write(RIGHT);
    textWriter.flush();
  }

  public final void putBomb() throws IOException {
    textWriter.write(PUT_BOMB);
    textWriter.flush();
  }

  public final void goWait() throws IOException {
    textWriter.write("\n");
    textWriter.flush();
  }

  public final boolean hasNext() {
    return hasNext;
  }

  public final GameState nextState() throws IOException {

    Board board = null;
    PlayerState pState = new PlayerState();
    int turn = 0;
    Duration d = null;
    String message = "";

    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      if (jsonReader.peek() == JsonToken.END_DOCUMENT) {
        hasNext = false;
        return null;
      }
      String name = jsonReader.nextName();

      if (pState.setField(name, jsonReader)) {
        continue;
      }

      if (name.equals("Board")) {
        board = new Board(jsonReader);
      } else if (name.equals("Turn")) {
        turn = jsonReader.nextInt();
      } else if (name.equals("TurnDuration")) {
        d = new Duration(jsonReader.nextLong());
      } else if (name.equals("Message")){
        message = jsonReader.nextString();
      } else {
        jsonReader.skipValue();
      }
    }
    jsonReader.endObject();

    return newState(board, pState, turn, d, message);
  }

  private static void close(Closeable c) {
    if (c == null) {
      return;
    }
    try {
      c.close();
    } catch (IOException e) { /* Don't care */ }
  }

  private static GameState newState(final Board b,
                                    final PlayerState p,
                                    final int turn,
                                    final Duration d,
                                    final String message) {
    return new GameState() {
      public int getTurn() {
        return turn;
      }

      public Duration getTurnDuration() {
        return d;
      }

      public PlayerState getPlayerState() {
        return p;
      }

      public Board getBoard() {
        return b;
      }

      public String getMessage() { return message; }
    };
  }
}
