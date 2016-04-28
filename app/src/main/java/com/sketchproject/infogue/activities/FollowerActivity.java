package com.sketchproject.infogue.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment;
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
        profileIntent.putExtra(SessionManager.KEY_STATUS, contributor.getStatus());
        profileIntent.putExtra(SessionManager.KEY_ARTICLE, contributor.getArticle());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWER, contributor.getFollowers());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWING, contributor.getFollowing());
        profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, contributor.isFollowing());
        startActivityForResult(profileIntent, ProfileActivity.PROFILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ProfileActivity.PROFILE_RESULT_CODE) {
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
                                            errorMessage = message+", please login again!";
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
                                            errorMessage = message+", please login again!";
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
                        } else if (selectedItem.equals(getString(R.string.action_long_unfollow))) {
                            ((ImageButton) followControl).setImageResource(R.drawable.btn_follow);
                            contributor.setIsFollowing(false);
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
