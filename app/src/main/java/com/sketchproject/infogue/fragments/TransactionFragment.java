package com.sketchproject.infogue.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.WalletActivity;
import com.sketchproject.infogue.adapters.TransactionRecyclerViewAdapter;
import com.sketchproject.infogue.models.Transaction;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.TimeAgo;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;
import com.sketchproject.infogue.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sketch Project Studio
 * Created by angga on 20/09/16.
 */
public class TransactionFragment extends Fragment {
    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Transaction> allTransactions;
    private TransactionRecyclerViewAdapter transactionAdapter;
    private OnTransactionInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String apiTransactionUrl = "";
    private String apiTransactionUrlFirstPage = "";
    private String apiTransactionParams = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(getContext());

        apiTransactionParams = "api_token=" + session.getSessionData(SessionManager.KEY_TOKEN, null) + "&contributor_id=" + session.getSessionData(SessionManager.KEY_ID, 0);
        apiTransactionUrl = APIBuilder.URL_API_WALLET + "?" + apiTransactionParams;
        apiTransactionUrlFirstPage = apiTransactionUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

            allTransactions = new ArrayList<>();
            transactionAdapter = new TransactionRecyclerViewAdapter(allTransactions, mListener);
            recyclerView.setAdapter(transactionAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    if (!isFirstCall) {
                        loadTransactions(page);
                    }
                }

                @Override
                public void onReachTop(boolean isFirst) {
                    ((WalletActivity) getActivity()).setSwipeEnable(isFirst);
                }
            });

            if (isFirstCall) {
                loadTransactions(0);
            }
        }
        return view;
    }

    private void loadTransactions(final int page) {
        if (!isEndOfPage && apiTransactionUrl != null) {
            if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                allTransactions.add(null);
                transactionAdapter.notifyItemInserted(allTransactions.size() - 1);
            }

            Log.i("Infogue/Transaction", "URL " + apiTransactionUrl);
            JsonObjectRequest messageRequest = new JsonObjectRequest(Request.Method.GET, apiTransactionUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                JSONObject transactions = response.getJSONObject("transactions");
                                double balance = response.getDouble("balance");
                                double deferred = response.getDouble("deferred");

                                if(getContext() instanceof WalletActivity){
                                    ((WalletActivity) getContext()).updateBalanceAndDeferred(balance, deferred);
                                }

                                String nextUrl = transactions.getString("next_page_url");
                                int currentPage = transactions.getInt("current_page");
                                int lastPage = transactions.getInt("last_page");
                                JSONArray data = transactions.optJSONArray("data");

                                apiTransactionUrl = nextUrl + "&" + apiTransactionParams;

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                                        // remove last loading in bottom of data
                                        allTransactions.remove(allTransactions.size() - 1);
                                        transactionAdapter.notifyItemRemoved(allTransactions.size());
                                    } else {
                                        // refreshing data then remove all first
                                        swipeRefreshLayout.setRefreshing(false);
                                        int total = allTransactions.size();
                                        for (int i = 0; i < total; i++) {
                                            allTransactions.remove(0);
                                        }
                                        transactionAdapter.notifyItemRangeRemoved(0, total);
                                    }

                                    List<Transaction> moreTransaction = new ArrayList<>();
                                    if (data != null) {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject transactionData = data.getJSONObject(i);

                                            Transaction transaction = new Transaction();
                                            transaction.setId(transactionData.getInt(Transaction.ID));
                                            transaction.setType(transactionData.getString(Transaction.TYPE));
                                            transaction.setDescription(transactionData.getString(Transaction.DESCRIPTION));
                                            transaction.setAmount(new BigDecimal(transactionData.getDouble(Transaction.AMOUNT)));
                                            transaction.setStatus(transactionData.getString(Transaction.STATUS));
                                            transaction.setDate(new TimeAgo(getContext()).timeAgo(transactionData.getString(Transaction.DATE)));
                                            moreTransaction.add(transaction);
                                        }
                                    }

                                    int curSize = transactionAdapter.getItemCount();
                                    allTransactions.addAll(moreTransaction);

                                    if (allTransactions.size() <= 0) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Transaction", "EMPTY on page " + page);
                                        Transaction emptyTransaction = new Transaction();
                                        emptyTransaction.setId(0);
                                        allTransactions.add(emptyTransaction);
                                    } else if (currentPage >= lastPage) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Transaction", "END on page " + page);
                                    }
                                    transactionAdapter.notifyItemRangeInserted(curSize, allTransactions.size() - 1);
                                } else {
                                    Log.i("Infogue/Transaction", "Error on page " + page);
                                    Helper.toastColor(getContext(), R.string.error_unknown, R.color.color_warning_transparent);
                                    isEndOfPage = true;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            isFirstCall = false;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();

                            String errorMessage = new Logger().networkRequestError(getContext(), error, "Transaction");
                            Helper.toastColor(getContext(), errorMessage, R.color.color_danger_transparent);

                            isEndOfPage = true;
                            isFirstCall = false;
                        }
                    }
            );

            messageRequest.setTag("transaction");
            messageRequest.setRetryPolicy(new DefaultRetryPolicy(
                    APIBuilder.TIMEOUT_SHORT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getContext()).addToRequestQueue(messageRequest);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        VolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("transaction");
    }

    /**
     * Remove view holder by ID.
     *
     * @param id article id
     */
    public void deleteTransactionRow(int id) {
        Log.i("INFOGUE/Transaction", "Delete id : " + id);
        for (int i = 0; i < allTransactions.size(); i++) {
            if (allTransactions.get(i) != null && allTransactions.get(i).getId() == id) {
                allTransactions.remove(i);
                transactionAdapter.notifyItemRemoved(i);
            }
        }
    }

    /**
     * Reload message list.
     *
     * @param swipeRefresh swipe view
     */
    public void refreshMessageList(SwipeRefreshLayout swipeRefresh) {
        swipeRefreshLayout = swipeRefresh;
        isEndOfPage = false;
        apiTransactionUrl = apiTransactionUrlFirstPage;

        loadTransactions(0);
    }

    /**
     * Attach event listener.
     *
     * @param context parent context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTransactionInteractionListener) {
            mListener = (OnTransactionInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTransactionInteractionListener");
        }
    }

    /**
     * Clear listener when detached.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnTransactionInteractionListener {
        void onTransactionClicked(Transaction transaction);

        void onDeleteTransaction(Transaction transaction);
    }
}
