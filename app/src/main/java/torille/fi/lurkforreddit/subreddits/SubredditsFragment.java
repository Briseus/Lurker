package torille.fi.lurkforreddit.subreddits;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import torille.fi.lurkforreddit.MyApplication;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.subreddit.SubredditActivity;

/**
 * Created by eva on 2/8/17.
 */

public class SubredditsFragment extends Fragment implements SubredditsContract.View {

    private SubredditsComponent subredditsComponent;

    @Inject
    SubredditsContract.Presenter<SubredditsContract.View> mActionsListener;

    private SubredditsAdapter mListAdapter;

    public static SubredditsFragment newInstance() {
        return new SubredditsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        subredditsComponent = DaggerSubredditsComponent.builder()
                .redditRepositoryComponent(((MyApplication) getActivity().getApplication()).getmRedditRepositoryComponent())
                .build();

        mListAdapter = new SubredditsAdapter(new ArrayList<Subreddit>(20), mItemListener, ContextCompat.getColor(getContext(), R.color.colorAccent));
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionsListener.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActionsListener.dispose();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_subreddits, container, false);
        subredditsComponent.inject(this);
        mActionsListener.setView(this);

        Context context = getContext();

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.subreddits_list);
        recyclerView.setAdapter(mListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Pull-to-refresh
        SwipeRefreshLayout swipeRefreshLayout =
                (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mActionsListener.loadSubreddits(true);
            }
        });
        return root;
    }

    /**
     * Listener for clicks on notes in the RecyclerView.
     */
    private final SubredditItemListener mItemListener = new SubredditItemListener() {
        @Override
        public void onSubredditClick(Subreddit subreddit) {
            mActionsListener.openSubreddit(subreddit);
        }
    };

    @Override
    public void setProgressIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showSubreddits(List<Subreddit> subreddits) {
        mListAdapter.replaceData(subreddits);
    }

    @Override
    public void loadSelectedSubreddit(Subreddit subreddit) {
        Intent intent = new Intent(getContext(), SubredditActivity.class);
        intent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit);
        startActivity(intent);
    }

    @Override
    public void onError(String errorText) {
        Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
    }

    private static class SubredditsAdapter extends RecyclerView.Adapter<SubredditsAdapter.ViewHolder> {

        private List<Subreddit> mSubreddits;
        private SubredditItemListener mItemListener;
        private final ColorStateList mDefaultColor;

        SubredditsAdapter(List<Subreddit> subreddits, SubredditItemListener itemListener, int color) {
            setList(subreddits);
            mItemListener = itemListener;
            mDefaultColor = ColorStateList.valueOf(color);
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View subredditsView = inflater.inflate(R.layout.item_subreddit, parent, false);
            return new ViewHolder(subredditsView, mItemListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Subreddit subreddit = mSubreddits.get(position);
            String keyColor = subreddit.keyColor();
            viewHolder.title.setText(subreddit.displayName());
            if (keyColor != null && !keyColor.isEmpty()) {
                viewHolder.colorButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(keyColor)));
            } else {
                viewHolder.colorButton.setBackgroundTintList(mDefaultColor);
            }

        }

        void replaceData(List<Subreddit> subreddits) {
            setList(subreddits);
            notifyDataSetChanged();
        }

        private void setList(List<Subreddit> subreddits) {
            mSubreddits = subreddits;
        }

        @Override
        public int getItemCount() {
            return mSubreddits.size();
        }

        @Override
        public long getItemId(int position) {
            return mSubreddits.get(position).id().hashCode();
        }

        Subreddit getItem(int position) {
            return mSubreddits.get(position);
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView title;
            final Button colorButton;

            ViewHolder(View itemView, SubredditItemListener listener) {
                super(itemView);
                mItemListener = listener;
                title = (TextView) itemView.findViewById(R.id.item_subreddit_title);
                colorButton = (Button) itemView.findViewById(R.id.item_subreddit_circle);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                mItemListener.onSubredditClick(getItem(position));

            }
        }

    }

    interface SubredditItemListener {
        void onSubredditClick(Subreddit subreddit);
    }
}
