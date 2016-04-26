package com.sketchproject.infogue.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

public class ProfileActivity extends AppCompatActivity implements
        ConnectionDetector.OnLostConnectionListener,
        ConnectionDetector.OnConnectionEstablished {

    public static final int PROFILE_RESULT_CODE = 200;

    private SessionManager session;
    private ConnectionDetector connectionDetector;

    private int contributorId;
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
            contributorId = extras.getInt(SessionManager.KEY_ID);
            username = extras.getString(SessionManager.KEY_USERNAME);
            isFollowing = extras.getBoolean(SessionManager.KEY_IS_FOLLOWING);

            String status = extras.getString(SessionManager.KEY_STATUS, "");
            if (!status.equals(Contributor.STATUS_ACTIVATED)) {
                AppHelper.toastColored(getBaseContext(), "Contributor is " + status, Color.parseColor("#ddd1205e"));
                Intent returnIntent = new Intent();
                setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
                finish();
            }

            if (mNameView != null) {
                mNameView.setText(extras.getString(SessionManager.KEY_NAME));
            }
            if (mLocationView != null) {
                mLocationView.setText(extras.getString(SessionManager.KEY_LOCATION));
            }
            if (mAboutView != null) {
                mAboutView.setText(extras.getString(SessionManager.KEY_ABOUT));
            }
            if (mArticleView != null) {
                mArticleView.setText(String.valueOf(extras.getInt(SessionManager.KEY_ARTICLE)));
            }
            if (mFollowerView != null) {
                mFollowerView.setText(String.valueOf(extras.getInt(SessionManager.KEY_FOLLOWER)));
            }
            if (mFollowingView != null) {
                mFollowingView.setText(String.valueOf(extras.getInt(SessionManager.KEY_FOLLOWING)));
            }

            if (mAvatarImage != null) {
                Glide.with(this)
                        .load(extras.getString(SessionManager.KEY_AVATAR))
                        .placeholder(R.drawable.placeholder_square)
                        .dontAnimate()
                        .into(mAvatarImage);
            }

            if (mCoverImage != null) {
                Glide.with(this)
                        .load(extras.getString(SessionManager.KEY_COVER))
                        .placeholder(R.drawable.placeholder_rectangle)
                        .centerCrop()
                        .crossFade()
                        .into(mCoverImage);
            }

            isAfterLogin = extras.getBoolean(AuthenticationActivity.AFTER_LOGIN);

            buildProfileEventHandler(contributorId, username);
        } else {
            AppHelper.toastColored(getBaseContext(), "Invalid user profile", Color.parseColor("#ddd1205e"));
            Intent returnIntent = new Intent();
            setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    private void buildProfileEventHandler(final int idContributor, final String usernameContributor) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(usernameContributor);
        }

        View mArticleButton = findViewById(R.id.btn_article);
        if (mArticleButton != null) {
            mArticleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                    articleIntent.putExtra(SessionManager.KEY_ID, idContributor);
                    articleIntent.putExtra(SessionManager.KEY_USERNAME, usernameContributor);
                    startActivity(articleIntent);
                }
            });
        }

        View mFollowerButton = findViewById(R.id.btn_followers);
        if (mFollowerButton != null) {
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
        }

        View mFollowingButton = findViewById(R.id.btn_following);
        if (mFollowingButton != null) {
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
        }

        final Button mDetailButton = (Button) findViewById(R.id.btn_detail);
        final ImageButton mMessageButton = (ImageButton) findViewById(R.id.btn_message);
        final ImageButton mInfoButton = (ImageButton) findViewById(R.id.btn_info);
        final Button mFollowButton = (Button) findViewById(R.id.btn_follow_control);

        // Open my profile
        int loggedUserId = session.getSessionData(SessionManager.KEY_ID, 0);
        if (session.isLoggedIn() && loggedUserId == idContributor) {
            if (mDetailButton != null) {
                mDetailButton.setVisibility(View.VISIBLE);
            }
            if (mFollowButton != null) {
                mFollowButton.setVisibility(View.GONE);
            }
            if (mMessageButton != null) {
                mMessageButton.setVisibility(View.GONE);
            }
            if (mInfoButton != null) {
                mInfoButton.setVisibility(View.GONE);
            }

            if (mDetailButton != null) {
                mDetailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.getContributorDetailUrl(usernameContributor)));
                        startActivity(browserIntent);
                    }
                });
            }
        } else { // Open another contributor
            if (mDetailButton != null) {
                mDetailButton.setVisibility(View.GONE);
            }
            if (mFollowButton != null) {
                mFollowButton.setVisibility(View.VISIBLE);
            }
            if (mMessageButton != null) {
                mMessageButton.setVisibility(View.VISIBLE);
            }
            if (mInfoButton != null) {
                mInfoButton.setVisibility(View.VISIBLE);
            }

            if (isFollowing) {
                stateFollow(mFollowButton);
            } else {
                stateUnfollow(mFollowButton);
            }

            if (mMessageButton != null) {
                mMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.getContributorDetailUrl(usernameContributor)));
                        startActivity(browserIntent);
                    }
                });
            }

            if (mMessageButton != null) {
                mMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.getContributorDetailUrl(usernameContributor)));
                        startActivity(browserIntent);
                    }
                });
            }

            if (mFollowButton != null) {
                mFollowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (session.isLoggedIn()) {
                            toggleFollowHandler(mFollowButton);
                        } else {
                            Intent authIntent = new Intent(getBaseContext(), AuthenticationActivity.class);
                            startActivity(authIntent);
                        }
                    }
                });
            }
        }
    }

    private void toggleFollowHandler(final Button mFollowButton) {
        if (isFollowing) {
            stateUnfollow(mFollowButton);
            Log.i("INFOGUE/PROFILE", "Unfollow " + contributorId + " " + username);
        } else {
            stateFollow(mFollowButton);
            Log.i("INFOGUE/PROFILE", "Follow " + contributorId + " " + username);
        }

        isFollowing = !isFollowing;
    }

    @SuppressWarnings("deprecation")
    private void stateFollow(Button mFollowButton) {
        mFollowButton.setBackgroundResource(R.drawable.btn_primary);
        mFollowButton.setTextColor(getResources().getColor(R.color.light));
        mFollowButton.setText(getString(R.string.action_unfollow));
    }

    @SuppressWarnings("deprecation")
    private void stateUnfollow(Button mFollowButton) {
        mFollowButton.setBackgroundResource(R.drawable.btn_toggle);
        mFollowButton.setTextColor(getResources().getColor(R.color.primary));
        mFollowButton.setText(getString(R.string.action_follow));
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
            if (isAfterLogin) {
                launchMainActivity();
            } else {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, isFollowing);
                setResult(AppCompatActivity.RESULT_OK, returnIntent);
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
        if (isAfterLogin) {
            launchMainActivity();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, isFollowing);
            setResult(AppCompatActivity.RESULT_OK, returnIntent);
        }

        super.onBackPressed();
    }

    private void launchMainActivity() {
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
