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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.GalleryActivity;
import com.sketchproject.infogue.adapters.GalleryRecyclerViewAdapter;
import com.sketchproject.infogue.models.Image;
import com.sketchproject.infogue.modules.EndlessRecyclerViewScrollListener;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;
import com.sketchproject.infogue.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Sketch Project Studio
 * Created by angga on 17/09/16.
 */
public class GalleryFragment extends Fragment {
    private boolean isFirstCall = true;
    private boolean isEndOfPage = false;

    private List<Image> allImages;
    private GalleryRecyclerViewAdapter galleryAdapter;
    private OnImageInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private String apiGalleryUrl = "";
    private String apiGalleryUrlFirstPage = "";
    private String apiGalleryParams = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(getContext());
        apiGalleryParams = "api_token=" + session.getSessionData(SessionManager.KEY_TOKEN, null) + "&contributor_id=" + session.getSessionData(SessionManager.KEY_ID, 0);
        apiGalleryUrl = APIBuilder.URL_API_GALLERY + "?" + apiGalleryParams;
        apiGalleryUrlFirstPage = apiGalleryUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            LinearLayoutManager layoutManager = new GridLayoutManager(context, 3);

            allImages = new ArrayList<>();
            galleryAdapter = new GalleryRecyclerViewAdapter(allImages, mListener);

            recyclerView = (RecyclerView) view;
            recyclerView.setAdapter(galleryAdapter);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    if (!isFirstCall) {
                        loadGallery(page);
                    }
                }

                @Override
                public void onReachTop(boolean isFirst) {
                    ((GalleryActivity) getActivity()).setSwipeEnable(isFirst);
                }
            });

            if (isFirstCall) {
                loadGallery(0);
            }
        }
        return view;
    }

    private void loadGallery(final int page) {
        if (!isEndOfPage && apiGalleryUrl != null) {
            if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                allImages.add(null);
                galleryAdapter.notifyItemInserted(allImages.size() - 1);
            }

            Log.i("Infogue/Gallery", "URL " + apiGalleryUrl);
            JsonObjectRequest messageRequest = new JsonObjectRequest(Request.Method.GET, apiGalleryUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                JSONObject messages = response.getJSONObject("gallery");

                                String nextUrl = messages.getString("next_page_url");
                                int currentPage = messages.getInt("current_page");
                                int lastPage = messages.getInt("last_page");
                                JSONArray data = messages.optJSONArray("data");

                                apiGalleryUrl = nextUrl + "&" + apiGalleryParams;

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                                        // remove last loading in bottom of data
                                        allImages.remove(allImages.size() - 1);
                                        galleryAdapter.notifyItemRemoved(allImages.size());
                                    } else {
                                        // refreshing data then remove all first
                                        swipeRefreshLayout.setRefreshing(false);
                                        int total = allImages.size();
                                        for (int i = 0; i < total; i++) {
                                            allImages.remove(0);
                                        }
                                        galleryAdapter.notifyItemRangeRemoved(0, total);
                                    }

                                    List<Image> moreImages = new ArrayList<>();
                                    if (data != null) {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject imageData = data.getJSONObject(i);

                                            Image image = new Image();
                                            image.setId(imageData.getInt(Image.ID));
                                            image.setSource(imageData.getString(Image.SOURCE));
                                            moreImages.add(image);
                                        }
                                    }

                                    int curSize = galleryAdapter.getItemCount();
                                    allImages.addAll(moreImages);

                                    if (allImages.size() <= 0) {
                                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                                        recyclerView.setLayoutManager(layoutManager);
                                        isEndOfPage = true;
                                        Log.i("Infogue/Gallery", "EMPTY on page " + page);
                                        Image emptyImage = new Image();
                                        emptyImage.setId(0);
                                        allImages.add(emptyImage);
                                    } else if (currentPage >= lastPage) {
                                        isEndOfPage = true;
                                        Log.i("Infogue/Gallery", "END on page " + page);
                                    }
                                    galleryAdapter.notifyItemRangeInserted(curSize, allImages.size() - 1);
                                } else {
                                    Log.i("Infogue/Gallery", "Error on page " + page);
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

                            String errorMessage = new Logger().networkRequestError(getContext(), error, "Gallery");
                            Helper.toastColor(getContext(), errorMessage, R.color.color_danger_transparent);

                            // add error view holder
                            isEndOfPage = true;
                            isFirstCall = false;
                        }
                    }
            );

            messageRequest.setTag("gallery");
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
        VolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("gallery");
    }

    /**
     * Retrieve all images in adapter
     *
     * @return list of images
     */
    public ArrayList<Image> getImages() {
        return (ArrayList<Image>) allImages;
    }

    /**
     * Insert new image after uploaded.
     *
     * @param image new image
     */
    public void insertImage(Image image) {
        if (allImages.size() == 1) {
            if (allImages.get(0).getId() <= 0) {
                allImages.remove(0);
                galleryAdapter.notifyItemRemoved(1);
                LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
                recyclerView.setLayoutManager(layoutManager);
            }
        }
        allImages.add(0, image);
        galleryAdapter.notifyItemInserted(0);
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
    }

    /**
     * Remove view holder by ID.
     *
     * @param id article id
     */
    public void deleteImage(int id) {
        Log.i("INFOGUE/Gallery", "Delete id : " + id);
        for (int i = 0; i < allImages.size(); i++) {
            if (allImages.get(i) != null && allImages.get(i).getId() == id) {
                allImages.remove(i);
                galleryAdapter.notifyItemRemoved(i);
            }
        }
    }

    /**
     * Reload message list.
     *
     * @param swipeRefresh swipe view
     */
    public void refreshGallery(SwipeRefreshLayout swipeRefresh) {
        swipeRefreshLayout = swipeRefresh;
        isEndOfPage = false;
        apiGalleryUrl = apiGalleryUrlFirstPage;
        loadGallery(0);
    }

    /**
     * Attach event listener.
     *
     * @param context parent context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnImageInteractionListener) {
            mListener = (OnImageInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnImageInteractionListener");
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
    public interface OnImageInteractionListener {
        void onImageClicked(Image image, int position);

        void onDeleteImage(Image image);
    }
}
