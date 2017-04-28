package torille.fi.lurkforreddit.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import torille.fi.lurkforreddit.MyApplication;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.view.SearchResult;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.subreddit.SubredditActivity;

/**
 * Created by eva on 3/20/17.
 */

public class SearchFragment extends Fragment implements SearchContract.View {

    @Inject
    SearchContract.Presenter<SearchContract.View> mActionsListener;

    private SearchComponent mSearchComponent;

    private SearchViewAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private boolean loading;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchComponent = DaggerSearchComponent.builder()
                .redditRepositoryComponent(((MyApplication) getActivity().getApplication()).getmRedditRepositoryComponent())
                .build();
        mAdapter = new SearchViewAdapter(
                new ArrayList<SearchResult>(0),
                searchClickListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        mSearchComponent.inject(this);
        mActionsListener.setView(this);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.search_list);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int pastVisiblesItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!loading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = true;
                        Timber.d("Last item reached, getting more!");
                        mActionsListener.searchMoreSubreddits();
                    }

                }
            }
        });
        recyclerView.setAdapter(mAdapter);

        final SearchView mSearchView = (SearchView) root.findViewById(R.id.searchView);
        mSearchView.setQueryHint("Find subreddits...");
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    Timber.d("Going to search for " + query);
                    mSearchView.clearFocus();
                    mActionsListener.searchSubreddits(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActionsListener.dispose();
    }

    @Override
    public void showResults(@NonNull List<SearchResult> results) {
        loading = false;
        mAdapter.addResults(results);
    }

    @Override
    public void showProgressbar() {
        mAdapter.addProgressBar();
    }

    @Override
    public void clearResults() {
        mAdapter.clear();
    }

    @Override
    public void showError(String errorText) {
        Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
    }

    private final SearchClickListener searchClickListener = new SearchClickListener() {
        @Override
        public void onSearchClick(@NonNull Subreddit subreddit) {
            Intent intent = new Intent(getContext(), SubredditActivity.class);
            intent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit);
            startActivity(intent);
        }
    };

    private static class SearchViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final static int VIEW_ITEM = 1;
        private final static int VIEW_PROGRESS = 0;
        private final List<SearchResult> mResults;
        private final SearchClickListener mClickListener;
        private final Handler mHandler = new Handler();

        SearchViewAdapter(List<SearchResult> results, SearchClickListener clickListener) {
            this.mResults = results;
            this.mClickListener = clickListener;
            setHasStableIds(true);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType) {
                case VIEW_ITEM:
                    return new SearchViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_search, parent, false));
                case VIEW_PROGRESS:
                default:
                    return new ProgressViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_progressbar, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SearchViewHolder) {
                ((SearchViewHolder) holder).bind(mResults.get(position));
            } else if (holder instanceof ProgressViewHolder) {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mResults.get(position).title().equals("Progressbar")) {
                return VIEW_PROGRESS;
            } else {
                return VIEW_ITEM;
            }
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }

        @Override
        public long getItemId(int position) {
            return mResults.get(position).title().hashCode();
        }


        void addResults(@NonNull List<SearchResult> results) {
            int position = mResults.size() - 1;
            mResults.remove(position);
            notifyItemRemoved(position);
            mResults.addAll(results);
            notifyItemRangeInserted(position, results.size());
        }

        void addProgressBar() {
            Subreddit subreddit = Subreddit.builder()
                    .build();

            SearchResult dummy = SearchResult.builder()
                    .setSubscriptionInfo("")
                    .setInfoText("")
                    .setDescription("")
                    .setTitle("Progressbar")
                    .setSubreddit(subreddit)
                    .build();

            mResults.add(dummy);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(mResults.size() - 1);
                }
            });

        }

        void clear() {
            final int index = mResults.size();
            mResults.clear();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved(0, index);
                }
            });

        }

        class SearchViewHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView infoText;
            final TextView description;
            final Button subscribe;

            SearchViewHolder(View v) {
                super(v);
                title = (TextView) v.findViewById(R.id.subreddit_title);
                infoText = (TextView) v.findViewById(R.id.subreddits_infotext);
                description = (TextView) v.findViewById(R.id.subreddit_description);
                subscribe = (Button) v.findViewById(R.id.subreddit_subscribe);
                description.setMovementMethod(LinkMovementMethod.getInstance());
                description.setLinksClickable(true);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClickListener.onSearchClick(mResults.get(getAdapterPosition()).subreddit());
                    }
                });
            }

            void bind(SearchResult result) {
                title.setText(result.title());
                description.setText(result.description());
                subscribe.setText(result.subscriptionInfo());
                infoText.setText(result.infoText());

            }
        }

        private static class ProgressViewHolder extends RecyclerView.ViewHolder {
            final ProgressBar progressBar;

            ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            }
        }
    }

    interface SearchClickListener {
        void onSearchClick(@NonNull Subreddit subreddit);
    }
}
