package com.sketchproject.infogue.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

public class FollowerActivity extends AppCompatActivity implements
        FollowerFragment.OnListFragmentInteractionListener,
        ConnectionDetector.OnLostConnectionListener,
        ConnectionDetector.OnConnectionEstablished {

    public static final String SCREEN_REQUEST = "FollowerScreen";
    public static final String CONTRIBUTOR_SCREEN = "Contributors";
    public static final String FOLLOWER_SCREEN = "Followers";
    public static final String FOLLOWING_SCREEN = "Following";

    private ConnectionDetector connectionDetector;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View mControlButton;
    private Contributor mContributor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);

        connectionDetector = new ConnectionDetector(getBaseContext());
        connectionDetector.setLostConnectionListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String activityTitle = extras.getString(SCREEN_REQUEST);
            int id = extras.getInt(SessionManager.KEY_ID);
            String username = extras.getString(SessionManager.KEY_USERNAME);
            String query = extras.getString(SearchActivity.QUERY_STRING);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                if(query != null){
                    getSupportActionBar().setTitle("All result for "+query);
                }
                else{
                    getSupportActionBar().setTitle(activityTitle);
                }
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

    public void setSwipeEnable(boolean state){
        swipeRefreshLayout.setEnabled(state);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_feedback) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_FEEDBACK));
            startActivity(browserIntent);
        } else if (id == R.id.action_help) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_HELP));
            startActivity(browserIntent);
        } else if (id == R.id.action_rating) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_APP));
            startActivity(browserIntent);
        } else if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(getBaseContext(), AboutActivity.class);
            startActivity(aboutActivity);
        }

        return super.onOptionsItemSelected(item);
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
            onLostConnectionNotified(getBaseContext());
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
                        onLostConnectionNotified(getBaseContext());
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
                        onLostConnectionNotified(getBaseContext());
                    }
                }
            });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLostConnectionNotified(Context context) {
        connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();

                if (!connectionDetector.isNetworkAvailable()) {
                    connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onLostConnectionNotified(getBaseContext());
                        }
                    }, Constant.jokes[(int) Math.floor(Math.random() * Constant.jokes.length)] + " stole my internet T_T", getString(R.string.action_retry));
                } else {
                    connectionDetector.snackbarConnectedNotification(findViewById(android.R.id.content), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectionDetector.dismissNotification();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onConnectionEstablished(Context context) {
        connectionDetector.snackbarConnectedNotification(findViewById(android.R.id.content), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();
            }
        });
    }
}
