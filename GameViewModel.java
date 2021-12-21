package androidsamples.java.tictactoe;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class GameViewModel extends ViewModel {

    private List<Integer> mGrid;

    public GameViewModel() {
        reset();
    }

    public List<Integer> getGrid() {
        return mGrid;
    }

    public void setGrid(List<Integer> grid) {
        this.mGrid = grid;
    }

    /**
     * reset the view model for new game
     */
    public void reset() {
        // Initialize empty Tic Tac Grid
        mGrid = new ArrayList<>();
        for (int i = 0; i < 9; ++i) {
            mGrid.add(0);
        }
    }

    /**
     * check if the player has won by checking three consecutive marked tiles
     * @return true/false depending on if the user has won
     */
    private boolean check() {
        // Check columns
        for (int i = 0; i < 9; i += 3) {
            if (mGrid.get(i).equals(mGrid.get(i+1)) && mGrid.get(i+1).equals(mGrid.get(i+2)) && mGrid.get(i+1) != 0) {
                return true;
            }
        }

        // Check rows
        for (int i = 0; i < 3; ++i) {
            if (mGrid.get(i).equals(mGrid.get(i+3)) && mGrid.get(i+3).equals(mGrid.get(i+6)) && mGrid.get(i+3) != 0) {
                return true;
            }
        }

        // Check diagonal 1
        if (mGrid.get(0).equals(mGrid.get(4)) && mGrid.get(4).equals(mGrid.get(8)) && mGrid.get(4) != 0) {
            return true;
        }

        // Check diagonal 2
        if (mGrid.get(2).equals(mGrid.get(4)) && mGrid.get(4).equals(mGrid.get(6)) && mGrid.get(4) != 0) {
            return true;
        }

        return false;
    }

    /**
     * mark the tile in grid
     * @param gridIndex the index of tile to be marked
     * @param isFirstPlayer player number for deciding whether to mark X or O
     * @return type of move: Valid (Tile marked), Win (Player wins), Tie (Game tied), Invalid (Tile already filled)
     */
    public MoveType move(int gridIndex, boolean isFirstPlayer) {
        if (mGrid.get(gridIndex) == 0) {
            mGrid.set(gridIndex, isFirstPlayer ? 1 : 2);

            // If the player marks 3 continuous tiles, they win.
            if (check()) return MoveType.WIN;

            boolean isFilled = true;
            for (int i = 0; i < 9; ++i) {
                if (mGrid.get(i) == 0) {
                    isFilled = false;
                    break;
                }
            }

            // If the grid is filled, the game ends in a tie.
            if (isFilled) {
                return MoveType.TIE;
            }

            return MoveType.VALID;
        }

        boolean isNotFilled = false;
        for (int i = 0; i < 9; ++i) {
            if (mGrid.get(i) == 0) {
                isNotFilled = true;
                break;
            }
        }

        // If the player clicks on a marked tile, the move in invalid.
        if (isNotFilled) return MoveType.INVALID;

        // If the whole grid is marked, the game ends in a draw.
        return MoveType.TIE;
    }

}
