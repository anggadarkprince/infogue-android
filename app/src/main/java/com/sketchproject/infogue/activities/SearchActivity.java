package com.sketchproject.infogue.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.adapters.ArticleRecyclerViewAdapter;
import com.sketchproject.infogue.adapters.FollowerRecyclerViewAdapter;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.IconizedMenu;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements
        FollowerFragment.OnListFragmentInteractionListener,
        ArticleFragment.OnArticleFragmentInteractionListener {

    public static final String QUERY_STRING = "query";

    private ConnectionDetector connectionDetector;
    private SessionManager session;

    private ProgressBar mSearchProgress;
    private ScrollView mSearchContainer;
    private RecyclerView mContributorRecycler;
    private RecyclerView mArticleRecycler;
    private Button mViewAllContributorButton;
    private Button mViewAllArticleButton;
    private TextView mTotalContributorView;
    private TextView mTotalArticleView;
    private View mControlButton;
    private Contributor mContributor;

    private List<Contributor> allContributors;
    private List<Article> allArticles;
    private FollowerRecyclerViewAdapter contributorAdapter;
    private ArticleRecyclerViewAdapter articleAdapter;

    private CharSequence[] itemsWithControl;
    private CharSequence[] itemsWithoutControl;

    private int mColumnCount;
    private int mResultContributor;
    private int mResultArticle;
    private String mSearchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(2);
        }

        connectionDetector = new ConnectionDetector(getBaseContext());
        session = new SessionManager(getBaseContext());

        mSearchProgress = (ProgressBar) findViewById(R.id.search_progress);
        mSearchContainer = (ScrollView) findViewById(R.id.scroll_container);
        mArticleRecycler = (RecyclerView) findViewById(R.id.article_list);
        mContributorRecycler = (RecyclerView) findViewById(R.id.contributor_list);
        mTotalContributorView = (TextView) findViewById(R.id.contributor_found);
        mTotalArticleView = (TextView) findViewById(R.id.article_found);
        mViewAllContributorButton = (Button) findViewById(R.id.btn_contributor_more);
        mViewAllArticleButton = (Button) findViewById(R.id.btn_article_more);

        allContributors = new ArrayList<>();
        allArticles = new ArrayList<>();
        contributorAdapter = null;
        articleAdapter = null;

        mColumnCount = 1;
        mResultContributor = 0;
        mResultArticle = 0;

        itemsWithoutControl = new CharSequence[]{
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
        };

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Result for " + query);
            }

            Log.i("INFOGUE/Search", query);
            mSearchQuery = query;
            setupSearchResult();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupSearchResult() {
        showProgress(true);
        JsonObjectRequest menuRequest = new JsonObjectRequest(Request.Method.GET, UrlHelper.getApiSearchUrl(mSearchQuery, UrlHelper.SEARCH_BOTH), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString(Constant.RESPONSE_STATUS);
                            JSONObject contributors = response.getJSONObject("contributors");
                            JSONObject articles = response.getJSONObject("articles");

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                mResultContributor = contributors.getInt("total");
                                mResultArticle = articles.getInt("total");

                                populateContributorResult(contributors.getJSONArray("data"));
                                populateArticleResult(articles.getJSONArray("data"));
                            } else {
                                String successMessage = getString(R.string.error_unknown);
                                AppHelper.toastColored(getBaseContext(), successMessage, ContextCompat.getColor(getBaseContext(), R.color.primary));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String successMessage = getString(R.string.error_parse_data);
                            AppHelper.toastColored(getBaseContext(), successMessage, ContextCompat.getColor(getBaseContext(), R.color.primary));
                        }
                        showProgress(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_unknown);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = getString(R.string.error_no_connection);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.optString(Constant.RESPONSE_STATUS);
                                String message = response.optString(Constant.RESPONSE_MESSAGE);

                                Log.e("Infogue/Search", "Error::" + message);

                                if (status.equals(Constant.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                } else if (message != null) {
                                    errorMessage = message;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }
                        AppHelper.toastColored(getBaseContext(), errorMessage,
                                ContextCompat.getColor(getBaseContext(), R.color.color_danger));

                        showProgress(false);
                    }
                }
        );
        menuRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(menuRequest);
    }

    private void populateContributorResult(JSONArray contributors) throws JSONException {
        final LinearLayoutManager layoutContributor;
        if (mColumnCount <= 1) {
            layoutContributor = new LinearLayoutManager(getBaseContext());
        } else {
            layoutContributor = new GridLayoutManager(getBaseContext(), mColumnCount);
        }

        allContributors = new ArrayList<>();
        if (contributors != null) {
            for (int i = 0; i < contributors.length(); i++) {
                JSONObject contributorData = contributors.getJSONObject(i);

                Contributor contributor = new Contributor();
                contributor.setId(contributorData.getInt(Contributor.CONTRIBUTOR_ID));
                contributor.setToken(contributorData.getString(Contributor.CONTRIBUTOR_TOKEN));
                contributor.setUsername(contributorData.getString(Contributor.CONTRIBUTOR_USERNAME));
                contributor.setName(contributorData.getString(Contributor.CONTRIBUTOR_NAME));
                contributor.setEmail(contributorData.getString(Contributor.CONTRIBUTOR_EMAIL));
                contributor.setLocation(contributorData.getString(Contributor.CONTRIBUTOR_LOCATION));
                contributor.setAbout(contributorData.getString(Contributor.CONTRIBUTOR_ABOUT));
                contributor.setAvatar(contributorData.getString(Contributor.CONTRIBUTOR_AVATAR_REF));
                contributor.setCover(contributorData.getString(Contributor.CONTRIBUTOR_COVER_REF));
                contributor.setStatus(contributorData.getString(Contributor.CONTRIBUTOR_STATUS));
                contributor.setArticle(contributorData.getInt(Contributor.CONTRIBUTOR_ARTICLE));
                contributor.setFollowers(contributorData.getInt(Contributor.CONTRIBUTOR_FOLLOWERS));
                contributor.setFollowing(contributorData.getInt(Contributor.CONTRIBUTOR_FOLLOWING));
                contributor.setIsFollowing(contributorData.getInt(Contributor.CONTRIBUTOR_IS_FOLLOWING) == 1);

                allContributors.add(contributor);
            }
        }

        if (allContributors.size() <= 0) {
            Contributor emptyContributor = new Contributor(0, null);
            allContributors.add(emptyContributor);
        }
        contributorAdapter = new FollowerRecyclerViewAdapter(allContributors, this, "Contributor");
        mContributorRecycler.setAdapter(contributorAdapter);
        mContributorRecycler.setLayoutManager(layoutContributor);

        String resultTotal = "FOUND " + String.valueOf(mResultContributor);
        mTotalContributorView.setText(resultTotal);
        if (mResultContributor > 4) {
            mViewAllContributorButton.setVisibility(View.VISIBLE);
            mViewAllContributorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent contributorIntent = new Intent(getBaseContext(), FollowerActivity.class);
                    contributorIntent.putExtra(FollowerActivity.SCREEN_REQUEST, FollowerActivity.CONTRIBUTOR_SCREEN);
                    contributorIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                    contributorIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));
                    contributorIntent.putExtra(QUERY_STRING, mSearchQuery);
                    startActivity(contributorIntent);
                }
            });
        } else {
            mViewAllContributorButton.setVisibility(View.GONE);
        }
    }

    private void populateArticleResult(JSONArray articles) throws JSONException {
        LinearLayoutManager layoutArticle;
        if (mColumnCount <= 1) {
            layoutArticle = new LinearLayoutManager(getBaseContext());
        } else {
            layoutArticle = new GridLayoutManager(getBaseContext(), mColumnCount);
        }

        allArticles = new ArrayList<>();
        if (articles != null) {
            for (int i = 0; i < articles.length(); i++) {
                JSONObject articleData = articles.getJSONObject(i);

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
                article.setContentUpdate(articleData.getString(Article.ARTICLE_CONTENT_UPDATE));
                article.setPublishedAt(articleData.getString(Article.ARTICLE_PUBLISHED_AT));
                article.setView(articleData.getInt(Article.ARTICLE_VIEW));
                article.setRating(articleData.getInt(Article.ARTICLE_RATING_TOTAL));
                article.setStatus(articleData.getString(Article.ARTICLE_STATUS));

                allArticles.add(article);
            }
        }
        if (allArticles.size() <= 0) {
            Article emptyArticle = new Article(0, null);
            allArticles.add(emptyArticle);

            LinearLayout articleWrapper = (LinearLayout) findViewById(R.id.article_wrapper);
            if (articleWrapper != null) {
                articleWrapper.setBackgroundResource(R.color.light);
            }
        }
        articleAdapter = new ArticleRecyclerViewAdapter(allArticles, this, false);
        mArticleRecycler.setAdapter(articleAdapter);
        mArticleRecycler.setLayoutManager(layoutArticle);

        String resultTotal = "FOUND " + String.valueOf(mResultArticle);
        mTotalArticleView.setText(resultTotal);
        if (mResultArticle > 8) {
            mViewAllArticleButton.setVisibility(View.VISIBLE);
            mViewAllArticleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                    articleIntent.putExtra(SessionManager.KEY_ID, 0);
                    articleIntent.putExtra(SessionManager.KEY_USERNAME, "");
                    articleIntent.putExtra(QUERY_STRING, mSearchQuery);
                    startActivity(articleIntent);
                }
            });
        } else {
            mViewAllArticleButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                boolean isFollowing = data.getBooleanExtra(SessionManager.KEY_IS_FOLLOWING, false);
                Log.i("INFOGUE/Follower", "Result " + isFollowing);
                mContributor.setIsFollowing(isFollowing);
                if (isFollowing) {
                    ((ImageButton) mControlButton).setImageResource(R.drawable.btn_unfollow);
                } else {
                    ((ImageButton) mControlButton).setImageResource(R.drawable.btn_follow);
                }
            }
        }
    }

    @Override
    public void onListFragmentInteraction(Contributor contributor, View followControl) {
        if (connectionDetector.isNetworkAvailable()) {
            showProfile(contributor, followControl);
            mContributor = contributor;
        } else {
            lostConnectionNotification();
        }
    }

    @Override
    public void onListFollowControlInteraction(View view, View followControl, final Contributor contributor) {
        final SessionManager session = new SessionManager(getBaseContext());
        if (session.isLoggedIn()) {
            final ImageButton control = (ImageButton) followControl;
            if (contributor.isFollowing()) {
                control.setImageResource(R.drawable.btn_follow);
                contributor.setIsFollowing(false);

                StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_UNFOLLOW,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject result = new JSONObject(response);
                                    String status = result.getString(Constant.RESPONSE_STATUS);
                                    String message = result.getString(Constant.RESPONSE_MESSAGE);

                                    if (status.equals(Constant.REQUEST_SUCCESS)) {
                                        Log.i("Infogue/Unfollow", message);
                                    } else {
                                        Log.w("Infogue/Unfollow", getString(R.string.error_unknown));
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

                                NetworkResponse networkResponse = error.networkResponse;
                                String errorMessage = getString(R.string.error_unknown);
                                if (networkResponse == null) {
                                    if (error.getClass().equals(TimeoutError.class)) {
                                        errorMessage = getString(R.string.error_timeout);
                                    }
                                } else {
                                    String result = new String(networkResponse.data);
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        String status = response.optString(Constant.RESPONSE_STATUS);
                                        String message = response.optString(Constant.RESPONSE_MESSAGE);

                                        if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                            errorMessage = getString(R.string.error_unauthorized);
                                        } else if (status.equals(Constant.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                            errorMessage = message;
                                        } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                            errorMessage = getString(R.string.error_server);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                AppHelper.toastColored(getBaseContext(), errorMessage,
                                        ContextCompat.getColor(getBaseContext(), R.color.color_danger));

                                control.setImageResource(R.drawable.btn_unfollow);
                                contributor.setIsFollowing(true);
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("api_token", session.getSessionData(SessionManager.KEY_TOKEN, null));
                        params.put("contributor_id", String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                        params.put("following_id", String.valueOf(contributor.getId()));
                        params.put("_method", "delete");
                        return params;
                    }
                };
                postRequest.setRetryPolicy(new DefaultRetryPolicy(
                        15000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
            } else {
                control.setImageResource(R.drawable.btn_unfollow);
                contributor.setIsFollowing(true);

                StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_FOLLOW,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject result = new JSONObject(response);
                                    String status = result.getString(Constant.RESPONSE_STATUS);
                                    String message = result.getString(Constant.RESPONSE_MESSAGE);

                                    if (status.equals(Constant.REQUEST_SUCCESS)) {
                                        Log.i("Infogue/Follow", message);
                                    } else {
                                        Log.w("Infogue/Follow", getString(R.string.error_unknown));
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

                                NetworkResponse networkResponse = error.networkResponse;
                                String errorMessage = getString(R.string.error_unknown);
                                if (networkResponse == null) {
                                    if (error.getClass().equals(TimeoutError.class)) {
                                        errorMessage = getString(R.string.error_timeout);
                                    }
                                } else {
                                    String result = new String(networkResponse.data);
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        String status = response.optString(Constant.RESPONSE_STATUS);
                                        String message = response.optString(Constant.RESPONSE_MESSAGE);

                                        if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                            errorMessage = getString(R.string.error_unauthorized);
                                        } else if (status.equals(Constant.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                            errorMessage = message;
                                        } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                            errorMessage = message;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                AppHelper.toastColored(getBaseContext(), errorMessage,
                                        ContextCompat.getColor(getBaseContext(), R.color.color_danger));

                                control.setImageResource(R.drawable.btn_follow);
                                contributor.setIsFollowing(false);
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("api_token", session.getSessionData(SessionManager.KEY_TOKEN, null));
                        params.put("contributor_id", String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                        params.put("following_id", String.valueOf(contributor.getId()));
                        return params;
                    }
                };
                postRequest.setRetryPolicy(new DefaultRetryPolicy(
                        15000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
            }
        } else {
            Intent authIntent = new Intent(getBaseContext(), AuthenticationActivity.class);
            startActivity(authIntent);
        }
    }

    @Override
    public void onListLongClickInteraction(final View view, final View followControl, final Contributor contributor) {
        itemsWithControl = new CharSequence[]{
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
                contributor.isFollowing() ? getString(R.string.action_long_unfollow) : getString(R.string.action_long_follow)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        SessionManager session = new SessionManager(getBaseContext());
        if (session.getSessionData(SessionManager.KEY_ID, 0) == contributor.getId()) {
            builder.setItems(itemsWithoutControl, new FollowerOnLongClickListener(contributor, followControl));
        } else {
            builder.setItems(itemsWithControl, new FollowerOnLongClickListener(contributor, followControl));
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    private class FollowerOnLongClickListener implements DialogInterface.OnClickListener{
        private Contributor contributor;
        private View followControl;

        public FollowerOnLongClickListener(Contributor contributor, View followControl){
            this.contributor = contributor;
            this.followControl = followControl;
        }

        @Override
        public void onClick(DialogInterface dialog, int item) {
            if (connectionDetector.isNetworkAvailable()) {
                String selectedItem = itemsWithControl[item].toString();
                if (selectedItem.equals(getString(R.string.action_long_open))) {
                    showProfile(contributor, followControl);
                    mContributor = contributor;
                }
                else if (selectedItem.equals(getString(R.string.action_long_browse))) {
                    String articleUrl = UrlHelper.getContributorUrl(contributor.getUsername());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                    startActivity(browserIntent);
                }
                else if (selectedItem.equals(getString(R.string.action_long_share))) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareContributorText(contributor.getUsername()));
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
                }
                else if (selectedItem.equals(getString(R.string.action_long_follow))) {
                    ((ImageButton) followControl).setImageResource(R.drawable.btn_unfollow);
                    contributor.setIsFollowing(true);
                }
                else if (selectedItem.equals(getString(R.string.action_long_unfollow))) {
                    ((ImageButton) followControl).setImageResource(R.drawable.btn_follow);
                    contributor.setIsFollowing(false);
                }
            } else {
                lostConnectionNotification();
            }
        }
    }

    @Override
    public void onArticleFragmentInteraction(View view, Article article) {
        if (connectionDetector.isNetworkAvailable()) {
            Log.i("INFOGUE/Article", article.getId() + " " + article.getSlug() + " " + article.getTitle());
            Intent postIntent = new Intent(getBaseContext(), PostActivity.class);
            postIntent.putExtra(Article.ARTICLE_ID, article.getId());
            postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
            postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
            postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
            startActivity(postIntent);
            connectionDetector.dismissNotification();
        } else {
            lostConnectionNotification();
        }
    }

    @Override
    public void onArticlePopupInteraction(View view, final Article article) {
        IconizedMenu popup = new IconizedMenu(new ContextThemeWrapper(view.getContext(), R.style.AppTheme_PopupOverlay), view);
        popup.inflate(R.menu.article);
        popup.setGravity(Gravity.END);
        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (connectionDetector.isNetworkAvailable()) {
                    if (id == R.id.action_view) {
                        Intent postIntent = new Intent(getBaseContext(), PostActivity.class);
                        postIntent.putExtra(Article.ARTICLE_ID, article.getId());
                        postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
                        postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
                        postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
                        startActivity(postIntent);
                    } else if (id == R.id.action_browse) {
                        String articleUrl = UrlHelper.getArticleUrl(article.getSlug());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                        startActivity(browserIntent);
                    } else if (id == R.id.action_share) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareArticleText(article.getSlug()));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
                    } else if (id == R.id.action_rate) {
                        rateArticle(article);
                    }
                    connectionDetector.dismissNotification();
                } else {
                    lostConnectionNotification();
                }

                return false;
            }
        });
        popup.show();
    }

    private void rateArticle(final Article article) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_RATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(Constant.RESPONSE_STATUS);
                            String message = result.getString(Constant.RESPONSE_MESSAGE);

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Rate", "Success::Average rating for article id " + article.getId() + " is " + message);
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

                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_unknown);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.getString(Constant.RESPONSE_STATUS);
                                String message = response.getString(Constant.RESPONSE_MESSAGE);

                                Log.i("Infogue/Article", "Error::" + message);

                                if (status.equals(Constant.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        String rateMessage = errorMessage + "\r\nYour rating was discarded";
                        AppHelper.toastColored(getBaseContext(), rateMessage, ContextCompat.getColor(getBaseContext(), R.color.color_danger));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Article.ARTICLE_FOREIGN, String.valueOf(article.getId()));
                params.put(Article.ARTICLE_RATE, String.valueOf(5));
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);

        String successMessage = "Awesome!, you give 5 Stars on \n\r\"" + article.getTitle() + "\"";
        AppHelper.toastColored(getBaseContext(), successMessage, ContextCompat.getColor(getBaseContext(), R.color.primary));
    }

    @Override
    public void onArticleLongClickInteraction(View view, final Article article) {
        final CharSequence[] items = {
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
                getString(R.string.action_long_rate)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (connectionDetector.isNetworkAvailable()) {
                    if (items[item].toString().equals(getString(R.string.action_long_open))) {
                        Intent postIntent = new Intent(getBaseContext(), PostActivity.class);
                        postIntent.putExtra(Article.ARTICLE_ID, article.getId());
                        postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
                        postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
                        postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
                        startActivity(postIntent);
                    } else if (items[item].toString().equals(getString(R.string.action_long_browse))) {
                        String articleUrl = UrlHelper.getArticleUrl(article.getSlug());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                        startActivity(browserIntent);
                    } else if (items[item].toString().equals(getString(R.string.action_long_share))) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareArticleText(article.getSlug()));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
                    } else if (items[item].toString().equals(getString(R.string.action_long_rate))) {
                        rateArticle(article);
                    }
                } else {
                    lostConnectionNotification();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showProgress(final boolean show) {
        int mediumAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mSearchContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        mSearchContainer
                .animate()
                .setDuration(mediumAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSearchContainer.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

        mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mSearchProgress.animate()
                .setDuration(mediumAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void showProfile(Contributor contributor, View buttonControl) {
        Log.i("INFOGUE/Contributor", contributor.getId() + " " + contributor.getUsername());
        mControlButton = buttonControl;

        Intent profileIntent = new Intent(getBaseContext(), ProfileActivity.class);
        profileIntent.putExtra(SessionManager.KEY_ID, contributor.getId());
        profileIntent.putExtra(SessionManager.KEY_USERNAME, contributor.getUsername());
        profileIntent.putExtra(SessionManager.KEY_NAME, contributor.getName());
        profileIntent.putExtra(SessionManager.KEY_LOCATION, contributor.getLocation());
        profileIntent.putExtra(SessionManager.KEY_ABOUT, contributor.getAbout());
        profileIntent.putExtra(SessionManager.KEY_AVATAR, contributor.getAvatar());
        profileIntent.putExtra(SessionManager.KEY_COVER, contributor.getCover());
        profileIntent.putExtra(SessionManager.KEY_STATUS, contributor.getStatus());
        profileIntent.putExtra(SessionManager.KEY_ARTICLE, contributor.getArticle());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWER, contributor.getFollowers());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWING, contributor.getFollowing());
        profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, contributor.isFollowing());
        startActivityForResult(profileIntent, ProfileActivity.PROFILE_RESULT_CODE);
    }

    private void lostConnectionNotification() {
        connectionDetector.snackbarDisconnectNotification(mSearchContainer, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();
            }
        });
    }
}
