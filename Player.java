import im.antoine.bombjava.Client;
import im.antoine.bombjava.GameState;
import im.antoine.bombjava.PlayerState;
import im.antoine.bombjava.Board;
import java.lang.Math;
import java.util.Collections;
import java.util.ArrayList;

// Bombs take 60 frames to explode

class Player
{
    public enum Move
    {
        UP, DOWN, LEFT, RIGHT, BOMB, WAIT 
    }

    static int BIG = 99999;

    class CellNote
    {
        boolean flag;    // traversal flag, 1=visited
        int distance;
        int obstruction; // distance in number of walls needed to blow up to get there
        int danger;
        Board.Cell cell;
        Move minMove;

        boolean isPassible()
        {
            return cell.object == Board.GameObject.Ground;
        }

        boolean isBreakible()
        {
            return cell.object == Board.GameObject.Rock;
        }

        boolean isDangerous()
        {
            return danger > 0;
        }

        boolean isPlayer() 
        {
            return cell.object == Board.GameObject.Player;
        }
    }

    class Enemy implements Comparable<Enemy>
    {
        int x;
        int y;
        Enemy(int _x, int _y)
        {
            x = _x; 
            y = _y;
        }

        public int compareTo(Enemy o)
        {
            return x - o.x;
        }
    }

    class Bomb
    {
        int timer;
        int x; int y;
        Bomb(int _x, int _y) 
        { 
            timer = 70; 
            x = _x;
            y = _y;
        }
        void update() { timer--; }
    }

    ArrayList<Bomb> bombs;
    ArrayList<Enemy> enemies;

    CellNote[][] cellNotes;
    int x;
    int y;
    int lastX;
    int lastY;
    int lastFrame;
    Move lastMove;

    Player(int _x, int _y)
    { 
        cellNotes = null;
        bombs = new ArrayList<Bomb>();
        enemies = new ArrayList<Enemy>();
        lastMove = Move.WAIT;
        x = _x;
        y = _y;
        lastFrame = 0;
        lastX = 0;
        lastY = 0;
    }

    int getX() 
    {
        return x;
    }

    int getY()
    {
        return y;
    }

    int getLastX()
    {
        return lastX;
    }

    int getLastY()
    {
        return lastY;
    }

    CellNote getCellNote(int x, int y)
    {
        return cellNotes[y][x]; 
    }

    CellNote getOffsetCellNote(int i, int j, Move m)
    {
        switch(m)
        {
            case UP: return getCellNote(i, j - 1);
            case DOWN: return getCellNote(i, j + 1);
            case LEFT: return getCellNote(i-1, j );
            case RIGHT: return getCellNote(i+1, j);
            default: return getCellNote(i, j);
        }
    }


    Board.Cell getBoardCell(Board b, int i, int j)
    {
        return b.getCells()[i][j];
    }

    boolean outOfBounds(Board b, int i, int j)
    {
        return (i < 0 || j < 0 || i >= b.getWidth() || j >= b.getHeight()); // out of bounds
    }

    void clearCellNotes()
    {
        for(int j = 0; j < cellNotes.length; j++)
        {
            for(int i = 0; i < cellNotes[j].length; i++)
            {
                CellNote note = getCellNote(i, j);
                note.flag = false;
                note.distance = BIG;
                note.obstruction = BIG;
                note.danger = 0;
                note.minMove = Move.WAIT;
            }
        }
    }

