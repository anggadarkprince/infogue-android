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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

public class ProfileActivity extends AppCompatActivity implements
        ConnectionDetector.OnLostConnectionListener,
        ConnectionDetector.OnConnectionEstablished {

    private SessionManager session;
    private ConnectionDetector connectionDetector;

    private int id;
    private String username;
    private boolean isFollowing;
    private boolean isAfterLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(getBaseContext());
        connectionDetector = new ConnectionDetector(getBaseContext());
        connectionDetector.setLostConnectionListener(this);
        connectionDetector.setEstablishedConnectionListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView mNameView = (TextView) findViewById(R.id.name);
        TextView mLocationView = (TextView) findViewById(R.id.location);
        TextView mAboutView = (TextView) findViewById(R.id.about);
        TextView mArticleView = (TextView) findViewById(R.id.valueArticle);
        TextView mFollowerView = (TextView) findViewById(R.id.valueFollower);
        TextView mFollowingView = (TextView) findViewById(R.id.valueFollowing);
        ImageView mAvatarImage = (ImageView) findViewById(R.id.avatar);
        ImageView mCoverImage = (ImageView) findViewById(R.id.cover);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getInt(SessionManager.KEY_ID);
            username = extras.getString(SessionManager.KEY_USERNAME);
            isFollowing = extras.getBoolean(SessionManager.KEY_IS_FOLLOWING);

            mNameView.setText(extras.getString(SessionManager.KEY_NAME));
            mLocationView.setText(extras.getString(SessionManager.KEY_LOCATION));
            mAboutView.setText(extras.getString(SessionManager.KEY_ABOUT));
            mArticleView.setText(String.valueOf(extras.getInt(SessionManager.KEY_ARTICLE)));
            mFollowerView.setText(String.valueOf(extras.getInt(SessionManager.KEY_FOLLOWER)));
            mFollowingView.setText(String.valueOf(extras.getInt(SessionManager.KEY_FOLLOWING)));

            Glide.with(this)
                    .load(extras.getString(SessionManager.KEY_AVATAR))
                    .placeholder(R.drawable.placeholder_square)
                    .dontAnimate()
                    .into(mAvatarImage);

            Glide.with(this)
                    .load(extras.getString(SessionManager.KEY_COVER))
                    .placeholder(R.drawable.placeholder_rectangle)
                    .centerCrop()
                    .crossFade()
                    .into(mCoverImage);

            isAfterLogin = extras.getBoolean(AuthenticationActivity.AFTER_LOGIN);

            buildProfileEventHandler(id, username);
        } else {
            Toast.makeText(getBaseContext(), "Invalid user profile", Toast.LENGTH_LONG).show();
        }
    }

    private void buildProfileEventHandler(final int idContributor, final String usernameContributor) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(usernameContributor);
        }

        View mArticleButton = findViewById(R.id.btn_article);
        mArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                articleIntent.putExtra(SessionManager.KEY_ID, idContributor);
                articleIntent.putExtra(SessionManager.KEY_USERNAME, usernameContributor);
                startActivity(articleIntent);
            }
        });

        View mFollowerButton = findViewById(R.id.btn_followers);
        mFollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent followerIntent = new Intent(getBaseContext(), FollowerActivity.class);
                followerIntent.putExtra(FollowerActivity.SCREEN_REQUEST, FollowerActivity.FOLLOWER_SCREEN);
                followerIntent.putExtra(SessionManager.KEY_ID, idContributor);
                followerIntent.putExtra(SessionManager.KEY_USERNAME, usernameContributor);
                startActivity(followerIntent);
            }
        });

        View mFollowingButton = findViewById(R.id.btn_following);
        mFollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent followingIntent = new Intent(getBaseContext(), FollowerActivity.class);
                followingIntent.putExtra(FollowerActivity.SCREEN_REQUEST, FollowerActivity.FOLLOWING_SCREEN);
                followingIntent.putExtra(SessionManager.KEY_ID, idContributor);
                followingIntent.putExtra(SessionManager.KEY_USERNAME, usernameContributor);
                startActivity(followingIntent);
            }
        });

        final Button mDetailButton = (Button) findViewById(R.id.btn_detail);
        final ImageButton mMessageButton = (ImageButton) findViewById(R.id.btn_message);
        final ImageButton mInfoButton = (ImageButton) findViewById(R.id.btn_info);
        final Button mFollowButton = (Button) findViewById(R.id.btn_follow_control);

        // Open my profile
        if (session.getSessionData(SessionManager.KEY_ID, 0) == idContributor) {
            mDetailButton.setVisibility(View.VISIBLE);
            mFollowButton.setVisibility(View.GONE);
            mMessageButton.setVisibility(View.GONE);
            mInfoButton.setVisibility(View.GONE);

            mDetailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.getContributorUrl(usernameContributor)));
                    startActivity(browserIntent);
                }
            });
        } else { // Open another contributor
            mDetailButton.setVisibility(View.GONE);
            mFollowButton.setVisibility(View.VISIBLE);
            mMessageButton.setVisibility(View.VISIBLE);
            mInfoButton.setVisibility(View.VISIBLE);

            if (isFollowing) {
                stateUnfollow(mFollowButton);
            } else {
                stateFollow(mFollowButton);
            }

            mMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.getContributorUrl(usernameContributor)));
                    startActivity(browserIntent);
                }
            });

            mMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.getContributorUrl(usernameContributor)));
                    startActivity(browserIntent);
                }
            });

            mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFollowHandler(mFollowButton);
                }
            });
        }
    }

    private void toggleFollowHandler(final Button mFollowButton) {
        if (isFollowing) {
            stateUnfollow(mFollowButton);

            mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("INFOGUE/PROFILE", "Unfollow " + id + " " + username);
                    stateFollow(mFollowButton);
                }
            });
        } else {
            stateFollow(mFollowButton);

            mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("INFOGUE/PROFILE", "Follow " + id + " " + username);
                    stateUnfollow(mFollowButton);
                }
            });
        }

        isFollowing = !isFollowing;
    }

    @SuppressWarnings("deprecation")
    private void stateFollow(Button mFollowButton) {
        mFollowButton.setBackgroundResource(R.drawable.button_light);
        mFollowButton.setTextColor(getResources().getColor(R.color.primary));
        mFollowButton.setText(getString(R.string.action_follow));
    }

    @SuppressWarnings("deprecation")
    private void stateUnfollow(Button mFollowButton) {
        mFollowButton.setBackgroundResource(R.drawable.button_primary);
        mFollowButton.setTextColor(getResources().getColor(R.color.light));
        mFollowButton.setText(getString(R.string.action_unfollow));
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
            if(isAfterLogin){
                launchMainActivity();
            }
            else{
                finish();
            }
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
    public void onBackPressed() {
        if(isAfterLogin){
            launchMainActivity();
        }

        super.onBackPressed();
    }

    private void launchMainActivity(){
        Intent applicationIntent = new Intent(getBaseContext(), ApplicationActivity.class);
        applicationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        applicationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(applicationIntent);
        finish();
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
                    }, "Holy Molly Connection T_T", "RETRY");
                }
                else{
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
