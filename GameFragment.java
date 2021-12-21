package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GameFragment extends Fragment {
    private static final String TAG = "GameFragment";
    private static final int GRID_SIZE = 9;

    private final Button[] mButtons = new Button[GRID_SIZE];
    private TextView txtHelper;
    private String mGameType;
    private String mGameKey;
    private boolean mIsCurrPlayer;
    private boolean mIsFirstPlayer;

    private NavController mNavController;
    private FirebaseAuth mAuth;
    private DatabaseReference mDbUsers;
    private DatabaseReference mDbGames;
    private DatabaseReference mCurrGame;

    private GameViewModel mGameViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Needed to display the action menu for this fragment

        mAuth = FirebaseAuth.getInstance();
        mDbUsers = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        mDbGames = FirebaseDatabase.getInstance().getReference().child("games");

        mGameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);

        // Extract the argument passed with the action in a type-safe way
        GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
        mGameType = args.getGameType();
        mGameKey = args.getGameKey();

        if (mGameType.equals(getString(R.string.two_player))) {
            if (mGameKey == null) {
                mGameKey = mDbGames.push().getKey();
                mCurrGame = mDbGames.child(mGameKey).getRef();
                mCurrGame.child("p1").setValue(mAuth.getCurrentUser().getUid());
                mCurrGame.child("p1_email").setValue(mAuth.getCurrentUser().getEmail());

                List<Integer> tempList = new ArrayList<>();
                for (int i = 0; i < 9; ++i) {
                    tempList.add(0);
                }
                mCurrGame.child("grid").setValue(tempList);
                mCurrGame.child("currPlayer").setValue(1);
                mCurrGame.child("status").setValue("n");
                mIsFirstPlayer = true;
            } else {
                mCurrGame = mDbGames.child(mGameKey).getRef();
                mCurrGame.child("p2").setValue(mAuth.getCurrentUser().getUid());
                mCurrGame.child("status").setValue("y");
                mIsFirstPlayer = false;
            }
        }

        // Handle the back press by adding a confirmation dialog
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back pressed");

                AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.forfeit_game_dialog_message)
                        .setPositiveButton(R.string.yes, (d, which) -> {
                            mDbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        snapshot.getRef().setValue((long) snapshot.getValue() - 1);
                                        mCurrGame.child("p2").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.getValue() == null) {
                                                    mCurrGame.child("status").setValue("invalid");
                                                } else {
                                                    mCurrGame.child("status").setValue("w" + (mIsFirstPlayer ? 2 : 1));
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            mNavController.popBackStack();
                        })
                        .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                        .create();
                dialog.show();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNavController = Navigation.findNavController(view);

        txtHelper = view.findViewById(R.id.txt_helper);

        mButtons[0] = view.findViewById(R.id.button0);
        mButtons[1] = view.findViewById(R.id.button1);
        mButtons[2] = view.findViewById(R.id.button2);

        mButtons[3] = view.findViewById(R.id.button3);
        mButtons[4] = view.findViewById(R.id.button4);
        mButtons[5] = view.findViewById(R.id.button5);

        mButtons[6] = view.findViewById(R.id.button6);
        mButtons[7] = view.findViewById(R.id.button7);
        mButtons[8] = view.findViewById(R.id.button8);

        if (mGameType.equals(getString(R.string.one_player))) {
            txtHelper.setText(R.string.your_turn);
        } else {
            mCurrGame.child("currPlayer").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        boolean currTemp = isCurrPlayer((Long) snapshot.getValue());
                        toggleButtons(currTemp);
                        mIsCurrPlayer = currTemp;
                        txtHelper.setText(currTemp ? R.string.your_turn : R.string.players_turn);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            mCurrGame.child("grid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Integer> tempGrid = new ArrayList<>();
                    for (DataSnapshot singleSnapShot : snapshot.getChildren()) {
                        if (singleSnapShot.getValue() != null)
                            tempGrid.add(((Long) singleSnapShot.getValue()).intValue());
                    }

                    mGameViewModel.setGrid(tempGrid);

                    if (mGameViewModel.getGrid().size() != 0) {
                        for (int i = 0; i < 9; ++i) {
                            int tile = mGameViewModel.getGrid().get(i);

                            String tileTxt = "";
                            if (tile == 1) tileTxt = "X";
                            else if (tile == 2) tileTxt = "O";

                            mButtons[i].setText(tileTxt);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            mCurrGame.child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        if (snapshot.getValue().equals("t")) {
                            tieHandler();
                        }
                        if (mIsFirstPlayer && snapshot.getValue().equals("l1")) {
                            loseHandler();
                        }
                        if (!mIsFirstPlayer && snapshot.getValue().equals("l2")) {
                            loseHandler();
                        }
                        if (mIsFirstPlayer && snapshot.getValue().equals("w1")) {
                            winHandler();
                        }
                        if (!mIsFirstPlayer && snapshot.getValue().equals("w2")) {
                            winHandler();
                        }
                        if (snapshot.getValue().equals("n")) {
                            txtHelper.setText(R.string.waiting_for_player);
                            toggleButtons(false);
                        }
                        if (snapshot.getValue().equals("y")) {
                            toggleButtons(mIsCurrPlayer);
                            txtHelper.setText(mIsCurrPlayer ? R.string.your_turn : R.string.players_turn);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        for (int i = 0; i < mButtons.length; i++) {
            int finalI = i;

            mButtons[i].setOnClickListener(v -> {

                Log.d(TAG, "Button " + finalI + " clicked");

                if (mGameType.equals(getString(R.string.one_player))) {
                    switch (mGameViewModel.move(finalI, true)) {
                        case VALID:
                            validMoveSinglePlayer(finalI);
                            break;
                        case WIN:
                            winHandler();
                            break;
                        case INVALID:
                            txtHelper.setText(R.string.invalid_move);
                            break;
                    }
                } else {
                    switch (mGameViewModel.move(finalI, mIsFirstPlayer)) {
                        case VALID:
                            validMoveTwoPlayer(finalI);
                            break;
                        case WIN:
                            mCurrGame.child("status").setValue("l" + (mIsFirstPlayer ? 2 : 1));
                            winHandler();
                            break;
                        case INVALID:
                            txtHelper.setText(R.string.invalid_move);
                            break;
                        case TIE:
                            mCurrGame.child("status").setValue("t");
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logout, menu);
        // this action menu is handled in MainActivity
    }

    /**
     * check if it is the player's turn
     * @param num player number: 1 / 2
     * @return true/false
     */
    private boolean isCurrPlayer(Long num) {
        return (mIsFirstPlayer && num == 1) || (!mIsFirstPlayer && num == 2);
    }

    /**
     * steps to follow for a valid move in a single player game
     * @param gridIndex the index of the tile selected by the player
     */
    private void validMoveSinglePlayer(int gridIndex) {
        Log.d(TAG, "Valid Move");
        mButtons[gridIndex].setText("X");
        toggleButtons(false);

        // Computer's turn
        txtHelper.setText(R.string.computers_turn);

        // Wait for 0.5 sec to complete computer's move
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                List<Integer> grid = mGameViewModel.getGrid();
                List<Integer> unmarkedTiles = new ArrayList<>();

                for (int i = 0; i < 9; ++i) {
                    if (grid.get(i) == 0) unmarkedTiles.add(i);
                }

                if (unmarkedTiles.size() == 0) {
                    Log.d(TAG, "Game tied");
                    tieHandler();
                } else {

                    int randIndex = (int) (Math.random() * (unmarkedTiles.size()));
                    switch (mGameViewModel.move(unmarkedTiles.get(randIndex), false)) {
                        case WIN:
                            loseHandler();
                            break;

                        case VALID:
                            requireActivity().runOnUiThread(() -> mButtons[unmarkedTiles.get(randIndex)].setText("O"));
                            break;

                        default:
                            Log.d(TAG, unmarkedTiles.toString());
                            Log.d(TAG, String.valueOf(randIndex));
                    }

                    requireActivity().runOnUiThread(() -> {
                        txtHelper.setText(R.string.your_turn);
                        toggleButtons(true);
                    });
                }
            }
        }, 500);
    }

    /**
     * steps to follow for a valid move in a two player game
     * @param gridIndex the index of the tile selected by the player
     */
    private void validMoveTwoPlayer(int gridIndex) {
        mCurrGame.child("grid").setValue(mGameViewModel.getGrid());
        mCurrGame.child("currPlayer").setValue(mIsFirstPlayer ? 2 : 1);
    }

    /**
     * increment score and show win dialog
     */
    private void winHandler() {
        mDbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                    snapshot.getRef().setValue((long) snapshot.getValue() + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        showDialog("Hurray", "You win :D");
    }

    /**
     * decrement score and show lose dialog
     */
    private void loseHandler() {
        mDbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                    snapshot.getRef().setValue((long) snapshot.getValue() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        showDialog("Game Over", "You were unable to mark 3 in a row.\nYou lose :(");
    }

    /**
     * show tie dialog
     */
    private void tieHandler() {
        showDialog("Game Over", "No player was able to mark 3 in a row. The game ends in a tie!");
    }

    /**
     * disable/enable grid buttons
     * @param enable enable/disable acc to the value
     */
    public void toggleButtons(boolean enable) {
        for (int i = 0; i < 9; ++i) {
            mButtons[i].setEnabled(enable);
        }
    }

    /**
     * show dialog with title and message which takes back to the dashboard
     * @param title the title for dialog
     * @param message the description for dialog
     */
    public void showDialog(String title, String message) {
        mGameViewModel.reset();
        requireActivity().runOnUiThread(() -> new AlertDialog.Builder(requireActivity())
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("OK", (d, which) -> mNavController.popBackStack())
                .create()
                .show());
    }
}