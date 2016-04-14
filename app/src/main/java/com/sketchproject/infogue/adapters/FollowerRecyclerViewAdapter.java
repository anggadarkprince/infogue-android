package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment.OnListFragmentInteractionListener;
import com.sketchproject.infogue.models.Contributor;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contributor} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_FOLLOWER = 1;

    private final List<Contributor> mContributors;
    private final OnListFragmentInteractionListener mListener;

    private int lastPosition = -1;

    public FollowerRecyclerViewAdapter(List<Contributor> items, OnListFragmentInteractionListener listener) {
        mContributors = items;
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mContributors.get(position) == null) {
            return VIEW_TYPE_LOADING;
        }

        return VIEW_TYPE_FOLLOWER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_FOLLOWER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_follower_row, parent, false);
            return new FollowerViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_loading, parent, false);
        return new FollowerProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_FOLLOWER:
                final FollowerViewHolder followerHolder = (FollowerViewHolder) holder;
                followerHolder.mItem = mContributors.get(position);
                followerHolder.mNameView.setText(mContributors.get(position).getName());
                followerHolder.mLocationView.setText(mContributors.get(position).getLocation());
                Glide.with(followerHolder.mView.getContext())
                        .load(mContributors.get(position).getAvatar())
                        .placeholder(R.drawable.placeholder_square)
                        .crossFade()
                        .into(followerHolder.mAvatarImage);
                if (mContributors.get(position).isFollowing()) {
                    followerHolder.mFollowButton.setImageResource(R.drawable.btn_unfollow);
                }

                followerHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onListFragmentInteraction(followerHolder.mItem);
                        }
                    }
                });
                break;
            case VIEW_TYPE_LOADING:
                final FollowerProgressViewHolder progressbarHolder = (FollowerProgressViewHolder) holder;
                progressbarHolder.progressBar.setIndeterminate(true);
                break;
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return mContributors.size();
    }

    public class FollowerProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public FollowerProgressViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.load_more_progress);
        }
    }

    public class FollowerViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mLocationView;
        public final ImageButton mFollowButton;
        public final ImageView mAvatarImage;
        public Contributor mItem;

        public FollowerViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mLocationView = (TextView) view.findViewById(R.id.location);
            mFollowButton = (ImageButton) view.findViewById(R.id.follow);
            mAvatarImage = (ImageView) view.findViewById(R.id.avatar);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
