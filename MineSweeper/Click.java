package game;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

//this class manages input
public class Click extends MouseAdapter {

    private Tile tile;
    private GameBoard game_board;

  //simple constructors
    public Click(GameBoard game_board) {
        this.game_board = game_board;
    }
    public Click(Tile tile) {
        this.tile = tile;
    }  
    public void setTile(Tile tile) {
        this.tile = tile;
    }
    
    //checks for mouse clicks and updates tiles
    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                if (this.game_board == null) {
                    if (!tile.getFlag()) {
                        tile.reveal();
                    }
                } else {
                    game_board.setup();
                }
                break;
            case MouseEvent.BUTTON3:
                if (this.game_board == null) {
                    tile.changeState();
                }
                break;
            default:
                break;
        }
    }
}
