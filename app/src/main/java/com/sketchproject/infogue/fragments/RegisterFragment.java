package com.sketchproject.infogue.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.modules.Validator;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements Validator.ViewValidation {

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
            registrationRequest();
        } else {
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
    }

    private void registrationRequest() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString("status");
                            String message = result.getString("message");

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                alert.setAlertType(AlertFragment.ALERT_SUCCESS);
                                alert.setAlertTitle("Registration Complete");
                                alert.setAlertMessage("Activation link has been sent to your email.");
                            } else {
                                alert.setAlertType(AlertFragment.ALERT_DANGER);
                                alert.setAlertMessage(message);
                            }
                            alert.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        showProgress(false);
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

                                if (status.equals(APIBuilder.REQUEST_EXIST) && networkResponse.statusCode == 400) {
                                    alert.setAlertType(AlertFragment.ALERT_WARNING);
                                    alert.setAlertTitle("Please review your inputs");
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
                params.put("name", mNameView.getText().toString());
                params.put("email", mEmailView.getText().toString());
                params.put("username", mUsernameView.getText().toString());
                params.put("password", mPasswordView.getText().toString());
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(getContext()).addToRequestQueue(postRequest);
    }
}
