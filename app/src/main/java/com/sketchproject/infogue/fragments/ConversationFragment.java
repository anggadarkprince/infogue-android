package com.sketchproject.infogue.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import com.sketchproject.infogue.adapters.ConversationRecyclerViewAdapter;
import com.sketchproject.infogue.models.Conversation;
import com.sketchproject.infogue.models.Message;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConversationFragment extends Fragment {
    private static final String ARG_USERNAME = "username";

    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Conversation> allMessages;
    private ConversationRecyclerViewAdapter messageAdapter;
    private RecyclerView recyclerView;

    private String apiMessageUrl = "";
    private String apiMessageParams = "";

    private int latestId = 0;
    private int curPage = 0;
    private boolean isLoading = false;

    public ConversationFragment() {
    }

    /**
     * Default newInstance to show comment list by article id and slug.
     *
     * @param username id of user interact with
     * @return fragment object of ConversationFragment
     */
    public static ConversationFragment newInstance(String username) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            String username = getArguments().getString(ARG_USERNAME);
            SessionManager session = new SessionManager(getContext());

            apiMessageParams = "api_token=" + session.getSessionData(SessionManager.KEY_TOKEN, null) + "&contributor_id=" + session.getSessionData(SessionManager.KEY_ID, 0);
            apiMessageUrl = APIBuilder.getApiConversationUrl(username) + "?" + apiMessageParams;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            //linearLayoutManager.setStackFromEnd(true);
            linearLayoutManager.setReverseLayout(true);

            allMessages = new ArrayList<>();
            messageAdapter = new ConversationRecyclerViewAdapter(allMessages);
            recyclerView.setAdapter(messageAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    if (!isFirstCall && !isLoading && !recyclerView.isLayoutFrozen()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadConversations(curPage);
                            }
                        }, 200);
                    }
                }

                @Override
                public void onReachTop(boolean isFirst) {
                    //Log.i("Infogue/Conversation", "Reach top");
                }
            });

            if (isFirstCall) {
                loadConversations(0);
            }
        }
        return view;
    }

    /**
     * Load conversation from server page by page.
     *
     * @param page of current request
     */
    private void loadConversations(final int page) {
        if (!isEndOfPage) {
            // add loading first
            isLoading = true;
            allMessages.add(null);
            messageAdapter.notifyItemInserted(allMessages.size() - 1);

            Log.i("Infogue/Conversation", "URL " + apiMessageUrl);
            JsonObjectRequest messageRequest = new JsonObjectRequest(Request.Method.GET, apiMessageUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {

                                String status = response.getString("status");
                                JSONObject messages = response.getJSONObject("conversations");

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    // populate pagination data
                                    String nextUrl = messages.getString("next_page_url");
                                    int currentPage = messages.getInt("current_page");
                                    int lastPage = messages.getInt("last_page");
                                    JSONArray data = messages.optJSONArray("data");

                                    // construct next url to fetch
                                    apiMessageUrl = nextUrl + "&" + apiMessageParams;

                                    // remove loading after last of data
                                    allMessages.remove(allMessages.size() - 1);
                                    messageAdapter.notifyItemRemoved(allMessages.size());

                                    TimeAgo timeAgo = new TimeAgo(getContext());

                                    // add new data
                                    List<Conversation> moreMessages = new ArrayList<>();
                                    if (data != null) {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject messageData = data.getJSONObject(i);

                                            Conversation message = new Conversation();
                                            message.setId(messageData.getInt(Message.ID));
                                            message.setOwner(messageData.getString(Conversation.OWNER));
                                            message.setMessage(messageData.getString(Message.MESSAGE));
                                            message.setAvatar(messageData.getString(Message.AVATAR));
                                            message.setTimestamp(timeAgo.timeAgo(messageData.getString(Message.TIMESTAMP)));
                                            moreMessages.add(message);

                                            if (message.getId() > latestId) {
                                                latestId = message.getId();
                                            }
                                        }
                                    }
                                    int curSize = messageAdapter.getItemCount();
                                    allMessages.addAll(moreMessages);

                                    // exception if data empty or reach the last page
                                    if (allMessages.size() <= 0) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Conversation", "EMPTY on page " + page);
                                        Conversation emptyMessage = new Conversation();
                                        emptyMessage.setId(0);
                                        allMessages.add(emptyMessage);
                                    } else if (currentPage >= lastPage) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Conversation", "END on page " + page);
                                        Conversation endMessage = new Conversation();
                                        endMessage.setId(-1);
                                        allMessages.add(endMessage);
                                    }

                                    // notify the observers to update the list by index range
                                    messageAdapter.notifyItemRangeInserted(curSize, allMessages.size() - 1);
                                } else {
                                    Log.i("Infogue/Message", "Error on page " + page);
                                    Helper.toastColor(getContext(), R.string.error_unknown, R.color.color_warning_transparent);

                                    isEndOfPage = true;
                                    Conversation failureMessage = new Conversation();
                                    failureMessage.setId(-2);
                                    failureMessage.setMessage(getString(R.string.error_unknown));
                                    allMessages.add(failureMessage);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            isFirstCall = false;
                            isLoading = false;
                            curPage++;
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();

                            String errorMessage = new Logger().networkRequestError(getContext(), error, "Conversation");
                            if (errorMessage.equals(getString(R.string.error_not_found))) {
                                errorMessage = "Your first message, be friendly";
                            }
                            // remove last loading as well
                            allMessages.remove(allMessages.size() - 1);
                            messageAdapter.notifyItemRemoved(allMessages.size());

                            // add error view holder
                            isEndOfPage = true;
                            Conversation errorMsg = new Conversation();
                            errorMsg.setId(-2);
                            errorMsg.setMessage(errorMessage);
                            allMessages.add(errorMsg);

                            isFirstCall = false;
                            isLoading = false;
                        }
                    }
            );

            messageRequest.setTag("conversation");
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
        VolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("conversation");
    }

    /**
     * Add message immediately without waiting request is success or not
     *
     * @param sessionManager session to of current user
     * @param message        sent conversation message
     */
    public void insertNewMessage(SessionManager sessionManager, String message) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        removeLoading();

        Conversation conversation = new Conversation();
        conversation.setId(latestId + 1);
        conversation.setOwner("me");
        conversation.setMessage(message);
        conversation.setAvatar(sessionManager.getSessionData(SessionManager.KEY_AVATAR, null));
        conversation.setTimestamp(new TimeAgo(getContext()).timeAgo(timeStamp));
        allMessages.add(0, conversation);
        messageAdapter.notifyItemInserted(0);
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
    }

    public void insertLoading() {
        Conversation conversation = new Conversation();
        conversation.setId(-3);
        conversation.setMessage("Message is sending...");
        allMessages.add(0, conversation);
        messageAdapter.notifyItemInserted(0);
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
    }

    public void removeLoading() {
        if (allMessages.get(0).getId() == -3) {
            allMessages.remove(0);
            messageAdapter.notifyItemRemoved(1);
        }
    }
}
