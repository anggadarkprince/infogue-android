package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment.OnArticleFragmentInteractionListener;
import com.sketchproject.infogue.fragments.dummy.DummyArticleContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnArticleFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ArticleRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ROW = 2;

    private final List<DummyItem> mValues;
    private final OnArticleFragmentInteractionListener mListener;
    private boolean header;

    private int lastPosition = -1;

    public ArticleRecyclerViewAdapter(List<DummyItem> items, OnArticleFragmentInteractionListener listener, boolean hasHeader) {
        mValues = items;
        mListener = listener;
        header = hasHeader;
    }

    @Override
    public int getItemViewType(int position) {
        if (mValues.get(position) == null) {
            return VIEW_TYPE_LOADING;
        }

        if (position == 0 && header) {
            return VIEW_TYPE_HEADER;
        }

        return VIEW_TYPE_ROW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_header, parent, false);
            return new ArticleHeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_ROW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_row, parent, false);
            return new ArticleRowViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_loading, parent, false);
        return new ArticleProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                final ArticleHeaderViewHolder headerHolder = (ArticleHeaderViewHolder) holder;
                headerHolder.mItem = mValues.get(position);
                headerHolder.mTitleView.setText(mValues.get(position).title);
                headerHolder.mDateView.setText(mValues.get(position).date);
                headerHolder.mContentView.setText(mValues.get(position).content);
                headerHolder.mCategoryView.setText(mValues.get(position).category);
                headerHolder.mFeaturedImage.setImageResource(mValues.get(position).featured);

                headerHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onArticleFragmentInteraction(headerHolder.mItem);
                        }
                    }
                });

                break;

            case VIEW_TYPE_ROW:
                final ArticleRowViewHolder rowHolder = (ArticleRowViewHolder) holder;
                rowHolder.mItem = mValues.get(position);
                rowHolder.mTitleView.setText(mValues.get(position).title);
                rowHolder.mDateView.setText(mValues.get(position).date);
                rowHolder.mFeaturedImage.setImageResource(mValues.get(position).featured);

                /*
                Glide.with(rowHolder.mView.getContext())
                        .load("http://infogue.id/images/covers/twitter-294039766.jpg")
                        .placeholder(R.drawable.loading)
                        .crossFade()
                        .into(rowHolder.mFeaturedImage); */

                rowHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onArticleFragmentInteraction(rowHolder.mItem);
                        }
                    }
                });

                break;
            case VIEW_TYPE_LOADING:
                final ArticleProgressViewHolder progressbarHolder = (ArticleProgressViewHolder) holder;
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
        return mValues.size();
    }

    public class ArticleProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ArticleProgressViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.load_more_progress);
        }
    }


    public class ArticleHeaderViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTitleView;
        public TextView mContentView;
        public TextView mCategoryView;
        public TextView mDateView;
        public ImageView mFeaturedImage;
        public DummyItem mItem;

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

    public class ArticleRowViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTitleView;
        public TextView mDateView;
        public ImageView mFeaturedImage;
        public DummyItem mItem;

        public ArticleRowViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDateView = (TextView) view.findViewById(R.id.date);
            mFeaturedImage = (ImageView) view.findViewById(R.id.featured);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
