package com.sketchproject.infogue.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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
import com.sketchproject.infogue.activities.FollowerActivity;
import com.sketchproject.infogue.adapters.FollowerRecyclerViewAdapter;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFollowerInteractionListener}
 * interface.
 */
public class FollowerFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_RELATED_ID = "contributor-id";
    private static final String ARG_RELATED_USERNAME = "contributor-username";
    private static final String ARG_TYPE = "screen-type";
    private static final String ARG_QUERY = "search-query";

    private int mColumnCount = 1;
    private String mType;
    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Contributor> allFollowers;
    private FollowerRecyclerViewAdapter followerAdapter;
    private OnFollowerInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String apiFollowerUrl = "";
    private String apiFollowerUrlFirstPage = "";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FollowerFragment() {
    }

    @SuppressWarnings("unused")
    public static FollowerFragment newInstance(int columnCount) {
        FollowerFragment fragment = new FollowerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public static FollowerFragment newInstance(int columnCount, int id, String username, String type, String query) {
        FollowerFragment fragment = new FollowerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_RELATED_ID, id);
        args.putString(ARG_RELATED_USERNAME, username);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(getContext());
        int mLoggedId = session.getSessionData(SessionManager.KEY_ID, 0);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mType = getArguments().getString(ARG_TYPE);

            // if needed remove the comment: int mId = getArguments().getInt(ARG_RELATED_ID);
            String mUsername = getArguments().getString(ARG_RELATED_USERNAME);
            String mQuery = getArguments().getString(ARG_QUERY);

            if (mQuery != null && !mQuery.isEmpty()) {
                Log.i("INFOGUE/Follower", "Query : " + mQuery);
                apiFollowerUrl = APIBuilder.getApiSearchUrl(mQuery, APIBuilder.SEARCH_CONTRIBUTOR, mLoggedId);
            } else if (mUsername != null && !mUsername.isEmpty()) {
                Log.i("INFOGUE/Follower", "Username : " + mUsername);
                apiFollowerUrl = APIBuilder.getApiFollowerUrl(mType, mLoggedId, mUsername);
            } else {
                Log.i("INFOGUE/Follower", "Default");
            }

            apiFollowerUrlFirstPage = apiFollowerUrl;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follower_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager;
            if (mColumnCount <= 1) {
                linearLayoutManager = new LinearLayoutManager(context);
            } else {
                linearLayoutManager = new GridLayoutManager(context, mColumnCount);
            }

            allFollowers = new ArrayList<>();
            followerAdapter = new FollowerRecyclerViewAdapter(allFollowers, mListener, mType);
            recyclerView.setAdapter(followerAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    if (!isFirstCall) {
                        loadFollowers(page);
                    }
                }

                @Override
                public void onFirstSight(boolean isFirst) {
                    ((FollowerActivity) getActivity()).setSwipeEnable(isFirst);
                }
            });

            if (isFirstCall) {
                isFirstCall = false;
                loadFollowers(0);
            }
        }
        return view;
    }

    /**
     * @param page starts at 0
     */
    private void loadFollowers(final int page) {
        if (!isEndOfPage && apiFollowerUrl != null) {
            if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                allFollowers.add(null);
                followerAdapter.notifyItemInserted(allFollowers.size() - 1);
            }

            Log.i("INFOGUE/" + mType, "URL " + apiFollowerUrl);
            JsonObjectRequest contributorRequest = new JsonObjectRequest(Request.Method.GET, apiFollowerUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");

                                JSONObject contributors = response.getJSONObject(mType.toLowerCase());
                                String nextUrl = contributors.getString("next_page_url");
                                int currentPage = contributors.getInt("current_page");
                                int lastPage = contributors.getInt("last_page");
                                JSONArray data = contributors.optJSONArray("data");

                                apiFollowerUrl = nextUrl;

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                                        allFollowers.remove(allFollowers.size() - 1);
                                        followerAdapter.notifyItemRemoved(allFollowers.size());
                                    } else {
                                        swipeRefreshLayout.setRefreshing(false);
                                        int total = allFollowers.size();
                                        for (int i = 0; i < total; i++) {
                                            allFollowers.remove(0);
                                        }
                                        followerAdapter.notifyItemRangeRemoved(0, total);
                                    }

                                    List<Contributor> moreFollowers = new ArrayList<>();
                                    if (data != null) {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject contributorData = data.getJSONObject(i);

                                            Contributor contributor = new Contributor();
                                            contributor.setId(contributorData.getInt(Contributor.ID));
                                            contributor.setToken(contributorData.getString(Contributor.TOKEN));
                                            contributor.setUsername(contributorData.getString(Contributor.USERNAME));
                                            contributor.setName(contributorData.getString(Contributor.NAME));
                                            contributor.setEmail(contributorData.getString(Contributor.EMAIL));
                                            contributor.setLocation(contributorData.getString(Contributor.LOCATION));
                                            contributor.setAbout(contributorData.getString(Contributor.ABOUT));
                                            contributor.setAvatar(contributorData.getString(Contributor.AVATAR_REF));
                                            contributor.setCover(contributorData.getString(Contributor.COVER_REF));
                                            contributor.setStatus(contributorData.getString(Contributor.STATUS));
                                            contributor.setArticle(contributorData.getInt(Contributor.ARTICLE));
                                            contributor.setFollowers(contributorData.getInt(Contributor.FOLLOWERS));
                                            contributor.setFollowing(contributorData.getInt(Contributor.FOLLOWING));
                                            contributor.setIsFollowing(contributorData.getInt(Contributor.IS_FOLLOWING) == 1);
                                            moreFollowers.add(contributor);
                                        }
                                    }

                                    int curSize = followerAdapter.getItemCount();
                                    allFollowers.addAll(moreFollowers);

                                    if (allFollowers.size() <= 0) {
                                        isEndOfPage = true;
                                        Log.i("INFOGUE/Contributor", "EMPTY on page " + page);
                                        Contributor emptyContributor = new Contributor(0, null);
                                        allFollowers.add(emptyContributor);
                                    } else if (currentPage >= lastPage) {
                                        isEndOfPage = true;
                                        Log.i("INFOGUE/Contributor", "END on page " + page);
                                        Contributor endContributor = new Contributor(-1, null);
                                        allFollowers.add(endContributor);
                                    }

                                    followerAdapter.notifyItemRangeInserted(curSize, allFollowers.size() - 1);
                                    Log.i("INFOGUE/Contributor", "Load More page " + page);
                                } else {
                                    Helper.toastColor(getContext(), R.string.error_unknown, R.color.color_warning_transparent);

                                    // add error view holder
                                    isEndOfPage = true;
                                    Contributor failureContributor = new Contributor(-1, null);
                                    allFollowers.add(failureContributor);
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

                            // remove last loading
                            allFollowers.remove(allFollowers.size() - 1);
                            followerAdapter.notifyItemRemoved(allFollowers.size());

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
                            Contributor failureContributor = new Contributor(-1, null);
                            allFollowers.add(failureContributor);
                        }
                    }
            );
            contributorRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getContext()).addToRequestQueue(contributorRequest);
        }
    }

    public void refreshArticleList(SwipeRefreshLayout swipeRefresh) {
        swipeRefreshLayout = swipeRefresh;
        isEndOfPage = false;
        apiFollowerUrl = apiFollowerUrlFirstPage;

        loadFollowers(0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFollowerInteractionListener) {
            mListener = (OnFollowerInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFollowerInteractionListener");
        }
    }

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
    public interface OnFollowerInteractionListener {
        void onFollowerInteraction(Contributor contributor, View followControl);

        void onFollowerControlInteraction(View view, View followControl, Contributor contributor);

        void onFollowerLongClickInteraction(View view, View followControl, Contributor contributor);
    }
}
