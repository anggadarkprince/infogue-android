package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.SuggestionFragment.OnContributorInteractionListener;
import com.sketchproject.infogue.fragments.holders.InfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Contributor;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contributor} and makes a call to the
 * specified {@link OnContributorInteractionListener}.
 * <p/>
 * Sketch Project Studio
 * Created by Angga on 14/09/2016 08.54.
 */
public class ContributorRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_CONTRIBUTOR = 1;
    private static final int VIEW_TYPE_EMPTY = 3;
    private static final int VIEW_TYPE_ERROR = 4;

    private final List<Contributor> mContributors;
    private final OnContributorInteractionListener mListener;

    /**
     * Default constructor.
     *
     * @param items    collection of contributors data
     * @param listener listener when interaction with view holder
     */
    public ContributorRecyclerViewAdapter(List<Contributor> items, OnContributorInteractionListener listener) {
        mContributors = items;
        mListener = listener;
    }

    /**
     * Get specific type of list, if contributor null mean loading, the rest depend on their id value.
     *
     * @param position of view holder
     * @return int type list
     */
    @Override
    public int getItemViewType(int position) {
        if (mContributors.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            Contributor contributor = mContributors.get(position);
            if (contributor.getId() > 0) {
                return VIEW_TYPE_CONTRIBUTOR;
            } else if (contributor.getId() == 0) {
                return VIEW_TYPE_EMPTY;
            }
            return VIEW_TYPE_ERROR;
        }
    }

    /**
     * Creating view holder each list type.
     *
     * @param parent   holder parent
     * @param viewType type has retrieve from getItemViewType()
     * @return ViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == VIEW_TYPE_LOADING) {
            view = inflater.inflate(R.layout.fragment_loading, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_CONTRIBUTOR) {
            view = inflater.inflate(R.layout.fragment_contributor_row, parent, false);
            return new ContributorViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.fragment_list_info, parent, false);
            return new InfoViewHolder(view);
        }
    }

    /**
     * Perform action and attribute when holder bind into list.
     *
     * @param holder   list view holder
     * @param position current position
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Contributor contributor = mContributors.get(holder.getAdapterPosition());
        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_CONTRIBUTOR:
                final ContributorViewHolder contributorHolder = (ContributorViewHolder) holder;
                contributorHolder.mItem = contributor;
                contributorHolder.mNameView.setText(contributor.getName());
                contributorHolder.mDetailView.setText(contributor.getUsername());
                Glide.with(contributorHolder.mView.getContext())
                        .load(contributor.getAvatar())
                        .placeholder(R.drawable.placeholder_square)
                        .dontAnimate()
                        .into(contributorHolder.mAvatarImage);

                contributorHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onContributorInteraction(contributorHolder.mView, contributorHolder.mItem);
                        }
                    }
                });

                break;
            case VIEW_TYPE_LOADING:
                final LoadingViewHolder progressbarHolder = (LoadingViewHolder) holder;
                progressbarHolder.mProgressBar.setIndeterminate(true);
                break;
            case VIEW_TYPE_EMPTY:
                final InfoViewHolder emptyHolder = (InfoViewHolder) holder;
                String label = "CONTRIBUTOR NOT FOUND";
                emptyHolder.mMessageView.setText(label);
                break;
            case VIEW_TYPE_ERROR:
                final InfoViewHolder errorHolder = (InfoViewHolder) holder;
                String error = contributor.getUsername();
                errorHolder.mMessageView.setText(error);
                break;
        }
    }

    /**
     * Count total items.
     *
     * @return total of contributor
     */
    @Override
    public int getItemCount() {
        return mContributors.size();
    }

    /**
     * Follower view holder.
     */
    public class ContributorViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mDetailView;
        public final ImageView mAvatarImage;
        public Contributor mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public ContributorViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mDetailView = (TextView) view.findViewById(R.id.detail);
            mAvatarImage = (ImageView) view.findViewById(R.id.avatar);
        }

        @Override
        public String toString() {
            return mNameView.getText() + " (" + mDetailView.getText() + ")";
        }
    }
}