    void updateCellNotes_r(Board b, int i, int j, int obst, int dist, Move minMove)
    {
        if(outOfBounds(b, i, j)) return; // out of bounds

        CellNote note = getCellNote(i, j);
        note.cell = getBoardCell(b, i, j);

        if(getBoardCell(b, i, j).object == Board.GameObject.Rock) obst++;
        if(getBoardCell(b, i, j).object == Board.GameObject.Wall) obst=BIG;
        if(getBoardCell(b, i, j).object == Board.GameObject.Flame) obst=BIG;
        if(note.obstruction < obst || (note.obstruction <= obst && note.distance < dist) || 
                obst == BIG) return;
        if(note.obstruction < obst) return;

        note.danger = 0;
        if(note.cell.object == Board.GameObject.Flame) note.danger = 100;
        if(note.cell.object == Board.GameObject.Bomb) note.danger = 50;
        if(note.cell.object == Board.GameObject.Player && i != getX() && j != getY()) 
            enemies.add(new Enemy(i, j));

        note.obstruction = obst;
        note.distance = dist;
        note.minMove = minMove;

        updateCellNotes_r(b, i + 1, j, obst, dist+1, minMove);
        updateCellNotes_r(b, i - 1, j, obst, dist+1, minMove);
        updateCellNotes_r(b, i, j + 1, obst, dist+1, minMove);
        updateCellNotes_r(b, i, j - 1, obst, dist+1, minMove);
    }

    void endangerFromBombs(GameState state, int x, int y, int dx, int dy, int max)
    {
        if(max <= 0 || outOfBounds(state.getBoard(), x, y)) return;
        CellNote n = getCellNote(x, y);
        if(!n.isPassible() && !n.isPlayer()) return;
        n.danger = 50;

        if(dx > 0)
            endangerFromBombs(state, x+1, y, dx, 0, max-1);
        if(dx < 0)
            endangerFromBombs(state, x-1, y, dx, 0, max-1);
        if(dy > 0)
            endangerFromBombs(state, x, y+1, 0, dy, max-1);
        if(dy < 0)
            endangerFromBombs(state, x, y-1, 0, dy, max-1);
    }

    void endangerFromBombs(GameState state)
    {
        for(int i = 0; i < bombs.size(); i++)
        {
            Bomb b = bombs.get(i);
            CellNote n = getCellNote(b.x, b.y);
            n.danger = 100;
            endangerFromBombs(state, b.x, b.y+1, 0, 1, 5);
            endangerFromBombs(state, b.x, b.y-1, 0, -1, 5);
            endangerFromBombs(state, b.x+1, b.y, 1, 0, 5);
            endangerFromBombs(state, b.x-1, b.y, -1, 0, 5);
        }
    }

    void updateCellNotes(GameState state)
    {
        Board b = state.getBoard();
        // initialize if needed
        if(cellNotes == null || 
                cellNotes.length < b.getHeight() || 
                cellNotes[0].length < b.getWidth())
        {
            cellNotes = new CellNote[b.getHeight()][b.getWidth()]; 
            for(int j = 0; j < cellNotes.length; j++)
            {
                for(int i = 0; i < cellNotes[j].length; i++)
                {
                    cellNotes[j][i] = new CellNote();
                }
            }
        }

        clearCellNotes();
        enemies.clear();

        updateCellNotes_r(b, getX() + 1, getY(), 0, 1, Move.RIGHT);
        updateCellNotes_r(b, getX() - 1, getY(), 0, 1, Move.LEFT);
        updateCellNotes_r(b, getX(), getY() + 1, 0, 1, Move.DOWN);
        updateCellNotes_r(b, getX(), getY() - 1, 0, 1, Move.UP);

        Collections.sort(enemies);
        endangerFromBombs(state);
    }

    Move pathToTarget(GameState state, int tx, int ty)
    {
        CellNote note = getCellNote(tx, ty); 
        /*
        for(int i = 0; i < cellNotes.length; i++)
        {
            for(int j = 0; j < cellNotes[0].length; j++)
            {
                if(cellNotes[i][j].minMove != Move.WAIT)
                    System.out.println("i:" + i + " j:" + j + " " + cellNotes[i][j].minMove);
            }
        }
        */
        return note.minMove;
    }

    int pathToSafety_r(GameState state, int x, int y, int dist)
    {
        if(outOfBounds(state.getBoard(), x, y)) return BIG;
        CellNote note = getCellNote(x, y);
        if(!note.isPassible()) return BIG;
        if(!note.isDangerous()) return dist+1;
        if(note.distance < dist) return BIG; 

        dist++;
        int r = pathToSafety_r(state, x+1, y, dist); 
        int l = pathToSafety_r(state, x-1, y, dist); 
        int d = pathToSafety_r(state, x, y+1, dist); 
        int u = pathToSafety_r(state, x, y-1, dist); 
        return Math.min(r, Math.min(l, Math.min(d, u)));
    }

