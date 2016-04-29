package com.sketchproject.infogue.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private static ImageButton followButton;

    public FollowerListEvent(Context context) {
        this.context = context;
    }

    public FollowerListEvent(Context context, Contributor contributorData, View buttonControl) {
        this.context = context;
        contributor = contributorData;
        followButton = (ImageButton) buttonControl;
    }

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

    public void browseContributor() {
        if (contributor == null) {
            throwArgumentContributorException();
        }

        String articleUrl = APIBuilder.getContributorUrl(contributor.getUsername());
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
        context.startActivity(browserIntent);
    }

    public void viewProfile() {
        Log.i("INFOGUE/Contributor", contributor.getId() + " " + contributor.getUsername());

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

    public void handleProfileResult(int requestCode, int resultCode, Intent data) {
        if (contributor == null || followButton == null) {
            throwArgumentException();
        }

        if (requestCode == ProfileActivity.PROFILE_RESULT_CODE && data != null) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                boolean isFollowing = data.getBooleanExtra(SessionManager.KEY_IS_FOLLOWING, false);
                contributor.setIsFollowing(isFollowing);
                if (isFollowing) {
                    followButton.setImageResource(R.drawable.btn_unfollow);
                } else {
                    followButton.setImageResource(R.drawable.btn_follow);
                }
            }
        }
    }

    public void followContributor() {
        if (contributor == null || followButton == null) {
            throwArgumentException();
        }

        final SessionManager session = new SessionManager(context);
        if (session.isLoggedIn()) {
            if (contributor.isFollowing()) {
                followButton.setImageResource(R.drawable.btn_follow);
                contributor.setIsFollowing(false);

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
                                    }
                                } else {
                                    String result = new String(networkResponse.data);
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                        String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                        if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                            errorMessage = context.getString(R.string.error_unauthorized);
                                        } else if (status.equals(APIBuilder.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                            errorMessage = message;
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                            errorMessage = context.getString(R.string.error_server);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Helper.toastColor(context, errorMessage,
                                        ContextCompat.getColor(context, R.color.color_danger));

                                followButton.setImageResource(R.drawable.btn_unfollow);
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

                VolleySingleton.getInstance(context).addToRequestQueue(postRequest);
            } else {
                followButton.setImageResource(R.drawable.btn_unfollow);
                contributor.setIsFollowing(true);

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
                                    }
                                } else {
                                    String result = new String(networkResponse.data);
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                        String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                        if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                            errorMessage = context.getString(R.string.error_unauthorized);
                                        } else if (status.equals(APIBuilder.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                            errorMessage = message;
                                        } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                            errorMessage = message;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Helper.toastColor(context, errorMessage,
                                        ContextCompat.getColor(context, R.color.color_danger));

                                followButton.setImageResource(R.drawable.btn_follow);
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

    private void throwInstanceException() throws IllegalStateException {
        throw new IllegalStateException(context.getClass().getSimpleName() +
                " must extends FragmentActivity class.");
    }

    private void throwArgumentContributorException() throws IllegalArgumentException {
        throw new IllegalArgumentException(context.getClass().getSimpleName() +
                " contributor or follow button must be initialized and referenced the same object." +
                " Did you mean to use FollowerListEvent(Context context, Contributor contributor)" +
                " constructor to start the activity?");
    }

    private void throwArgumentException() throws IllegalArgumentException {
        throw new IllegalArgumentException(context.getClass().getSimpleName() +
                " contributor or follow button must be initialized and referenced the same object." +
                " Did you mean to use FollowerListEvent(Context context, Contributor contributor, View buttonControl)" +
                " constructor to start the activity?");
    }
}
