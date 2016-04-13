package com.sketchproject.infogue.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ApplicationActivity;
import com.sketchproject.infogue.activities.AuthenticationActivity;
import com.sketchproject.infogue.activities.ProfileActivity;
import com.sketchproject.infogue.modules.SessionManager;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private UserLoginTask mAuthTask = null;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mUsernameView = (EditText) getActivity().findViewById(R.id.input_username);
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
                if(mActivity instanceof AuthenticationActivity){
                    ((AuthenticationActivity) getActivity()).setTabRegisterActive();
                }
            }
        });

        mLoginFormView = getActivity().findViewById(R.id.login_form);
        mProgressView = getActivity().findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView
                    .animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return false;
            }

            return mUsername.equals("angga") && mPassword.equals("angga1234");
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                if(demoOnly()){
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
                } else{
                    Toast.makeText(getContext(), "Something is getting wrong", Toast.LENGTH_LONG).show();
                }

            } else {
                showProgress(false);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private boolean demoOnly(){
        SessionManager sessionManager = new SessionManager(getActivity().getBaseContext());
        HashMap<String, Object> user = new HashMap<>();
        user.put(SessionManager.KEY_ID, 1);
        user.put(SessionManager.KEY_TOKEN, "6224573472634636534");
        user.put(SessionManager.KEY_USERNAME, "anggadarkprince");
        user.put(SessionManager.KEY_NAME, "Angga Ari Wijaya");
        user.put(SessionManager.KEY_LOCATION, "Gresik, Indonesia");
        user.put(SessionManager.KEY_ABOUT, "Freelancer UI/UX Designer, The wind at my back so it's time to fly. angga-ari.com");
        user.put(SessionManager.KEY_AVATAR, "http://infogue.id/images/contributors/avatar_1.jpg");
        user.put(SessionManager.KEY_COVER, "http://infogue.id/images/covers/cover_5.jpg");
        user.put(SessionManager.KEY_ARTICLE, 234);
        user.put(SessionManager.KEY_FOLLOWER, 362);
        user.put(SessionManager.KEY_FOLLOWING, 345);
        return sessionManager.createLoginSession(user);
    }
}
