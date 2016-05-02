package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.events.ArticleContextBuilder;
import com.sketchproject.infogue.events.ArticleListEvent;
import com.sketchproject.infogue.events.ArticlePopupBuilder;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.APIBuilder;

/**
 * A simple {@link AppCompatActivity} subclass to show credit title.
 *
 * Sketch Project Studio
 * Created by Angga on 7/04/2016 10.37.
 */
public class ArticleActivity extends AppCompatActivity implements
        ArticleFragment.OnArticleInteractionListener,
        ArticleFragment.OnArticleEditableInteractionListener {

    public static final String DISCARD_ARTICLE = "discard";
    public static final String SAVE_ARTICLE = "save";

    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Perform initialization of ArticleActivity.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        SessionManager session = new SessionManager(getBaseContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int authorId = extras.getInt(SessionManager.KEY_ID);
            String authorUsername = extras.getString(SessionManager.KEY_USERNAME);
            String query = extras.getString(SearchActivity.QUERY_STRING);
            String tag = extras.getString(Article.TAG);

            // set actionbar title and subtitle (if needed)
            if (getSupportActionBar() != null) {
                String title =getString(R.string.title_activity_article);
                if (query != null) {
                    title = "All result for " + query;
                } else if(tag != null){
                    getSupportActionBar().setSubtitle("TAG");
                    title = tag;
                }
                getSupportActionBar().setTitle(title);
            }


            // setup swipe layout
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setEnabled(true);
                swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        ArticleFragment fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                        fragment.refreshArticleList(swipeRefreshLayout);
                    }
                });
            }

            // this extras is sent from Article create/edit form activity
            boolean saveResult = extras.getBoolean(ArticleActivity.SAVE_ARTICLE);
            boolean isCalledFromMain = extras.getBoolean(ArticleCreateActivity.CALLED_FROM_MAIN);
            int resultCode = extras.getInt(ArticleCreateActivity.RESULT_CODE);

            if (isCalledFromMain) {
                handleResult(resultCode, saveResult);
            }

            // find out if this list is logged user's article, the show editable list marker
            Boolean isMyArticle = false;
            if (session.isLoggedIn() && session.isMe(authorId)) {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                if (fab != null) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent createArticleIntent = new Intent(getBaseContext(), ArticleCreateActivity.class);
                            startActivityForResult(createArticleIntent, ArticleCreateActivity.ARTICLE_FORM_CODE);
                        }
                    });
                }
                isMyArticle = true;
            }

            // determine type of article list which showed
            Fragment fragment;
            if (tag != null && !tag.isEmpty()) {
                fragment = ArticleFragment.newInstanceTag(1, tag);
            } else if (query != null && !query.isEmpty()) {
                fragment = ArticleFragment.newInstanceQuery(1, query);
            } else {
                fragment = ArticleFragment.newInstanceAuthor(1, authorId, authorUsername, isMyArticle);
            }

            // replacing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        }
    }

    /**
     * Set swipe to refresh enable or disable, user could swipe when reach top.
     *
     * @param state enable or not
     */
    public void setSwipeEnable(boolean state) {
        swipeRefreshLayout.setEnabled(state);
    }

    /**
     * Waiting result from create or edit form and passing to another method to handle it.
     *
     * @param requestCode catch code request when activity started
     * @param resultCode type of return state
     * @param data intent extras handle save status
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ArticleCreateActivity.ARTICLE_FORM_CODE && data != null) {
            boolean saveResult = data.getBooleanExtra(SAVE_ARTICLE, false);
            handleResult(resultCode, saveResult);
        }
    }

    /**
     * Handle result from create or edit form and decide what type of notification
     * must created and show.
     *
     * @param resultCode state return type RESULT_OK = save, RESULT_CANCELED = discard, else invalid
     * @param saveResult status of saving article data on serve.
     */
    private void handleResult(int resultCode, boolean saveResult) {
        final Snackbar snackbar = Snackbar.make(swipeRefreshLayout, R.string.message_article_saved, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(getBaseContext(), R.color.light));
        snackbar.setAction(R.string.action_ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        View snackbarView = snackbar.getView();

        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (saveResult) {
                snackbarView.setBackgroundResource(R.color.color_success);
                swipeRefreshLayout.setRefreshing(true);
                ArticleFragment fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                fragment.refreshArticleList(swipeRefreshLayout);
            } else {
                snackbar.setText(R.string.error_server);
                snackbarView.setBackgroundResource(R.color.color_danger);
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            snackbarView.setBackgroundResource(R.color.color_warning);
            snackbar.setText(R.string.message_article_discarded);
        } else {
            snackbarView.setBackgroundResource(R.color.color_danger);
            snackbar.setText(R.string.message_article_invalid);
        }

        snackbar.show();
    }

    /**
     * Create option menu.
     *
     * @param menu content of option menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    /**
     * Select action for option menu.
     *
     * @param item of selected option menu
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_feedback) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_FEEDBACK));
            startActivity(browserIntent);
        } else if (id == R.id.action_help) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_HELP));
            startActivity(browserIntent);
        } else if (id == R.id.action_rating) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_APP));
            startActivity(browserIntent);
        } else if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(getBaseContext(), AboutActivity.class);
            startActivity(aboutActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Interaction with article row.
     *
     * @param view    row article list recycler view holder
     * @param article contain article model data
     */
    @Override
    public void onArticleInteraction(View view, Article article) {
        new ArticleListEvent(this, article)
                .viewArticle();
    }

    /**
     * Show popup and show more action to interact with related article.
     *
     * @param view    row article list recycler view holder
     * @param article plain old java object for article
     */
    @Override
    public void onArticlePopupInteraction(final View view, final Article article) {
        new ArticlePopupBuilder(this, view, article)
                .buildPopup()
                .show();
    }

    /**
     * When user do long tap on view holder, show identical action like popup.
     *
     * @param view    row article list recycler view holder
     * @param article plain old java object for article
     */
    @Override
    public void onArticleLongClickInteraction(final View view, final Article article) {
        new ArticleContextBuilder(this, article)
                .buildContext()
                .show();
    }

    /**
     * Browse article to web version.
     *
     * @param view row article list recycler view holder
     * @param article plain old java object for article
     */
    @Override
    public void onBrowseClicked(View view, Article article) {
        new ArticleListEvent(this, article).browseArticle();
    }

    /**
     * Share article to another providers.
     *
     * @param view row article list recycler view holder
     * @param article plain old java object for article
     */
    @Override
    public void onShareClicked(View view, Article article) {
        new ArticleListEvent(this, article).shareArticle();
    }

    /**
     * Launch edit activity and passing necessary data like id and slug as reference
     * to retrieve related data from server.
     *
     * @param view row article list recycler view holder
     * @param article plain old java object for article
     */
    @Override
    public void onEditClicked(View view, Article article) {
        new ArticleListEvent(this, article).editArticle();
    }

    /**
     * Delete data from server and notified the recycler adapter to sync directly if success.
     *
     * @param view row article list recycler view holder
     * @param article plain old java object for article
     */
    @Override
    public void onDeleteClicked(View view, final Article article) {
        new ArticleListEvent(this, article).deleteArticle();
    }
}
