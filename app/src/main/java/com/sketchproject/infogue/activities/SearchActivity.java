package com.sketchproject.infogue.activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.adapters.ArticleRecyclerViewAdapter;
import com.sketchproject.infogue.adapters.FollowerRecyclerViewAdapter;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.fragments.dummy.DummyArticleContent;
import com.sketchproject.infogue.fragments.dummy.DummyFollowerContent;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.IconizedMenu;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.UrlHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements
        FollowerFragment.OnListFragmentInteractionListener,
        ArticleFragment.OnArticleFragmentInteractionListener {

    public static final String QUERY_STRING = "query";

    private ConnectionDetector connectionDetector;
    private SessionManager session;

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
        mResultContributor = 20;
        mResultArticle = 50;

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
        // result of contributors
        LinearLayoutManager layoutContributor;
        if (mColumnCount <= 1) {
            layoutContributor = new LinearLayoutManager(getBaseContext());
        } else {
            layoutContributor = new GridLayoutManager(getBaseContext(), mColumnCount);
        }

        allContributors = DummyFollowerContent.generateDummy(0, 3);
        if (allContributors.size() <= 0) {
            Contributor emptyContributor = new Contributor(0, null);
            allContributors.add(emptyContributor);
        }
        contributorAdapter = new FollowerRecyclerViewAdapter(allContributors, this, "Contributor");
        mContributorRecycler.setAdapter(contributorAdapter);
        mContributorRecycler.setLayoutManager(layoutContributor);

        mTotalContributorView.setText("FOUND " + String.valueOf(mResultContributor));
        if (mResultContributor > 3) {
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

        // result of articles
        LinearLayoutManager layoutArticle;
        if (mColumnCount <= 1) {
            layoutArticle = new LinearLayoutManager(getBaseContext());
        } else {
            layoutArticle = new GridLayoutManager(getBaseContext(), mColumnCount);
        }

        allArticles = DummyArticleContent.generateDummy(0, 8);
        if (allArticles.size() <= 0) {
            Article emptyArticle = new Article(0, null, "Empty page");
            allArticles.add(emptyArticle);
            //noinspection ConstantConditions
            findViewById(R.id.article_wrapper).setBackgroundResource(R.color.light);
        }
        articleAdapter = new ArticleRecyclerViewAdapter(allArticles, this, false);
        mArticleRecycler.setAdapter(articleAdapter);
        mArticleRecycler.setLayoutManager(layoutArticle);

        mTotalArticleView.setText("FOUND " + String.valueOf(mResultArticle));
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
        // Inflate the menu; this adds items to the action bar if it is present.
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
    public void onListFollowControlInteraction(View view, View followControl, Contributor contributor) {
        SessionManager session = new SessionManager(getBaseContext());
        if (session.isLoggedIn()) {
            ImageButton control = (ImageButton) followControl;
            if (contributor.isFollowing()) {
                control.setImageResource(R.drawable.btn_follow);
                contributor.setIsFollowing(false);
            } else {
                control.setImageResource(R.drawable.btn_unfollow);
                contributor.setIsFollowing(true);
            }
        } else {
            Intent authIntent = new Intent(getBaseContext(), AuthenticationActivity.class);
            startActivity(authIntent);
        }
    }

    @Override
    public void onListLongClickInteraction(final View view, final View followControl, final Contributor contributor) {
        final CharSequence[] items = {
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
                contributor.isFollowing() ? getString(R.string.action_long_unfollow) : getString(R.string.action_long_follow)
        };

        final CharSequence[] itemsWithoutControl = {
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SessionManager session = new SessionManager(getBaseContext());
        if (session.getSessionData(SessionManager.KEY_ID, 0) == contributor.getId()) {
            builder.setItems(itemsWithoutControl, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (connectionDetector.isNetworkAvailable()) {
                        String selectedItem = items[item].toString();
                        if (selectedItem.equals(getString(R.string.action_long_open))) {
                            showProfile(contributor, followControl);
                            mContributor = contributor;
                        } else if (selectedItem.equals(getString(R.string.action_long_browse))) {
                            String articleUrl = UrlHelper.getContributorUrl(contributor.getUsername());
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                            startActivity(browserIntent);
                        } else if (selectedItem.equals(getString(R.string.action_long_share))) {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareContributorText(contributor.getUsername()));
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
                        }
                    } else {
                        lostConnectionNotification();
                    }
                }
            });
        } else {
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (connectionDetector.isNetworkAvailable()) {
                        String selectedItem = items[item].toString();
                        if (selectedItem.equals(getString(R.string.action_long_open))) {
                            showProfile(contributor, followControl);
                            mContributor = contributor;
                        } else if (selectedItem.equals(getString(R.string.action_long_browse))) {
                            String articleUrl = UrlHelper.getContributorUrl(contributor.getUsername());
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                            startActivity(browserIntent);
                        } else if (selectedItem.equals(getString(R.string.action_long_share))) {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareContributorText(contributor.getUsername()));
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
                        } else if (selectedItem.equals(getString(R.string.action_long_follow))) {
                            ((ImageButton) followControl).setImageResource(R.drawable.btn_unfollow);
                            contributor.setIsFollowing(true);
                            AppHelper.toastColored(view.getContext(), "Awesome!, you now is following\n\"" + contributor.getName() + "\"");
                        } else if (selectedItem.equals(getString(R.string.action_long_unfollow))) {
                            ((ImageButton) followControl).setImageResource(R.drawable.btn_follow);
                            contributor.setIsFollowing(false);
                            AppHelper.toastColored(view.getContext(), "Too bad!, you stop following\n\"" + contributor.getName() + "\"");
                        }
                    } else {
                        lostConnectionNotification();
                    }
                }
            });
        }
        AlertDialog alert = builder.create();
        alert.show();
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
                        AppHelper.toastColored(getBaseContext(), "Awesome!, you give 5 Stars on \"" + article.getTitle() + "\"", Color.parseColor("#ddd1205e"));
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
                        AppHelper.toastColored(getBaseContext(), "Awesome!, you give 5 Stars on \"" + article.getTitle() + "\"", Color.parseColor("#ddd1205e"));
                    }
                } else {
                    lostConnectionNotification();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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
        profileIntent.putExtra(SessionManager.KEY_ARTICLE, contributor.getArticle());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWER, contributor.getFollowers());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWING, contributor.getFollowing());
        profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, contributor.isFollowing());
        startActivityForResult(profileIntent, 200);
    }

    private void lostConnectionNotification() {
        connectionDetector.snackbarDisconnectNotification(findViewById(R.id.scroll_container), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();
            }
        });
    }
}