    Move selectMin(int l, int r, int u, int d)
    {
        if(r <= l && r <= d && r <= u) return Move.RIGHT;
        if(l <= r && l <= d && l <= u) return Move.LEFT;
        if(u <= l && u <= d && u <= r) return Move.UP;
        if(d <= l && d <= r && d <= u) return Move.DOWN;
        return Move.WAIT;
    }

    Move pathToSafety(GameState state)
    {
        PlayerState pstate = state.getPlayerState();
        if(!getCellNote(getX(), getY()).isDangerous()) return Move.WAIT;

        int r = pathToSafety_r(state, getX()+1, getY(), 1);
        int l = pathToSafety_r(state, getX()-1, getY(), 1);
        int d = pathToSafety_r(state, getX(), getY()+1, 1);
        int u = pathToSafety_r(state, getX(), getY()-1, 1);

        Move m = selectMin(l,r,u,d);
        return selectMin(l, r, u, d);
    }

    Move think(GameState state)
    {
        Board board = state.getBoard();
        PlayerState pstate = state.getPlayerState();

        
        if(getCellNote(getX(), getY()).isDangerous() || pstate.getBombsPlaced() > 0)
        {
            return pathToSafety(state);
        }

        Enemy enemy = null;
        Move m = Move.WAIT;

        if(enemies.size() > 0)
        {
            CellNote note = getCellNote(enemies.get(0).x, enemies.get(0).y);
            if(note.distance <= 2) return Move.BOMB;
            m = pathToTarget(state, enemies.get(0).x, enemies.get(0).y);
            System.out.println("TARGET " + enemies.get(0).x + " : " + enemies.get(0).y + " ; " + m);
        }
        CellNote off = getOffsetCellNote(getX(), getY(), m);
        if(!off.isPassible() && off.isBreakible())
        {
            return Move.BOMB;
        }

        return off.minMove;

        /*
        if(pstate.getBombsPlaced() < pstate.getMaxBombs())
        {
            return updateDestroy(); 
        } 

        return updateHide();*/
    }

    boolean bombAt(int x, int y)
    {
        for(int i = 0; i < bombs.size(); i++)
        {
            if(bombs.get(i).x == x && bombs.get(i).y == y) return true; 
        }
        return false;
    }

    void updatePosition(GameState state)
    {
        x = state.getPlayerState().getX();
        y = state.getPlayerState().getY();
    }

    boolean queued(GameState state)
    {
        PlayerState pstate = state.getPlayerState();
        return (lastMove != Move.WAIT || lastMove != Move.BOMB) && 
            (state.getTurn() - lastFrame < 3);
    }
    
    static int syncTimer = 0;
    boolean sync(GameState state) throws Exception
    {
        PlayerState pstate = state.getPlayerState();
        if(getX() == pstate.getX() && getY() == pstate.getY() && 
                (lastMove != Move.BOMB && lastMove != Move.WAIT) )
        {
            if(state.getTurn() - lastFrame > 10 || syncTimer >= 100)
            { 
                updatePosition(state);
                syncTimer = 0;
                return true;
            }

            syncTimer++;
            
            return false;
        }
        syncTimer = 0;

        updatePosition(state);
        return true;
    }

    Move update(GameState state)
    {
        boolean refresh = true;
        updatePosition(state);
        updateCellNotes(state);

        PlayerState pstate = state.getPlayerState();
        Move m = think(state);
        if(m == Move.BOMB)
        {
            bombs.add(new Bomb(getX(), getY()));
            refresh = true;
        }

        // update bombs
        for(int i = 0; i < bombs.size(); i++)
        {
            bombs.get(i).update();
            if(bombs.get(i).timer <= 0)
            {
                bombs.remove(i);
                refresh = true;
            }
        }

        lastMove = m;
        lastX = x;
        lastY = y;
        lastFrame = state.getTurn();
        return m;
    }
}
