package com.sketchproject.infogue.fragments;

import android.content.Context;
import android.graphics.Color;
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
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
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
    private OnListFragmentInteractionListener mListener;
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
                apiFollowerUrl = UrlHelper.getApiSearchUrl(mQuery, UrlHelper.SEARCH_CONTRIBUTOR);
            } else if (mUsername != null && !mUsername.isEmpty()) {
                Log.i("INFOGUE/Follower", "Username : " + mUsername);
                apiFollowerUrl = UrlHelper.getApiFollowerUrl(mType, mLoggedId, mUsername);
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

                                if (status.equals(Constant.REQUEST_SUCCESS)) {
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
                                            contributor.setId(contributorData.getInt(Contributor.CONTRIBUTOR_ID));
                                            contributor.setToken(contributorData.getString(Contributor.CONTRIBUTOR_TOKEN));
                                            contributor.setUsername(contributorData.getString(Contributor.CONTRIBUTOR_USERNAME));
                                            contributor.setName(contributorData.getString(Contributor.CONTRIBUTOR_NAME));
                                            contributor.setEmail(contributorData.getString(Contributor.CONTRIBUTOR_EMAIL));
                                            contributor.setLocation(contributorData.getString(Contributor.CONTRIBUTOR_LOCATION));
                                            contributor.setAbout(contributorData.getString(Contributor.CONTRIBUTOR_ABOUT));
                                            contributor.setAvatar(contributorData.getString(Contributor.CONTRIBUTOR_AVATAR_REF));
                                            contributor.setCover(contributorData.getString(Contributor.CONTRIBUTOR_COVER_REF));
                                            contributor.setStatus(contributorData.getString(Contributor.CONTRIBUTOR_STATUS));
                                            contributor.setArticle(contributorData.getInt(Contributor.CONTRIBUTOR_ARTICLE));
                                            contributor.setFollowers(contributorData.getInt(Contributor.CONTRIBUTOR_FOLLOWERS));
                                            contributor.setFollowing(contributorData.getInt(Contributor.CONTRIBUTOR_FOLLOWING));
                                            contributor.setIsFollowing(contributorData.getInt(Contributor.CONTRIBUTOR_IS_FOLLOWING) == 1);
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
                                    AppHelper.toastColored(getContext(), getActivity().getString(R.string.error_server), Color.parseColor("#ddd1205e"));

                                    // indicate the error
                                    isEndOfPage = true;
                                    Log.i("INFOGUE/Contributor", "Failure on page " + page);
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
                            // remove last loading
                            allFollowers.remove(allFollowers.size() - 1);
                            followerAdapter.notifyItemRemoved(allFollowers.size());

                            String errorMessage = getActivity().getString(R.string.error_server);
                            if (error.networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = getActivity().getString(R.string.error_timeout);
                                } else {
                                    errorMessage = getActivity().getString(R.string.error_unknown);
                                }
                            }
                            AppHelper.toastColored(getContext(), errorMessage, Color.parseColor("#ddd1205e"));

                            // indicate the error or timeout
                            isEndOfPage = true;
                            Log.i("INFOGUE/Contributor", "Failure or timeout on page " + page);
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
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
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
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Contributor contributor, View followControl);

        void onListFollowControlInteraction(View view, View followControl, Contributor contributor);

        void onListLongClickInteraction(View view, View followControl, Contributor contributor);
    }
}
