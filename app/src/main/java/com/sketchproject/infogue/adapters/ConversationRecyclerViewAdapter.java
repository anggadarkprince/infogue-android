package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.holders.InfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Conversation;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Conversation}
 * <p/>
 * Sketch Project Studio
 * Created by Angga on 26/04/2016 18.48.
 */
public class ConversationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_LOADING_SEND = 1;
    private static final int VIEW_TYPE_MESSAGE_OURS = 2;
    private static final int VIEW_TYPE_MESSAGE_THEIRS = 3;
    private static final int VIEW_TYPE_END = 4;
    private static final int VIEW_TYPE_EMPTY = 5;
    private static final int VIEW_TYPE_ERROR = 6;

    private final List<Conversation> mMessages;

    /**
     * Default constructor.
     *
     * @param items collection of comments data
     */
    public ConversationRecyclerViewAdapter(List<Conversation> items) {
        mMessages = items;
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
            Conversation message = mMessages.get(position);
            if (message.getId() > 0) {
                if (message.getOwner().equals("me")) {
                    return VIEW_TYPE_MESSAGE_OURS;
                } else {
                    return VIEW_TYPE_MESSAGE_THEIRS;
                }
            } else if (message.getId() == 0) {
                return VIEW_TYPE_EMPTY;
            } else if (message.getId() == -1) {
                return VIEW_TYPE_END;
            } else if (message.getId() == -2) {
                return VIEW_TYPE_ERROR;
            } else if (message.getId() == -3) {
                return VIEW_TYPE_LOADING_SEND;
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == VIEW_TYPE_LOADING) {
            view = inflater.inflate(R.layout.fragment_loading, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_OURS) {
            view = inflater.inflate(R.layout.fragment_conversation_ours, parent, false);
            return new ConversationViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_THEIRS) {
            view = inflater.inflate(R.layout.fragment_conversation_theirs, parent, false);
            return new ConversationViewHolder(view);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Conversation conversation = mMessages.get(holder.getAdapterPosition());

        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_MESSAGE_OURS:
            case VIEW_TYPE_MESSAGE_THEIRS:
                final ConversationViewHolder messageHolder = (ConversationViewHolder) holder;
                messageHolder.mItem = conversation;
                messageHolder.mTimestampView.setText(conversation.getTimestamp());
                messageHolder.mMessageView.setText(conversation.getMessage());
                Glide.with(messageHolder.mView.getContext())
                        .load(conversation.getAvatar())
                        .dontAnimate()
                        .placeholder(R.drawable.placeholder_square)
                        .into(messageHolder.mAvatarImage);

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
            case VIEW_TYPE_LOADING_SEND:
                final InfoViewHolder errorHolder = (InfoViewHolder) holder;
                errorHolder.mMessageView.setText(conversation.getMessage());
                if (conversation.getId() == -3) {
                    errorHolder.mLogoImage.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * Count total items.
     *
     * @return total of comments
     */
    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    /**
     * Conversation bubble view holder.
     */
    public class ConversationViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTimestampView;
        public final TextView mMessageView;
        public final CircleImageView mAvatarImage;
        public Conversation mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public ConversationViewHolder(View view) {
            super(view);
            mView = view;
            mTimestampView = (TextView) view.findViewById(R.id.date);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mAvatarImage = (CircleImageView) view.findViewById(R.id.avatar);
        }

        @Override
        public String toString() {
            return mMessageView.getText().toString();
        }
    }
}
