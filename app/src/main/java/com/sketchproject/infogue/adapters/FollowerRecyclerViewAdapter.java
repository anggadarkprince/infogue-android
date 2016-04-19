package com.sketchproject.infogue.adapters;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment.OnListFragmentInteractionListener;
import com.sketchproject.infogue.fragments.holders.ListInfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contributor} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_FOLLOWER = 1;
    private static final int VIEW_TYPE_END = 2;
    private static final int VIEW_TYPE_EMPTY = 3;

    private final List<Contributor> mContributors;
    private final OnListFragmentInteractionListener mListener;

    private int mLastPosition = -1;
    private String mScreenType;

    public FollowerRecyclerViewAdapter(List<Contributor> items, OnListFragmentInteractionListener listener, String type) {
        mContributors = items;
        mListener = listener;
        mScreenType = type;
    }

    @Override
    public int getItemViewType(int position) {
        if (mContributors.get(position) == null) {
            return VIEW_TYPE_LOADING;
        }
        else{
            Contributor contributor = mContributors.get(position);
            if (contributor.getId() == -1) {
                return VIEW_TYPE_END;
            } else if (contributor.getId() == 0) {
                return VIEW_TYPE_EMPTY;
            }

            return VIEW_TYPE_FOLLOWER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_FOLLOWER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_follower_row, parent, false);
            return new FollowerViewHolder(view);
        } else if (viewType == VIEW_TYPE_END) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_info, parent, false);
            return new ListInfoViewHolder(view);
        } else if (viewType == VIEW_TYPE_EMPTY) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_info, parent, false);
            return new ListInfoViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_loading, parent, false);
        return new LoadingViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), (position > mLastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        mLastPosition = holder.getAdapterPosition();

        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_FOLLOWER:
                final FollowerViewHolder followerHolder = (FollowerViewHolder) holder;
                followerHolder.mItem = mContributors.get(holder.getAdapterPosition());
                followerHolder.mNameView.setText(mContributors.get(holder.getAdapterPosition()).getName());
                followerHolder.mLocationView.setText(mContributors.get(holder.getAdapterPosition()).getLocation());
                Glide.with(followerHolder.mView.getContext())
                        .load(mContributors.get(holder.getAdapterPosition()).getAvatar())
                        .placeholder(R.drawable.placeholder_square)
                        .crossFade()
                        .into(followerHolder.mAvatarImage);
                if (mContributors.get(holder.getAdapterPosition()).isFollowing()) {
                    followerHolder.mFollowButton.setImageResource(R.drawable.btn_unfollow);
                }

                SessionManager session = new SessionManager(followerHolder.itemView.getContext());
                if(session.isLoggedIn()){
                    if(followerHolder.mItem.getId() == session.getSessionData(SessionManager.KEY_ID, 0)){
                        followerHolder.mFollowButton.setVisibility(View.GONE);
                    }
                    else{
                        followerHolder.mFollowButton.setVisibility(View.VISIBLE);
                    }
                }

                followerHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onListFragmentInteraction(followerHolder.mItem, followerHolder.mFollowButton);
                        }
                    }
                });

                followerHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (null != mListener) {
                            mListener.onListLongClickInteraction(view, followerHolder.mFollowButton, followerHolder.mItem);
                        }
                        return false;
                    }
                });

                followerHolder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (null != mListener) {
                            mListener.onListFollowControlInteraction(view, followerHolder.mFollowButton, followerHolder.mItem);
                        }
                    }
                });

                break;
            case VIEW_TYPE_LOADING:
                final LoadingViewHolder progressbarHolder = (LoadingViewHolder) holder;
                progressbarHolder.mProgressBar.setIndeterminate(true);
                break;
            case VIEW_TYPE_END:
                final ListInfoViewHolder endHolder = (ListInfoViewHolder) holder;
                endHolder.mMessageView.setVisibility(View.GONE);
                break;
            case VIEW_TYPE_EMPTY:
                final ListInfoViewHolder emptyHolder = (ListInfoViewHolder) holder;
                emptyHolder.mMessageView.setText("NO "+ mScreenType.toUpperCase()+" AVAILABLE");
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
