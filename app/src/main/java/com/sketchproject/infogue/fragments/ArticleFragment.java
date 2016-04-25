package com.sketchproject.infogue.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ApplicationActivity;
import com.sketchproject.infogue.activities.ArticleActivity;
import com.sketchproject.infogue.adapters.ArticleRecyclerViewAdapter;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnArticleFragmentInteractionListener}
 * interface.
 */
public class ArticleFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_CATEGORY_ID = "category-id";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_SUBCATEGORY_ID = "subcategory-id";
    private static final String ARG_SUBCATEGORY = "subcategory";
    private static final String ARG_FEATURED = "featured";
    private static final String ARG_AUTHOR_ID = "author-id";
    private static final String ARG_AUTHOR_IS_ME = "author-is-me";
    private static final String ARG_QUERY = "search-query";

    public static final String FEATURED_LATEST = "latest";
    public static final String FEATURED_POPULAR = "popular";
    public static final String FEATURED_TRENDING = "trending";
    public static final String FEATURED_RANDOM = "random";
    public static final String FEATURED_HEADLINE = "headline";

    private int mColumnCount = 1;
    private int mCategoryId = 0;
    private int mSubcategoryId = 0;
    private String mCategory;
    private String mSubcategory;
    private String mFeatured;
    private int mAuthorId;
    private boolean mMyArticle = false;
    private boolean hasHeader = false;
    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Article> allArticles = new ArrayList<>();
    private ArticleRecyclerViewAdapter articleAdapter;
    private OnArticleFragmentInteractionListener mArticleListListener;
    private OnArticleEditableFragmentInteractionListener mArticleEditableListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private String apiArticleUrl = "";
    private String apiArticleUrlFirstPage = "";

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

    public static ArticleFragment newInstanceAuthor(int columnCount, int id, boolean isMyArticle, String query) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_AUTHOR_ID, id);
        args.putBoolean(ARG_AUTHOR_IS_ME, isMyArticle);
        args.putString(ARG_QUERY, query);

        fragment.setArguments(args);

        return fragment;
    }

    public static ArticleFragment newInstanceFeatured(int columnCount, String featured) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_FEATURED, featured);

        fragment.setArguments(args);

        return fragment;
    }

    public static ArticleFragment newInstanceCategory(int columnCount, int categoryId, String category) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY, category);

        fragment.setArguments(args);

        return fragment;
    }

    @SuppressWarnings("unused")
    public static ArticleFragment newInstanceSubCategory(int columnCount, int categoryId, String category, int subcategoryId, String subcategory) {
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
            mAuthorId = getArguments().getInt(ARG_AUTHOR_ID);
            mMyArticle = getArguments().getBoolean(ARG_AUTHOR_IS_ME);
        }

        if (mSubcategoryId > 0 && mSubcategory != null) {
            Log.i("INFOGUE/Article", "Sub Category " + mSubcategory + " ID : " + mSubcategoryId);
            apiArticleUrl = UrlHelper.getApiCategoryUrl(mCategory, mSubcategory, 0);
        } else if (mCategoryId > 0 && mCategory != null) {
            Log.i("INFOGUE/Article", "Category " + mCategory + " ID : " + mCategoryId);
            apiArticleUrl = UrlHelper.getApiCategoryUrl(mCategory, null, 0);
        } else if (mFeatured != null) {
            hasHeader = true;
            Log.i("INFOGUE/Article", "Featured : " + mFeatured);
            apiArticleUrl = UrlHelper.getApiFeaturedUrl(mFeatured, 0);
        } else if (mAuthorId != 0) {
            Log.i("INFOGUE/ARTICLE", "Contributor ID : " + String.valueOf(mAuthorId));
        } else {
            Log.i("INFOGUE/Article", "Default");
        }

        apiArticleUrlFirstPage = apiArticleUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager;
            if (mColumnCount <= 1) {
                linearLayoutManager = new LinearLayoutManager(context);
            } else {
                linearLayoutManager = new GridLayoutManager(context, mColumnCount);
            }

            if (mMyArticle) {
                articleAdapter = new ArticleRecyclerViewAdapter(allArticles, mArticleListListener, mArticleEditableListener);
            } else {
                articleAdapter = new ArticleRecyclerViewAdapter(allArticles, mArticleListListener, hasHeader);
            }
            recyclerView.setAdapter(articleAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    if (!isFirstCall) {
                        loadArticles(page);
                    }
                }

                @Override
                public void onFirstSight(boolean isFirst) {
                    if (getActivity() instanceof ArticleActivity) {
                        ((ArticleActivity) getActivity()).setSwipeEnable(isFirst);
                    } else if (getActivity() instanceof ApplicationActivity) {
                        ((ApplicationActivity) getActivity()).setSwipeEnable(isFirst);
                    }
                }
            });

            if (isFirstCall) {
                isFirstCall = false;
                loadArticles(0);
            }
        }
        return view;
    }

    /**
     * @param page starts at 0
     */
    private void loadArticles(final int page) {
        if (!isEndOfPage && apiArticleUrl != null) {
            if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                allArticles.add(null);
                articleAdapter.notifyItemInserted(allArticles.size() - 1);
            }

            Log.i("INFOGUE/Article", "URL " + apiArticleUrl);
            JsonObjectRequest articleRequest = new JsonObjectRequest(Request.Method.GET, apiArticleUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                JSONObject articles = response.getJSONObject("articles");
                                String nextUrl = articles.getString("next_page_url");
                                int currentPage = articles.getInt("current_page");
                                int lastPage = articles.getInt("last_page");
                                JSONArray data = articles.optJSONArray("data");

                                apiArticleUrl = nextUrl;

                                if (status.equals(Constant.REQUEST_SUCCESS)) {
                                    if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                                        allArticles.remove(allArticles.size() - 1);
                                        articleAdapter.notifyItemRemoved(allArticles.size());
                                    } else {
                                        swipeRefreshLayout.setRefreshing(false);
                                        int total = allArticles.size();
                                        for (int i = 0; i < total; i++) {
                                            allArticles.remove(0);
                                        }
                                        articleAdapter.notifyItemRangeRemoved(0, total);
                                    }

                                    List<Article> moreArticles = new ArrayList<>();

                                    if (data != null) {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject articleData = data.getJSONObject(i);
                                            Article article = new Article();
                                            article.setId(articleData.getInt(Article.ARTICLE_ID));
                                            article.setSlug(articleData.getString(Article.ARTICLE_SLUG));
                                            article.setTitle(articleData.getString(Article.ARTICLE_TITLE));
                                            article.setFeatured(articleData.getString(Article.ARTICLE_FEATURED_REF));
                                            article.setCategoryId(articleData.getInt(Article.ARTICLE_CATEGORY_ID));
                                            article.setCategory(articleData.getString(Article.ARTICLE_CATEGORY));
                                            article.setSubcategoryId(articleData.getInt(Article.ARTICLE_SUBCATEGORY_ID));
                                            article.setSubcategory(articleData.getString(Article.ARTICLE_SUBCATEGORY));
                                            article.setContent(articleData.getString(Article.ARTICLE_CONTENT));
                                            article.setPublishedAt(articleData.getString(Article.ARTICLE_PUBLISHED_AT));
                                            article.setView(articleData.getInt(Article.ARTICLE_VIEW));
                                            article.setRating(articleData.getInt(Article.ARTICLE_RATING));
                                            moreArticles.add(article);
                                        }
                                    }

                                    int curSize = articleAdapter.getItemCount();
                                    allArticles.addAll(moreArticles);

                                    if (allArticles.size() <= 0) {
                                        isEndOfPage = true;
                                        Log.i("INFOGUE/Article", "Empty on page " + page);
                                        Article emptyArticle = new Article(0, null, "Empty page");
                                        allArticles.add(emptyArticle);
                                    } else if (currentPage >= lastPage) {
                                        isEndOfPage = true;
                                        Log.i("INFOGUE/Article", "End on page " + page);
                                        Article endArticle = new Article(-1, null, "End of page");
                                        allArticles.add(endArticle);
                                    }

                                    articleAdapter.notifyItemRangeInserted(curSize, allArticles.size() - 1);
                                    Log.i("INFOGUE/Article", "Load More page " + page);
                                } else {
                                    isEndOfPage = true;
                                    Log.i("INFOGUE/Article", "Empty on page " + page);
                                    Article emptyArticle = new Article(0, null, "Empty page");
                                    allArticles.add(emptyArticle);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // remove loading
                            allArticles.remove(allArticles.size() - 1);
                            articleAdapter.notifyItemRemoved(allArticles.size());

                            // indicate the error
                            isEndOfPage = true;
                            Log.i("INFOGUE/Article", "Empty on page " + page);
                            Article emptyArticle = new Article(0, null, "Empty page");
                            allArticles.add(emptyArticle);
                            error.printStackTrace();
                        }
                    }
            );
            articleRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getContext()).addToRequestQueue(articleRequest);
        }
    }

    public void deleteArticleRow(int id) {
        Log.i("INFOGUE/Article", "Delete id : " + id);
        for (int i = 0; i < allArticles.size(); i++) {
            if (allArticles.get(i) != null && allArticles.get(i).getId() == id) {
                allArticles.remove(i);
                articleAdapter.notifyItemRemoved(i);
            }
        }
    }

    public void refreshArticleList(SwipeRefreshLayout swipeRefresh) {
        swipeRefreshLayout = swipeRefresh;
        isEndOfPage = false;
        apiArticleUrl = apiArticleUrlFirstPage;

        loadArticles(0);
    }

    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            mMyArticle = getArguments().getBoolean(ARG_AUTHOR_IS_ME);
        }

        if (context instanceof OnArticleFragmentInteractionListener) {
            mArticleListListener = (OnArticleFragmentInteractionListener) context;

            if (mMyArticle) {
                if (context instanceof OnArticleEditableFragmentInteractionListener) {
                    mArticleEditableListener = (OnArticleEditableFragmentInteractionListener) context;
                } else {
                    throw new RuntimeException(context.toString() + " must implement OnArticleEditableFragmentInteractionListener");
                }
            }
        } else {
            throw new RuntimeException(context.toString() + " must implement OnArticleFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mArticleListListener = null;
        if (mArticleEditableListener != null) {
            mArticleEditableListener = null;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnArticleFragmentInteractionListener {
        void onArticleFragmentInteraction(View view, Article article);

        void onArticlePopupInteraction(View view, Article article);

        void onArticleLongClickInteraction(View view, Article article);
    }

    public interface OnArticleEditableFragmentInteractionListener {
        void onBrowseClicked(View view, Article article);

        void onShareClicked(View view, Article article);

        void onEditClicked(View view, Article article);

        void onDeleteClicked(View view, Article article);
    }
}
