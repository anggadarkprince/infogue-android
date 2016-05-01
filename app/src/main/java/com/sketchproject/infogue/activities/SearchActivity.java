package com.sketchproject.infogue.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.adapters.ArticleRecyclerViewAdapter;
import com.sketchproject.infogue.adapters.FollowerRecyclerViewAdapter;
import com.sketchproject.infogue.events.ArticleContextBuilder;
import com.sketchproject.infogue.events.ArticleListEvent;
import com.sketchproject.infogue.events.ArticlePopupBuilder;
import com.sketchproject.infogue.events.FollowerContextBuilder;
import com.sketchproject.infogue.events.FollowerListEvent;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements
        FollowerFragment.OnFollowerInteractionListener,
        ArticleFragment.OnArticleInteractionListener {

    public static final String QUERY_STRING = "query";

    private SessionManager session;
    private ProgressBar mSearchProgress;
    private ScrollView mSearchContainer;
    private RecyclerView mContributorRecycler;
    private RecyclerView mArticleRecycler;
    private Button mViewAllContributorButton;
    private Button mViewAllArticleButton;
    private TextView mTotalContributorView;
    private TextView mTotalArticleView;

    private List<Contributor> allContributors;
    private List<Article> allArticles;
    private FollowerRecyclerViewAdapter contributorAdapter;
    private ArticleRecyclerViewAdapter articleAdapter;

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

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
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
        new FollowerListEvent(this)
                .handleProfileResult(requestCode, resultCode, data);
    }

    @Override
    public void onFollowerInteraction(Contributor contributor, View followControl) {
        new FollowerListEvent(this, contributor, followControl)
                .viewProfile();
    }

    @Override
    public void onFollowerControlInteraction(View view, View followControl, final Contributor contributor) {
        new FollowerListEvent(this, contributor, followControl)
                .followContributor();
    }

    @Override
    public void onFollowerLongClickInteraction(View view, View followControl, Contributor contributor) {
        new FollowerContextBuilder(this, contributor, followControl)
                .buildContext()
                .show();
    }

    @Override
    public void onArticleInteraction(View view, Article article) {
        new ArticleListEvent(this, article)
                .viewArticle();
    }

    @Override
    public void onArticlePopupInteraction(View view, final Article article) {
        new ArticlePopupBuilder(this, view, article)
                .buildPopup()
                .show();
    }

    @Override
    public void onArticleLongClickInteraction(View view, final Article article) {
        new ArticleContextBuilder(this, article)
                .buildContext()
                .show();
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

    private void setupSearchResult() {
        showProgress(true);
        String url = APIBuilder.getApiSearchUrl(mSearchQuery, APIBuilder.SEARCH_BOTH, session.getSessionData(SessionManager.KEY_ID, 0));
        JsonObjectRequest menuRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString(APIBuilder.RESPONSE_STATUS);
                            JSONObject contributors = response.getJSONObject(Contributor.TABLE);
                            JSONObject articles = response.getJSONObject(Article.TABLE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                mResultContributor = contributors.getInt("total");
                                mResultArticle = articles.getInt("total");

                                populateContributorResult(contributors.getJSONArray("data"));
                                populateArticleResult(articles.getJSONArray("data"));
                            } else {
                                String successMessage = getString(R.string.error_unknown);
                                Helper.toastColor(getBaseContext(), successMessage, R.color.color_warning_transparent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                                String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Search", "Error::" + message);

                                if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                } else if (message != null) {
                                    errorMessage = message;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }
                        Helper.toastColor(getBaseContext(), errorMessage, R.color.color_danger_transparent);

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
                contributor.setId(contributorData.getInt(Contributor.ID));
                contributor.setToken(contributorData.getString(Contributor.TOKEN));
                contributor.setUsername(contributorData.getString(Contributor.USERNAME));
                contributor.setName(contributorData.getString(Contributor.NAME));
                contributor.setEmail(contributorData.getString(Contributor.EMAIL));
                contributor.setLocation(contributorData.getString(Contributor.LOCATION));
                contributor.setAbout(contributorData.getString(Contributor.ABOUT));
                contributor.setAvatar(contributorData.getString(Contributor.AVATAR_REF));
                contributor.setCover(contributorData.getString(Contributor.COVER_REF));
                contributor.setStatus(contributorData.getString(Contributor.STATUS));
                contributor.setArticle(contributorData.getInt(Contributor.ARTICLE));
                contributor.setFollowers(contributorData.getInt(Contributor.FOLLOWERS));
                contributor.setFollowing(contributorData.getInt(Contributor.FOLLOWING));
                contributor.setIsFollowing(contributorData.getInt(Contributor.IS_FOLLOWING) == 1);

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

}
