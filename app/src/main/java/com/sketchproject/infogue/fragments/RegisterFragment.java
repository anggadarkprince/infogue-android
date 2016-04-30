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
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Contributor;
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
 * A simple {@link Fragment} subclass to handle register screen inside view pager.
 *
 * Sketch Project Studio
 * Created by Angga on 1/04/2016 10.37.
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

    /**
     * Default constructor fragment, triggered when device rotate and first instantiate
     */
    public RegisterFragment() {
        // Required empty public constructor
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
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    /**
     * Triggered after parent (activity) created, to make sure UI is accessible.
     *
     * @param savedInstanceState latest state if exist
     */
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
                preValidation();
                postValidation(onValidateView());
            }
        });

        mRegisterFormView = getActivity().findViewById(R.id.register_form);
        mProgressView = getActivity().findViewById(R.id.register_progress);

        alert = (AlertFragment) getChildFragmentManager().findFragmentById(R.id.alert_register);
    }

    /**
     * Shows the progress UI and hides the registration form.
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
     * Placeholder validator method to handle populating inputs.
     */
    @Override
    public void preValidation() {
        // Store values at the time of the login attempt.
        name = mNameView.getText().toString();
        email = mEmailView.getText().toString();
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
            registrationRequest();
        } else {
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
    }

    /**
     * Send registration request to server with passing data name, username, email and password.
     * Catch necessary error like contributor exist and internal server error
     */
    private void registrationRequest() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                alert.setAlertType(AlertFragment.ALERT_SUCCESS);
                                alert.setAlertTitle(getString(R.string.label_registration_complete));
                                alert.setAlertMessage(getString(R.string.message_activation_link));
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
                        error.printStackTrace();

                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_unknown);
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

                                if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                }
                                if (status.equals(APIBuilder.REQUEST_EXIST) && networkResponse.statusCode == 400) {
                                    type = AlertFragment.ALERT_WARNING;
                                    errorMessage = message;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }

                        alert.setAlertType(type);
                        alert.setAlertMessage(errorMessage);
                        alert.show();

                        showProgress(false);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Contributor.NAME, mNameView.getText().toString());
                params.put(Contributor.EMAIL, mEmailView.getText().toString());
                params.put(Contributor.USERNAME, mUsernameView.getText().toString());
                params.put(Contributor.PASSWORD, mPasswordView.getText().toString());
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getContext()).addToRequestQueue(postRequest);
    }
}
