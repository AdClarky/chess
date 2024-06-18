package chessboard;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * A regular chess board.
 * Only method for interaction is the moveWithValidation function.
 * At the end of a move boardChanged is called.
 * Can use getMoves to find the individual moves made - e.g. in castling what moves were made.
 */
public class Board {
    private final Collection<BoardListener> boardListeners = new ArrayList<>(1);
    private int currentTurn = Piece.WHITE_PIECE;
    private final Piece[][] board  =  new Piece[8][8];
    private Iterable<Move> movesMade;
    private final ArrayList<Piece> blackPieces = new ArrayList<>(16);
    private final ArrayList<Piece> whitePieces = new ArrayList<>(16);
    private final Stack<TempMove> tempMoves = new Stack<>();
    private final Stack<TempMove> redoMoves = new Stack<>();
    private Pawn lastPawn;

    /**
     * Initialises the board with the pieces in default positions.
     * Blank squares are null.
     */
    public Board(){
        whitePieces.add(new King(3, 0, Piece.WHITE_PIECE));
        whitePieces.add(new Rook(0, 0, Piece.WHITE_PIECE));
        whitePieces.add(new Knight(1, 0, Piece.WHITE_PIECE));
        whitePieces.add(new Bishop(2, 0, Piece.WHITE_PIECE));
        whitePieces.add(new Queen(4, 0, Piece.WHITE_PIECE));
        whitePieces.add(new Bishop(5, 0, Piece.WHITE_PIECE));
        whitePieces.add(new Knight(6, 0, Piece.WHITE_PIECE));
        whitePieces.add(new Rook(7, 0, Piece.WHITE_PIECE));
        blackPieces.add(new King(3, 7, Piece.BLACK_PIECE));
        blackPieces.add(new Rook(0, 7, Piece.BLACK_PIECE));
        blackPieces.add(new Knight(1, 7, Piece.BLACK_PIECE));
        blackPieces.add(new Bishop(2, 7, Piece.BLACK_PIECE));
        blackPieces.add(new Queen(4, 7, Piece.BLACK_PIECE));
        blackPieces.add(new Bishop(5, 7, Piece.BLACK_PIECE));
        blackPieces.add(new Knight(6, 7, Piece.BLACK_PIECE));
        blackPieces.add(new Rook(7, 7, Piece.BLACK_PIECE));
        for(int x = 0; x < 8; x++){
            blackPieces.add(new Pawn(x, 6, Piece.BLACK_PIECE));
            whitePieces.add(new Pawn(x, 1, Piece.WHITE_PIECE));
        }
        for(int y = 2; y < 6; y++){
            for(int x = 0; x < 8; x++){
                board[y][x] = new Blank(x, y);
            }
        }
        for(Piece piece : blackPieces){
            board[piece.getY()][piece.getX()] = piece;
        }
        for(Piece piece : whitePieces){
            board[piece.getY()][piece.getX()] = piece;
        }
    }

    /**
     * Finds the piece in a specific square
     * @param x x position
     * @param y y position
     * @return the piece on that square.
     */
    @NotNull
    public Piece getPiece(int x, int y){
        if(x < 0 || x >= 8 || y < 0 || y >= 8)
            return new Blank(x, y);
        return board[y][x];
    }

    /**
     * Checks if a square is blank or contains a piece
     * @param x x value of square to check
     * @param y y value of square to check
     * @return if the square is blank
     */
    public boolean isSquareBlank(int x, int y){return board[y][x] instanceof Blank;}

    public int getCurrentTurn(){return currentTurn;}

    /**
     * Calculates if moving a piece to a position would put that teams king in check.
     * This assumes the piece moving to the new position is a valid move.
     * @param newX the x position to move to.
     * @param newY the y position to move to.
     * @param pieceToCheck the piece being moved.
     * @return true if in check, false if not
     */
    public boolean isMoveSafe(int newX, int newY, Piece pieceToCheck){
        TempMove tempMove = new TempMove(newX, newY, pieceToCheck, this);
        boolean inCheck = isKingInCheck(pieceToCheck.getDirection());
        tempMove.undo();
        return inCheck;
    }

