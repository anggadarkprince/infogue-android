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

import com.android.volley.NetworkResponse;
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
import com.sketchproject.infogue.utils.Constant;
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
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements Validator.ViewValidation {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "Rg1JqRPoxbflYe7XtQGCkcKsw";
    private static final String TWITTER_SECRET = "tVI6dgYF89AHNTkVz3yqywoE9WvjrF4ZVdq2dmk0l2bndLdW9d";

    private Validator validator;
    private AlertFragment alert;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private LoginButton loginButtonFacebook;
    private TwitterLoginButton loginButtonTwitter;
    private CallbackManager callbackManager;

    private String username;
    private String password;
    private List<String> validationMessage;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(getContext(), new Twitter(authConfig));
    }

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
                                Log.i("Infogue/facebook", response.toString());

                                try {
                                    String name = object.getString("name");
                                    String email = object.getString("email");
                                    String birthday = object.getString("birthday");
                                    String gender = object.getString("gender");
                                    String about = object.getString("website");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,website");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        loginButtonTwitter.onActivityResult(requestCode, resultCode, data);
        Log.i("Infogue/twitter", String.valueOf(resultCode));
    }

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
                attemptLogin();
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_FORGOT));
                startActivity(browserIntent);
            }
        });

        mLoginFormView = getActivity().findViewById(R.id.login_form);
        mProgressView = getActivity().findViewById(R.id.login_progress);

        alert = (AlertFragment) getChildFragmentManager().findFragmentById(R.id.alert_fragment);
    }

    private void attemptLogin() {
        preValidation();
        postValidation(onValidateView());
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

    @Override
    public void preValidation() {
        // Store values at the time of the login attempt.
        username = mUsernameView.getText().toString();
        password = mPasswordView.getText().toString();
        validationMessage = new ArrayList<>();
    }

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

    @Override
    public void postValidation(boolean isValid) {
        if (isValid) {
            alert.dismiss();
            showProgress(true);
            loginRequest();
        } else {
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
    }

    private void loginRequest() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString("status");
                            String message = result.getString("message");
                            String login = result.getString("login");

                            if (status.equals(Contributor.STATUS_ACTIVATED) && login.equals(Constant.REQUEST_GRANTED)) {
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
                                    alert.setAlertMessage("Creating session failed");
                                    alert.show();
                                    showProgress(false);
                                }
                            } else {
                                alert.setAlertType(AlertFragment.ALERT_DANGER);
                                alert.setAlertMessage(message);
                                alert.show();
                                showProgress(false);
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
                        if (networkResponse != null) {
                            String result = new String(networkResponse.data);
                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");
                                String login = response.getString("login");

                                if ((status.equals(Constant.REQUEST_UNREGISTERED) || login.equals(Constant.REQUEST_RESTRICT)) && networkResponse.statusCode == 403) {
                                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                                    alert.setAlertMessage(message);
                                } else if (status.equals(Contributor.STATUS_PENDING) && networkResponse.statusCode == 403) {
                                    String urlResendEmail = Constant.BASE_URL + "auth/resend/" + response.getString("token");
                                    alert.setAlertType(AlertFragment.ALERT_WARNING);
                                    alert.setAlertTitle("Pending");
                                    alert.setAlertMessage("Account is pending please activate via email.\nResend email activation?\n" + urlResendEmail);
                                } else if (status.equals(Contributor.STATUS_SUSPENDED) && networkResponse.statusCode == 403) {
                                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                                    alert.setAlertTitle("Suspended");
                                    alert.setAlertMessage("Account is suspended");
                                } else if (login.equals(Constant.REQUEST_MISMATCH) && networkResponse.statusCode == 401) {
                                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                                    alert.setAlertMessage(message);
                                } else {
                                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                                    alert.setAlertMessage(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            alert.setAlertType(AlertFragment.ALERT_DANGER);
                            if (error.getClass().equals(TimeoutError.class)) {
                                alert.setAlertMessage(getString(R.string.error_timeout));
                            }
                            else{
                                alert.setAlertMessage(getString(R.string.error_unknown));
                            }
                        }
                        alert.show();

                        showProgress(false);
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", mUsernameView.getText().toString());
                params.put("password", mPasswordView.getText().toString());
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(getContext()).addToRequestQueue(postRequest);
    }

    private boolean populateSessionData(JSONObject result) {
        SessionManager sessionManager = new SessionManager(getActivity().getBaseContext());
        HashMap<String, Object> user = new HashMap<>();
        try {
            user.put(SessionManager.KEY_ID, result.getInt("id"));
            user.put(SessionManager.KEY_TOKEN, result.getString("api_token"));
            user.put(SessionManager.KEY_USERNAME, result.getString("username"));
            user.put(SessionManager.KEY_NAME, result.getString("name"));
            user.put(SessionManager.KEY_LOCATION, result.getString("location"));
            user.put(SessionManager.KEY_ABOUT, result.getString("about"));
            user.put(SessionManager.KEY_AVATAR, result.getString("avatar_ref"));
            user.put(SessionManager.KEY_COVER, result.getString("cover_ref"));
            user.put(SessionManager.KEY_STATUS, result.getString("status"));
            user.put(SessionManager.KEY_ARTICLE, result.getInt("article_total"));
            user.put(SessionManager.KEY_FOLLOWER, result.getInt("followers_total"));
            user.put(SessionManager.KEY_FOLLOWING, result.getInt("following_total"));
            return sessionManager.createLoginSession(user);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
