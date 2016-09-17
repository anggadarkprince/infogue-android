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
import com.sketchproject.infogue.activities.CommentActivity;
import com.sketchproject.infogue.adapters.CommentRecyclerViewAdapter;
import com.sketchproject.infogue.models.Comment;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;
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
 * A fragment representing a list of comment Items.
 * Activities containing this fragment MUST implement the {@link OnCommentInteractionListener}
 * interface.
 * <p/>
 * Sketch Project Studio
 * Created by Angga on 26/04/2016 19.09.
 */
public class CommentFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_ARTICLE_ID = "article-id";
    private static final String ARG_ARTICLE_SLUG = "article-slug";

    private int mColumnCount = 1;
    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Comment> allComments;
    private CommentRecyclerViewAdapter commentAdapter;
    private OnCommentInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String apiCommentUrl = "";
    private String apiCommentUrlFirstPage = "";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CommentFragment() {
    }

    /**
     * Default newInstance to show comment list by article id and slug.
     *
     * @param columnCount column layout
     * @param articleId   id of article
     * @param articleSlug slug article
     * @return fragment object of CommentFragment
     */
    public static CommentFragment newInstance(int columnCount, int articleId, String articleSlug) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_ARTICLE_ID, articleId);
        args.putString(ARG_ARTICLE_SLUG, articleSlug);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Perform initialization of CommentFragment.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            String mArticleSlug = getArguments().getString(ARG_ARTICLE_SLUG);

            apiCommentUrl = APIBuilder.getApiCommentUrl(mArticleSlug);
            apiCommentUrlFirstPage = apiCommentUrl;
        }
    }

    /**
     * Called after onCreate() and before onCreated()
     *
     * @param inflater           The LayoutInflater object that can be used to inflate view
     * @param container          If non-null, this is the parent view that the fragment's attached
     * @param savedInstanceState If non-null, this fragment is being re-constructed from previous
     * @return return the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager;
            if (mColumnCount <= 1) {
                linearLayoutManager = new LinearLayoutManager(context);
            } else {
                linearLayoutManager = new GridLayoutManager(context, mColumnCount);
            }

            allComments = new ArrayList<>();
            commentAdapter = new CommentRecyclerViewAdapter(allComments, mListener);
            recyclerView.setAdapter(commentAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    if (!isFirstCall) {
                        loadComments(page);
                    }
                }

                @Override
                public void onReachTop(boolean isFirst) {
                    ((CommentActivity) getActivity()).setSwipeEnable(isFirst);
                }
            });

            if (isFirstCall) {
                loadComments(0);
            }
        }
        return view;
    }

    /**
     * Actually we do not use page variable from scroll listener, because web
     * service take care the pagination.
     *
     * @param page starts at 0
     */
    private void loadComments(final int page) {
        if (!isEndOfPage && apiCommentUrl != null) {
            if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                allComments.add(null);
                commentAdapter.notifyItemInserted(allComments.size() - 1);
            }

            Log.i("Infogue/Comment", "URL " + apiCommentUrl);
            JsonObjectRequest commentRequest = new JsonObjectRequest(Request.Method.GET, apiCommentUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                JSONObject comments = response.getJSONObject("comments");

                                String nextUrl = comments.getString("next_page_url");
                                int currentPage = comments.getInt("current_page");
                                int lastPage = comments.getInt("last_page");
                                JSONArray data = comments.optJSONArray("data");

                                apiCommentUrl = nextUrl;

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                                        allComments.remove(allComments.size() - 1);
                                        commentAdapter.notifyItemRemoved(allComments.size());
                                    } else {
                                        swipeRefreshLayout.setRefreshing(false);
                                        int total = allComments.size();
                                        for (int i = 0; i < total; i++) {
                                            allComments.remove(0);
                                        }
                                        commentAdapter.notifyItemRangeRemoved(0, total);
                                    }

                                    List<Comment> moreComments = new ArrayList<>();
                                    TimeAgo timeAgo = new TimeAgo(getContext());
                                    if (data != null) {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject commentData = data.getJSONObject(i);

                                            Comment comment = new Comment();
                                            comment.setId(commentData.getInt(Comment.ID));
                                            comment.setContributorId(commentData.getInt(Comment.CONTRIBUTOR_ID));
                                            comment.setArticleId(commentData.getInt(Comment.ARTICLE_ID));
                                            comment.setUsername(commentData.getString(Comment.USERNAME));
                                            comment.setName(commentData.getString(Comment.NAME));
                                            comment.setComment(commentData.getString(Comment.CONTENT));
                                            comment.setAvatar(commentData.getString(Comment.AVATAR));
                                            comment.setTimestamp(timeAgo.timeAgo(commentData.getString(Comment.TIMESTAMP)));
                                            moreComments.add(comment);
                                        }
                                    }

                                    int curSize = commentAdapter.getItemCount();
                                    allComments.addAll(moreComments);

                                    if (allComments.size() <= 0) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Comment", "EMPTY on page " + page);
                                        Comment emptyComment = new Comment();
                                        emptyComment.setId(0);
                                        allComments.add(emptyComment);
                                    } else if (currentPage >= lastPage) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Comment", "END on page " + page);
                                        Comment endComment = new Comment();
                                        endComment.setId(-1);
                                        allComments.add(endComment);
                                    }

                                    commentAdapter.notifyItemRangeInserted(curSize, allComments.size() - 1);
                                } else {
                                    Log.i("Infogue/Comment", "Error on page " + page);
                                    Helper.toastColor(getContext(), R.string.error_unknown, R.color.color_warning_transparent);

                                    isEndOfPage = true;
                                    Comment failureComment = new Comment();
                                    failureComment.setId(-2);
                                    failureComment.setComment(getString(R.string.error_unknown));
                                    allComments.add(failureComment);
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
                            allComments.remove(allComments.size() - 1);
                            commentAdapter.notifyItemRemoved(allComments.size());

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
                            Comment errorComment = new Comment();
                            errorComment.setId(-2);
                            errorComment.setComment(errorMessage);
                            allComments.add(errorComment);

                            isFirstCall = false;
                        }
                    }
            );

            commentRequest.setTag("comment");
            commentRequest.setRetryPolicy(new DefaultRetryPolicy(
                    APIBuilder.TIMEOUT_SHORT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getContext()).addToRequestQueue(commentRequest);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        VolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("comment");
    }

    /**
     * Reload comment list.
     *
     * @param swipeRefresh swipe view
     */
    public void refreshCommentList(SwipeRefreshLayout swipeRefresh) {
        swipeRefreshLayout = swipeRefresh;
        isEndOfPage = false;
        apiCommentUrl = apiCommentUrlFirstPage;

        loadComments(0);
    }

    /**
     * Attach event listener.
     *
     * @param context parent context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCommentInteractionListener) {
            mListener = (OnCommentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCommentInteractionListener");
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
    public interface OnCommentInteractionListener {
        void onCommentListClicked(Comment comment);
    }
}
