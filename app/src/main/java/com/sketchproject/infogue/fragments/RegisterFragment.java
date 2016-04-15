package com.sketchproject.infogue.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.sketchproject.infogue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    private UserRegisterTask mRegisterTask = null;

    private EditText mNameView;
    private EditText mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private CheckBox mAgreeView;
    private View mProgressView;
    private View mRegisterFormView;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNameView = (EditText) getActivity().findViewById(R.id.name);
        mEmailView = (EditText) getActivity().findViewById(R.id.email);
        mUsernameView = (EditText) getActivity().findViewById(R.id.username);
        mPasswordView = (EditText) getActivity().findViewById(R.id.password);
        mAgreeView = (CheckBox) getActivity().findViewById(R.id.agree);
        mAgreeView.setText(Html.fromHtml(getResources().getString(R.string.label_check_agree)));
        mAgreeView.setMovementMethod(LinkMovementMethod.getInstance());

        Button mEmailSignInButton = (Button) getActivity().findViewById(R.id.btn_sign_up);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView = getActivity().findViewById(R.id.register_form);
        mProgressView = getActivity().findViewById(R.id.register_progress);
    }

    private void attemptRegister() {
        if (mRegisterTask != null) {
            return;
        }

        List<String> validationMessage = new ArrayList<>();

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            validationMessage.add(getString(R.string.error_name_required));
            focusView = mNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            validationMessage.add(getString(R.string.error_username_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (!isValidUsername(username)) {
            validationMessage.add(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            validationMessage.add(getString(R.string.error_email_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (!isValidEmail(email)) {
            validationMessage.add(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            validationMessage.add(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!mAgreeView.isChecked()) {
            validationMessage.add(getString(R.string.error_agreement));
            focusView = mAgreeView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            AlertFragment fragment = (AlertFragment) getChildFragmentManager().findFragmentById(R.id.alert_register);
            fragment.setAlertType(AlertFragment.ALERT_WARNING);
            fragment.setAlertMessage(validationMessage);
            fragment.show();
        } else {
            showProgress(true);
            mRegisterTask = new UserRegisterTask(name, email, username, password);
            mRegisterTask.execute((Void) null);
        }
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9-_]*$");
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int mediumAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRegisterFormView
                .animate()
                .setDuration(mediumAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mName;
        private final String mEmail;
        private final String mUsername;
        private final String mPassword;

        UserRegisterTask(String name, String email, String username, String password) {
            mName = name;
            mEmail = email;
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

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegisterTask = null;
            showProgress(false);

            if (success) {
                getActivity().finish();
            } else {
                AlertFragment fragment = (AlertFragment) getChildFragmentManager().findFragmentById(R.id.alert_fragment);
                fragment.setAlertType(AlertFragment.ALERT_DANGER);
                fragment.setAlertMessage("Server error occurred, Try again!");
                fragment.show();
            }
        }

        @Override
        protected void onCancelled() {
            mRegisterTask = null;
            showProgress(false);
        }
    }
}
