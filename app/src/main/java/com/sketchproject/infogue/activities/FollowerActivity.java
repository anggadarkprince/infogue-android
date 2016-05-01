package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.events.FollowerContextBuilder;
import com.sketchproject.infogue.events.FollowerListEvent;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.APIBuilder;

/**
 * A {@link AppCompatActivity} subclass, show follower and following list.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 1/012/2016 10.37.
 */
public class FollowerActivity extends AppCompatActivity implements
        FollowerFragment.OnFollowerInteractionListener {

    public static final String SCREEN_REQUEST = "Screen";
    public static final String CONTRIBUTOR_SCREEN = "Contributors";
    public static final String FOLLOWER_SCREEN = "Followers";
    public static final String FOLLOWING_SCREEN = "Following";

    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Perform initialization of FollowerActivity.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt(SessionManager.KEY_ID);
            String username = extras.getString(SessionManager.KEY_USERNAME);
            String query = extras.getString(SearchActivity.QUERY_STRING);
            String activityTitle = extras.getString(SCREEN_REQUEST);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                if (query != null) {
                    activityTitle = "All result for " + query;
                }
                getSupportActionBar().setTitle(activityTitle);
            }

            Fragment fragment = FollowerFragment.newInstance(1, id, username, activityTitle, query);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    FollowerFragment fragment = (FollowerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    fragment.refreshArticleList(swipeRefreshLayout);
                }
            });
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
     * Check if there is result from profile activity, if so update button follow state.
     *
     * @param requestCode code request when profile activity called
     * @param resultCode  result state for now just catch RESULT_OK
     * @param data        data from activity called is follow or unfollow
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new FollowerListEvent(this)
                .handleProfileResult(requestCode, resultCode, data);
    }

    /**
     * Triggered when user click the follower row view holder.
     *
     * @param contributor   contain contributor model data
     * @param followControl button follow view (castable to ImageButton)
     */
    @Override
    public void onFollowerInteraction(Contributor contributor, View followControl) {
        new FollowerListEvent(this, contributor, followControl)
                .viewProfile();
    }

    /**
     * Triggered when user click follow toggle button.
     *
     * @param view          row article list recycler view holder
     * @param followControl button follow view (castable to ImageButton)
     * @param contributor   contain contributor model data
     */
    @Override
    public void onFollowerControlInteraction(View view, View followControl, final Contributor contributor) {
        new FollowerListEvent(this, contributor, followControl)
                .followContributor();
    }

    /**
     * Show popup and show more action to interact with related contributor.
     *
     * @param view          row article list recycler view holder
     * @param followControl button follow view (castable to ImageButton)
     * @param contributor   contain contributor model data
     */
    @Override
    public void onFollowerLongClickInteraction(final View view, final View followControl, final Contributor contributor) {
        new FollowerContextBuilder(this, contributor, followControl)
                .buildContext()
                .show();
    }
}
