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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements
        ConnectionDetector.OnLostConnectionListener,
        ConnectionDetector.OnConnectionEstablished {

    public static final int PROFILE_RESULT_CODE = 200;

    private SessionManager session;
    private ConnectionDetector connectionDetector;

    private TextView mArticleView;
    private TextView mFollowerView;
    private TextView mFollowingView;

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
        ImageView mAvatarImage = (ImageView) findViewById(R.id.avatar);
        ImageView mCoverImage = (ImageView) findViewById(R.id.cover);

        mArticleView = (TextView) findViewById(R.id.valueArticle);
        mFollowerView = (TextView) findViewById(R.id.valueFollower);
        mFollowingView = (TextView) findViewById(R.id.valueFollowing);

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
            updateProfileInBackground();

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

            StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_UNFOLLOW,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject result = new JSONObject(response);
                                String status = result.getString("status");
                                String message = result.getString("message");

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
                                    String status = response.getString("status");
                                    String message = response.getString("message");

                                    if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                        errorMessage = message + ", please login again!";
                                    } else if (status.equals(Constant.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                        errorMessage = message;
                                    } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                        errorMessage = message;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));

                            stateFollow(mFollowButton);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("api_token", session.getSessionData(SessionManager.KEY_TOKEN, null));
                    params.put("contributor_id", String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                    params.put("following_id", String.valueOf(contributorId));
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
            stateFollow(mFollowButton);
            Log.i("INFOGUE/PROFILE", "Follow " + contributorId + " " + username);

            StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_FOLLOW,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject result = new JSONObject(response);
                                String status = result.getString("status");
                                String message = result.getString("message");

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
                                    String status = response.getString("status");
                                    String message = response.getString("message");

                                    if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                        errorMessage = message + ", please login again!";
                                    } else if (status.equals(Constant.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                        errorMessage = message;
                                    } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                        errorMessage = message;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));

                            stateUnfollow(mFollowButton);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("api_token", session.getSessionData(SessionManager.KEY_TOKEN, null));
                    params.put("contributor_id", String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                    params.put("following_id", String.valueOf(contributorId));
                    return params;
                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
        }

        isFollowing = !isFollowing;
    }

    private void updateProfileInBackground() {
        JsonObjectRequest contributorRequest = new JsonObjectRequest(Request.Method.GET, UrlHelper.getApiContributorUrl(username), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            JSONObject contributor = response.getJSONObject("contributor");

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                session.setSessionData(SessionManager.KEY_ARTICLE, contributor.getInt("article_total"));
                                session.setSessionData(SessionManager.KEY_FOLLOWER, contributor.getInt("followers_total"));
                                session.setSessionData(SessionManager.KEY_FOLLOWING, contributor.getInt("following_total"));

                                mArticleView.setText(String.valueOf(session.getSessionData(SessionManager.KEY_ARTICLE, 0)));
                                mFollowerView.setText(String.valueOf(session.getSessionData(SessionManager.KEY_FOLLOWER, 0)));
                                mFollowingView.setText(String.valueOf(session.getSessionData(SessionManager.KEY_FOLLOWING, 0)));
                            } else {
                                Log.w("Infogue/Profile", getString(R.string.error_unknown));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                Log.e("Infogue/Profile", getString(R.string.error_timeout));
                            } else {
                                Log.e("Infogue/Profile", getString(R.string.error_unknown));
                            }
                        } else {
                            Log.e("Infogue/Profile", getString(R.string.error_server));
                        }
                    }
                }
        );
        contributorRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(contributorRequest);
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
