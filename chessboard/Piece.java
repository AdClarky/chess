package chessboard;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract class which parents all chess pieces.
 */
public abstract class Piece {
    /**
     * Flag for a piece being white
     */
    public static final int WHITE_PIECE = 1;
    /**
     * Flag for a piece being black
     */
    public static final int BLACK_PIECE = -1;
    private final Icon pieceIcon;
    protected int x;
    protected int y;
    protected int direction;

    /**
     * Initialises the position and whether the piece is white or black
     * @param x starting x value
     * @param y starting y value
     * @param pieceIcon image of the piece
     * @param direction value should be the flag {@link #BLACK_PIECE} or {@link #WHITE_PIECE}
     */
    protected Piece(int x, int y, Icon pieceIcon, int direction) {
        this.x = x;
        this.y = y;
        this.pieceIcon = pieceIcon;
        this.direction = direction;
    }

    /**
     * Calculates all possible moves based on surrounding pieces and checks.
     * @param board the board which the piece is on
     * @return a list of coordinates the piece can move to.
     */
    public abstract List<Coordinate> getPossibleMoves(Board board);

    /**
     * For pieces where the first move must be tracked.
     * Implementations in Pawn, King and Rook.
     */
    public abstract void firstMove();

    public ArrayList<Move> getMoves(int newX, int newY, Board board) {
        ArrayList<Move> moves = new ArrayList<>(1);
        moves.add(new Move(x, y, newX, newY));
        return moves;
    }

    @Override
    public abstract String toString();

    public Icon getPieceIcon() {return pieceIcon;}

    public int getDirection() {return direction;}


    public void setX(int x) {this.x = x;}

    public void setY(int y) {this.y = y;}
    public int getX() {return x;}
    public int getY() {return y;}

    /**
     * Calculates if a move to a specific square is valid.
     * Validates the coords are within the board and then checks if it's a friendly piece.
     * @param x new x position
     * @param y new y position
     * @param board the board this piece is on
     * @param moves a collection of possible moves
     * @return if the move is valid
     */
    protected boolean cantMove(int x, int y, Board board, Collection<Coordinate> moves) {
        if(x < 0 || x >= 8 || y < 0 || y >= 8)
            return false;
        Piece piece = board.getPiece(x, y);
        if(piece != null && piece.getDirection() == direction){ // if a friendly piece
            return true;
        }
        moves.add(new Coordinate(x, y));
        return false;
    }

    /**
     * Removes moves which would put the king in check.
     * @param board the board this piece is on
     * @param moves the possible moves not considering checks
     */
    protected void removeMovesInCheck(Board board, Collection<Coordinate> moves) {
        if(board.getCurrentTurn() != direction)
            return;
        moves.removeIf(move -> board.isInCheck(move.getX(), move.getY(), this));
    }

    /**
     * Calculates how far a piece can move in each diagonal direction.
     * @param board the board being worked on
     * @param moves a list of possible moves
     */
    protected void calculateDiagonalMoves(Board board, Collection<Coordinate> moves){
        for(int x = this.x+1, y = this.y+1; x < 8 && x>=0 && y>=0 && y < 8; x++, y++) {
            if(cantMove(x, y, board, moves))
                break;
        }
        for(int x = this.x-1, y = this.y-1; x < 8 && x>=0 && y>=0 && y < 8; x--, y--) {
            if(cantMove(x, y, board, moves))
                break;
        }
        for(int x = this.x+1, y = this.y-1; x < 8 && x>=0 && y>=0 && y < 8; x++, y--) {
            if(cantMove(x, y, board, moves))
                break;
        }
        for(int x = this.x-1, y = this.y+1; x < 8 && x>=0 && y>=0 && y < 8; x--, y++) {
            if(cantMove(x, y, board, moves))
                break;
        }
    }

    /**
     * Calculates how far a piece can move in each straight direction.
     * @param board the board being worked on
     * @param moves a list of possible moves
     */
    protected void calculateStraightMoves(Board board, Collection<Coordinate> moves) {
        for(int x = this.x+1; x < 8 && x >= 0; x++){
            if(cantMove(x, y, board, moves))
                break;
        }
        for(int x = this.x-1; x < 8 && x >= 0; x--){
            if(cantMove(x, y, board, moves))
                break;
        }
        for(int y = this.y+1; y < 8 && y >= 0; y++){
            if(cantMove(x, y, board, moves))
                break;
        }
        for(int y = this.y-1; y < 8 && y >= 0; y--){
            if(cantMove(x, y, board, moves))
                break;
        }
    }
}