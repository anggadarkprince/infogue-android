package com.sketchproject.infogue.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.AuthenticationActivity;
import com.sketchproject.infogue.activities.ProfileActivity;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 16.50.
 */
public class FollowerListEvent {
    private Context context;
    private static Contributor contributor;
    private static View followButton;

    /**
     * Initialize Follower event list.
     *
     * @param context parent context
     */
    public FollowerListEvent(Context context) {
        this.context = context;
    }

    /**
     * Initialize Follower event list.
     *
     * @param context         parent context
     * @param contributorData model data of contributor
     * @param buttonControl   toggle button follow (castable to ImageButton)
     */
    public FollowerListEvent(Context context, Contributor contributorData, View buttonControl) {
        Log.i("Infogue/Contributor", "ID : " + contributorData.getId() + " Username : " + contributorData.getUsername());
        this.context = context;
        contributor = contributorData;
        if (buttonControl instanceof ImageButton) {
            followButton = buttonControl;
        } else if (buttonControl instanceof Button) {
            followButton = buttonControl;
        } else {
            followButton = null;
            throwArgumentException();
        }
    }

    /**
     * Share contributor link to another provider.
     */
    public void shareContributor() {
        if (contributor == null) {
            throwArgumentContributorException();
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, APIBuilder.getShareContributorText(contributor.getUsername()));
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.label_intent_share)));
    }

    /**
     * Open contributor link on browser.
     */
    public void browseContributor() {
        if (contributor == null) {
            throwArgumentContributorException();
        }

        String articleUrl = APIBuilder.getContributorUrl(contributor.getUsername());
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
        context.startActivity(browserIntent);
    }

    /**
     * Launch Profile Activity and passing necessary value from contributor data.
     */
    public void viewProfile() {
        Log.i("Infogue/Contributor", contributor.getId() + " " + contributor.getUsername());

        if (contributor == null) {
            throwArgumentContributorException();
        }

        Intent profileIntent = new Intent(context, ProfileActivity.class);
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

        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).startActivityForResult(profileIntent, ProfileActivity.PROFILE_RESULT_CODE);
        } else {
            throwInstanceException();
        }
    }

    /**
     * Handle result of ProfileActivity, find out if user follow or unfollow profile after
     * launch ProfileActivity the sync immediately.
     *
     * @param requestCode unique code when call ProfileActivity
     * @param resultCode  result state code currently always catch RESULT_OK
     * @param data        related status of following
     */
    public void handleProfileResult(int requestCode, int resultCode, Intent data) {
        if (contributor == null || followButton == null) {
            throwArgumentException();
        }

        if (requestCode == ProfileActivity.PROFILE_RESULT_CODE && data != null) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                boolean isFollowing = data.getBooleanExtra(SessionManager.KEY_IS_FOLLOWING, false);
                contributor.setIsFollowing(isFollowing);
                toggleFollowButton(contributor.isFollowing());
            }
        }
    }

    /**
     * Follow button event, check if user need to follow by state of 'is_following' attribute,
     * notify the toggle state immediately, if follow or unfollow failed, rollback the state and
     * button appearance depends on last 'is_following' value.
     */
    public void followContributor() {
        if (contributor == null || followButton == null) {
            throwArgumentException();
        }

        final SessionManager session = new SessionManager(context);
        if (session.isLoggedIn()) {
            if (contributor.isFollowing()) {
                contributor.setIsFollowing(false);
                toggleFollowButton(contributor.isFollowing());

                StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_UNFOLLOW,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject result = new JSONObject(response);
                                    String status = result.getString(APIBuilder.RESPONSE_STATUS);
                                    String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                                    if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                        Log.i("Infogue/Unfollow", message);
                                    } else {
                                        Log.w("Infogue/Unfollow", context.getString(R.string.error_unknown));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();

                                NetworkResponse networkResponse = error.networkResponse;
                                String errorMessage = context.getString(R.string.error_unknown);
                                if (networkResponse == null) {
                                    if (error.getClass().equals(TimeoutError.class)) {
                                        errorMessage = context.getString(R.string.error_timeout);
                                    } else if (error.getClass().equals(NoConnectionError.class)) {
                                        errorMessage = context.getString(R.string.error_no_connection);
                                    }
                                } else {
                                    String result = new String(networkResponse.data);
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                        String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                        if (status.equals(APIBuilder.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                            errorMessage = message;
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                            errorMessage = context.getString(R.string.error_unauthorized);
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                            errorMessage = context.getString(R.string.error_server);
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                            errorMessage = context.getString(R.string.error_maintenance);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        errorMessage = context.getString(R.string.error_parse_data);
                                    }
                                }
                                Helper.toastColor(context, errorMessage, R.color.color_danger_transparent);

                                contributor.setIsFollowing(true);
                                toggleFollowButton(contributor.isFollowing());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put(Contributor.TOKEN, session.getSessionData(SessionManager.KEY_TOKEN, null));
                        params.put(Contributor.FOREIGN, String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                        params.put(Contributor.FOLLOWING_CONTRIBUTOR, String.valueOf(contributor.getId()));
                        params.put(APIBuilder.METHOD, APIBuilder.METHOD_DELETE);
                        return params;
                    }
                };
                postRequest.setRetryPolicy(new DefaultRetryPolicy(
                        15000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                VolleySingleton.getInstance(context).addToRequestQueue(postRequest);
            } else {
                contributor.setIsFollowing(true);
                toggleFollowButton(contributor.isFollowing());

                StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_FOLLOW,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject result = new JSONObject(response);
                                    String status = result.getString(APIBuilder.RESPONSE_STATUS);
                                    String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                                    if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                        Log.i("Infogue/Follow", message);
                                    } else {
                                        Log.w("Infogue/Follow", context.getString(R.string.error_unknown));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();

                                NetworkResponse networkResponse = error.networkResponse;
                                String errorMessage = context.getString(R.string.error_unknown);
                                if (networkResponse == null) {
                                    if (error.getClass().equals(TimeoutError.class)) {
                                        errorMessage = context.getString(R.string.error_timeout);
                                    } else if (error.getClass().equals(NoConnectionError.class)) {
                                        errorMessage = context.getString(R.string.error_no_connection);
                                    }
                                } else {
                                    String result = new String(networkResponse.data);
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                        String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                        if (status.equals(APIBuilder.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                            errorMessage = message;
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                            errorMessage = context.getString(R.string.error_unauthorized);
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                            errorMessage = context.getString(R.string.error_server);
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                            errorMessage = context.getString(R.string.error_maintenance);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        errorMessage = context.getString(R.string.error_parse_data);
                                    }
                                }
                                Helper.toastColor(context, errorMessage, R.color.color_danger_transparent);

                                contributor.setIsFollowing(false);
                                toggleFollowButton(contributor.isFollowing());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put(Contributor.TOKEN, session.getSessionData(SessionManager.KEY_TOKEN, null));
                        params.put(Contributor.FOREIGN, String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                        params.put(Contributor.FOLLOWING_CONTRIBUTOR, String.valueOf(contributor.getId()));
                        return params;
                    }
                };
                postRequest.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                VolleySingleton.getInstance(context).addToRequestQueue(postRequest);
            }
        } else {
            Intent authIntent = new Intent(context, AuthenticationActivity.class);
            context.startActivity(authIntent);
        }
    }

    /**
     * Toggle button state is following or unfollowing.
     *
     * @param isFollowing current following state
     */
    private void toggleFollowButton(boolean isFollowing){
        if (isFollowing) {
            if(followButton instanceof ImageButton){
                ((ImageButton)followButton).setImageResource(R.drawable.btn_unfollow);
            } else if (followButton instanceof Button){
                followButton.setBackgroundResource(R.drawable.btn_primary);
                ((Button)followButton).setTextColor(ContextCompat.getColor(context, R.color.light));
                ((Button)followButton).setText(R.string.action_unfollow);
            }
        } else {
            if(followButton instanceof ImageButton){
                ((ImageButton)followButton).setImageResource(R.drawable.btn_follow);
            } else if (followButton instanceof Button){
                followButton.setBackgroundResource(R.drawable.btn_toggle);
                ((Button)followButton).setTextColor(ContextCompat.getColor(context, R.color.primary));
                ((Button)followButton).setText(R.string.action_follow);
            }
        }
    }

    /**
     * @throws IllegalStateException
     */
    private void throwInstanceException() throws IllegalStateException {
        throw new IllegalStateException(context.getClass().getSimpleName() +
                " must extends FragmentActivity class.");
    }

    /**
     * @throws IllegalArgumentException
     */
    private void throwArgumentContributorException() throws IllegalArgumentException {
        throw new IllegalArgumentException(context.getClass().getSimpleName() +
                " contributor or follow button must be initialized and referenced the same object." +
                " Did you mean to use FollowerListEvent(Context context, Contributor contributor)" +
                " constructor to start the activity?");
    }

    /**
     * @throws IllegalArgumentException
     */
    private void throwArgumentException() throws IllegalArgumentException {
        throw new IllegalArgumentException(context.getClass().getSimpleName() +
                " contributor or follow button must be initialized and referenced the same object." +
                " Did you mean to use FollowerListEvent(Context context, Contributor contributor, View buttonControl)" +
                " constructor to start the activity?");
    }
}
