package com.sketchproject.infogue.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Category;
import com.sketchproject.infogue.models.Contributor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.gujun.android.taggroup.TagGroup;

public class PostActivity extends AppCompatActivity {
    private SessionManager session;
    private int mLoggedId;
    private int mAuthorId;
    private String mApiToken;

    private int articleId;
    private String articleSlug;
    private String articleTitle;
    private boolean isFollowingAuthor;

    private LinearLayout mArticleWrapper;
    private ImageView mArticleFeatured;
    private TextView mArticleTitle;
    private TextView mArticleCategory;
    private TextView mArticleContributor;
    private TextView mArticlePublished;
    private TextView mArticleExcerpt;
    private TextView mArticleDetail;
    private WebView mArticleContent;
    private RatingBar mArticleRating;
    private TextView mArticleRatingDesc;
    private TagGroup mArticleTags;
    private ViewGroup mContributorButton;
    private ImageView mContributorAvatar;
    private TextView mContributorName;
    private TextView mContributorLocation;
    private ImageButton mContributorFollowButton;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        session = new SessionManager(getBaseContext());
        mLoggedId = session.getSessionData(SessionManager.KEY_ID, 0);
        mApiToken = session.getSessionData(SessionManager.KEY_TOKEN, "");

        mArticleWrapper = (LinearLayout) findViewById(R.id.article);
        mArticleFeatured = (ImageView) findViewById(R.id.featured);
        mArticleTitle = (TextView) findViewById(R.id.article_title);
        mArticleCategory = (TextView) findViewById(R.id.article_category);
        mArticleContributor = (TextView) findViewById(R.id.article_contributor);
        mArticlePublished = (TextView) findViewById(R.id.article_published);
        mArticleContent = (WebView) findViewById(R.id.article_content);
        if (mArticleContent != null) {
            mArticleContent.getSettings().setBuiltInZoomControls(false);
            mArticleContent.getSettings().setDisplayZoomControls(false);
            mArticleContent.setScrollContainer(false);
            mArticleContent.setVerticalScrollBarEnabled(false);
        }
        mArticleRating = (RatingBar) findViewById(R.id.article_rating);
        mArticleRatingDesc = (TextView) findViewById(R.id.article_rating_description);
        mArticleTags = (TagGroup) findViewById(R.id.article_tags);
        mArticleExcerpt = (TextView) findViewById(R.id.article_excerpt);
        mArticleDetail = (TextView) findViewById(R.id.article_detail);
        mContributorAvatar = (ImageView) findViewById(R.id.contributor_avatar);
        mContributorName = (TextView) findViewById(R.id.contributor_name);
        mContributorLocation = (TextView) findViewById(R.id.contributor_location);
        mContributorButton = (RelativeLayout) findViewById(R.id.btn_contributor);
        mContributorFollowButton = (ImageButton) findViewById(R.id.btn_follow_control);

