package com.sketchproject.infogue.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.TransactionFragment;
import com.sketchproject.infogue.models.Transaction;
import com.sketchproject.infogue.utils.Helper;

import java.text.NumberFormat;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity implements TransactionFragment.OnTransactionInteractionListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private TransactionFragment transactionFragment;
    private NumberFormat formatter;
    private TextView balance;
    private TextView deferred;
    private Button buttonWithdraw;

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

        formatter = NumberFormat.getInstance(Locale.getDefault());
        transactionFragment = (TransactionFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    transactionFragment.refreshMessageList(swipeRefreshLayout);
                }
            });
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

    public void updateBalanceAndDeferred(double balance, double deferred) {
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
    public void onDeleteTransaction(Transaction transaction) {
        if (transaction.getStatus().equals(Transaction.STATUS_PENDING)) {
            transactionFragment.deleteTransactionRow(transaction.getId());

            String successMessage = "You have cancelled transaction ID#" + transaction.getId();
            Helper.toastColor(WalletActivity.this, successMessage, R.color.color_warning_transparent);
        }

        Helper.toastColor(WalletActivity.this, "You can't cancel this transaction", R.color.color_danger_transparent);
    }
}
