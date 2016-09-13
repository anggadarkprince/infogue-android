package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.MessageFragment.OnMessageInteractionListener;
import com.sketchproject.infogue.fragments.holders.InfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Message;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Message} and makes a call to the
 * specified {@link OnMessageInteractionListener}.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 26/04/2016 18.48.
 */
public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_MESSAGE = 1;
    private static final int VIEW_TYPE_END = 2;
    private static final int VIEW_TYPE_EMPTY = 3;
    private static final int VIEW_TYPE_ERROR = 4;

    private final List<Message> mMessages;
    private final OnMessageInteractionListener mInteractionListener;

    private int mLastPosition = -1;

    /**
     * Default constructor.
     *
     * @param items        collection of comments data
     * @param listListener listener when interaction with view holder
     */
    public MessageRecyclerViewAdapter(List<Message> items, OnMessageInteractionListener listListener) {
        mMessages = items;
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
        if (mMessages.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            Message message = mMessages.get(position);
            if (message.getId() > 0) {
                return VIEW_TYPE_MESSAGE;
            } else if (message.getId() == 0) {
                return VIEW_TYPE_EMPTY;
            } else if (message.getId() == -1) {
                return VIEW_TYPE_END;
            } else if (message.getId() == -2) {
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

        if (viewType == VIEW_TYPE_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_message_row, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_END) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_info, parent, false);
            return new InfoViewHolder(view);
        } else if (viewType == VIEW_TYPE_EMPTY) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_info, parent, false);
            return new InfoViewHolder(view);
        } else if (viewType == VIEW_TYPE_ERROR) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_info, parent, false);
            return new InfoViewHolder(view);
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
            case VIEW_TYPE_MESSAGE:
                final MessageViewHolder messageHolder = (MessageViewHolder) holder;
                messageHolder.mItem = mMessages.get(holder.getAdapterPosition());
                messageHolder.mNameView.setText(mMessages.get(holder.getAdapterPosition()).getName());
                messageHolder.mTimestampView.setText(mMessages.get(holder.getAdapterPosition()).getTimestamp());
                messageHolder.mMessageView.setText(mMessages.get(holder.getAdapterPosition()).getMessage());
                Glide.with(messageHolder.mView.getContext())
                        .load(mMessages.get(holder.getAdapterPosition()).getAvatar())
                        .placeholder(R.drawable.placeholder_square)
                        .into(messageHolder.mAvatarImage);

                messageHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mInteractionListener) {
                            mInteractionListener.onMessageListClicked(messageHolder.mItem);
                        }
                    }
                });

                messageHolder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mInteractionListener) {
                            mInteractionListener.onDeleteMessage(messageHolder.mItem);
                        }
                    }
                });

                break;
            case VIEW_TYPE_LOADING:
                final LoadingViewHolder progressbarHolder = (LoadingViewHolder) holder;
                progressbarHolder.mProgressBar.setIndeterminate(true);
                break;
            case VIEW_TYPE_END:
                final InfoViewHolder endHolder = (InfoViewHolder) holder;
                endHolder.mMessageView.setVisibility(View.GONE);
                break;
            case VIEW_TYPE_EMPTY:
                final InfoViewHolder emptyHolder = (InfoViewHolder) holder;
                emptyHolder.mMessageView.setText(R.string.label_no_message);
                break;
            case VIEW_TYPE_ERROR:
                final InfoViewHolder errorHolder = (InfoViewHolder) holder;
                errorHolder.mMessageView.setText(mMessages.get(holder.getAdapterPosition()).getMessage());
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
     * @return total of messages
     */
    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    /**
     * Message view holder.
     */
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mTimestampView;
        public final TextView mMessageView;
        public final CircleImageView mAvatarImage;
        public final ImageView mDeleteButton;
        public Message mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public MessageViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mTimestampView = (TextView) view.findViewById(R.id.date);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mAvatarImage = (CircleImageView) view.findViewById(R.id.avatar);
            mDeleteButton = (ImageView) view.findViewById(R.id.btn_delete);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
