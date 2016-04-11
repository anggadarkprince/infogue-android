package com.sketchproject.infogue.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.adapters.ArticleRecyclerViewAdapter;
import com.sketchproject.infogue.fragments.dummy.DummyArticleContent;
import com.sketchproject.infogue.fragments.dummy.DummyArticleContent.DummyItem;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnArticleFragmentInteractionListener}
 * interface.
 */
public class ArticleFragment extends Fragment {

    public static final String NOHEADER = "noheader";

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_CATEGORY_ID = "category-id";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_SUBCATEGORY_ID = "subcategory-id";
    private static final String ARG_SUBCATEGORY = "subcategory";
    private static final String ARG_FEATURED = "featured";
    private static final String ARG_AUTHOR = "author";

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private int mCategoryId = 0;
    private int mSubcategoryId = 0;
    private String mCategory;
    private String mSubcategory;
    private String mFeatured;
    private String mAuthor;
    private boolean hasHeader = false;

    private OnArticleFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleFragment() {
    }

    @SuppressWarnings("unused")
    public static ArticleFragment newInstance(int columnCount) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unused")
    public static ArticleFragment newInstance(String author) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, 1);
        args.putString(ARG_AUTHOR, author);

        fragment.setArguments(args);

        return fragment;
    }

    @SuppressWarnings("unused")
    public static ArticleFragment newInstance(int columnCount, String featured) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_FEATURED, featured);

        fragment.setArguments(args);

        return fragment;
    }

    @SuppressWarnings("unused")
    public static ArticleFragment newInstance(int columnCount, int categoryId, String category) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY, category);

        fragment.setArguments(args);

        return fragment;
    }

    @SuppressWarnings("unused")
    public static ArticleFragment newInstance(int columnCount, int categoryId, String category, int subcategoryId, String subcategory) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY, category);
        args.putInt(ARG_SUBCATEGORY_ID, subcategoryId);
        args.putString(ARG_SUBCATEGORY, subcategory);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mCategoryId = getArguments().getInt(ARG_CATEGORY_ID);
            mSubcategoryId = getArguments().getInt(ARG_SUBCATEGORY_ID);
            mCategory = getArguments().getString(ARG_CATEGORY);
            mSubcategory = getArguments().getString(ARG_SUBCATEGORY);
            mFeatured = getArguments().getString(ARG_FEATURED);
            mAuthor = getArguments().getString(ARG_AUTHOR);
        }

        if(mSubcategoryId > 0 && mSubcategory != null){
            Log.i("ARTICLE SUB CAT", mSubcategory+" id : "+mSubcategoryId);
        }
        else  if(mCategoryId > 0 && mCategory != null){
            Log.i("ARTICLE", mCategory+" id : "+mCategoryId);
        }
        else if(mFeatured != null){
            hasHeader = true;
            Log.i("ARTICLE FEATURED", mFeatured);
        }
        else if(mAuthor != null){
            Log.i("ARTICLE CONTRIBUTOR", mAuthor);
        }
        else{
            Log.i("ARTICLE", "DEFAULT");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            final List<DummyItem> allArticles = DummyArticleContent.generateDummy(0);
            final ArticleRecyclerViewAdapter articleAdapter = new ArticleRecyclerViewAdapter(allArticles, mListener, hasHeader);
            recyclerView.setAdapter(articleAdapter);

            final LinearLayoutManager linearLayoutManager;

            if (mColumnCount <= 1) {
                linearLayoutManager = new LinearLayoutManager(context);
            } else {
                linearLayoutManager = new GridLayoutManager(context, mColumnCount);
            }

            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    allArticles.add(null);
                    articleAdapter.notifyItemInserted(allArticles.size() - 1);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            allArticles.remove(allArticles.size() - 1);
                            articleAdapter.notifyItemRemoved(allArticles.size());

                            List<DummyItem> moreArticles = DummyArticleContent.generateDummy(page);
                            int curSize = articleAdapter.getItemCount();
                            allArticles.addAll(moreArticles);
                            articleAdapter.notifyItemRangeInserted(curSize, allArticles.size() - 1);
                            Log.i("ARTICLE LOAD", "MORE");
                        }
                    }, 3000);
                }
            });

        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnArticleFragmentInteractionListener) {
            mListener = (OnArticleFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnArticleFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnArticleFragmentInteractionListener {
        void onArticleFragmentInteraction(DummyItem item);
    }
}
