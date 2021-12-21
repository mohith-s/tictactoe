package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private NavController mNavController;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private FloatingActionButton mNewGameBtn;
    private TextView mTxtScore;
    private RecyclerView mRecyclerView;
    private OpenGamesAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DashboardFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Needed to display the action menu for this fragment

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mNewGameBtn = v.findViewById(R.id.fab_new_game);
        mTxtScore = v.findViewById(R.id.txt_score);
        mRecyclerView = v.findViewById(R.id.list);

        mAdapter = new OpenGamesAdapter(getActivity());

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavController = Navigation.findNavController(view);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_need_auth);
        } else {
            mDatabase.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String scoreStr = "Score: ";
                    if (snapshot.getValue() == null) {
                        snapshot.getRef().setValue(0);
                        scoreStr += "0";
                    } else {
                        scoreStr += snapshot.getValue();
                    }

                    mTxtScore.setText(scoreStr);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mAdapter);

            mDatabase.child("games").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<GameEntry> mEntryList = new ArrayList<>();
                    for (DataSnapshot singleSnapShot : snapshot.getChildren()) {
                        Log.d(TAG, singleSnapShot.toString());
                        if (singleSnapShot.getValue() != null) {
                            if (singleSnapShot.child("status").getValue() != null && singleSnapShot.child("status").getValue().equals("invalid")) {
                                singleSnapShot.getRef().removeValue();
                            } else if (singleSnapShot.child("p2").getValue() == null) {
                                    GameEntry entry = new GameEntry(singleSnapShot.getKey(), (String) singleSnapShot.child("p1_email").getValue());
                                    mEntryList.add(entry);
                            }
                        }
                    }

                    mAdapter.setEntries(mEntryList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        // Show a dialog when the user clicks the "new game" button
        mNewGameBtn.setOnClickListener(v -> {

            // A listener for the positive and negative buttons of the dialog
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                String gameType = "No type";
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    gameType = getString(R.string.two_player);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    gameType = getString(R.string.one_player);
                }
                Log.d(TAG, "New Game: " + gameType);

                // Passing the game type as a parameter to the action
                // extract it in GameFragment in a type safe way
                NavDirections action = DashboardFragmentDirections.actionGame(gameType, null);
                mNavController.navigate(action);
            };

            // create the dialog
            AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.new_game)
                    .setMessage(R.string.new_game_dialog_message)
                    .setPositiveButton(R.string.two_player, listener)
                    .setNegativeButton(R.string.one_player, listener)
                    .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
                    .create();
            dialog.show();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logout, menu);
        // this action menu is handled in MainActivity
    }
}