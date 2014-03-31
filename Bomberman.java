import im.antoine.bombjava.Client;
import im.antoine.bombjava.Duration;
import im.antoine.bombjava.GameState;
import im.antoine.bombjava.PlayerState;
import java.util.Date;

class Bomberman implements Runnable
{
    volatile GameState state;
    Client client;

    Bomberman()
    {
        try {
        client = new Client("127.0.0.1", 40000);
            client.open();
            Thread stateThread = new Thread(this);
            stateThread.start();
        } catch (Exception e) {e.printStackTrace();}
    }

    public void run()
    {
        try {
        while(client.hasNext())
        {
            state = client.nextState();
        }
        } catch(Exception e) {e.printStackTrace();}
    }

    public void loop()
    {
        Player player = null;
        Player.Move lastMove = Player.Move.WAIT;
        int lastFrame = -1;
        try
        {
        while(true)
        {
            if(state == null) continue;
            if(player == null) 
                player = new Player(state.getPlayerState().getX(), state.getPlayerState().getY());
            Player.Move move = player.update(state);
            if(state.getTurn() == lastFrame && lastMove != Player.Move.WAIT) continue;
            switch(move)
            {
                case UP:
                    client.goUp();
                    break;
                case DOWN:
                    client.goDown();
                    break;
                case LEFT:
                    client.goLeft();
                    break;
                case RIGHT:
                    client.goRight();
                    break;
                case BOMB:
                    client.putBomb();
                    break;
                default:
                    break;
            }
            lastMove = move;
            lastFrame = state.getTurn();
        }
        } catch(Exception e)
        {e.printStackTrace(); }
    }

    public static void main(String[] args)
    {
        Bomberman bomberman = new Bomberman();
        bomberman.loop();
    }
}
