package com.sketchproject.infogue.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.modules.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements Validator.ViewValidation {

    private UserRegisterTask mRegisterTask = null;
    private Validator validator;
    private AlertFragment alert;

    private EditText mNameView;
    private EditText mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private CheckBox mAgreeView;
    private View mProgressView;
    private View mRegisterFormView;

    private String name;
    private String email;
    private String username;
    private String password;
    private List<String> validationMessage;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        validator = new Validator();
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNameView = (EditText) getActivity().findViewById(R.id.name);
        mEmailView = (EditText) getActivity().findViewById(R.id.email);
        mUsernameView = (EditText) getActivity().findViewById(R.id.username);
        mUsernameView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
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

        alert = (AlertFragment) getChildFragmentManager().findFragmentById(R.id.alert_register);
    }

    private void attemptRegister() {
        if (mRegisterTask != null) {
            return;
        }

        preValidation();
        postValidation(onValidateView());
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

    @Override
    public void preValidation() {
        // Store values at the time of the login attempt.
        name = mNameView.getText().toString();
        email = mEmailView.getText().toString();
        username = mUsernameView.getText().toString();
        password = mPasswordView.getText().toString();
        validationMessage = new ArrayList<>();
    }

    @Override
    public boolean onValidateView() {
        boolean isInvalid = false;
        View focusView = null;

        // validation of name
        boolean isNameEmpty = validator.isEmpty(name, true);
        boolean isNameValid = validator.isPersonName(name);
        boolean isNameValidLength = validator.maxLength(name, 50);
        if (isNameEmpty || !isNameValid || !isNameValidLength) {
            if (isNameEmpty) {
                validationMessage.add(getString(R.string.error_name_required));
            } else if (!isNameValid) {
                validationMessage.add(getString(R.string.error_name_person));
            } else {
                validationMessage.add(getString(R.string.error_name_length));
            }
            focusView = mNameView;
            isInvalid = true;
        }

        // validation of username
        boolean isUsernameEmpty = validator.isEmpty(username, true);
        boolean isUsernameValid = validator.isAlphaDash(username);
        boolean isUsernameValidLength = validator.maxLength(username, 20);
        if (isUsernameEmpty || !isUsernameValid || !isUsernameValidLength) {
            if (isUsernameEmpty) {
                validationMessage.add(getString(R.string.error_username_required));
            } else if (!isUsernameValid) {
                validationMessage.add(getString(R.string.error_username_invalid));
            } else {
                validationMessage.add(getString(R.string.error_username_length));
            }
            focusView = mUsernameView;
            isInvalid = true;
        }

        // validation of email
        boolean isEmailEmpty = validator.isEmpty(email, true);
        boolean isEmailValid = validator.isValidEmail(email);
        boolean isEmailValidLength = validator.maxLength(email, 30);
        if (isEmailEmpty || !isEmailValid || !isEmailValidLength) {
            if (isEmailEmpty) {
                validationMessage.add(getString(R.string.error_email_required));
            } else if (!isEmailValid) {
                validationMessage.add(getString(R.string.error_email_invalid));
            } else {
                validationMessage.add(getString(R.string.error_email_length));
            }
            focusView = mEmailView;
            isInvalid = true;
        }

        // validation of password
        boolean isPasswordEmpty = validator.isEmpty(password, true);
        boolean isPasswordValid = validator.rangeLength(password, 6, 20);
        if (isPasswordEmpty || !isPasswordValid) {
            if (isPasswordEmpty) {
                validationMessage.add(getString(R.string.error_password_required));
            } else {
                validationMessage.add(getString(R.string.error_password_range));
            }
            focusView = mPasswordView;
            isInvalid = true;
        }

        // validation of agreement
        if (!mAgreeView.isChecked()) {
            validationMessage.add(getString(R.string.error_agreement_checked));
            focusView = mAgreeView;
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
            mRegisterTask = new UserRegisterTask(name, email, username, password);
            mRegisterTask.execute((Void) null);
        } else {
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
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
            if (success) {
                getActivity().finish();
            } else {
                showProgress(false);
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