    public boolean isKingInCheck(int direction){
        King king = (King) getColourPieces(direction).getFirst();
        Coordinate kingPos = new Coordinate(king.getX(), king.getY());
        Iterable<Piece> enemyPieces = getColourPieces(direction * -1);
        for(Piece piece : enemyPieces){
            if(piece.getPossibleMoves(this).contains(kingPos)){
                return true;
            }
        }
        return false;
    }

    /**
     * Moves a piece to a new location while validating it is a valid move.
     * Assumes the provided old coordinates are valid coordinates for a piece.
     * After a move has been made, it notifies all listeners then moves on to the next turn.
     * @param oldX current x position of the piece
     * @param oldY current y position of the piece
     * @param newX new x position of the piece
     * @param newY new y position of the piece
     */
    public void moveWithValidation(int oldX, int oldY, int newX, int newY){
        Piece piece = getPiece(oldX, oldY);
        if(!piece.getPossibleMoves(this).contains(new Coordinate(newX, newY))) // if invalid move
            return;
        movesMade = piece.getMoves(newX, newY, this);
        tempMoves.add(new TempMove(newX,newY,board[oldY][oldX],this));
        piece.firstMove(); // if a piece has a first move constraint e.g. pawn, rook, king
        if(lastPawn != null)
            lastPawn.setCanBePassanted(false);
        if(piece instanceof Pawn pawn) {
            lastPawn = pawn;
        }
        notifyBoardChanged(oldX, oldY, newX, newY);
        nextTurn();
        if(isCheckmate()) {
            King king = (King) getColourPieces(currentTurn).getFirst();
            notifyCheckmate(king.getX(), king.getY());
        }
    }

    public void moveWithValidation(@NotNull Move move){
        moveWithValidation(move.oldX(), move.oldY(), move.newX(), move.newY());
    }

    /**
     * Checks all possible moves that can be made and if any result in non-check.
     * @return if in checkmate.
     */
    public boolean isCheckmate(){
        ArrayList<Piece> enemyPieces = getColourPieces(currentTurn);
        for(int i = 0; i < enemyPieces.size(); i++){
            Piece piece = enemyPieces.get(i);
            for(Coordinate move : piece.getPossibleMoves(this)){
                if(!isMoveSafe(move.x(), move.y(), piece))
                    return false;
            }
        }
        return true;
    }

    void setSquare(int x, int y, Piece piece){board[y][x] = piece;}

    ArrayList<Piece> getColourPieces(int direction){
        if(direction == Piece.BLACK_PIECE)
            return blackPieces;
        return whitePieces;
    }

    List<Piece> getColourPieces(Piece piece){
        if(piece.getDirection() == Piece.BLACK_PIECE)
            return blackPieces;
        return whitePieces;
    }

    private void nextTurn(){
        currentTurn = currentTurn == Piece.WHITE_PIECE ? Piece.BLACK_PIECE : Piece.WHITE_PIECE;
    }

    /**
     * During a board change event, contains the moves performed on the board.
     * @return a list of individual moves taken to reach the new board state.
     */
    public Iterable<Move> getMovesMade(){return movesMade;}

    /**
     * Adds a board listener to receive events from this board.
     * @param listener the board listener
     */
    public void addBoardListener(BoardListener listener){
        boardListeners.add(listener);
    }

    private void notifyBoardChanged(int oldX, int oldY, int newX, int newY){
        for(BoardListener listener : boardListeners){
            listener.boardChanged(oldX, oldY, newX, newY);
        }
    }

    private void notifyCheckmate(int x, int y){
        for(BoardListener listener : boardListeners)
            listener.checkmate(x, y);
    }
}
