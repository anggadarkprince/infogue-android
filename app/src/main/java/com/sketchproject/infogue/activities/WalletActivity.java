package com.sketchproject.infogue.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.TransactionFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Transaction;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;
import com.sketchproject.infogue.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WalletActivity extends AppCompatActivity implements TransactionFragment.OnTransactionInteractionListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private TransactionFragment transactionFragment;
    private NumberFormat formatter;
    private TextView balance;
    private TextView deferred;
    private Button buttonWithdraw;

    private double minWithdrawal;
    private double balanceValue;
    private double deferredValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        balance = (TextView) findViewById(R.id.balance);
        deferred = (TextView) findViewById(R.id.deferred);
        buttonWithdraw = (Button) findViewById(R.id.btn_withdraw);
        buttonWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWithdrawal = new Intent(WalletActivity.this, WithdrawalActivity.class);
                intentWithdrawal.putExtra("balance", balanceValue);
                intentWithdrawal.putExtra("deferred", deferredValue);
                intentWithdrawal.putExtra("min", minWithdrawal);
                startActivityForResult(intentWithdrawal, WithdrawalActivity.WITHDRAWAL);
            }
        });

        formatter = NumberFormat.getInstance(Locale.getDefault());
        transactionFragment = (TransactionFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    transactionFragment.refreshTransactionList(swipeRefreshLayout);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WithdrawalActivity.WITHDRAWAL) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                final Snackbar snackbar = Snackbar.make(swipeRefreshLayout, R.string.message_withdraw_saved, Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(ContextCompat.getColor(getBaseContext(), R.color.light));
                snackbar.setAction(R.string.action_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundResource(R.color.color_success);

                swipeRefreshLayout.setRefreshing(true);
                transactionFragment.refreshTransactionList(swipeRefreshLayout);
            }
        }
    }

    /**
     * Set swipe to refresh enable or disable, user could swipe when reach top.
     *
     * @param state enable or not
     */
    public void setSwipeEnable(boolean state) {
        swipeRefreshLayout.setEnabled(state);
    }

    public void updateBalanceAndDeferred(double balance, double deferred, double min) {
        minWithdrawal = min;
        balanceValue = balance;
        deferredValue = deferred;

        buttonWithdraw.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
        buttonWithdraw.setEnabled(true);
        this.balance.setText(getString(R.string.label_currency, formatter.format(balance)));
        this.deferred.setText(getString(R.string.label_deferred, formatter.format(deferred)));
        if (deferred > 0) {
            this.deferred.setVisibility(View.VISIBLE);
        } else {
            this.deferred.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTransactionClicked(Transaction transaction) {
        Log.i("Infogue/Transaction", transaction.getDescription());
    }

    @Override
    public void onDeleteTransaction(final Transaction transaction) {
        if (transaction.getStatus().equals(Transaction.STATUS_PENDING)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
            builder.setTitle(R.string.label_dialog_cancel_withdrawal);
            builder.setMessage(R.string.message_cancel_withdrawal);
            builder.setPositiveButton(R.string.action_cancel_withdrawal, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ConnectionDetector connectionDetector = new ConnectionDetector(getBaseContext());
                    if (connectionDetector.isNetworkAvailable()) {
                        transactionFragment.cancelTransactionRow(transaction.getId());

                        String successMessage = "You have cancelled transaction ID#" + transaction.getId();
                        Helper.toastColor(WalletActivity.this, successMessage, R.color.color_warning_transparent);

                        deferredValue -= transaction.getAmount().doubleValue();
                        deferred.setText(getString(R.string.label_deferred, formatter.format(deferredValue)));
                        if (deferredValue > 0) {
                            deferred.setVisibility(View.VISIBLE);
                        } else {
                            deferred.setVisibility(View.GONE);
                        }

                        cancelTransaction(transaction.getId());
                    } else {
                        connectionDetector.snackbarDisconnectNotification(findViewById(R.id.btn_withdraw), null);
                    }
                }
            });
            builder.setNegativeButton(R.string.action_dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog confirmationDialog = builder.create();
            confirmationDialog.show();
            Helper.setDialogButtonTheme(this, confirmationDialog);
        } else {
            Helper.toastColor(WalletActivity.this, "You can't cancel this transaction", R.color.color_danger_transparent);
        }
    }

    /**
     * Delete image from server and remove the adapter immediately without waiting delete progress
     * and ignoring the status of the action.
     *
     * @param id image unique identity
     */
    public void cancelTransaction(final int id) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_WALLET_CANCEL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Transaction", "[Cancel] Success : " + message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        String errorMessage = new Logger().networkRequestError(getBaseContext(), error, "Transaction");
                        Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                SessionManager sessionManager = new SessionManager(getBaseContext());
                Map<String, String> params = new HashMap<>();
                params.put(APIBuilder.METHOD, APIBuilder.METHOD_PUT);
                params.put(Contributor.API_TOKEN, sessionManager.getSessionData(SessionManager.KEY_TOKEN, null));
                params.put(Contributor.FOREIGN, String.valueOf(sessionManager.getSessionData(SessionManager.KEY_ID, 0)));
                params.put(Transaction.ID, String.valueOf(id));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_MEDIUM,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
    }
}
