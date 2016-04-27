package com.sketchproject.infogue.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.AlertFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.RealPathResolver;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.Validator;
import com.sketchproject.infogue.modules.VolleyMultipartRequest;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Setting activity handle user configuration and password
 * Created by Angga 20/04/2016 19:32
 */
public class SettingsActivity extends AppCompatActivity implements Validator.ViewValidation {

    public static final int SETTING_RESULT_CODE = 100;

    protected final int PICK_IMAGE_AVATAR = 1;
    protected final int PICK_IMAGE_COVER = 2;

    private Validator validator;
    private SessionManager session;
    private ConnectionDetector connectionDetector;
    private AlertFragment alert;
    private Contributor contributor;
    private List<String> validationMessage;
    private ProgressDialog progress;

    private AlertDialog dialogDiscard;
    private AlertDialog dialogSave;
    private ScrollView mScrollView;
    private ImageView mAvatarImage;
    private ImageView mCoverImage;
    private EditText mNameInput;
    private EditText mLocationInput;
    private EditText mAboutInput;
    private EditText mContact;
    private EditText mBirthdayInput;
    private RadioButton mGenderMaleRadio;
    private RadioButton mGenderFemaleRadio;
    private EditText mFacebookInput;
    private EditText mTwitterInput;
    private EditText mGooglePlusInput;
    private EditText mInstagramInput;
    private CheckBox mNotificationSubscribeCheck;
    private CheckBox mNotificationMessageCheck;
    private CheckBox mNotificationFollowerCheck;
    private CheckBox mNotificationStreamCheck;
    private SwitchCompat mPushNotificationSwitch;
    private EditText mUsernameInput;
    private EditText mEmailInput;
    private EditText mPasswordInput;
    private EditText mNewPasswordInput;
    private EditText mConfirmPasswordInput;

    private String mRealPathAvatar;
    private String mRealPathCover;
    private boolean mIsSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        validator = new Validator();
        session = new SessionManager(getBaseContext());
        connectionDetector = new ConnectionDetector(getBaseContext());
        alert = (AlertFragment) getSupportFragmentManager().findFragmentById(R.id.alert_fragment);

        progress = new ProgressDialog(SettingsActivity.this);
        progress.setIndeterminate(true);

