package im.antoine.bombjava;

public interface GameState {

  public int getTurn();

  public Duration getTurnDuration();

  public PlayerState getPlayerState();

  public Board getBoard();

  public String getMessage();
}
