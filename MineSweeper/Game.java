package game;
import javax.swing.JFrame;
import java.io.IOException;
import java.util.Scanner;
public class Game {
	
    //Starts the instance of Minesweeper
    public static void main(String[] argv) throws IOException {
        String input = "";
        int width = -1;
        int length = -1;
        int mines = -1;

        //Set the Mode
        if (argv.length == 1) {
            input = argv[0];
        }
        
        @SuppressWarnings("resource")
        Scanner scan = new Scanner(System.in);

        GameBoard game_board;
        
        System.out.println("Small/Medium/Large/Custom");

        //Annoy the user until he chooses something
        while (!input.equals("small")
                && !input.equals("medium")
                && !input.equals("large")
                && !input.equals("custom")) {

            System.out.print("Enter your desired size: ");
            input = scan.nextLine();
            input = input.toLowerCase();
        }
       
        //Sets sizes for the game
        if (input.equals("small")) {
            width = 9;
            length = 8;
            mines = 10;
            game_board = new GameBoard(width, length, mines);
        } else if (input.equals("medium")) {
            width = 14;
            length = 11;
            mines = 30;
            game_board = new GameBoard(width, length, mines);
        } else if (input.equals("large")) {
            width = 18;
            length = 14;
            mines = 55;
            game_board = new GameBoard(width, length, mines);
        } else {
            System.out.println();
            while (width < 6 || width > 35) {
                System.out.print("Enter your desired width (min:6/max:35): ");
                width = scan.nextInt();

            }

            System.out.println();
            while (length < 6 || length > 20) {
                System.out.print("Enter your desired length (min:6/max:20): ");
                length = scan.nextInt();

            }
            
            int max_mines = length * width - 1;

            System.out.println();
            while (mines < 1 || mines > max_mines) {
                System.out.print("Plese enter the number of mines (min:1/max:" + max_mines + "): ");
                mines = scan.nextInt();
            }
            
            //use user input to create board
            game_board = new GameBoard(width, length, mines);
        }
       
        //Creates the display window
        Display display = new Display(game_board);
        JFrame frame = new JFrame("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(display);
        frame.setVisible(true);
        frame.pack();
        frame.setAlwaysOnTop(true);
    }
}
