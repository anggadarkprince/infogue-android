package com.sketchproject.infogue.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Category;
import com.sketchproject.infogue.models.Subcategory;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link AppCompatActivity} subclass contains article form and handle save operation.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 7/04/2016 10.37.
 */
public class ArticleEditActivity extends ArticleCreateActivity {
    private String articleSlug;
    private String articleFeatured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_article_edit);
        }

        mSaveButton.setText(R.string.action_update_article);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // int articleId = extras.getInt(Article.ID);
            articleSlug = extras.getString(Article.SLUG);
            articleFeatured = extras.getString(Article.FEATURED);
            apiUrl = APIBuilder.getApiPostUrl(articleSlug);
            isUpdate = true;

            retrieveArticleData();
        } else {
            Helper.toastColor(getBaseContext(), R.string.message_invalid_article, R.color.color_danger_transparent);
            finish();
        }
    }

    /**
     * Retrieve article which performed to update.
     */
    private void retrieveArticleData() {
        // make request image featured immediately from extras data
        mFeaturedImage.setVisibility(View.VISIBLE);
        Glide.with(getBaseContext()).load(articleFeatured)
                .placeholder(R.drawable.placeholder_rectangle)
                .centerCrop()
                .crossFade()
                .into(mFeaturedImage);
        realPathFeatured = articleFeatured;

        // set progress active
        progress.setMessage(getString(R.string.label_retrieve_article_progress));
        progress.show();

        // retrieve post request from server
        JsonObjectRequest articleRequest = new JsonObjectRequest(Request.Method.GET, APIBuilder.getApiPostUrl(articleSlug), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString(APIBuilder.RESPONSE_STATUS);

                            Log.e("Infogue/Article", "[Edit] Success : " + status);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                JSONObject articleObject = response.getJSONObject(Article.DATA);
                                JSONObject subcategory = articleObject.getJSONObject(Article.SUBCATEGORY);
                                JSONObject category = subcategory.getJSONObject(Article.CATEGORY);
                                JSONArray tags = articleObject.getJSONArray(Article.TAGS);

                                // set plain input text and content
                                mTitleInput.setText(articleObject.getString(Article.TITLE));
                                mSlugInput.setText(articleObject.getString(Article.SLUG));
                                mExcerptInput.setText(articleObject.getString(Article.EXCERPT));
                                String contentUpdate = articleObject.getString(Article.CONTENT_UPDATE);
                                if (contentUpdate != null && !contentUpdate.equals("null") && !contentUpdate.trim().isEmpty()) {
                                    mContentEditor.setHtml(contentUpdate);
                                } else {
                                    mContentEditor.setHtml(articleObject.getString(Article.CONTENT));
                                }

                                // populate category data into spinner
                                for (int i = 0; i < categoriesList.size(); i++) {
                                    if (categoriesList.get(i).getId() == category.getInt(Category.ID)) {
                                        mCategorySpinner.setSelection(i + 1);
                                        populateSubcategory(i);
                                        break;
                                    }
                                }

                                // populate subcategory data into spinner
                                for (int j = 0; j < subcategoriesList.size(); j++) {
                                    if (subcategoriesList.get(j).getId() == subcategory.getInt(Subcategory.ID)) {
                                        final int position = j;
                                        mSubcategorySpinner.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSubcategorySpinner.setSelection(position + 1);
                                            }
                                        }, 200);

                                        break;
                                    }
                                }

                                // populate tags into tags input
                                List<String> tagsList = new ArrayList<>();
                                for (int i = 0; i < tags.length(); i++) {
                                    tagsList.add(tags.getJSONObject(i).getString(Article.TAG));
                                }
                                mTagsInput.setTags(tagsList);

                                // determine checked status
                                boolean isPending = articleObject.getString(Article.STATUS).equals(Article.STATUS_PENDING);
                                boolean isPublished = articleObject.getString(Article.STATUS).equals(Article.STATUS_PUBLISHED);
                                boolean isDraft = articleObject.getString(Article.STATUS).equals(Article.STATUS_DRAFT);
                                if (isPending || isPublished) {
                                    mPublishedRadio.setChecked(true);
                                } else if (isDraft) {
                                    mDraftRadio.setChecked(true);
                                } else {
                                    mPublishedRadio.setChecked(false);
                                    mDraftRadio.setChecked(false);
                                }
                                mScrollView.smoothScrollTo(0, 0);

                            } else {
                                mScrollView.smoothScrollTo(0, 0);
                                Helper.toastColor(getBaseContext(), R.string.error_unknown, R.color.color_danger_transparent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            finish();
                            Helper.toastColor(getBaseContext(), R.string.error_parse_data, R.color.color_danger_transparent);
                        }
                        progress.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_unknown);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = getString(R.string.error_no_connection);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Article", "[Edit] Error : " + message);

                                if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                    errorMessage = getString(R.string.error_unauthorized);
                                } else if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }
                        Helper.toastColor(getBaseContext(), errorMessage, R.color.color_danger_transparent);
                        progress.dismiss();
                    }
                }
        );

        articleRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(articleRequest);
    }
}
