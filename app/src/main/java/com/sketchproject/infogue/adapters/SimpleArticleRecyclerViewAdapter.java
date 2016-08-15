package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment.OnArticleInteractionListener;
import com.sketchproject.infogue.models.Article;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Article} and makes a call to the
 * specified {@link OnArticleInteractionListener}.
 * <p/>
 * Sketch Project Studio
 * Created by Angga on 14/08/2016 21.55.
 */
public class SimpleArticleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleArticleRecyclerViewAdapter.ArticleSimpleRowViewHolder> {
    private final List<Article> mArticles;
    private final OnArticleInteractionListener mInteractionListener;

    /**
     * Constructor for article has header.
     *
     * @param items        collection of article objects
     * @param listListener listener when interaction with view holder
     */
    public SimpleArticleRecyclerViewAdapter(List<Article> items, OnArticleInteractionListener listListener) {
        mArticles = items;
        mInteractionListener = listListener;
    }

    /**
     * Creating view holder each list type.
     *
     * @param parent   holder parent
     * @param viewType type has retrieve from getItemViewType()
     * @return ViewHolder
     */
    @Override
    public ArticleSimpleRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_simple_row, parent, false);
        return new ArticleSimpleRowViewHolder(view);
    }

    /**
     * Perform action and attribute when holder bind into list.
     *
     * @param holder   list view holder
     * @param position current position
     */
    @Override
    public void onBindViewHolder(final ArticleSimpleRowViewHolder holder, int position) {
        holder.mItem = mArticles.get(holder.getAdapterPosition());
        holder.mTitleView.setText(mArticles.get(holder.getAdapterPosition()).getTitle());
        holder.mDateView.setText(mArticles.get(holder.getAdapterPosition()).getPublishedAt());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mInteractionListener) {
                    mInteractionListener.onArticleInteraction(view, holder.mItem);
                }
            }
        });
        if (holder.getAdapterPosition() == mArticles.size() - 1) {
            holder.mDivider.setVisibility(View.GONE);
        }
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
     * Default article row view holder.
     */
    public class ArticleSimpleRowViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public View mDivider;
        public TextView mTitleView;
        public TextView mDateView;
        public Article mItem;

        /**
         * Default constructor.
         *
         * @param view holder
         */
        public ArticleSimpleRowViewHolder(View view) {
            super(view);
            mView = view;
            mDivider = view.findViewById(R.id.divider);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDateView = (TextView) view.findViewById(R.id.date);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
