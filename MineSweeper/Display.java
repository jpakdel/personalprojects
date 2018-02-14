package game;

import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Manages the display for the game
@SuppressWarnings("serial")
public class Display extends JPanel implements Observer {
	
    private GameBoard game_board;
    private Buttons[][] map;
    private JPanel display;
    private JLabel mines, gameState, clock;
    
    //basic constructor
    public Display(GameBoard game_board) {
        this.game_board = game_board;
        this.display = new JPanel();
        this.setLayout(new BorderLayout());
        this.mines = setLabel(this.mines, "Mines:  " + Integer.toString(game_board.remainingMines()));
        this.gameState = setLabel(this.gameState, "Status:  " + game_board.getState());
        this.clock = setLabel(this.clock, "Time:  " + game_board.getClock());
        this.add(this.mines, BorderLayout.WEST);
        this.add(this.gameState, BorderLayout.EAST);
        this.add(this.clock, BorderLayout.CENTER);
        this.add(restartButton(), BorderLayout.NORTH);
        this.map = new Buttons[game_board.getLength()][game_board.getWidth()];
        this.game_board.addObserver(this);
        this.display.setLayout(new GridLayout(game_board.getLength(), game_board.getWidth()));
        setup();
        this.add(display, BorderLayout.SOUTH);
    }

    //updates the display
    public void update(Observable obs, Object o) {
        if (o != null) {
            updateButtons();
        }
        this.mines = setLabel(this.mines, "Mines:  " + Integer.toString(game_board.remainingMines()));
        this.gameState = setLabel(this.gameState, "Status:  " + game_board.getState());
        this.clock = setLabel(this.clock, "Time:  " + this.game_board.getClock());

    }

    //returns correct label
    private JLabel setLabel(JLabel label, String string) {
        if (!(label instanceof JLabel)) {
            label = new JLabel("");
        }
        label.setText(string);
        label.setPreferredSize(new Dimension(100, 40));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
  
    public JPanel get_display() {
        return this.display;
    }
   
    public JButton restartButton() {
        JButton button = new JButton("Restart");
        button.setPreferredSize(new Dimension(20, 40));
        Click clicks = new Click(game_board);
        button.addMouseListener(clicks);
        return button;
    }

    //updates buttons upon restart of the game
    public void updateButtons() {
        removeButtons();
        setup();
    }

    private void removeButtons() {
        for (int i = 0; i < this.game_board.getLength(); i++) {
            for (int j = 0; j < this.game_board.getWidth(); j++) {
                this.display.remove(map[i][j].getButton());
            }
       }
    }

    private void setup() {
        for (int i = 0; i < this.game_board.getLength(); i++) {
            for (int j = 0; j < this.game_board.getWidth(); j++) {
                Buttons button = new Buttons(this.game_board.getTile(i, j));
                map[i][j] = button;
                this.display.add(button.getButton());
            }
        }
    }
}
