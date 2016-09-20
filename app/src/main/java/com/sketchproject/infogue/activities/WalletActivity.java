package com.sketchproject.infogue.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.TransactionFragment;
import com.sketchproject.infogue.models.Transaction;
import com.sketchproject.infogue.utils.Helper;

public class WalletActivity extends AppCompatActivity implements TransactionFragment.OnTransactionInteractionListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private TransactionFragment transactionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

    @Override
    public void onTransactionClicked(Transaction transaction) {
        Log.i("Infogue/Transaction", transaction.getDescription());
    }

    @Override
    public void onDeleteTransaction(Transaction transaction) {
        transactionFragment.deleteTransactionRow(transaction.getId());

        String successMessage = "You have cancelled transaction ID#" + transaction.getId();
        Helper.toastColor(WalletActivity.this, successMessage, R.color.color_warning_transparent);
    }
}
