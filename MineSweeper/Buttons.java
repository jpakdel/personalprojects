package game;

import java.util.Observer;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Observable;
import javax.swing.JButton;

//manages buttons for each tile
public class Buttons extends JButton implements Observer {

    private static final long serialVersionUID = 1L;
    private JButton button;
    private Tile tile;
    private Click clicks;

    //basic constructor with mouselistener
    public Buttons(Tile tile) {
        this.tile = tile;
        this.button = new JButton("");
        this.button.setPreferredSize(new Dimension(50, 50));
        this.clicks = new Click(tile);
        this.button.addMouseListener(clicks);
        this.tile.addObserver(this);
    }

    public JButton getButton() {
        return this.button;
    }

    //updates tile/button based on visibility, whether it's a mine, flag etc
    public void update(Observable obs, Object o) {
        if (tile.show() == true) {
            if (this.tile.get_id() == 9) {
                this.button.setBackground(Color.RED);
                this.button.setText("M");
            } else {
                this.button.setBackground(Color.LIGHT_GRAY);
                if (this.tile.get_id() == 0) {
                    this.button.setText("");
                } else {
                    this.button.setText(Integer.toString(this.tile.get_id()));
                }
            }
        }
        if (tile.getFlag()) {
            this.button.setText("!");
            this.button.setBackground(Color.orange);
        }
        if (!tile.getFlag() && !tile.show()) {
            this.button.setText("");
            this.button.setBackground(new JButton().getBackground());
        }
    }
}
