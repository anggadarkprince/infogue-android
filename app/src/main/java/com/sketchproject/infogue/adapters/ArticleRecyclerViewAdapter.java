package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.ArticleFragment.OnArticleEditableInteractionListener;
import com.sketchproject.infogue.fragments.ArticleFragment.OnArticleInteractionListener;
import com.sketchproject.infogue.fragments.holders.ListInfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Article;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Article} and makes a call to the
 * specified {@link OnArticleInteractionListener}.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 13/04/2016 08.54.
 */
public class ArticleRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ROW = 2;
    private static final int VIEW_TYPE_ROW_EDITABLE = 3;
    private static final int VIEW_TYPE_END = 4;
    private static final int VIEW_TYPE_EMPTY = 5;
    private static final int VIEW_TYPE_ERROR = 6;

    private final List<Article> mArticles;
    private final ArticleFragment.OnArticleInteractionListener mInteractionListener;
    private final OnArticleEditableInteractionListener mEditableListener;

    private boolean mHeader;
    private boolean mIsEditable;
    private int mLastPosition = -1;

    /**
     * Constructor for article has header.
     *
     * @param items        collection of article objects
     * @param listListener listener when interaction with view holder
     * @param hasHeader    is list has header
     */
    public ArticleRecyclerViewAdapter(List<Article> items, OnArticleInteractionListener listListener, boolean hasHeader) {
        mArticles = items;
        mInteractionListener = listListener;
        mEditableListener = null;
        mHeader = hasHeader;
        mIsEditable = false;
    }

    /**
     * Constructor with editable listener.
     *
     * @param items            collection of article objects
     * @param listListener     listener when interact with view holder
     * @param editableListener listener when interact with view holder
     */
    public ArticleRecyclerViewAdapter(List<Article> items, ArticleFragment.OnArticleInteractionListener listListener, OnArticleEditableInteractionListener editableListener) {
        mArticles = items;
        mInteractionListener = listListener;
        mEditableListener = editableListener;
        mIsEditable = true;
    }

    /**
     * Get specific type of list, if article null mean loading, the rest depend on their id value.
     *
     * @param position of view holder
     * @return int type list
     */
    @Override
    public int getItemViewType(int position) {
        if (mArticles.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            Article article = mArticles.get(position);

            if (article.getId() > 0 && position == 0 && mHeader) {
                return VIEW_TYPE_HEADER;
            } else if (article.getId() > 0 && mIsEditable) {
                return VIEW_TYPE_ROW_EDITABLE;
            } else if (article.getId() > 0) {
                return VIEW_TYPE_ROW;
            } else if (article.getId() == 0) {
                return VIEW_TYPE_EMPTY;
            } else if (article.getId() == -1) {
                return VIEW_TYPE_END;
            } else if (article.getId() == -2) {
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

        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_header, parent, false);
            return new ArticleHeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_ROW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_row, parent, false);
            return new ArticleRowViewHolder(view);
        } else if (viewType == VIEW_TYPE_ROW_EDITABLE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_editable, parent, false);
            return new ArticleEditableViewHolder(view);
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
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), (holder.getAdapterPosition() > mLastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        mLastPosition = holder.getAdapterPosition();

        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_HEADER:
                final ArticleHeaderViewHolder headerHolder = (ArticleHeaderViewHolder) holder;
                headerHolder.mItem = mArticles.get(holder.getAdapterPosition());
                headerHolder.mTitleView.setText(mArticles.get(holder.getAdapterPosition()).getTitle());
                headerHolder.mDateView.setText(mArticles.get(holder.getAdapterPosition()).getPublishedAt());
                headerHolder.mContentView.setText(mArticles.get(holder.getAdapterPosition()).getContent());
                headerHolder.mCategoryView.setText(mArticles.get(holder.getAdapterPosition()).getCategory());
                Glide.with(headerHolder.mView.getContext())
                        .load(mArticles.get(holder.getAdapterPosition()).getFeatured())
                        .placeholder(R.drawable.placeholder_logo_wide)
                        .crossFade()
                        .into(headerHolder.mFeaturedImage);

                setDefaultRowEventListener(headerHolder.mView, null, headerHolder.mItem);

                break;
            case VIEW_TYPE_ROW:
                final ArticleRowViewHolder rowHolder = (ArticleRowViewHolder) holder;
                rowHolder.mItem = mArticles.get(holder.getAdapterPosition());
                rowHolder.mTitleView.setText(mArticles.get(holder.getAdapterPosition()).getTitle());
                rowHolder.mDateView.setText(mArticles.get(holder.getAdapterPosition()).getPublishedAt());
                Glide.with(rowHolder.mView.getContext())
                        .load(mArticles.get(holder.getAdapterPosition()).getFeatured())
                        .placeholder(R.drawable.placeholder_logo)
                        .crossFade()
                        .into(rowHolder.mFeaturedImage);

                setDefaultRowEventListener(rowHolder.mView, rowHolder.mMoreImage, rowHolder.mItem);

                break;
            case VIEW_TYPE_ROW_EDITABLE:
                final ArticleEditableViewHolder rowEditableHolder = (ArticleEditableViewHolder) holder;
                String status = mArticles.get(holder.getAdapterPosition()).getStatus();
                String contentUpdate = mArticles.get(holder.getAdapterPosition()).getContentUpdate();
                rowEditableHolder.mItem = mArticles.get(holder.getAdapterPosition());
                rowEditableHolder.mTitleView.setText(mArticles.get(holder.getAdapterPosition()).getTitle());
                rowEditableHolder.mDateView.setText(mArticles.get(holder.getAdapterPosition()).getPublishedAt());
                rowEditableHolder.mCategoryView.setText(mArticles.get(holder.getAdapterPosition()).getCategory());

                if (contentUpdate != null && !contentUpdate.equals("null") && !contentUpdate.trim().isEmpty()) {
                    rowEditableHolder.mStatusView.setText(Article.STATUS_UPDATED.toUpperCase());
                    rowEditableHolder.mControlBar.setBackgroundResource(R.color.color_warning_light);
                    //rowEditableHolder.mBrowse.setBackgroundResource(R.color.color_warning);
                    //rowEditableHolder.mShare.setBackgroundResource(R.color.color_warning_medium);
                    //rowEditableHolder.mEdit.setBackgroundResource(R.color.color_warning_hard);
                    //rowEditableHolder.mDelete.setBackgroundResource(R.color.color_warning_darker);
                } else {
                    rowEditableHolder.mStatusView.setText(status.toUpperCase());
                    switch (status) {
                        case Article.STATUS_PENDING:
                            rowEditableHolder.mControlBar.setBackgroundResource(R.color.color_warning_light);
                            //rowEditableHolder.mBrowse.setBackgroundResource(R.color.color_warning);
                            //rowEditableHolder.mShare.setBackgroundResource(R.color.color_warning_medium);
                            //rowEditableHolder.mEdit.setBackgroundResource(R.color.color_warning_hard);
                            //rowEditableHolder.mDelete.setBackgroundResource(R.color.color_warning_darker);
                            break;
                        case Article.STATUS_PUBLISHED:
                            rowEditableHolder.mControlBar.setBackgroundResource(R.color.color_success_light);
                            //rowEditableHolder.mBrowse.setBackgroundResource(R.color.color_success);
                            //rowEditableHolder.mShare.setBackgroundResource(R.color.color_success_medium);
                            //rowEditableHolder.mEdit.setBackgroundResource(R.color.color_success_hard);
                            //rowEditableHolder.mDelete.setBackgroundResource(R.color.color_success_darker);
                            break;
                        case Article.STATUS_REJECTED:
                            rowEditableHolder.mControlBar.setBackgroundResource(R.color.color_danger_light);
                            //rowEditableHolder.mBrowse.setBackgroundResource(R.color.color_danger);
                            //rowEditableHolder.mShare.setBackgroundResource(R.color.color_danger_medium);
                            //rowEditableHolder.mEdit.setBackgroundResource(R.color.color_danger_hard);
                            //rowEditableHolder.mDelete.setBackgroundResource(R.color.color_danger_darker);
                            break;
                        case Article.STATUS_DRAFT:
                            rowEditableHolder.mControlBar.setBackgroundResource(R.color.color_caution_light);
                            //rowEditableHolder.mBrowse.setBackgroundResource(R.color.color_caution);
                            //rowEditableHolder.mShare.setBackgroundResource(R.color.color_caution_medium);
                            //rowEditableHolder.mEdit.setBackgroundResource(R.color.color_caution_hard);
                            //rowEditableHolder.mDelete.setBackgroundResource(R.color.color_caution_darker);
                            break;
                    }
                }

                Glide.with(rowEditableHolder.mView.getContext())
                        .load(mArticles.get(position).getFeatured())
                        .placeholder(R.drawable.placeholder_logo)
                        .crossFade()
                        .into(rowEditableHolder.mFeaturedImage);

                setDefaultRowEventListener(rowEditableHolder.mView, rowEditableHolder.mMoreImage, rowEditableHolder.mItem);

                rowEditableHolder.mBrowse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mEditableListener) {
                            mEditableListener.onBrowseClicked(rowEditableHolder.mView, rowEditableHolder.mItem);
                        }
                    }
                });

                rowEditableHolder.mShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mEditableListener) {
                            mEditableListener.onShareClicked(rowEditableHolder.mView, rowEditableHolder.mItem);
                        }
                    }
                });

                rowEditableHolder.mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mEditableListener) {
                            mEditableListener.onEditClicked(rowEditableHolder.mView, rowEditableHolder.mItem);
                        }
                    }
                });

                rowEditableHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mEditableListener) {
                            mEditableListener.onDeleteClicked(rowEditableHolder.mView, rowEditableHolder.mItem);
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
                emptyHolder.mMessageView.setText(R.string.label_no_article);
                break;
            case VIEW_TYPE_ERROR:
                final ListInfoViewHolder errorHolder = (ListInfoViewHolder) holder;
                errorHolder.mMessageView.setText(mArticles.get(holder.getAdapterPosition()).getTitle());
                break;
        }
    }

    /**
     * Binding event into views.
     *
     * @param view    view passing the event
     * @param more    view button ellipsis
     * @param article model article data
     */
    private void setDefaultRowEventListener(View view, View more, final Article article) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mInteractionListener) {
                    mInteractionListener.onArticleInteraction(view, article);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (null != mInteractionListener) {
                    mInteractionListener.onArticleLongClickInteraction(view, article);
                }
                return false;
            }
        });

        if (more != null) {
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != mInteractionListener) {
                        mInteractionListener.onArticlePopupInteraction(view, article);
                    }
                }
            });
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
        return mArticles.size();
    }

    /**
     * Default article header view holder.
     */
    public class ArticleHeaderViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTitleView;
        public TextView mContentView;
        public TextView mCategoryView;
        public TextView mDateView;
        public ImageView mFeaturedImage;
        public Article mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public ArticleHeaderViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mContentView = (TextView) view.findViewById(R.id.content);
            mCategoryView = (TextView) view.findViewById(R.id.category);
            mDateView = (TextView) view.findViewById(R.id.date);
            mFeaturedImage = (ImageView) view.findViewById(R.id.featured);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }

    /**
     * Default article row view holder.
     */
    public class ArticleRowViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTitleView;
        public TextView mDateView;
        public ImageView mFeaturedImage;
        public ImageView mMoreImage;
        public Article mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public ArticleRowViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDateView = (TextView) view.findViewById(R.id.date);
            mFeaturedImage = (ImageView) view.findViewById(R.id.featured);
            mMoreImage = (ImageView) view.findViewById(R.id.more);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }

    /**
     * Article view holder with editable functionality.
     */
    public class ArticleEditableViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTitleView;
        public TextView mDateView;
        public TextView mCategoryView;
        public TextView mStatusView;
        public ImageView mFeaturedImage;
        public ImageView mMoreImage;
        public ImageButton mBrowse;
        public ImageButton mShare;
        public ImageButton mEdit;
        public ImageButton mDelete;
        public RelativeLayout mControlBar;
        public Article mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public ArticleEditableViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDateView = (TextView) view.findViewById(R.id.date);
            mStatusView = (TextView) view.findViewById(R.id.status);
            mFeaturedImage = (ImageView) view.findViewById(R.id.featured);
            mCategoryView = (TextView) view.findViewById(R.id.category);
            mMoreImage = (ImageView) view.findViewById(R.id.more);
            mBrowse = (ImageButton) view.findViewById(R.id.btn_browse);
            mShare = (ImageButton) view.findViewById(R.id.btn_share);
            mEdit = (ImageButton) view.findViewById(R.id.btn_edit);
            mDelete = (ImageButton) view.findViewById(R.id.btn_delete);
            mControlBar = (RelativeLayout) view.findViewById(R.id.editor_control);
        }

        @Override
        public String toString() {
            return super.toString() + " editable '" + mTitleView.getText() + "'";
        }
    }
}
