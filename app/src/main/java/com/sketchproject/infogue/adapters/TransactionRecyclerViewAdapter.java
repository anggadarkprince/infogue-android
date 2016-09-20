package com.sketchproject.infogue.adapters;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.TransactionFragment.OnTransactionInteractionListener;
import com.sketchproject.infogue.fragments.holders.InfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Transaction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Sketch Project Studio
 * Created by angga on 16/09/16.
 */
public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_TRANSACTION = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    private List<Transaction> mTransactions;
    private OnTransactionInteractionListener mTransactionListener;

    public TransactionRecyclerViewAdapter(List<Transaction> items, OnTransactionInteractionListener listListener) {
        mTransactions = items;
        mTransactionListener = listListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTransactions.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            Transaction transaction = mTransactions.get(position);
            if (transaction.getId() > 0) {
                return VIEW_TYPE_TRANSACTION;
            } else {
                return VIEW_TYPE_EMPTY;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == VIEW_TYPE_LOADING) {
            view = inflater.inflate(R.layout.fragment_loading, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_TRANSACTION) {
            view = inflater.inflate(R.layout.fragment_transaction_row, parent, false);
            return new TransactionViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.fragment_list_info, parent, false);
            return new InfoViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Transaction transaction = mTransactions.get(holder.getAdapterPosition());

        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_TRANSACTION:
                final TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
                NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
                transactionHolder.mItem = transaction;
                transactionHolder.mType.setText(transaction.getType().toUpperCase());
                transactionHolder.mAmount.setText("IDR " + formatter.format(transaction.getAmount().longValue()));
                transactionHolder.mStatus.setText(transaction.getStatus().toUpperCase());
                transactionHolder.mDescription.setText(transaction.getDescription());
                transactionHolder.mDate.setText(transaction.getDate());
                transactionHolder.mItem = transaction;

                switch (transaction.getStatus().toLowerCase()) {
                    case Transaction.STATUS_PENDING:
                        transactionHolder.mStatus.setBackgroundResource(R.color.color_warning);
                        break;
                    case Transaction.STATUS_PROCEED:
                        transactionHolder.mStatus.setBackgroundResource(R.color.color_info);
                        break;
                    case Transaction.STATUS_SUCCESS:
                        transactionHolder.mStatus.setBackgroundResource(R.color.color_success);
                        break;
                    case Transaction.STATUS_CANCEL:
                        transactionHolder.mStatus.setBackgroundResource(R.color.color_danger);
                        break;
                }

                if (transaction.getStatus().toLowerCase().equals(Transaction.STATUS_PENDING)) {
                    transactionHolder.mDelete.setVisibility(View.VISIBLE);
                    transactionHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mTransactionListener.onDeleteTransaction(transaction);
                        }
                    });
                } else {
                    transactionHolder.mDelete.setVisibility(View.GONE);
                }

                transactionHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTransactionListener.onTransactionClicked(transaction);
                    }
                });

                break;
            case VIEW_TYPE_LOADING:
                final LoadingViewHolder progressbarHolder = (LoadingViewHolder) holder;
                progressbarHolder.mProgressBar.setIndeterminate(true);
                break;
            case VIEW_TYPE_EMPTY:
                final InfoViewHolder emptyHolder = (InfoViewHolder) holder;
                emptyHolder.mMessageView.setText(R.string.label_no_transaction);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mType;
        public final TextView mStatus;
        public final TextView mDescription;
        public final TextView mAmount;
        public final TextView mDate;
        public final Button mDelete;
        public Transaction mItem;

        public TransactionViewHolder(View view) {
            super(view);
            mView = view;
            mType = (TextView) view.findViewById(R.id.type);
            mStatus = (TextView) view.findViewById(R.id.status);
            mDescription = (TextView) view.findViewById(R.id.description);
            mAmount = (TextView) view.findViewById(R.id.amount);
            mDate = (TextView) view.findViewById(R.id.date);
            mDelete = (Button) view.findViewById(R.id.btn_delete);
        }

        @Override
        public String toString() {
            return mItem.getDescription();
        }
    }
}
