package game;

import java.util.Observable;
public class GameBoard extends Observable {
    private Tile[][] board;
    private int length, width, mines, mines_left, revealed, clock;
    private String state;
    private Thread thread;
    private boolean running;

   //standard constructor
    public GameBoard(int width, int length, int mines) {
        //Creates the board as a 2D arrm of tiles
        this.board = new Tile[length][width];
        this.width = width;
        this.length = length;
        this.mines = mines;
        this.mines_left = mines;
        this.revealed = 0;
        this.clock = 0;
        this.state = "running";
        this.running = false;
        this.thread = new Thread();
        setupBoard();
    }

    public void setup() {
        this.state = "lost";
        this.setChanged();
        this.notifyObservers();
        resetBoard();
        this.state = "running";
        this.mines_left = this.mines;
        this.revealed = 0;
        resetThread();
        setupBoard();
        this.setChanged();
        this.notifyObservers(true);
    }

    //creates thread for the clock
    private void setThread() {
        this.thread = new Thread() {
            @SuppressWarnings("static-access")
            //if running = true timer increments every second by 1
            @Override
            public void run() {
                while (running) {
                    try {
                        setClock();
                        setChanged();
                        notifyObservers();
                        this.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        this.thread.start();
    }

    public void setClock() {
        this.clock++;
        this.setChanged();
        this.notifyObservers();
    }

    //creates game board
    public void setupBoard() {
        setAllMap();
        //randomly generate mines
        for (int i = 0; i < mines; i++) {
            int width, length;
            do {
                width = (int) (Math.random() * (this.width));
                length = (int) (Math.random() * (this.length));
            } while (this.board[length][width].get_id() == 9);
            this.board[length][width].setMine();
            minecount(getTile(length, width));
        }
    }

    //calculates and returns distance to mine
    public int nearmines(int y, int x) {
        int result = 0;
        int n, m;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                m = y + i;
                n = x + j;
                if (m >= 0 && m < this.length && n >= 0 && n < this.width) {
                    if (this.board[m][n].get_id() == 9) {
                        result++;
                    }
                }

            }
        }
        return result;

    }

    //adds one to board everywhere
    public void minecount(Tile tile) {
        int n;
        int m;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                m = tile.getPosy() + i;
                n = tile.getPosx() + j;

                if (m >= 0 && m < this.length && n >= 0 && n < this.width) {
                    if (this.board[m][n].get_id() != 9) {
                        this.board[m][n].add1();
                    }
                }

            }
        }
    }

    //reveals all zeros on map
    public void revealZeros(Tile tile) {
        int x = tile.getPosx();
        int y = tile.getPosy();
        int n, m;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                m = y + i;
                n = x + j;
                if (m >= 0 && m < this.length && n >= 0 && n < this.width) {
                    if (!this.board[m][n].getFlag()) {
                        this.board[m][n].reveal();
                    }
                }
            }
        }

    }

   //sets state
    public void setState(String state) {
        this.state = state;
        this.setChanged();
        this.notifyObservers();
    }

    //initialized all of the tiles
    private void setAllMap() {
        for (int i = 0; i < this.length; i++) {
            for (int j = 0; j < this.width; j++) {
                this.board[i][j] = new Tile(this, j, i, 0);
            }
        }
    }

    //resets entire board
    private void resetBoard() {
        for (int i = 0; i < this.length; i++) {
            for (int j = 0; j < this.width; j++) {
                this.board[i][j].setup(this, j, i, 0);
            }
        }
    }

   //adds to num revealed mines, potentially win
    public void addToRevealed() {
        this.revealed++;
        if (this.revealed >= ((this.width * this.length) - mines)) {
            setState("won");

        }
        this.setChanged();
        this.notifyObservers();
    }

    //adds 1 to the num mines
    public void addMine() {
        this.mines_left++;
        this.setChanged();
        this.notifyObservers();
    }

    //Removes 1 mine
    public void removeMine() {
        this.mines_left--;
        this.setChanged();
        this.notifyObservers();
    }

    public void startThread() {
        this.running = true;
        this.setThread();
    }

    public void resetThread() {
        this.running = false;
        this.clock = 0;
        this.setChanged();
        this.notifyObservers();
    }

    public void stopThread() {
        this.running = false;

    }

    //basic getters
    public Thread getThread() {
        return this.thread;
    }

    public int getLength() {
        return this.length;
    }

    public int getWidth() {
        return this.width;
    }

    public String getState() {
        return this.state;
    }
 
    public Tile getTile(int length, int width) {
        return this.board[length][width];
    }

    public int remainingMines() {
        return this.mines_left;
    }
    
    public int getClock() {
        return this.clock;
    }
}
