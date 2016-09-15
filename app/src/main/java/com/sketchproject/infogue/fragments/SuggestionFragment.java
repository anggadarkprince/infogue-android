package com.sketchproject.infogue.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.adapters.ContributorRecyclerViewAdapter;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sketch Project Studio
 * Created by angga on 15/09/16.
 */
public class SuggestionFragment extends Fragment {
    private List<Contributor> contributors;
    private ContributorRecyclerViewAdapter contributorAdapter;
    private OnContributorInteractionListener mListener;
    private boolean isLoading = false;

    public SuggestionFragment() {
    }

    /**
     * Attach event listener.
     *
     * @param context parent context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContributorInteractionListener) {
            mListener = (OnContributorInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnContributorInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contributor_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

            contributors = new ArrayList<>();
            contributorAdapter = new ContributorRecyclerViewAdapter(contributors, mListener);

            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setAdapter(contributorAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        return view;
    }

    public void fetchSuggestion(final String query) {
        final String querySearch = query.replaceAll(" ", "+").trim();
        Log.i("Infogue/Query", querySearch);

        if (isLoading) {
            VolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("suggestion");
        } else {
            // first time or new search then add loading
            contributors.clear();
            contributorAdapter.notifyDataSetChanged();

            contributors.add(null);
            contributorAdapter.notifyItemInserted(0);
            isLoading = true;
        }

        JsonArrayRequest suggestionRequest = new JsonArrayRequest(Request.Method.GET, APIBuilder.getApiContributorSuggestionUrl(querySearch), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // remove last loading
                        if (contributors.size() > 0) {
                            contributors.remove(0);
                            contributorAdapter.notifyItemRemoved(1);
                        }

                        try {
                            contributors.clear();
                            if (response != null && response.length() > 0) {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject contributorData = response.getJSONObject(i);
                                    Contributor contributor = new Contributor();
                                    contributor.setId(contributorData.getInt(Contributor.ID));
                                    contributor.setUsername(contributorData.getString(Contributor.USERNAME));
                                    contributor.setName(contributorData.getString(Contributor.NAME));
                                    contributor.setAvatar(APIBuilder.ASSET_IMAGES_URL + "contributors/" + contributorData.getString(Contributor.AVATAR));
                                    contributors.add(contributor);
                                }
                            } else {
                                Contributor contributor = new Contributor(0, "Empty");
                                contributors.add(contributor);
                            }
                        } catch (JSONException error) {
                            error.printStackTrace();
                            Contributor contributor = new Contributor(-1, error.getMessage());
                            contributors.add(contributor);
                        }

                        contributorAdapter.notifyDataSetChanged();
                        isLoading = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // remove last loading
                        if (contributors.size() > 0) {
                            contributors.remove(0);
                            contributorAdapter.notifyItemRemoved(1);
                        }

                        String errorMessage = new Logger().networkRequestError(getContext(), error, "Suggestion");
                        Log.e("Infogue/Suggestion", errorMessage);

                        Contributor contributor = new Contributor(-1, errorMessage);
                        contributors.add(contributor);
                        contributorAdapter.notifyDataSetChanged();
                        isLoading = false;
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> messageParams = new HashMap<>();
                messageParams.put("query", querySearch);
                return messageParams;
            }
        };

        suggestionRequest.setTag("suggestion");
        suggestionRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_SHORT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getContext()).addToRequestQueue(suggestionRequest);
    }

    /**
     * Clear listener when detached.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        VolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("suggestion");
    }

    public interface OnContributorInteractionListener {
        void onContributorInteraction(View view, Contributor contributor);
    }
}
