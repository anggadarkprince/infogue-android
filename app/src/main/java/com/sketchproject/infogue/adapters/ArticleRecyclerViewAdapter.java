package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ROW = 1;

    private final List<DummyItem> mValues;
    private final OnArticleFragmentInteractionListener mListener;
    private boolean header;

    public ArticleRecyclerViewAdapter(List<DummyItem> items, OnArticleFragmentInteractionListener listener, boolean hasHeader) {
        mValues = items;
        mListener = listener;
        header = hasHeader;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && header){
            return VIEW_TYPE_HEADER;
        }

        return VIEW_TYPE_ROW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if(viewType == VIEW_TYPE_HEADER){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_header, parent, false);
            return new ArticleHeaderViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_article_row, parent, false);
        return new ArticleRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)){
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

                rowHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onArticleFragmentInteraction(rowHolder.mItem);
                        }
                    }
                });

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
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
