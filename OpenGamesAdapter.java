package androidsamples.java.tictactoe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private List<GameEntry> mEntries;

    public OpenGamesAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (mEntries != null) {
            GameEntry current = mEntries.get(position);
            holder.bind(current);
        }
    }

    @Override
    public int getItemCount() {
        return (mEntries == null) ? 0 : mEntries.size();
    }

    /**
     * set the list of games for recyclerview
     * @param entries the list of active games
     */
    public void setEntries(List<GameEntry> entries) {
        mEntries = entries;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mEmailView;
        public String mGameKey;

        public ViewHolder(View view) {
            super(view);
            mEmailView = view.findViewById(R.id.email);

            view.setOnClickListener(this::launchGame);
        }

        private void launchGame(View view) {
            DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections
                    .actionGame(view.getContext().getString(R.string.two_player), mGameKey);

            Navigation.findNavController(view).navigate(action);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mEmailView.getText() + "'";
        }

        /**
         * bind the view holder to data
         * @param entry list item for game
         */
        public void bind(GameEntry entry) {
            this.mEmailView.setText(entry.getEmail());
            this.mGameKey = entry.getGameKey();
        }
    }
}