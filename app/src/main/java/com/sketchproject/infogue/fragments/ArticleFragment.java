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
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ApplicationActivity;
import com.sketchproject.infogue.activities.ArticleActivity;
import com.sketchproject.infogue.adapters.ArticleRecyclerViewAdapter;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnArticleInteractionListener}
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
    private static final String ARG_AUTHOR_USERNAME = "author-username";
    private static final String ARG_AUTHOR_IS_ME = "author-is-me";
    private static final String ARG_QUERY = "search-query";
    private static final String ARG_TAG = "tag";

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
    private String mAuthorUsername;
    private String mQuery;
    private String mTag;
    private boolean mMyArticle = false;
    private boolean hasHeader = false;
    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Article> allArticles = new ArrayList<>();
    private ArticleRecyclerViewAdapter articleAdapter;
    private OnArticleInteractionListener mArticleListListener;
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

    public static ArticleFragment newInstanceAuthor(int columnCount, int id, String username, boolean isMyArticle) {
        ArticleFragment fragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_AUTHOR_ID, id);
        args.putString(ARG_AUTHOR_USERNAME, username);
        args.putBoolean(ARG_AUTHOR_IS_ME, isMyArticle);

        fragment.setArguments(args);
        return fragment;
    }

    public static ArticleFragment newInstanceQuery(int columnCount, String query) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_QUERY, query);

        fragment.setArguments(args);
        return fragment;
    }

    public static ArticleFragment newInstanceTag(int columnCount, String tag) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_TAG, tag);

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
            mAuthorUsername = getArguments().getString(ARG_AUTHOR_USERNAME);
            mMyArticle = getArguments().getBoolean(ARG_AUTHOR_IS_ME);
            mQuery = getArguments().getString(ARG_QUERY);
            mTag = getArguments().getString(ARG_TAG);
        }

        if (mSubcategoryId > 0 && mSubcategory != null) {
            Log.i("INFOGUE/Article", "Sub Category " + mSubcategory + " ID : " + mSubcategoryId);
            apiArticleUrl = APIBuilder.getApiCategoryUrl(mCategory, mSubcategory, 0);
        } else if (mCategoryId > 0 && mCategory != null) {
            Log.i("INFOGUE/Article", "Category " + mCategory + " ID : " + mCategoryId);
            apiArticleUrl = APIBuilder.getApiCategoryUrl(mCategory, null, 0);
        } else if (mFeatured != null) {
            hasHeader = true;
            Log.i("INFOGUE/Article", "Featured : " + mFeatured);
            apiArticleUrl = APIBuilder.getApiFeaturedUrl(mFeatured, 0);
        } else if (mAuthorId != 0) {
            Log.i("INFOGUE/Article", "Contributor ID : " + String.valueOf(mAuthorId) + " Username : " + mAuthorUsername);
            apiArticleUrl = APIBuilder.getApiArticleUrl(mAuthorId, mAuthorUsername, mMyArticle, mQuery);
        } else if (mTag != null && !mTag.isEmpty()) {
            Log.i("INFOGUE/Article", "Tag : " + mTag);
            apiArticleUrl = APIBuilder.getApiTagUrl(mTag);
        } else if (mQuery != null && !mQuery.isEmpty()) {
            Log.i("INFOGUE/Article", "Query : " + mQuery);
            apiArticleUrl = APIBuilder.getApiSearchUrl(mQuery, APIBuilder.SEARCH_ARTICLE, mAuthorId);
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
                public void onReachTop(boolean isFirst) {
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

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
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
                                            article.setId(articleData.getInt(Article.ID));
                                            article.setSlug(articleData.getString(Article.SLUG));
                                            article.setTitle(articleData.getString(Article.TITLE));
                                            article.setFeatured(articleData.getString(Article.FEATURED_REF));
                                            article.setCategoryId(articleData.getInt(Article.CATEGORY_ID));
                                            article.setCategory(articleData.getString(Article.CATEGORY));
                                            article.setSubcategoryId(articleData.getInt(Article.SUBCATEGORY_ID));
                                            article.setSubcategory(articleData.getString(Article.SUBCATEGORY));
                                            article.setContent(articleData.getString(Article.CONTENT));
                                            article.setContentUpdate(articleData.getString(Article.CONTENT_UPDATE));
                                            article.setPublishedAt(articleData.getString(Article.PUBLISHED_AT));
                                            article.setView(articleData.getInt(Article.VIEW));
                                            article.setRating(articleData.getInt(Article.RATING_TOTAL));
                                            article.setStatus(articleData.getString(Article.STATUS));
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
                                    Helper.toastColor(getContext(), R.string.error_server, R.color.color_warning_transparent);

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
                            error.printStackTrace();

                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            // remove last loading
                            allArticles.remove(allArticles.size() - 1);
                            articleAdapter.notifyItemRemoved(allArticles.size());

                            NetworkResponse networkResponse = error.networkResponse;
                            String errorMessage = getActivity().getString(R.string.error_unknown);
                            if (networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = getActivity().getString(R.string.error_timeout);
                                } else if (error.getClass().equals(NoConnectionError.class)) {
                                    errorMessage = getString(R.string.error_no_connection);
                                }
                            }
                            else{
                                if (networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                }
                            }
                            Helper.toastColor(getContext(), errorMessage, R.color.color_danger_transparent);

                            // add error view holder
                            isEndOfPage = true;
                            Article emptyArticle = new Article(0, null, "Error page");
                            allArticles.add(emptyArticle);
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

        if (context instanceof OnArticleInteractionListener) {
            mArticleListListener = (OnArticleInteractionListener) context;

            if (mMyArticle) {
                if (context instanceof OnArticleEditableFragmentInteractionListener) {
                    mArticleEditableListener = (OnArticleEditableFragmentInteractionListener) context;
                } else {
                    throw new RuntimeException(context.toString() + " must implement OnArticleEditableFragmentInteractionListener");
                }
            }
        } else {
            throw new RuntimeException(context.toString() + " must implement OnArticleInteractionListener");
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
    public interface OnArticleInteractionListener {
        void onArticleInteraction(View view, Article article);

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
