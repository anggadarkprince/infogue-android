package com.sketchproject.infogue.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.Constant;

public class FollowerActivity extends AppCompatActivity implements
        FollowerFragment.OnListFragmentInteractionListener,
        ConnectionDetector.OnLostConnectionListener,
        ConnectionDetector.OnConnectionEstablished {

    public static final String SCREEN_REQUEST = "FollowerScreen";
    public static final String FOLLOWER_SCREEN = "Followers";
    public static final String FOLLOWING_SCREEN = "Following";

    private String activityTitle = "Followers";
    private ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);

        connectionDetector = new ConnectionDetector(getBaseContext());
        connectionDetector.setLostConnectionListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(activityTitle);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            activityTitle = extras.getString(SCREEN_REQUEST);
            Log.i("INFOGUE/" + activityTitle, String.valueOf(extras.getInt(SessionManager.KEY_ID)) + " " + extras.getString(SessionManager.KEY_USERNAME));
        }
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

    @Override
    public void onListFragmentInteraction(Contributor contributor) {
        if (connectionDetector.isNetworkAvailable()) {
            Log.i("INFOGUE/Contributor", contributor.getId() + " " + contributor.getUsername());

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
            startActivity(profileIntent);
        } else {
            onLostConnectionNotified(getBaseContext());
        }
    }

    @Override
    public void onLostConnectionNotified(Context context) {
        connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();

                if (!connectionDetector.isNetworkAvailable()) {
                    String[] jokes = {"Syahrini", "Jupe", "Depe", "Nabilah"};
                    connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onLostConnectionNotified(getBaseContext());
                        }
                    }, jokes[(int) Math.floor(Math.random() * jokes.length)] + "steal my internet T_T", "RETRY");
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
