import chessboard.Board;
import chessboard.BoardListener;
import chessboard.Coordinate;
import chessboard.Move;
import chessboard.Piece;
import org.jetbrains.annotations.NotNull;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Window which links with chessboard package to display a chess board.
 * Click once to select, click again to either deselect or move.
 * Member of BoardListener and is updated when boardChanged is called.
 */
public class GameWindow extends JFrame implements BoardListener, MouseListener {
    private static final Color LIGHT_SQUARE = new Color(180, 180, 180);
    private static final Color DARK_SQUARE = new Color(124, 124, 124);
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private final Square[][] squares = new Square[8][8];
    private Square squareSelected;
    private final Board board;
    private Collection<Coordinate> possibleMoves = new ArrayList<>(8);

    /**
     * Creates a new window and populates it with squares which icons are set based on the board input.
     * @param board the board to be linked with.
     */
    public GameWindow(Board board){
        super();
        this.board = board;
        setLayout(new GridLayout(8,8));
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        Color currentColour = LIGHT_SQUARE;
        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                Piece piece = board.getPiece(x, y);
                squares[y][x] = new Square(piece, currentColour, x, y);
                squares[y][x].addMouseListener(this);
                add(squares[y][x]);
                currentColour = (currentColour == LIGHT_SQUARE) ? DARK_SQUARE : LIGHT_SQUARE;
            }
            currentColour = (currentColour == LIGHT_SQUARE) ? DARK_SQUARE : LIGHT_SQUARE;
        }
        setVisible(true);
    }

    /**
     * Run when a square is clicked.
     * Window tracks which square is currently highlighted.
     * Logic is handled here for clicking e.g. clicked on an enemy square etc
     * @param square the square which has been clicked
     */
    private void squareClicked(@NotNull Square square){
        if(squareSelected == null && square.isBlank())
            return;
        Piece piece = square.getCurrentPiece();
        if(square.isBlank() || piece.getDirection() != board.getCurrentTurn()){ // if clicked a blank or enemy square
            Piece pieceSelected = squareSelected.getCurrentPiece();
            board.moveWithValidation(pieceSelected.getX(), pieceSelected.getY(), square.getBoardX(), square.getBoardY());
            unselectSquare();
        } else { // if the player's piece
            unselectSquare();
            showPossibleMoves(piece);
            squareSelected = square;
            squareSelected.selected();
        }
    }

    /**
     * Unselects the selected square tracked by {@link #squareSelected}.
     * Also, unhighlights any possible moves.
     */
    private void unselectSquare(){
        if(squareSelected == null)
            return;
        squareSelected.unhighlight();
        squareSelected = null;
        unhighlightPossibleMoves();
    }

    /**
     * Unhighlights the previously highlighted squares then highlights the new possible squares.
     * @param piece the piece whose possible moves will be calculated
     */
    private void showPossibleMoves(@NotNull Piece piece) {
        unhighlightPossibleMoves();
        possibleMoves = piece.getPossibleMoves(board);
        for(Coordinate move : possibleMoves){
            squares[move.y()][move.x()].showPossibleMove();
        }
    }

    /**
     * Removes the highlighting of possible move squares.
     */
    private void unhighlightPossibleMoves(){
        for (Coordinate move : possibleMoves) { // remove old possible moves
            squares[move.y()][move.x()].unhighlight();
        }
        possibleMoves.clear();
    }

    @Override
    public void boardChanged(int oldX, int oldY, int newX, int newY) {
        for(Move move : board.getMovesMade()) {
            squares[move.oldY()][move.oldX()].setCurrentPiece(board.getPiece(move.oldX(), move.oldY()));
            squares[move.newY()][move.newX()].setCurrentPiece(board.getPiece(move.newX(), move.newY()));
        }
    }

    @Override
    public void checkmate(int x, int y) {
        squares[y][x].showPossibleMove();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(!(e.getSource() instanceof Square clickedSquare))
            return;
        squareClicked(clickedSquare);
    }
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {

    }
    @Override
    public void mouseEntered(MouseEvent e) {

    }
    @Override
    public void mouseExited(MouseEvent e) {

    }
}
