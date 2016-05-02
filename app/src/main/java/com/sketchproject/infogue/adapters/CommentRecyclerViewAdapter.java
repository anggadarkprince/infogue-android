package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.CommentFragment.OnCommentInteractionListener;
import com.sketchproject.infogue.fragments.holders.ListInfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Comment;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Comment} and makes a call to the
 * specified {@link OnCommentInteractionListener}.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 26/04/2016 18.48.
 */
public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_COMMENT = 1;
    private static final int VIEW_TYPE_END = 2;
    private static final int VIEW_TYPE_EMPTY = 3;
    private static final int VIEW_TYPE_ERROR = 4;

    private final List<Comment> mComments;
    private final OnCommentInteractionListener mInteractionListener;

    private int mLastPosition = -1;

    /**
     * Default constructor.
     *
     * @param items        collection of comments data
     * @param listListener listener when interaction with view holder
     */
    public CommentRecyclerViewAdapter(List<Comment> items, OnCommentInteractionListener listListener) {
        mComments = items;
        mInteractionListener = listListener;
    }

    /**
     * Get specific type of list, if comment null mean loading, the rest depend on their id value.
     *
     * @param position of view holder
     * @return int type list
     */
    @Override
    public int getItemViewType(int position) {
        if (mComments.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            Comment comment = mComments.get(position);
            if (comment.getId() > 0) {
                return VIEW_TYPE_COMMENT;
            } else if (comment.getId() == 0) {
                return VIEW_TYPE_EMPTY;
            } else if (comment.getId() == -1) {
                return VIEW_TYPE_END;
            } else if (comment.getId() == -2) {
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

        if (viewType == VIEW_TYPE_COMMENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_comment_row, parent, false);
            return new CommentViewHolder(view);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), (position > mLastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        mLastPosition = holder.getAdapterPosition();

        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_COMMENT:
                final CommentViewHolder commentHolder = (CommentViewHolder) holder;
                commentHolder.mItem = mComments.get(holder.getAdapterPosition());
                commentHolder.mNameView.setText(mComments.get(holder.getAdapterPosition()).getName());
                commentHolder.mTimestampView.setText(mComments.get(holder.getAdapterPosition()).getTimestamp());
                commentHolder.mCommentView.setText(mComments.get(holder.getAdapterPosition()).getComment());
                Glide.with(commentHolder.mView.getContext())
                        .load(mComments.get(holder.getAdapterPosition()).getAvatar())
                        .placeholder(R.drawable.placeholder_square)
                        .into(commentHolder.mAvatarImage);

                commentHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mInteractionListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mInteractionListener.onCommentListClicked(commentHolder.mItem);
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
                emptyHolder.mMessageView.setText(R.string.label_no_comment);
                break;
            case VIEW_TYPE_ERROR:
                final ListInfoViewHolder errorHolder = (ListInfoViewHolder) holder;
                errorHolder.mMessageView.setText(mComments.get(holder.getAdapterPosition()).getComment());
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
     * @return total of comments
     */
    @Override
    public int getItemCount() {
        return mComments.size();
    }

    /**
     * Comment view holder.
     */
    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mTimestampView;
        public final TextView mCommentView;
        public final CircleImageView mAvatarImage;
        public Comment mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public CommentViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mTimestampView = (TextView) view.findViewById(R.id.date);
            mCommentView = (TextView) view.findViewById(R.id.comment);
            mAvatarImage = (CircleImageView) view.findViewById(R.id.avatar);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
