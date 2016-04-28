package com.sketchproject.infogue.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.AlertFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Category;
import com.sketchproject.infogue.models.Subcategory;
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

public class ArticleEditActivity extends ArticleCreateActivity {
    private int articleId;
    private String articleSlug;
    private String articleFeatured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_article_edit);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            articleId = extras.getInt(Article.ARTICLE_ID);
            articleSlug = extras.getString(Article.ARTICLE_SLUG);
            articleFeatured = extras.getString(Article.ARTICLE_FEATURED);
            apiUrl = Constant.URL_API_ARTICLE + "/" + articleSlug;
            isUpdate = true;

            mFeaturedImage.setVisibility(View.VISIBLE);
            Glide.with(getBaseContext()).load(articleFeatured)
                    .placeholder(R.drawable.placeholder_rectangle)
                    .centerCrop()
                    .crossFade()
                    .into(mFeaturedImage);
            realPathFeatured = articleFeatured;
        } else {
            AppHelper.toastColored(getBaseContext(), "Invalid article!", Color.parseColor("#ddd9534f"));
            finish();
        }

        mSaveButton.setText(R.string.action_update_article);

        retrieveArticleData();
    }

    private void retrieveArticleData() {
        progress.setMessage(getString(R.string.label_retrieve_article_progress));
        progress.show();

        JsonObjectRequest articleRequest = new JsonObjectRequest(Request.Method.GET, UrlHelper.getApiPostUrl(articleSlug), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString(Constant.RESPONSE_STATUS);

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                JSONObject articleObject = response.getJSONObject("article");
                                JSONObject subcategory = articleObject.getJSONObject(Article.ARTICLE_SUBCATEGORY);
                                JSONObject category = subcategory.getJSONObject(Article.ARTICLE_CATEGORY);
                                JSONArray tags = articleObject.getJSONArray(Article.ARTICLE_TAGS);

                                mTitleInput.setText(articleObject.getString(Article.ARTICLE_TITLE));
                                mSlugInput.setText(articleObject.getString(Article.ARTICLE_SLUG));
                                String contentUpdate = articleObject.getString(Article.ARTICLE_CONTENT_UPDATE);
                                if (contentUpdate != null && !contentUpdate.equals("null") && !contentUpdate.trim().isEmpty()) {
                                    mContentEditor.setHtml(contentUpdate);
                                } else {
                                    mContentEditor.setHtml(articleObject.getString(Article.ARTICLE_CONTENT));
                                }

                                mExcerptInput.setText(articleObject.getString(Article.ARTICLE_EXCERPT));

                                for (int i = 0; i < categoriesList.size(); i++) {
                                    if (categoriesList.get(i).getId() == category.getInt(Category.COLUMN_ID)) {
                                        mCategorySpinner.setSelection(i +1);
                                        populateSubcategory(i);
                                        break;
                                    }
                                }

                                for (int j = 0; j < subcategoriesList.size(); j++) {
                                    if (subcategoriesList.get(j).getId() == subcategory.getInt(Subcategory.COLUMN_ID)) {
                                        final int position = j;
                                        mSubcategorySpinner.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSubcategorySpinner.setSelection(position+1);
                                                Log.i("Infogue/Subcategory","Set selection 2 - "+position);
                                            }
                                        }, 200);

                                        break;
                                    }
                                }

                                List<String> tagsList = new ArrayList<>();
                                for (int i = 0; i < tags.length(); i++) {
                                    tagsList.add(tags.getJSONObject(i).getString(Article.ARTICLE_TAG));
                                }
                                mTagsInput.setTags(tagsList);

                                boolean isPending = articleObject.getString(Article.ARTICLE_STATUS).equals(Article.STATUS_PENDING);
                                boolean isPublished = articleObject.getString(Article.ARTICLE_STATUS).equals(Article.STATUS_PUBLISHED);
                                boolean isDraft = articleObject.getString(Article.ARTICLE_STATUS).equals(Article.STATUS_DRAFT);
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
                                alert.setAlertType(AlertFragment.ALERT_INFO);
                                alert.setAlertMessage(getString(R.string.error_unknown));
                                alert.show();
                                mScrollView.smoothScrollTo(0, 0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            finish();
                            AppHelper.toastColored(getBaseContext(), getString(R.string.error_parse_data),
                                    ContextCompat.getColor(getBaseContext(), R.color.primary));
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
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.optString(Constant.RESPONSE_STATUS);
                                String message = response.optString(Constant.RESPONSE_MESSAGE);

                                Log.e("Infogue/Article", "Error::" + message);

                                if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                    errorMessage = getString(R.string.error_unauthorized);
                                } else if (status.equals(Constant.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }
                        AppHelper.toastColored(getBaseContext(), errorMessage,
                                ContextCompat.getColor(getBaseContext(), R.color.color_danger));
                        progress.dismiss();
                    }
                }
        );
        articleRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(articleRequest);
    }
}