        Button mCommentButton = (Button) findViewById(R.id.btn_comment);
        if (mCommentButton != null) {
            mCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent commentIntent = new Intent(getBaseContext(), CommentActivity.class);
                    commentIntent.putExtra(Article.ARTICLE_ID, articleId);
                    commentIntent.putExtra(Article.ARTICLE_SLUG, articleSlug);
                    commentIntent.putExtra(Article.ARTICLE_TITLE, articleTitle);
                    startActivity(commentIntent);
                }
            });
        }

        mArticleTags.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                Log.i("Infogue/Tag", tag);
            }
        });

        progress = new ProgressDialog(PostActivity.this);
        progress.setMessage(getString(R.string.label_retrieve_article_progress));
        progress.setIndeterminate(true);
        progress.show();

        buildArticle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ProfileActivity.PROFILE_RESULT_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                boolean isFollowing = data.getBooleanExtra(SessionManager.KEY_IS_FOLLOWING, false);
                Log.i("INFOGUE/Post", "Result " + isFollowing);
                isFollowingAuthor = isFollowing;
                if (isFollowing) {
                    mContributorFollowButton.setImageResource(R.drawable.btn_unfollow);
                } else {
                    mContributorFollowButton.setImageResource(R.drawable.btn_follow);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void buildArticle() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            articleId = extras.getInt(Article.ARTICLE_ID);
            articleSlug = extras.getString(Article.ARTICLE_SLUG);
            articleTitle = extras.getString(Article.ARTICLE_TITLE);

            mArticleWrapper.setVisibility(View.GONE);
            Glide.with(getBaseContext())
                    .load(extras.getString(Article.ARTICLE_FEATURED))
                    .placeholder(R.drawable.placeholder_rectangle)
                    .centerCrop()
                    .crossFade()
                    .into(mArticleFeatured);
            mArticleTitle.setText(extras.getString(Article.ARTICLE_TITLE));

            JsonObjectRequest articleRequest = new JsonObjectRequest(Request.Method.GET, UrlHelper.getApiPostUrl(articleSlug, mLoggedId), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");

                                if (status.equals(Constant.REQUEST_SUCCESS)) {
                                    JSONObject article = response.getJSONObject("article");
                                    final JSONObject author = article.getJSONObject("contributor");
                                    JSONObject subcategory = article.getJSONObject(Article.ARTICLE_SUBCATEGORY);
                                    JSONObject category = subcategory.getJSONObject(Article.ARTICLE_CATEGORY);
                                    JSONArray tags = article.getJSONArray(Article.ARTICLE_TAGS);

                                    mArticleCategory.setText(category.getString(Category.COLUMN_CATEGORY).toUpperCase());
                                    mArticleContributor.setText(author.getString(Contributor.CONTRIBUTOR_NAME));
                                    mArticlePublished.setText(article.getString(Article.ARTICLE_CREATED_AT));
                                    mArticleContent.loadData(AppHelper.wrapHtmlString(article.getString(Article.ARTICLE_CONTENT)), "text/html", "UTF-8");
                                    String excerpt = article.getString(Article.ARTICLE_EXCERPT);
                                    if (excerpt == null || excerpt.isEmpty()) {
                                        mArticleExcerpt.setVisibility(View.GONE);
                                    } else {
                                        mArticleExcerpt.setVisibility(View.VISIBLE);
                                        mArticleExcerpt.setText(article.getString(Article.ARTICLE_EXCERPT));
                                    }

                                    String sub = subcategory.getString(Subcategory.COLUMN_SUBCATEGORY);
                                    int view = article.getInt(Article.ARTICLE_VIEW);
                                    int stars = article.getInt(Article.ARTICLE_RATING);
                                    mArticleDetail.setText(sub + "  |  " + view + "X Views  |  " + stars + " Stars");
                                    mArticleRating.setRating(stars);
                                    switch (stars) {
                                        case 1:
                                            mArticleRatingDesc.setText("WORST ARTICLE");
                                            break;
                                        case 2:
                                            mArticleRatingDesc.setText("BAD ARTICLE");
                                            break;
                                        case 3:
                                            mArticleRatingDesc.setText("GOOD ARTICLE");
                                            break;
                                        case 4:
                                            mArticleRatingDesc.setText("EXCELLENT ARTICLE");
                                            break;
                                        case 5:
                                            mArticleRatingDesc.setText("GREAT ARTICLE");
                                            break;
                                        default:
                                            mArticleRatingDesc.setText("UNRATED ARTICLE");
                                            break;
                                    }
                                    mArticleRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                        @Override
                                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                                            rateArticle(rating, fromUser);
                                        }
                                    });

                                    List<String> tagList = new ArrayList<>();
                                    for (int i = 0; i < tags.length(); i++) {
                                        tagList.add(tags.getJSONObject(i).getString(Article.ARTICLE_TAG));
                                    }
                                    mArticleTags.setTags(tagList);

                                    mAuthorId = author.getInt(Contributor.CONTRIBUTOR_ID);
                                    Glide.with(getBaseContext())
                                            .load(author.getString(Contributor.CONTRIBUTOR_AVATAR_REF))
                                            .placeholder(R.drawable.placeholder_square)
                                            .centerCrop()
                                            .dontAnimate()
                                            .into(mContributorAvatar);
                                    mContributorName.setText(author.getString(Contributor.CONTRIBUTOR_NAME));
                                    mContributorLocation.setText(author.getString(Contributor.CONTRIBUTOR_LOCATION));

                                    isFollowingAuthor = author.getInt(Contributor.CONTRIBUTOR_IS_FOLLOWING) == 1;
                                    if (isFollowingAuthor) {
                                        mContributorFollowButton.setImageResource(R.drawable.btn_unfollow);
                                    } else {
                                        mContributorFollowButton.setImageResource(R.drawable.btn_follow);
                                    }
                                    mContributorFollowButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (session.isLoggedIn()) {
                                                toggleFollowHandler();
                                            } else {
                                                Intent authIntent = new Intent(getBaseContext(), AuthenticationActivity.class);
                                                startActivity(authIntent);
                                            }
                                        }
                                    });
                                    mContributorButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                Intent profileIntent = new Intent(getBaseContext(), ProfileActivity.class);
                                                profileIntent.putExtra(SessionManager.KEY_ID, author.getInt(Contributor.CONTRIBUTOR_ID));
                                                profileIntent.putExtra(SessionManager.KEY_USERNAME, author.getString(Contributor.CONTRIBUTOR_USERNAME));
                                                profileIntent.putExtra(SessionManager.KEY_NAME, author.getString(Contributor.CONTRIBUTOR_NAME));
                                                profileIntent.putExtra(SessionManager.KEY_LOCATION, author.getString(Contributor.CONTRIBUTOR_LOCATION));
                                                profileIntent.putExtra(SessionManager.KEY_ABOUT, author.getString(Contributor.CONTRIBUTOR_ABOUT));
                                                profileIntent.putExtra(SessionManager.KEY_AVATAR, author.getString(Contributor.CONTRIBUTOR_AVATAR_REF));
                                                profileIntent.putExtra(SessionManager.KEY_COVER, author.getString(Contributor.CONTRIBUTOR_COVER_REF));
                                                profileIntent.putExtra(SessionManager.KEY_STATUS, author.getString(Contributor.CONTRIBUTOR_STATUS));
                                                profileIntent.putExtra(SessionManager.KEY_ARTICLE, author.getInt(Contributor.CONTRIBUTOR_ARTICLE));
                                                profileIntent.putExtra(SessionManager.KEY_FOLLOWER, author.getInt(Contributor.CONTRIBUTOR_FOLLOWERS));
                                                profileIntent.putExtra(SessionManager.KEY_FOLLOWING, author.getInt(Contributor.CONTRIBUTOR_FOLLOWING));
                                                profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, isFollowingAuthor);
                                                startActivityForResult(profileIntent, ProfileActivity.PROFILE_RESULT_CODE);
                                            } catch (JSONException e) {
                                                progress.dismiss();
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    mArticleWrapper.setVisibility(View.VISIBLE);
                                } else {
                                    AppHelper.toastColored(getBaseContext(), getString(R.string.error_server), Color.parseColor("#ddd1205e"));
                                }

                                progress.dismiss();
                                countArticleViewer();
                            } catch (JSONException e) {
                                progress.dismiss();
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progress.dismiss();
                            String errorMessage = getString(R.string.error_server);
                            if (error.networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = getString(R.string.error_timeout);
                                } else {
                                    errorMessage = getString(R.string.error_unknown);
                                }
                            }
                            AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));
                        }
                    }
            );
            articleRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(articleRequest);

        } else {
            articleId = 0;
            articleSlug = "";
            progress.dismiss();
            AppHelper.toastColored(getBaseContext(), "Invalid article data", Color.parseColor("#ddd1205e"));
            finish();
        }
    }

    private void toggleFollowHandler() {
        if (isFollowingAuthor) {
            mContributorFollowButton.setImageResource(R.drawable.btn_follow);

            StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_UNFOLLOW,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject result = new JSONObject(response);
                                String status = result.getString("status");
                                String message = result.getString("message");

                                if (status.equals(Constant.REQUEST_SUCCESS)) {
                                    Log.i("Infogue/Unfollow", message);
                                } else {
                                    Log.w("Infogue/Unfollow", getString(R.string.error_unknown));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse = error.networkResponse;
                            String errorMessage = getString(R.string.error_unknown);
                            if (networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = getString(R.string.error_timeout);
                                }
                            } else {
                                String result = new String(networkResponse.data);
                                try {
                                    JSONObject response = new JSONObject(result);
                                    String status = response.getString("status");
                                    String message = response.getString("message");

                                    if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                        errorMessage = message+", please login again!";
                                    } else if (status.equals(Constant.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                        errorMessage = message;
                                    } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                        errorMessage = message;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));

                            mContributorFollowButton.setImageResource(R.drawable.btn_unfollow);
                            isFollowingAuthor = true;
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("api_token", mApiToken);
                    params.put("contributor_id", String.valueOf(mLoggedId));
                    params.put("following_id", String.valueOf(mAuthorId));
                    params.put("_method", "delete");
                    return params;
                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
        } else {
            mContributorFollowButton.setImageResource(R.drawable.btn_unfollow);

            StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_FOLLOW,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject result = new JSONObject(response);
                                String status = result.getString("status");
                                String message = result.getString("message");

                                if (status.equals(Constant.REQUEST_SUCCESS)) {
                                    Log.i("Infogue/Follow", message);
                                } else {
                                    Log.w("Infogue/Follow", getString(R.string.error_unknown));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse = error.networkResponse;
                            String errorMessage = getString(R.string.error_unknown);
                            if (networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = getString(R.string.error_timeout);
                                }
                            } else {
                                String result = new String(networkResponse.data);
                                try {
                                    JSONObject response = new JSONObject(result);
                                    String status = response.getString("status");
                                    String message = response.getString("message");

                                    if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                        errorMessage = message+", please login again!";
                                    } else if (status.equals(Constant.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                        errorMessage = message;
                                    } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                        errorMessage = message;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));

                            mContributorFollowButton.setImageResource(R.drawable.btn_follow);
                            isFollowingAuthor = false;
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("api_token", mApiToken);
                    params.put("contributor_id", String.valueOf(mLoggedId));
                    params.put("following_id", String.valueOf(mAuthorId));
                    return params;
                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
        }

        isFollowingAuthor = !isFollowingAuthor;
    }

    private void countArticleViewer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_HIT,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject result = new JSONObject(response);
                                    String status = result.getString("status");
                                    String message = result.getString("message");

                                    if (status.equals(Constant.REQUEST_SUCCESS)) {
                                        Log.i("Infogue/Hit", "Current hit article id : " + articleId + " is " + message);
                                    } else {
                                        Log.w("Infogue/Hit", getString(R.string.error_unknown));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse == null) {
                                    if (error.getClass().equals(TimeoutError.class)) {
                                        Log.e("Infogue/Hit", getString(R.string.error_timeout));
                                    } else {
                                        Log.e("Infogue/Hit", getString(R.string.error_unknown));
                                    }
                                } else {
                                    Log.e("Infogue/Hit", getString(R.string.error_server));
                                }
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put(Article.ARTICLE_FOREIGN, String.valueOf(articleId));
                        return params;
                    }
                };
                postRequest.setRetryPolicy(new DefaultRetryPolicy(
                        15000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
            }
        }, 15000);
    }

    private void rateArticle(final float rating, boolean fromUser) {
        if (rating > 0 && fromUser) {
            StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_RATE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject result = new JSONObject(response);
                                String status = result.getString("status");
                                String message = result.getString("message");

                                if (status.equals(Constant.REQUEST_SUCCESS)) {
                                    Log.i("Infogue/Rate", "Average rating for article id : " + articleId + " is " + message);
                                } else {
                                    String errorMessage = getString(R.string.error_unknown) + "\r\nYour rating was discarded";
                                    AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse = error.networkResponse;
                            String errorMessage = getString(R.string.error_server);
                            if (networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = getString(R.string.error_timeout) + "\r\nYour rating was discarded";
                                } else {
                                    errorMessage = getString(R.string.error_unknown);
                                }
                            } else {
                                try {
                                    String result = new String(networkResponse.data);
                                    JSONObject response = new JSONObject(result);
                                    String status = response.getString("status");
                                    String message = response.getString("message");

                                    if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                        errorMessage = message;
                                    } else {
                                        errorMessage = getString(R.string.error_unknown);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put(Article.ARTICLE_FOREIGN, String.valueOf(articleId));
                    params.put(Article.ARTICLE_RATE, String.valueOf((int) rating));
                    return params;
                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);

            if (rating >= 3) {
                AppHelper.toastColored(getBaseContext(), "Awesome!, you give " + rating + " Stars on \n\r\"" + articleTitle + "\"", Color.parseColor("#ddd1205e"));
            } else {
                AppHelper.toastColored(getBaseContext(), "Too bad!, you give under 3 Stars on \n\r\"" + articleTitle + "\"", Color.parseColor("#ddf1ae50"));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareArticleText(articleSlug));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
        } else if (id == R.id.action_refresh) {
            progress.show();
            buildArticle();
        } else if (id == R.id.action_browse) {
            String articleUrl = UrlHelper.getArticleUrl(articleSlug);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
            startActivity(browserIntent);
        } else if (id == R.id.action_comment) {
            Intent commentIntent = new Intent(getBaseContext(), CommentActivity.class);
            commentIntent.putExtra(Article.ARTICLE_ID, articleId);
            commentIntent.putExtra(Article.ARTICLE_SLUG, articleSlug);
            commentIntent.putExtra(Article.ARTICLE_TITLE, articleTitle);
            startActivity(commentIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}