        mScrollView = (ScrollView) findViewById(R.id.scroll_container);
        mAvatarImage = (ImageView) findViewById(R.id.avatar);
        mCoverImage = (ImageView) findViewById(R.id.cover);
        mNameInput = (EditText) findViewById(R.id.input_name);
        mLocationInput = (EditText) findViewById(R.id.input_location);
        mAboutInput = (EditText) findViewById(R.id.input_about);
        mContact = (EditText) findViewById(R.id.input_contact);
        mGenderMaleRadio = (RadioButton) findViewById(R.id.radio_male);
        mGenderFemaleRadio = (RadioButton) findViewById(R.id.radio_female);
        mFacebookInput = (EditText) findViewById(R.id.input_facebook);
        mTwitterInput = (EditText) findViewById(R.id.input_twitter);
        mGooglePlusInput = (EditText) findViewById(R.id.input_google);
        mInstagramInput = (EditText) findViewById(R.id.input_instagram);
        mNotificationSubscribeCheck = (CheckBox) findViewById(R.id.notification_subscribe);
        mNotificationMessageCheck = (CheckBox) findViewById(R.id.notification_message);
        mNotificationFollowerCheck = (CheckBox) findViewById(R.id.notification_follower);
        mNotificationStreamCheck = (CheckBox) findViewById(R.id.notification_stream);
        mPushNotificationSwitch = (SwitchCompat) findViewById(R.id.push_notification);
        mUsernameInput = (EditText) findViewById(R.id.input_username);
        mEmailInput = (EditText) findViewById(R.id.input_email);
        mPasswordInput = (EditText) findViewById(R.id.input_password);
        mNewPasswordInput = (EditText) findViewById(R.id.input_new_password);
        mConfirmPasswordInput = (EditText) findViewById(R.id.input_confirm_password);
        mBirthdayInput = (EditText) findViewById(R.id.input_birthday);
        mBirthdayInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });
        mBirthdayInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectDate();
                }
            }
        });

        Button mChangeAvatarButton = (Button) findViewById(R.id.btn_change_avatar);
        if (mChangeAvatarButton != null) {
            mChangeAvatarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectImage(getString(R.string.label_intent_media_avatar), PICK_IMAGE_AVATAR);
                }
            });
        }

        Button mChangeCoverButton = (Button) findViewById(R.id.btn_change_cover);
        if (mChangeCoverButton != null) {
            mChangeCoverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectImage(getString(R.string.label_intent_media_cover), PICK_IMAGE_COVER);
                }
            });
        }

        Button mSaveSettingButton = (Button) findViewById(R.id.btn_save_setting);
        if (mSaveSettingButton != null) {
            mSaveSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preValidation();
                    postValidation(onValidateView());
                }
            });
        }

        AlertDialog.Builder builderDiscard = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builderDiscard.setTitle(getString(R.string.label_dialog_discard_setting));
        builderDiscard.setMessage(getString(R.string.message_discard_setting));
        builderDiscard.setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent returnIntent = new Intent();
                setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        builderDiscard.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDiscard = builderDiscard.create();

        AlertDialog.Builder builderSave = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builderSave.setTitle(R.string.label_dialog_save_setting);
        builderSave.setMessage(R.string.message_save_setting);
        builderSave.setPositiveButton(R.string.action_save_setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveSettings();
            }
        });

        builderSave.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogSave = builderSave.create();

        mIsSaved = false;
        retrieveProfile();
    }

    @Override
    public void onBackPressed() {
        if (dialogDiscard != null && dialogDiscard.isShowing()) {
            dialogDiscard.cancel();
        } else {
            discardConfirmation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            discardConfirmation();
        } else if (id == R.id.action_save) {
            preValidation();
            postValidation(onValidateView());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void preValidation() {
        contributor = new Contributor();
        contributor.setId(session.getSessionData(SessionManager.KEY_ID, 0));
        contributor.setUsername(mUsernameInput.getText().toString());
        contributor.setEmail(mEmailInput.getText().toString());
        contributor.setPassword(mPasswordInput.getText().toString());
        contributor.setNewPassword(mNewPasswordInput.getText().toString());
        contributor.setName(mNameInput.getText().toString());
        contributor.setLocation(mLocationInput.getText().toString());
        contributor.setAbout(mAboutInput.getText().toString());
        contributor.setFacebook(mFacebookInput.getText().toString());
        contributor.setTwitter(mTwitterInput.getText().toString());
        contributor.setGooglePlus(mGooglePlusInput.getText().toString());
        contributor.setInstagram(mInstagramInput.getText().toString());
        contributor.setNotificationSubscribe(mNotificationSubscribeCheck.isChecked());
        contributor.setNotificationMessage(mNotificationMessageCheck.isChecked());
        contributor.setNotificationFollower(mNotificationFollowerCheck.isChecked());
        contributor.setNotificationStream(mNotificationStreamCheck.isChecked());
        contributor.setPushNotification(mPushNotificationSwitch.isChecked());
        contributor.setAvatar(mRealPathAvatar);
        contributor.setCover(mRealPathCover);
        contributor.setContact(mContact.getText().toString());
        contributor.setGender(mGenderMaleRadio.isChecked() ? Contributor.GENDER_MALE :
                mGenderFemaleRadio.isChecked() ? Contributor.GENDER_FEMALE : Contributor.GENDER_OTHER);
        try {
            DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
            Date birthday = simpleDateFormat.parse(mBirthdayInput.getText().toString());
            contributor.setBirthday(birthday);
        } catch (ParseException e) {
            contributor.setBirthday(null);
            e.printStackTrace();
        }
    }

    @Override
    public boolean onValidateView() {
        boolean isInvalid = false;
        View focusView = null;

        validationMessage = new ArrayList<>();

        // validation of session
        boolean isUserIdEmpty = validator.isEmpty(contributor.getId());
        if (isUserIdEmpty) {
            validationMessage.add(getString(R.string.error_session_required));
            focusView = null;
        }

        // validation of name
        boolean isNameEmpty = validator.isEmpty(contributor.getName(), true);
        boolean isNameValidLength = validator.maxLength(contributor.getName(), 50);
        if (isNameEmpty || !isNameValidLength) {
            if (isNameEmpty) {
                validationMessage.add(getString(R.string.error_name_required));
            } else {
                validationMessage.add(getString(R.string.error_name_length));
            }
            focusView = mNameInput;
            isInvalid = true;
        }

        // validation of location
        boolean isLocationEmpty = validator.isEmpty(contributor.getLocation());
        boolean isLocationValidLength = validator.maxLength(contributor.getLocation(), 30);
        if (isLocationEmpty || !isLocationValidLength) {
            if (isLocationEmpty) {
                validationMessage.add(getString(R.string.error_location_required));
            } else {
                validationMessage.add(getString(R.string.error_location_length));
            }
            focusView = mLocationInput;
            isInvalid = true;
        }

        // validation of about
        boolean isAboutEmpty = validator.isEmpty(contributor.getAbout());
        boolean isAboutValidLength = validator.maxLength(contributor.getAbout(), 160);
        if (isAboutEmpty || !isAboutValidLength) {
            if (isAboutEmpty) {
                validationMessage.add(getString(R.string.error_about_required));
            } else {
                validationMessage.add(getString(R.string.error_about_length));
            }
            focusView = mAboutInput;
            isInvalid = true;
        }

        // validation of contact
        boolean isContactEmpty = validator.isEmpty(contributor.getContact());
        boolean isContactValidLength = validator.maxLength(contributor.getContact(), 30);
        if (isContactEmpty || !isContactValidLength) {
            if (isContactEmpty) {
                validationMessage.add(getString(R.string.error_contact_required));
            } else {
                validationMessage.add(getString(R.string.error_contact_length));
            }
            focusView = mContact;
            isInvalid = true;
        }

        // validation of birthday
        boolean isBirthdayEmpty = validator.isEmpty(contributor.getBirthday());
        boolean isBirthdayValid = false;
        if (!isBirthdayEmpty) {
            isBirthdayValid = validator.isValidDate(new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault()).format(contributor.getBirthday()));
        }
        if (isBirthdayEmpty || !isBirthdayValid) {
            if (isBirthdayEmpty) {
                validationMessage.add(getString(R.string.error_birthday_required));
            } else {
                validationMessage.add(getString(R.string.error_birthday_invalid));
            }
            focusView = mBirthdayInput;
            isInvalid = true;
        }

        // validation of gender
        boolean isGenderValid = validator.isMemberOf(contributor.getGender(),
                new String[]{
                        Contributor.GENDER_MALE,
                        Contributor.GENDER_FEMALE,
                        Contributor.GENDER_OTHER
                });
        if (!isGenderValid) {
            validationMessage.add(getString(R.string.error_gender_invalid));
            focusView = mGenderMaleRadio;
            isInvalid = true;
        }

        // validation of facebook
        if (!validator.isEmpty(contributor.getFacebook(), true)) {
            boolean isFacebookValidUrl = validator.isValidUrl(contributor.getFacebook());
            boolean isFacebookValidDomain = contributor.getFacebook().contains("facebook.com");
            boolean isFacebookValidLength = validator.maxLength(contributor.getFacebook(), 100);
            if (!isFacebookValidUrl || !isFacebookValidDomain || !isFacebookValidLength) {
                if (!isFacebookValidUrl) {
                    validationMessage.add(getString(R.string.error_facebook_invalid_url));
                } else if (!isFacebookValidDomain) {
                    validationMessage.add(getString(R.string.error_facebook_invalid_domain));
                } else {
                    validationMessage.add(getString(R.string.error_facebook_invalid_length));
                }
                focusView = mFacebookInput;
                isInvalid = true;
            }
        }

        // validation of twitter
        if (!validator.isEmpty(contributor.getTwitter(), true)) {
            boolean isTwitterValidUrl = validator.isValidUrl(contributor.getTwitter());
            boolean isTwitterValidDomain = contributor.getTwitter().contains("twitter.com");
            boolean isTwitterValidLength = validator.maxLength(contributor.getTwitter(), 100);
            if (!isTwitterValidUrl || !isTwitterValidDomain || !isTwitterValidLength) {
                if (!isTwitterValidUrl) {
                    validationMessage.add(getString(R.string.error_twitter_invalid_url));
                } else if (!isTwitterValidDomain) {
                    validationMessage.add(getString(R.string.error_twitter_invalid_domain));
                } else {
                    validationMessage.add(getString(R.string.error_twitter_invalid_length));
                }
                focusView = mTwitterInput;
                isInvalid = true;
            }
        }

        // validation of google
        if (!validator.isEmpty(contributor.getGooglePlus(), true)) {
            boolean isGooglePlusValidUrl = validator.isValidUrl(contributor.getGooglePlus());
            boolean isGooglePlusValidDomain = contributor.getGooglePlus().contains("plus.google.com");
            boolean isGooglePlusValidLength = validator.maxLength(contributor.getGooglePlus(), 100);
            if (!isGooglePlusValidUrl || !isGooglePlusValidDomain || !isGooglePlusValidLength) {
                if (!isGooglePlusValidUrl) {
                    validationMessage.add(getString(R.string.error_google_plus_invalid_url));
                } else if (!isGooglePlusValidDomain) {
                    validationMessage.add(getString(R.string.error_google_plus_invalid_domain));
                } else {
                    validationMessage.add(getString(R.string.error_google_plus_invalid_length));
                }
                focusView = mGooglePlusInput;
                isInvalid = true;
            }
        }

        // validation of instagram
        if (!validator.isEmpty(contributor.getInstagram(), true)) {
            boolean isInstagramValidUrl = validator.isValidUrl(contributor.getInstagram());
            boolean isInstagramValidDomain = contributor.getInstagram().contains("instagram.com");
            boolean isInstagramValidLength = validator.maxLength(contributor.getInstagram(), 100);
            if (!isInstagramValidUrl || !isInstagramValidDomain || !isInstagramValidLength) {
                if (!isInstagramValidUrl) {
                    validationMessage.add(getString(R.string.error_instagram_invalid_url));
                } else if (!isInstagramValidDomain) {
                    validationMessage.add(getString(R.string.error_instagram_invalid_domain));
                } else {
                    validationMessage.add(getString(R.string.error_instagram_invalid_length));
                }
                focusView = mInstagramInput;
                isInvalid = true;
            }
        }

        boolean isPasswordEmpty = validator.isEmpty(contributor.getPassword());
        if (isPasswordEmpty) {
            validationMessage.add(getString(R.string.error_password_current));
            focusView = mPasswordInput;
            isInvalid = true;
        }

        // validation of new password
        boolean isNewPasswordEmpty = validator.isEmpty(mNewPasswordInput.getText().toString(), true);
        boolean isConfirmPasswordEmpty = validator.isEmpty(mConfirmPasswordInput.getText().toString(), true);
        if (!isNewPasswordEmpty || !isConfirmPasswordEmpty) {
            boolean isNewPasswordConfirmed = mConfirmPasswordInput.getText().toString().equals(mNewPasswordInput.getText().toString());
            boolean isNewPasswordValidRange = validator.rangeLength(contributor.getNewPassword(), 6, 20);
            if (!isNewPasswordConfirmed || !isNewPasswordValidRange) {
                if (!isNewPasswordConfirmed) {
                    validationMessage.add(getString(R.string.error_password_confirmed));
                } else {
                    validationMessage.add(getString(R.string.error_password_range));
                }
                focusView = mConfirmPasswordInput;
                isInvalid = true;
            }
        }

        // validation of username
        boolean isUsernameEmpty = validator.isEmpty(contributor.getUsername(), true);
        boolean isUsernameValid = validator.isAlphaDash(contributor.getUsername());
        boolean isUsernameValidLength = validator.maxLength(contributor.getUsername(), 20);
        if (isUsernameEmpty || !isUsernameValid || !isUsernameValidLength) {
            if (isUsernameEmpty) {
                validationMessage.add(getString(R.string.error_username_required));
            } else if (!isUsernameValid) {
                validationMessage.add(getString(R.string.error_username_invalid));
            } else {
                validationMessage.add(getString(R.string.error_username_length));
            }
            focusView = mUsernameInput;
            isInvalid = true;
        }

        // validation of email
        boolean isEmailEmpty = validator.isEmpty(contributor.getEmail(), true);
        boolean isEmailValid = validator.isValidEmail(contributor.getEmail());
        boolean isEmailValidLength = validator.maxLength(contributor.getEmail(), 30);
        if (isEmailEmpty || !isEmailValid || !isEmailValidLength) {
            if (isEmailEmpty) {
                validationMessage.add(getString(R.string.error_email_required));
            } else if (!isEmailValid) {
                validationMessage.add(getString(R.string.error_email_invalid));
            } else {
                validationMessage.add(getString(R.string.error_email_length));
            }
            focusView = mEmailInput;
            isInvalid = true;
        }

        if (isInvalid && focusView != null) {
            focusView.requestFocus();
        }

        return !isInvalid;
    }

    @Override
    public void postValidation(boolean isValid) {
        if (isValid) {
            alert.dismiss();
            saveConfirmation();
        } else {
            mScrollView.smoothScrollTo(0, 0);
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertTitle(getString(R.string.message_validation_warning));
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            String realPath;

            if (Build.VERSION.SDK_INT < 19) {
                realPath = RealPathResolver.getRealPathFromURI_API11to18(this, data.getData());
            } else {
                realPath = RealPathResolver.getRealPathFromURI_API19(this, data.getData());
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (requestCode == PICK_IMAGE_AVATAR) {
                    mRealPathAvatar = realPath;
                    mAvatarImage.setImageBitmap(bitmap);
                } else if (requestCode == PICK_IMAGE_COVER) {
                    mRealPathCover = realPath;
                    mCoverImage.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void retrieveProfile() {
        progress.setMessage(getString(R.string.label_retrieve_setting_progress));
        progress.show();
        String username = session.getSessionData(SessionManager.KEY_USERNAME, null);

        JsonObjectRequest contributorRequest = new JsonObjectRequest(Request.Method.GET, UrlHelper.getApiContributorUrl(username), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            JSONObject contributor = response.getJSONObject("contributor");

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                Glide.clear(mAvatarImage);
                                Glide.with(getBaseContext()).load(contributor.getString(Contributor.CONTRIBUTOR_AVATAR_REF))
                                        .placeholder(R.drawable.placeholder_square)
                                        .centerCrop()
                                        .crossFade()
                                        .into(mAvatarImage);
                                Glide.clear(mCoverImage);
                                Glide.with(getBaseContext()).load(contributor.getString(Contributor.CONTRIBUTOR_COVER_REF))
                                        .placeholder(R.drawable.placeholder_rectangle)
                                        .centerCrop()
                                        .crossFade()
                                        .into(mCoverImage);
                                mNameInput.setText(contributor.getString(Contributor.CONTRIBUTOR_NAME));
                                mLocationInput.setText(contributor.getString(Contributor.CONTRIBUTOR_LOCATION));
                                mAboutInput.setText(contributor.getString(Contributor.CONTRIBUTOR_ABOUT));
                                mContact.setText(contributor.getString(Contributor.CONTRIBUTOR_CONTACT));
                                mGenderMaleRadio.setChecked(contributor.getString(Contributor.CONTRIBUTOR_GENDER).equals(Contributor.GENDER_MALE));
                                mGenderFemaleRadio.setChecked(contributor.getString(Contributor.CONTRIBUTOR_GENDER).equals(Contributor.GENDER_FEMALE));
                                mBirthdayInput.setText(contributor.getString(Contributor.CONTRIBUTOR_BIRTHDAY));
                                mFacebookInput.setText(contributor.getString(Contributor.CONTRIBUTOR_FACEBOOK));
                                mTwitterInput.setText(contributor.getString(Contributor.CONTRIBUTOR_TWITTER));
                                mGooglePlusInput.setText(contributor.getString(Contributor.CONTRIBUTOR_GOOGLE_PLUS));
                                mInstagramInput.setText(contributor.getString(Contributor.CONTRIBUTOR_INSTAGRAM));
                                mNotificationSubscribeCheck.setChecked(contributor.getInt(Contributor.CONTRIBUTOR_SUBSCRIPTION) == 1);
                                mNotificationMessageCheck.setChecked(contributor.getInt(Contributor.CONTRIBUTOR_MESSAGE) == 1);
                                mNotificationFollowerCheck.setChecked(contributor.getInt(Contributor.CONTRIBUTOR_FOLLOWER) == 1);
                                mNotificationStreamCheck.setChecked(contributor.getInt(Contributor.CONTRIBUTOR_FEED) == 1);
                                mPushNotificationSwitch.setChecked(contributor.getInt(Contributor.CONTRIBUTOR_MOBILE) == 1);
                                mUsernameInput.setText(contributor.getString(Contributor.CONTRIBUTOR_USERNAME));
                                mEmailInput.setText(contributor.getString(Contributor.CONTRIBUTOR_EMAIL));
                            } else {
                                Log.w("Infogue/Profile", getString(R.string.error_unknown));
                                AppHelper.toastColored(getBaseContext(), getString(R.string.error_unknown), Color.parseColor("#ddd1205e"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            AppHelper.toastColored(getBaseContext(), getString(R.string.error_parse_data), Color.parseColor("#ddd1205e"));
                        }

                        progress.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_unknown);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout);
                            }
                        } else {
                            String result = new String(networkResponse.data);
                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");

                                if (status.equals(Constant.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = message;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }
                        AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));
                        progress.dismiss();

                        Intent returnIntent = new Intent();
                        setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
                }
        );
        contributorRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(contributorRequest);
    }

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(SettingsActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mBirthdayInput.setText(new StringBuilder().append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth));
            }
        }, year, month, day).show();
    }

    private void selectImage(String title, int type) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, title), type);
    }

    private void discardConfirmation() {
        if (mIsSaved) {
            Intent returnIntent = new Intent();
            setResult(AppCompatActivity.RESULT_OK, returnIntent);
            finish();
        } else {
            dialogDiscard.show();
            AppHelper.dialogButtonTheme(this, dialogDiscard);
        }
    }

    private void saveConfirmation() {
        dialogSave.show();
        AppHelper.dialogButtonTheme(this, dialogSave);
    }

    private void saveSettings() {
        if (connectionDetector.isNetworkAvailable()) {
            progress.setMessage(getString(R.string.label_save_setting_progress));
            progress.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constant.URL_API_SETTING, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String resultResponse = new String(response.data);
                    try {
                        JSONObject result = new JSONObject(resultResponse);
                        String status = result.getString("status");
                        String message = result.getString("message");
                        JSONObject contributorUpdated = result.getJSONObject("contributor");

                        if (status.equals(Constant.REQUEST_SUCCESS)) {
                            alert.setAlertType(AlertFragment.ALERT_SUCCESS);
                            alert.setAlertMessage(message);
                            mIsSaved = true;

                            session.setSessionData(SessionManager.KEY_NAME, contributorUpdated.getString(Contributor.CONTRIBUTOR_NAME));
                            session.setSessionData(SessionManager.KEY_USERNAME, contributorUpdated.getString(Contributor.CONTRIBUTOR_USERNAME));
                            session.setSessionData(SessionManager.KEY_LOCATION, contributorUpdated.getString(Contributor.CONTRIBUTOR_LOCATION));
                            session.setSessionData(SessionManager.KEY_ABOUT, contributorUpdated.getString(Contributor.CONTRIBUTOR_ABOUT));
                            session.setSessionData(SessionManager.KEY_AVATAR, contributorUpdated.getString(Contributor.CONTRIBUTOR_AVATAR_REF));
                            session.setSessionData(SessionManager.KEY_COVER, contributorUpdated.getString(Contributor.CONTRIBUTOR_COVER_REF));
                        } else {
                            alert.setAlertType(AlertFragment.ALERT_INFO);
                            alert.setAlertMessage(getString(R.string.error_unknown));
                        }

                        mRealPathAvatar = null;
                        mRealPathCover = null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    alert.show();
                    mScrollView.smoothScrollTo(0, 0);
                    progress.dismiss();

                    Log.i("Infogue/setting", resultResponse);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    String errorMessage = getString(R.string.error_unknown);
                    if (networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            errorMessage = getString(R.string.error_timeout);
                        }
                    } else {
                        String result = new String(networkResponse.data);
                        try {
                            JSONObject response = new JSONObject(result);
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals(Constant.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                errorMessage = getString(R.string.error_not_found);
                            } else if (status.equals(Constant.REQUEST_MISMATCH) && networkResponse.statusCode == 401) {
                                errorMessage = message;
                            } else if (status.equals(Constant.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                errorMessage = message;
                            } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                errorMessage = message;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorMessage = getString(R.string.error_parse_data);
                        }
                    }

                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                    alert.setAlertMessage(errorMessage);
                    alert.show();

                    mScrollView.smoothScrollTo(0, 0);
                    progress.dismiss();

                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("_method", "put");
                    params.put(Contributor.CONTRIBUTOR_API, session.getSessionData(SessionManager.KEY_TOKEN, null));
                    params.put(Contributor.CONTRIBUTOR_FOREIGN, String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                    params.put(Contributor.CONTRIBUTOR_NAME, contributor.getName());
                    params.put(Contributor.CONTRIBUTOR_LOCATION, contributor.getLocation());
                    params.put(Contributor.CONTRIBUTOR_ABOUT, contributor.getAbout());
                    params.put(Contributor.CONTRIBUTOR_CONTACT, contributor.getContact());
                    params.put(Contributor.CONTRIBUTOR_GENDER, contributor.getGender());
                    params.put(Contributor.CONTRIBUTOR_BIRTHDAY, new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault()).format(contributor.getBirthday()));
                    params.put(Contributor.CONTRIBUTOR_FACEBOOK, contributor.getFacebook());
                    params.put(Contributor.CONTRIBUTOR_TWITTER, contributor.getTwitter());
                    params.put(Contributor.CONTRIBUTOR_GOOGLE_PLUS, contributor.getGooglePlus());
                    params.put(Contributor.CONTRIBUTOR_INSTAGRAM, contributor.getInstagram());
                    params.put(Contributor.CONTRIBUTOR_EMAIL, contributor.getEmail());
                    params.put(Contributor.CONTRIBUTOR_USERNAME, contributor.getUsername());
                    params.put(Contributor.CONTRIBUTOR_SUBSCRIPTION, String.valueOf(contributor.isNotificationSubscribe() ? 1 : 0));
                    params.put(Contributor.CONTRIBUTOR_MESSAGE, String.valueOf(contributor.isNotificationMessage() ? 1 : 0));
                    params.put(Contributor.CONTRIBUTOR_FOLLOWER, String.valueOf(contributor.isNotificationFollower() ? 1 : 0));
                    params.put(Contributor.CONTRIBUTOR_FEED, String.valueOf(contributor.isNotificationStream() ? 1 : 0));
                    params.put(Contributor.CONTRIBUTOR_MOBILE, String.valueOf(contributor.isPushNotification() ? 1 : 0));
                    params.put(Contributor.CONTRIBUTOR_PASSWORD, String.valueOf(contributor.getPassword()));
                    params.put(Contributor.CONTRIBUTOR_NEW_PASSWORD, String.valueOf(contributor.getNewPassword()));
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    if (mRealPathAvatar != null && !mRealPathAvatar.trim().isEmpty()) {
                        params.put(Contributor.CONTRIBUTOR_AVATAR, new DataPart("file_avatar.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mAvatarImage.getDrawable()), "image/jpeg"));
                    }
                    if (mRealPathCover != null && !mRealPathCover.trim().isEmpty()) {
                        params.put(Contributor.CONTRIBUTOR_COVER, new DataPart("file_cover.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mCoverImage.getDrawable()), "image/jpeg"));
                    }

                    return params;
                }
            };

            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);

        } else {
            connectionDetector.snackbarDisconnectNotification(mScrollView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectionDetector.dismissNotification();
                }
            });
        }
    }
}
