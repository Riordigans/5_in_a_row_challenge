package Server;

import java.lang.StringBuilder;
import java.util.Arrays;

/*
Class Game
This class provides methods that implement the core rules of the game
*/
public class Game {
    // Specify number of columns and rows in the game
    // Could potentially make this user configurable
    private final int numCols = 9;
    private final int numRows = 6;
    int moveCol = -1;
    int moveRow = -1;

    // Array to track number of pieces in column
    private int[] piecesInCol = new int[numCols];

    // Matrix representation of game board
    private char[][] grid = new char[numCols][numRows];

    private static final char[] players = { 'x', 'o' };

    // Constructor for the Game class
    public Game() {
        for (int i = 0; i < numCols; i++) {
            // Initialise empty game board
            Arrays.fill(grid[i] = new char[numRows], ' ');
        }
    }

    // Return game board as String
    public String toString() {
        StringBuilder gb = new StringBuilder();
        for (int c = grid[0].length - 1; c >= 0; c--) {
            for (int r = 0; r < grid.length; r++) {
                gb.append("[" + grid[r][c] + "]");
            }
            gb.append("\r\n");
        }
        String gameBoard = gb.toString();
        return gameBoard;
    }

    public void newMove(int playerNo, int column) throws InvalidMoveException {
        if (column > numCols || column < 1) {
            // Player has attempted to put a piece in an invalid column
            throw new InvalidMoveException("Invalid Move: That column does not exist!");
        }
        if (piecesInCol[column - 1] >= numRows) {
            // Player has attempted to put piece in a full column
            throw new InvalidMoveException("Invalid Move: That column is already full!");
        } else {
            // Player has made a valid move
            // Record location of players piece
            moveCol = column - 1;
            moveRow = piecesInCol[column - 1];
            // Add piece to board
            grid[moveCol][moveRow] = players[playerNo - 1];
            // Increment number of pieces in specified column
            piecesInCol[column - 1]++;
        }
    }

    // Check if a player has won the game
    public boolean checkVictory(int playerNo) {
        if (moveCol == -1){
            return false;
        }
        // Retrieve player symbol
        char symbol = players[playerNo - 1];
        // Construct string of characters that appear if player has won
        String victoryCondition = String.format("%c%c%c%c%c", symbol, symbol, symbol, symbol, symbol);
        // Check victory conditions. True is returned if player has won.
        if (checkCol(moveCol, victoryCondition))
            return true;
        else if (checkRow(moveRow, victoryCondition))
            return true;
        else if (checkFwdDiag(moveRow, moveCol, victoryCondition))
            return true;
        else if (checkBkwdDiag(moveRow, moveCol, victoryCondition))
            return true;
        else
            return false;
    }

    //Check if the board is full
    public boolean boardFull(){
        for (int i = 0; i < numCols; i++){
            if (grid[i][numRows - 1] == ' '){
                return false;
            }
        }
        return true;
    }

    // Check for five pieces arranged vertically
    private boolean checkCol(int moveCol, String victoryCondition) {
        String colContents = new String(grid[moveCol]);

        if (colContents.contains(victoryCondition)) {
            return true;
        } else {
            return false;
        }
    }

    // Check for five pieces arranged horizontally
    private boolean checkRow(int moveRow, String victoryCondition) {
        StringBuilder rowCon = new StringBuilder(numCols);

        for (int col = 0; col < numCols; col++) {
            rowCon.append(grid[col][moveRow]);
        }

        String rowContents = rowCon.toString();
        if (rowContents.contains(victoryCondition)) {
            return true;
        } else {
            return false;
        }
    }

    // Check for five pieces arranged diagonally
    private boolean checkFwdDiag(int moveRow, int moveCol, String victoryCondition) {
        // Forward slash (/) diagonal
        StringBuilder fwdDiag = new StringBuilder(numCols);

        int startCol = moveCol - moveRow;
        if (startCol < 0){
            startCol = 0;
        }

        int rowNum = moveRow - moveCol;
        if (rowNum < 0){
            rowNum = 0;
        }

        int endCol = startCol + ((numRows - 1) - rowNum);
        if (endCol > (numCols - 1)){
            endCol = (numCols - 1);
        }

        for (int col = startCol; col <= endCol; col++) {
            fwdDiag.append(grid[col][rowNum]);
            rowNum++;
        }

        String fwdDiagContents = fwdDiag.toString();

        if (fwdDiagContents.contains(victoryCondition)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkBkwdDiag(int moveRow, int moveCol, String victoryCondition) {
        // Back slash (\) diagonal
        StringBuilder bkwdDiag = new StringBuilder(numCols);

        int endCol = moveCol + moveRow;
        if (endCol > (numCols - 1)){
            endCol = (numCols - 1);
        }

        int rowNum = moveRow + moveCol;
        if (rowNum > (numRows - 1)){
            rowNum = (numRows - 1);
        }

        int startCol = endCol - rowNum;
        if (startCol < 0){
            startCol = 0;
        }

        for (int col = startCol; col <= endCol; col++) {
            bkwdDiag.append(grid[col][rowNum]);
            rowNum--;
        }

        String bkwdDiagContents = bkwdDiag.toString();

        if (bkwdDiagContents.contains(victoryCondition)) {
            return true;
        } else {
            return false;
        }
    }
}