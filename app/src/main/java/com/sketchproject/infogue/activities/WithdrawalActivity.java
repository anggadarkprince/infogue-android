package com.sketchproject.infogue.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.AlertFragment;
import com.sketchproject.infogue.models.Bank;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Repositories.BankRepository;
import com.sketchproject.infogue.models.Transaction;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.Validator;
import com.sketchproject.infogue.modules.VolleyMultipartRequest;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;
import com.sketchproject.infogue.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WithdrawalActivity extends AppCompatActivity {
    public static final int WITHDRAWAL = 123;

    private ProgressDialog progress;
    private SessionManager session;
    private ConnectionDetector connectionDetector;
    private BankRepository bankRepository;
    private AlertFragment alert;
    private Validator validator;
    private NumberFormat formatter;

    private ScrollView scrollView;
    private ImageView imageBank;
    private TextView labelAccountName;
    private TextView labelAccountNumber;
    private TextView labelBank;
    private TextView labelIncomplete;
    private EditText inputAmount;
    private Button buttonWithdraw;

    private double balance;
    private double deferred;
    private double min;
    private double max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        validator = new Validator();
        alert = (AlertFragment) getSupportFragmentManager().findFragmentById(R.id.alert_fragment);
        session = new SessionManager(getBaseContext());
        connectionDetector = new ConnectionDetector(getBaseContext());
        bankRepository = new BankRepository();
        progress = new ProgressDialog(WithdrawalActivity.this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        scrollView = (ScrollView) findViewById(R.id.scroll_container);
        imageBank = (ImageView) findViewById(R.id.label_bank_logo);
        labelAccountName = (TextView) findViewById(R.id.label_account_name);
        labelAccountNumber = (TextView) findViewById(R.id.label_account_number);
        labelBank = (TextView) findViewById(R.id.label_bank);
        inputAmount = (EditText) findViewById(R.id.input_amount);

        TextView labelBalance = (TextView) findViewById(R.id.label_balance);
        TextView labelDeferred = (TextView) findViewById(R.id.label_deferred);
        TextView labelMin = (TextView) findViewById(R.id.label_min);
        TextView labelMax = (TextView) findViewById(R.id.label_max);

        labelIncomplete = (TextView) findViewById(R.id.label_incomplete);
        buttonWithdraw = (Button) findViewById(R.id.btn_withdraw);
        buttonWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionDetector.isNetworkAvailable()) {
                    withdrawValidation();
                } else {
                    connectionDetector.snackbarDisconnectNotification(findViewById(R.id.btn_withdraw), null);
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        balance = extras.getDouble("balance");
        deferred = extras.getDouble("deferred");
        min = extras.getDouble("min");
        max = balance - deferred;
        if (max > 5000000) {
            max = 5000000;
        }

        formatter = NumberFormat.getInstance(Locale.getDefault());

        labelBalance.setText(getString(R.string.label_currency, String.valueOf(formatter.format(balance))));
        labelDeferred.setText(getString(R.string.label_currency, String.valueOf(formatter.format(deferred))));
        labelMin.setText(getString(R.string.label_currency, String.valueOf(formatter.format(min))));
        labelMax.setText(getString(R.string.label_currency, String.valueOf(formatter.format(max))));

        retrieveProfile();
    }

    private void withdrawValidation() {
        alert.dismiss();

        double amount;
        try{
            amount = Double.parseDouble(inputAmount.getText().toString());
        } catch (NumberFormatException error){
            amount = 0;
        }

        boolean isEmpty = validator.isEmpty(inputAmount.getText().toString(), true);
        boolean isValidMax = validator.maxValue(amount, max);
        boolean isValidMin = validator.minValue(amount, min);
        List<String> validationMessage = new ArrayList<>();

        if(isEmpty){
            validationMessage.add(getString(R.string.error_opt_required, "Amount"));
        }
        if(!isValidMax){
            validationMessage.add(getString(R.string.error_opt_max_value, "Amount", String.valueOf(formatter.format(max))));
        }
        if(!isValidMin){
            validationMessage.add(getString(R.string.error_opt_min_value, "Amount", String.valueOf(formatter.format(min))));
        }

        if(!isEmpty && isValidMax && isValidMin){
            withdraw();
        } else {
            scrollView.smoothScrollTo(0, 0);
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertTitle(getString(R.string.message_validation_warning));
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
    }

    private void withdraw() {
        progress.setMessage(getString(R.string.label_save_withdraw_progress));
        progress.show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, APIBuilder.URL_API_WALLET_WITHDRAW, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    String status = result.getString(APIBuilder.RESPONSE_STATUS);
                    String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                    Log.i("Infogue/Withdrawal", message);

                    if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                        Intent returnIntent = new Intent();
                        setResult(AppCompatActivity.RESULT_OK, returnIntent);
                        finish();
                    } else {
                        scrollView.smoothScrollTo(0, 0);
                        alert.setAlertType(AlertFragment.ALERT_INFO);
                        alert.setAlertMessage(getString(R.string.error_unknown));
                        alert.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    scrollView.smoothScrollTo(0, 0);
                    alert.setAlertType(AlertFragment.ALERT_INFO);
                    alert.setAlertMessage(getString(R.string.error_parse_data));
                }
                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                String errorMessage = new Logger().networkRequestError(getBaseContext(), error, "Withdrawal");

                scrollView.smoothScrollTo(0, 0);
                alert.setAlertType(AlertFragment.ALERT_DANGER);
                alert.setAlertMessage(errorMessage);
                alert.show();
                progress.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Contributor.API_TOKEN, session.getSessionData(SessionManager.KEY_TOKEN, null));
                params.put(Contributor.FOREIGN, String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                params.put(Transaction.AMOUNT, inputAmount.getText().toString());
                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_LONG,
                APIBuilder.NO_RETRY,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }

    private void retrieveProfile() {
        progress.setMessage(getString(R.string.label_retrieve_setting_progress));
        progress.show();
        String username = session.getSessionData(SessionManager.KEY_USERNAME, null);

        JsonObjectRequest contributorRequest = new JsonObjectRequest(Request.Method.GET, APIBuilder.getApiContributorUrl(username), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString(APIBuilder.RESPONSE_STATUS);
                            JSONObject contributor = response.getJSONObject(Contributor.DATA);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                boolean incomplete = false;

                                if (contributor.optInt(Contributor.BANK_ID, 0) != 0) {
                                    Bank bank = bankRepository.findData(new Bank(contributor.optInt(Contributor.BANK_ID, 0), null, null));
                                    labelBank.setText(bank.getBank());
                                    Glide.with(getBaseContext()).load(bank.getLogoUrl())
                                            .crossFade()
                                            .into(imageBank);
                                } else {
                                    imageBank.setVisibility(View.GONE);
                                    labelBank.setText(R.string.label_no_data);
                                    incomplete = true;
                                }

                                if (!contributor.getString(Contributor.BANK_ACCOUNT_NAME).equals("null")) {
                                    labelAccountName.setText(contributor.getString(Contributor.BANK_ACCOUNT_NAME));
                                } else {
                                    labelAccountName.setText(R.string.label_no_data);
                                    incomplete = true;
                                }

                                if (!contributor.getString(Contributor.BANK_ACCOUNT_NUMBER).equals("null")) {
                                    labelAccountNumber.setText(contributor.getString(Contributor.BANK_ACCOUNT_NUMBER));
                                } else {
                                    labelAccountName.setText(R.string.label_no_data);
                                    incomplete = true;
                                }

                                if (incomplete) {
                                    labelIncomplete.setVisibility(View.VISIBLE);
                                } else {
                                    buttonWithdraw.setEnabled(true);
                                    buttonWithdraw.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.light));
                                }

                            } else {
                                Log.w("Infogue/Withdrawal", getString(R.string.error_unknown));
                                Helper.toastColor(getBaseContext(), R.string.error_unknown, R.color.color_warning_transparent);

                                Intent returnIntent = new Intent();
                                setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Intent returnIntent = new Intent();
                            setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
                            finish();
                        }
                        progress.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        String errorMessage = new Logger().networkRequestError(getBaseContext(), error, "Withdrawal");

                        Helper.toastColor(getBaseContext(), errorMessage, R.color.color_danger_transparent);
                        progress.dismiss();

                        Intent returnIntent = new Intent();
                        setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
                }
        );
        contributorRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_SHORT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(contributorRequest);
    }
}
