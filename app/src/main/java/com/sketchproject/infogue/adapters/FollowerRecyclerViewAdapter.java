package com.sketchproject.infogue.adapters;

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
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.fragments.FollowerFragment.OnFollowerInteractionListener;
import com.sketchproject.infogue.fragments.holders.ListInfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contributor} and makes a call to the
 * specified {@link OnFollowerInteractionListener}.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 14/04/2016 08.54.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_FOLLOWER = 1;
    private static final int VIEW_TYPE_END = 2;
    private static final int VIEW_TYPE_EMPTY = 3;
    private static final int VIEW_TYPE_ERROR = 4;

    private final List<Contributor> mContributors;
    private final OnFollowerInteractionListener mListener;

    private int mLastPosition = -1;
    private String mScreenType;

    /**
     * Default constructor.
     *
     * @param items    collection of comments data
     * @param listener listener when interaction with view holder
     * @param type     of screen
     */
    public FollowerRecyclerViewAdapter(List<Contributor> items, FollowerFragment.OnFollowerInteractionListener listener, String type) {
        mContributors = items;
        mListener = listener;
        mScreenType = type;
    }

    /**
     * Get specific type of list, if follower null mean loading, the rest depend on their id value.
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
                return VIEW_TYPE_FOLLOWER;
            } else if (contributor.getId() == 0) {
                return VIEW_TYPE_EMPTY;
            } else if (contributor.getId() == -1) {
                return VIEW_TYPE_END;
            } else if (contributor.getId() == -2) {
                return VIEW_TYPE_ERROR;
            }

            return VIEW_TYPE_END;
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
        } else if (viewType == VIEW_TYPE_ERROR) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_info, parent, false);
            return new ListInfoViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_loading, parent, false);
        return new LoadingViewHolder(view);
    }

    /**
     * Perform action and attribute when holder bind into list.
     *
     * @param holder   list view holder
     * @param position current position
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), (position > mLastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        mLastPosition = holder.getAdapterPosition();

        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_FOLLOWER:
                final FollowerViewHolder followerHolder = (FollowerViewHolder) holder;
                followerHolder.mItem = mContributors.get(holder.getAdapterPosition());
                followerHolder.mNameView.setText(followerHolder.mItem.getName());
                followerHolder.mLocationView.setText(followerHolder.mItem.getLocation());
                Glide.with(followerHolder.mView.getContext())
                        .load(followerHolder.mItem.getAvatar())
                        .placeholder(R.drawable.placeholder_square)
                        .crossFade()
                        .into(followerHolder.mAvatarImage);
                if (followerHolder.mItem.isFollowing()) {
                    followerHolder.mFollowButton.setImageResource(R.drawable.btn_unfollow);
                }

                SessionManager session = new SessionManager(followerHolder.itemView.getContext());
                if (session.isLoggedIn()) {
                    boolean isActivated = followerHolder.mItem.getStatus().equals(Contributor.STATUS_ACTIVATED);
                    boolean isMe = session.isMe(followerHolder.mItem.getId());
                    if (isMe || !isActivated) {
                        followerHolder.mFollowButton.setVisibility(View.GONE);
                    } else {
                        followerHolder.mFollowButton.setVisibility(View.VISIBLE);
                    }
                }

                followerHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onFollowerInteraction(followerHolder.mItem, followerHolder.mFollowButton);
                        }
                    }
                });

                followerHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (null != mListener) {
                            mListener.onFollowerLongClickInteraction(view, followerHolder.mFollowButton, followerHolder.mItem);
                        }
                        return false;
                    }
                });

                followerHolder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (null != mListener) {
                            mListener.onFollowerControlInteraction(view, followerHolder.mFollowButton, followerHolder.mItem);
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
                String label = "NO " + mScreenType.toUpperCase() + " AVAILABLE";
                emptyHolder.mMessageView.setText(label);
                break;
            case VIEW_TYPE_ERROR:
                final ListInfoViewHolder errorHolder = (ListInfoViewHolder) holder;
                errorHolder.mMessageView.setText(mContributors.get(holder.getAdapterPosition()).getName());
                break;
        }
    }

    /**
     * Clear animation when holder detached.
     *
     * @param holder list view holder
     */
    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    /**
     * Count total items.
     *
     * @return total of articles
     */
    @Override
    public int getItemCount() {
        return mContributors.size();
    }

    /**
     * Follower view holder.
     */
    public class FollowerViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mLocationView;
        public final ImageButton mFollowButton;
        public final ImageView mAvatarImage;
        public Contributor mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
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
