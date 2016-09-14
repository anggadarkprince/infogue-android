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
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.MessageActivity;
import com.sketchproject.infogue.adapters.MessageRecyclerViewAdapter;
import com.sketchproject.infogue.models.Message;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.TimeAgo;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageFragment extends Fragment {
    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Message> allMessages;
    private MessageRecyclerViewAdapter messageAdapter;
    private OnMessageInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String apiMessageUrl = "";
    private String apiMessageUrlFirstPage = "";
    private String apiMessageParams = "";

    public MessageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(getContext());

        apiMessageParams = "api_token=" + session.getSessionData(SessionManager.KEY_TOKEN, null) + "&contributor_id=" + session.getSessionData(SessionManager.KEY_ID, 0);
        apiMessageUrl = APIBuilder.URL_API_MESSAGE + "?" + apiMessageParams;
        apiMessageUrlFirstPage = apiMessageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

            allMessages = new ArrayList<>();
            messageAdapter = new MessageRecyclerViewAdapter(allMessages, mListener);
            recyclerView.setAdapter(messageAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    if (!isFirstCall) {
                        loadMessages(page);
                    }
                }

                @Override
                public void onReachTop(boolean isFirst) {
                    ((MessageActivity) getActivity()).setSwipeEnable(isFirst);
                }
            });

            if (isFirstCall) {
                loadMessages(0);
            }
        }
        return view;
    }

    private void loadMessages(final int page) {
        if (!isEndOfPage && apiMessageUrl != null) {
            if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                allMessages.add(null);
                messageAdapter.notifyItemInserted(allMessages.size() - 1);
            }

            Log.i("Infogue/Message", "URL " + apiMessageUrl);
            JsonObjectRequest messageRequest = new JsonObjectRequest(Request.Method.GET, apiMessageUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                JSONObject messages = response.getJSONObject("messages");

                                String nextUrl = messages.getString("next_page_url");
                                int currentPage = messages.getInt("current_page");
                                int lastPage = messages.getInt("last_page");
                                JSONArray data = messages.optJSONArray("data");

                                apiMessageUrl = nextUrl + "&" + apiMessageParams;

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                                        // remove last loading in bottom of data
                                        allMessages.remove(allMessages.size() - 1);
                                        messageAdapter.notifyItemRemoved(allMessages.size());
                                    } else {
                                        // refreshing data then remove all first
                                        swipeRefreshLayout.setRefreshing(false);
                                        int total = allMessages.size();
                                        for (int i = 0; i < total; i++) {
                                            allMessages.remove(0);
                                        }
                                        messageAdapter.notifyItemRangeRemoved(0, total);
                                    }

                                    List<Message> moreMessages = new ArrayList<>();
                                    if (data != null) {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject messageData = data.getJSONObject(i);

                                            Message message = new Message();
                                            message.setId(messageData.getInt(Message.ID));
                                            message.setContributorId(messageData.getInt(Message.CONTRIBUTOR_ID));
                                            message.setUsername(messageData.getString(Message.USERNAME));
                                            message.setName(messageData.getString(Message.NAME));
                                            message.setMessage(messageData.getString(Message.MESSAGE));
                                            message.setAvatar(messageData.getString(Message.AVATAR));
                                            message.setTimestamp(new TimeAgo(getContext()).timeAgo(messageData.getString(Message.TIMESTAMP)));
                                            moreMessages.add(message);
                                        }
                                    }

                                    int curSize = messageAdapter.getItemCount();
                                    allMessages.addAll(moreMessages);

                                    if (allMessages.size() <= 0) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Message", "EMPTY on page " + page);
                                        Message emptyMessage = new Message();
                                        emptyMessage.setId(0);
                                        allMessages.add(emptyMessage);
                                    } else if (currentPage >= lastPage) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Message", "END on page " + page);
                                        Message endMessage = new Message();
                                        endMessage.setId(-1);
                                        allMessages.add(endMessage);
                                    }

                                    messageAdapter.notifyItemRangeInserted(curSize, allMessages.size() - 1);
                                } else {
                                    Log.i("Infogue/Message", "Error on page " + page);
                                    Helper.toastColor(getContext(), R.string.error_unknown, R.color.color_warning_transparent);

                                    isEndOfPage = true;
                                    Message failureMessage = new Message();
                                    failureMessage.setId(-2);
                                    failureMessage.setMessage(getString(R.string.error_unknown));
                                    allMessages.add(failureMessage);
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

                            // remove last loading
                            allMessages.remove(allMessages.size() - 1);
                            messageAdapter.notifyItemRemoved(allMessages.size());

                            NetworkResponse networkResponse = error.networkResponse;
                            String errorMessage = getActivity().getString(R.string.error_server);
                            if (networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = getActivity().getString(R.string.error_timeout);
                                } else if (error.getClass().equals(NoConnectionError.class)) {
                                    errorMessage = getString(R.string.error_no_connection);
                                }
                            } else {
                                if (networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                }
                            }
                            Helper.toastColor(getContext(), errorMessage, R.color.color_danger_transparent);

                            // add error view holder
                            isEndOfPage = true;
                            Message errorMsg = new Message();
                            errorMsg.setId(-2);
                            errorMsg.setMessage(errorMessage);
                            allMessages.add(errorMsg);

                            isFirstCall = false;
                        }
                    }
            );

            messageRequest.setTag("message");
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
        VolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("message");
    }

    /**
     * Remove view holder by ID.
     *
     * @param id article id
     */
    public void deleteMessageRow(int id) {
        Log.i("INFOGUE/Message", "Delete id : " + id);
        for (int i = 0; i < allMessages.size(); i++) {
            if (allMessages.get(i) != null && allMessages.get(i).getId() == id) {
                allMessages.remove(i);
                messageAdapter.notifyItemRemoved(i);
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
        apiMessageUrl = apiMessageUrlFirstPage;

        loadMessages(0);
    }

    /**
     * Attach event listener.
     *
     * @param context parent context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMessageInteractionListener) {
            mListener = (OnMessageInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnMessageInteractionListener");
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
    public interface OnMessageInteractionListener {
        void onMessageListClicked(Message message);

        void onDeleteMessage(Message message);
    }
}
