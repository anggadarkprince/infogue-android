package com.sketchproject.infogue.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.AuthenticationActivity;
import com.sketchproject.infogue.activities.ProfileActivity;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.Validator;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements Validator.ViewValidation {

    private Validator validator;
    private AlertFragment alert;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String username;
    private String password;
    private List<String> validationMessage;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        validator = new Validator();
        return inflater.inflate(R.layout.fragment_login, container, false);
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

            }
        });

        ImageButton mTwitterButton = (ImageButton) getActivity().findViewById(R.id.btn_twitter);
        mTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                                if (status.equals(Constant.REQUEST_UNREGISTERED) && networkResponse.statusCode == 403) {
                                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                                    alert.setAlertMessage(message);
                                } else if (status.equals(Contributor.STATUS_PENDING) && networkResponse.statusCode == 403) {
                                    String urlResendEmail = Constant.BASE_URL + "auth/resend/" + response.getString("token");
                                    alert.setAlertType(AlertFragment.ALERT_WARNING);
                                    alert.setAlertTitle("Pending");
                                    alert.setAlertMessage("Account is pending please activate via email.\nResend email activation?\n"+urlResendEmail);
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
                            alert.setAlertMessage("Request Timeout, please try again!");
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
