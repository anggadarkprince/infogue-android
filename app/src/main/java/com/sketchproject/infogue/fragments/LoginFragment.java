package com.sketchproject.infogue.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.AuthenticationActivity;
import com.sketchproject.infogue.activities.ProfileActivity;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.Validator;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * A simple {@link Fragment} subclass to handle login screen inside view pager.
 *
 * Sketch Project Studio
 * Created by Angga on 1/04/2016 10.37.
 */
public class LoginFragment extends Fragment implements Validator.ViewValidation {
    private static final String TWITTER_KEY = "Rg1JqRPoxbflYe7XtQGCkcKsw";
    private static final String TWITTER_SECRET = "tVI6dgYF89AHNTkVz3yqywoE9WvjrF4ZVdq2dmk0l2bndLdW9d";

    private static final String VENDOR_MOBILE = "mobile";
    private static final String VENDOR_FACEBOOK = "facebook";
    private static final String VENDOR_TWITTER = "twitter";

    private Validator validator;
    private AlertFragment alert;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private LoginButton loginButtonFacebook;
    private TwitterLoginButton loginButtonTwitter;
    private CallbackManager callbackManager;

    private List<String> validationMessage;
    private String username;
    private String password;

    /**
     * Default constructor fragment, triggered when device rotate and first instantiate
     */
    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Perform initialization of AuthenticationActivity.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(getContext(), new Twitter(authConfig));
    }

    /**
     * Init views and preparing login UI.
     *
     * @param inflater The LayoutInflater object that can be used to inflate view
     * @param container parent (activity) container
     * @param savedInstanceState latest instance state
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        validator = new Validator();
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Facebook SDK setup
        loginButtonFacebook = (LoginButton) view.findViewById(R.id.login_button_facebook);
        loginButtonFacebook.setReadPermissions("email", "public_profile", "user_about_me", "user_website", "user_birthday");

        // If using in a fragment
        loginButtonFacebook.setFragment(this);

        // Init callback manager
        FacebookSdk.sdkInitialize(getContext());
        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButtonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("Infogue/facebook", loginResult.getAccessToken().getUserId());

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("Infogue/Facebook", response.toString());

                                try {
                                    Map<String, String> values = new HashMap<>();
                                    values.put("id", object.getString("id"));
                                    values.put("name", object.getString("name"));
                                    values.put("email", object.getString("email"));
                                    values.put("avatar", object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                    values.put("cover", object.getJSONObject("cover").getString("source"));
                                    loginRequest(VENDOR_FACEBOOK, values);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,website,picture.height(720),cover");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                alert.setAlertType(AlertFragment.ALERT_WARNING);
                alert.setAlertMessage("Facebook login attempt canceled");
                alert.show();
            }

            @Override
            public void onError(FacebookException exception) {
                alert.setAlertType(AlertFragment.ALERT_DANGER);
                alert.setAlertMessage("Facebook login attempt failed");
                alert.show();
            }
        });


        // Twitter Fabric
        loginButtonTwitter = (TwitterLoginButton) view.findViewById(R.id.login_button_twitter);
        loginButtonTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                // with your app's user model
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Log.i("Infogue/twitter", msg);

                Twitter.getApiClient(session).getAccountService().verifyCredentials(true, false, new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        User user = result.data;
                        String name = user.name;
                        String username = user.screenName;
                        String location = user.location;
                        String email = user.email;
                        String about = user.description;
                        String avatar = user.profileImageUrl;
                        String cover = user.profileBannerUrl;

                        Log.i("Infogue/twitter", name + " " + username + " " + location + " " + email + " " + about + " " + avatar + " " + cover);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        alert.setAlertType(AlertFragment.ALERT_DANGER);
                        alert.setAlertMessage("Login with Twitter failure");
                        alert.show();
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                alert.setAlertType(AlertFragment.ALERT_DANGER);
                alert.setAlertMessage("Login with Twitter failure");
                alert.show();
            }
        });

        return view;
    }

    /**
     * Triggered after parent (activity) created, to make sure UI is accessible.
     *
     * @param savedInstanceState latest state if exist
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mUsernameView = (EditText) getActivity().findViewById(R.id.input_username);
        mUsernameView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mPasswordView = (EditText) getActivity().findViewById(R.id.input_password);

        Button mEmailSignInButton = (Button) getActivity().findViewById(R.id.btn_sign_in);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preValidation();
                postValidation(onValidateView());
            }
        });

        TextView mCreateAccountButton = (TextView) getActivity().findViewById(R.id.btn_create_account);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity mActivity = getActivity();
                if (mActivity instanceof AuthenticationActivity) {
                    ((AuthenticationActivity) getActivity()).setTabRegisterActive();
                }
            }
        });

        ImageButton mFacebookButton = (ImageButton) getActivity().findViewById(R.id.btn_facebook);
        mFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonFacebook.performClick();
            }
        });

        ImageButton mTwitterButton = (ImageButton) getActivity().findViewById(R.id.btn_twitter);
        mTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonTwitter.performClick();
            }
        });

        TextView mForgotPassword = (TextView) getActivity().findViewById(R.id.btn_forgot);
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_FORGOT));
                startActivity(browserIntent);
            }
        });

        mLoginFormView = getActivity().findViewById(R.id.login_form);
        mProgressView = getActivity().findViewById(R.id.login_progress);

        alert = (AlertFragment) getChildFragmentManager().findFragmentById(R.id.alert_fragment);
    }

    /**
     * Populate data from oAuth facebook & twitter and passing into their callback.
     *
     * @param requestCode code request when oauth activity called
     * @param resultCode  result state activity
     * @param data        data from activity called if necessary
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        loginButtonTwitter.onActivityResult(requestCode, resultCode, data);
        Log.i("Infogue/twitter", String.valueOf(resultCode));
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int mediumAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView
                .animate()
                .setDuration(mediumAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate()
                .setDuration(mediumAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    /**
     * Placeholder validator method to handle populating inputs.
     */
    @Override
    public void preValidation() {
        // Store values at the time of the login attempt.
        username = mUsernameView.getText().toString();
        password = mPasswordView.getText().toString();
        validationMessage = new ArrayList<>();
    }

    /**
     * Placeholder validator method to handle validation rules.
     *
     * @return boolean
     */
    @Override
    public boolean onValidateView() {
        boolean isInvalid = false;
        View focusView = null;

        if (validator.isEmpty(password, true)) {
            validationMessage.add(getString(R.string.error_password_required));
            focusView = mPasswordView;
            isInvalid = true;
        }

        if (validator.isEmpty(username, true)) {
            validationMessage.add(getString(R.string.error_username_required));
            focusView = mUsernameView;
            isInvalid = true;
        }

        if (isInvalid) {
            focusView.requestFocus();
        }

        return !isInvalid;
    }

    /**
     * Validator placeholder function to handle process after validation.
     *
     * @param isValid indicate input form is valid or not
     */
    @Override
    public void postValidation(boolean isValid) {
        if (isValid) {
            alert.dismiss();
            showProgress(true);

            Map<String, String> values = new HashMap<>();
            values.put("username", mUsernameView.getText().toString());
            values.put("password", mPasswordView.getText().toString());
            loginRequest(VENDOR_MOBILE, values);
        } else {
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
    }

    /**
     * Send login request to server, data depend on the way of user
     * login (default, facebook, twitter) and catch necessary errors.
     */
    private void loginRequest(String vendor, final Map<String, String> values) {
        String url = APIBuilder.URL_API_LOGIN;
        if (vendor.equals(VENDOR_FACEBOOK)) {
            url = APIBuilder.URL_API_OAUTH_FACEBOOK;
        } else if (vendor.equals(VENDOR_TWITTER)) {
            url = APIBuilder.URL_API_OAUTH_TWITTER;
        }

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);
                            String login = result.getString("login");

                            if (status.equals(Contributor.STATUS_ACTIVATED) && login.equals(APIBuilder.REQUEST_GRANTED)) {
                                JSONObject user = result.getJSONObject("user");
                                if (populateSessionData(user)) {
                                    getActivity().finish();
                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    SessionManager session = new SessionManager(getContext());

                                    profileIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                                    profileIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));
                                    profileIntent.putExtra(SessionManager.KEY_NAME, session.getSessionData(SessionManager.KEY_NAME, null));
                                    profileIntent.putExtra(SessionManager.KEY_LOCATION, session.getSessionData(SessionManager.KEY_LOCATION, null));
                                    profileIntent.putExtra(SessionManager.KEY_ABOUT, session.getSessionData(SessionManager.KEY_ABOUT, null));
                                    profileIntent.putExtra(SessionManager.KEY_AVATAR, session.getSessionData(SessionManager.KEY_AVATAR, null));
                                    profileIntent.putExtra(SessionManager.KEY_COVER, session.getSessionData(SessionManager.KEY_COVER, null));
                                    profileIntent.putExtra(SessionManager.KEY_STATUS, session.getSessionData(SessionManager.KEY_STATUS, null));
                                    profileIntent.putExtra(SessionManager.KEY_ARTICLE, session.getSessionData(SessionManager.KEY_ARTICLE, 0));
                                    profileIntent.putExtra(SessionManager.KEY_FOLLOWER, session.getSessionData(SessionManager.KEY_FOLLOWER, 0));
                                    profileIntent.putExtra(SessionManager.KEY_FOLLOWING, session.getSessionData(SessionManager.KEY_FOLLOWING, 0));
                                    profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, false);
                                    profileIntent.putExtra(AuthenticationActivity.AFTER_LOGIN, true);
                                    profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(profileIntent);
                                } else {
                                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                                    alert.setAlertMessage(getString(R.string.error_creating_session));
                                    alert.show();
                                }
                            } else {
                                alert.setAlertType(AlertFragment.ALERT_DANGER);
                                alert.setAlertMessage(message);
                                alert.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        showProgress(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_unknown);
                        String errorTitle = "";
                        int type = AlertFragment.ALERT_DANGER;
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = getString(R.string.error_no_connection);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                String message = response.optString(APIBuilder.RESPONSE_MESSAGE);
                                String login = response.optString("login");

                                if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if ((status.equals(APIBuilder.REQUEST_UNREGISTERED) || login.equals(APIBuilder.REQUEST_RESTRICT)) && networkResponse.statusCode == 403) {
                                    errorMessage = message; // mismatch
                                    if (status.equals(Contributor.STATUS_PENDING)) { // turns out pending
                                        String urlResendEmail = APIBuilder.BASE_URL + "auth/resend/" + response.getString("token");
                                        type = AlertFragment.ALERT_WARNING;
                                        errorMessage = "Account is pending please activate via email.\n\rResend email activation?\n\r" + urlResendEmail;
                                        errorTitle = "Pending";
                                    } else if (status.equals(Contributor.STATUS_SUSPENDED)) { // turns out suspended
                                        errorTitle = "Suspended";
                                        errorMessage = "Account is suspended";
                                    }
                                } else if (login.equals(APIBuilder.REQUEST_MISMATCH) && networkResponse.statusCode == 401) {
                                    errorMessage = getString(R.string.error_unauthorized);
                                } else if (status.equals(APIBuilder.REQUEST_EXIST) && networkResponse.statusCode == 400) {
                                    errorMessage = message; // catch credentials exist when using oAuth
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        alert.setAlertType(type);
                        if (!errorTitle.isEmpty()) {
                            alert.setAlertTitle(errorTitle);
                        }
                        alert.setAlertMessage(errorMessage);
                        alert.show();

                        showProgress(false);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params;
                params = values;
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getContext()).addToRequestQueue(postRequest);
    }

    /**
     * Populate and mapping data into session.
     *
     * @param result result of login data
     * @return boolean
     */
    private boolean populateSessionData(JSONObject result) {
        SessionManager sessionManager = new SessionManager(getActivity().getBaseContext());
        HashMap<String, Object> user = new HashMap<>();
        try {
            user.put(SessionManager.KEY_ID, result.getInt(Contributor.ID));
            user.put(SessionManager.KEY_TOKEN, result.getString(Contributor.TOKEN));
            user.put(SessionManager.KEY_USERNAME, result.getString(Contributor.USERNAME));
            user.put(SessionManager.KEY_NAME, result.getString(Contributor.NAME));
            user.put(SessionManager.KEY_LOCATION, result.getString(Contributor.LOCATION));
            user.put(SessionManager.KEY_ABOUT, result.getString(Contributor.ABOUT));
            user.put(SessionManager.KEY_AVATAR, result.getString(Contributor.AVATAR_REF));
            user.put(SessionManager.KEY_COVER, result.getString(Contributor.COVER_REF));
            user.put(SessionManager.KEY_STATUS, result.getString(Contributor.STATUS));
            user.put(SessionManager.KEY_ARTICLE, result.getInt(Contributor.ARTICLE));
            user.put(SessionManager.KEY_FOLLOWER, result.getInt(Contributor.FOLLOWERS));
            user.put(SessionManager.KEY_FOLLOWING, result.getInt(Contributor.FOLLOWING));
            return sessionManager.createLoginSession(user);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
