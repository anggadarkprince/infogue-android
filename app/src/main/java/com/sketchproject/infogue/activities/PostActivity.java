package com.sketchproject.infogue.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.events.ArticleListEvent;
import com.sketchproject.infogue.events.FollowerListEvent;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Category;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Subcategory;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.gujun.android.taggroup.TagGroup;

/**
 * A {@link AppCompatActivity} subclass, show post article.
 *
 * Sketch Project Studio
 * Created by Angga on 20/012/2016 10.37.
 */
public class PostActivity extends AppCompatActivity {
    private int mLoggedId;
    private String articleSlug;

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

    private Article articleModel;
    private Contributor contributorModel;

    private ProgressDialog progress;
    private Context context;

    /**
     * Perform initialization of PostActivity.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        context = this;
        SessionManager session = new SessionManager(getBaseContext());
        mLoggedId = session.getSessionData(SessionManager.KEY_ID, 0);

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

        mArticleTags.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                Log.i("Infogue/Tag", tag);
                Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                articleIntent.putExtra(Article.TAG, tag);
                startActivity(articleIntent);
            }
        });

        progress = new ProgressDialog(PostActivity.this);
        progress.setMessage(getString(R.string.label_retrieve_article_progress));
        progress.setIndeterminate(true);
        progress.setCanceledOnTouchOutside(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // articleId = extras.getInt(Article.ID);
            // articleTitle = extras.getString(Article.TITLE);
            articleSlug = extras.getString(Article.SLUG);

            mArticleWrapper.setVisibility(View.GONE);

            Glide.with(getBaseContext())
                    .load(extras.getString(Article.FEATURED))
                    .placeholder(R.drawable.placeholder_rectangle)
                    .centerCrop()
                    .crossFade()
                    .into(mArticleFeatured);

            retrieveArticle();
        } else {
            progress.dismiss();
            Helper.toastColor(getBaseContext(), R.string.message_invalid_article, R.color.color_danger_transparent);
            finish();
        }
    }

    /**
     * Retrieve post article from server.
     */
    private void retrieveArticle() {
        progress.show();
        JsonObjectRequest articleRequest = new JsonObjectRequest(
                Request.Method.GET, APIBuilder.getApiPostUrl(articleSlug, mLoggedId), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString(APIBuilder.RESPONSE_STATUS);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                final JSONObject article = response.getJSONObject(Article.DATA);
                                final JSONObject author = article.getJSONObject(Contributor.DATA);

                                articleModel = new Article();
                                articleModel.setId(article.getInt(Article.ID));
                                articleModel.setTitle(article.getString(Article.TITLE));
                                articleModel.setSlug(article.getString(Article.SLUG));
                                articleModel.setFeatured(article.getString(Article.FEATURED_REF));
                                buildArticle(article, author);

                                contributorModel = new Contributor();
                                contributorModel.setId(author.getInt(Contributor.ID));
                                contributorModel.setUsername(author.getString(Contributor.USERNAME));
                                contributorModel.setName(author.getString(Contributor.NAME));
                                contributorModel.setLocation(author.getString(Contributor.LOCATION));
                                contributorModel.setAbout(author.getString(Contributor.ABOUT));
                                contributorModel.setAvatar(author.getString(Contributor.AVATAR_REF));
                                contributorModel.setCover(author.getString(Contributor.COVER_REF));
                                contributorModel.setStatus(author.getString(Contributor.STATUS));
                                contributorModel.setArticle(author.getInt(Contributor.ARTICLE));
                                contributorModel.setFollowers(author.getInt(Contributor.FOLLOWERS));
                                contributorModel.setFollowing(author.getInt(Contributor.FOLLOWING));
                                contributorModel.setIsFollowing(author.getInt(Contributor.IS_FOLLOWING) == 1);
                                buildAuthor(author);

                                mArticleWrapper.setVisibility(View.VISIBLE);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        new ArticleListEvent(context, articleModel).countViewer();
                                    }
                                }, 15000);
                            } else {
                                Helper.toastColor(getBaseContext(), R.string.error_server, R.color.color_danger_transparent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                        if (error.networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = getString(R.string.error_no_connection);
                            }
                        } else {
                            String result = new String(networkResponse.data);

                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Post", "[Retrieve] Error : " + message);

                                if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
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
        articleRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_SHORT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(articleRequest);
    }

    /**
     * Build article data content.
     *
     * @param article object article from database
     * @param author  object contributor data from database.
     */
    private void buildArticle(JSONObject article, JSONObject author) {
        try {
            JSONObject subcategory = article.getJSONObject(Article.SUBCATEGORY);
            JSONObject category = subcategory.getJSONObject(Article.CATEGORY);
            JSONArray tags = article.getJSONArray(Article.TAGS);

            Glide.with(getBaseContext())
                    .load(article.getString(Article.FEATURED_REF))
                    .placeholder(R.drawable.placeholder_rectangle)
                    .centerCrop()
                    .crossFade()
                    .into(mArticleFeatured);
            mArticleTitle.setText(article.getString(Article.TITLE));
            mArticleCategory.setText(category.getString(Category.CATEGORY).toUpperCase());
            mArticleContributor.setText(author.getString(Contributor.NAME));
            mArticlePublished.setText(article.getString(Article.CREATED_AT));
            mArticleContent.loadData(Helper.wrapHtmlString(article.getString(Article.CONTENT)), "text/html", "UTF-8");
            String excerpt = article.getString(Article.EXCERPT);
            if (excerpt == null || excerpt.isEmpty()) {
                mArticleExcerpt.setVisibility(View.GONE);
            } else {
                mArticleExcerpt.setVisibility(View.VISIBLE);
                mArticleExcerpt.setText(article.getString(Article.EXCERPT));
            }

            String sub = subcategory.getString(Subcategory.SUBCATEGORY);
            int view = article.getInt(Article.VIEW);
            int stars = article.getInt(Article.RATING);
            String stats = sub + "  |  " + view + "X Views  |  " + stars + " Stars";
            mArticleDetail.setText(stats);
            mArticleRating.setRating(stars);
            switch (stars) {
                case 1:
                    mArticleRatingDesc.setText(R.string.label_article_worst);
                    break;
                case 2:
                    mArticleRatingDesc.setText(R.string.label_article_bad);
                    break;
                case 3:
                    mArticleRatingDesc.setText(R.string.label_article_good);
                    break;
                case 4:
                    mArticleRatingDesc.setText(R.string.label_article_excellent);
                    break;
                case 5:
                    mArticleRatingDesc.setText(R.string.label_article_great);
                    break;
                default:
                    mArticleRatingDesc.setText(R.string.label_article_unrated);
                    break;
            }
            mArticleRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (rating > 0 && fromUser) {
                        new ArticleListEvent(context, articleModel).rateArticle(Math.round(rating));
                    }
                }
            });

            List<String> tagList = new ArrayList<>();
            for (int i = 0; i < tags.length(); i++) {
                tagList.add(tags.getJSONObject(i).getString(Article.TAG));
            }
            mArticleTags.setTags(tagList);

            Button mCommentButton = (Button) findViewById(R.id.btn_comment);
            if (mCommentButton != null) {
                mCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ArticleListEvent(context, articleModel).leaveComment();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build contributor view content.
     *
     * @param author object from database
     */
    private void buildAuthor(JSONObject author) {
        try {
            Glide.with(getBaseContext())
                    .load(author.getString(Contributor.AVATAR_REF))
                    .placeholder(R.drawable.placeholder_square)
                    .centerCrop()
                    .dontAnimate()
                    .into(mContributorAvatar);
            mContributorName.setText(author.getString(Contributor.NAME));
            mContributorLocation.setText(author.getString(Contributor.LOCATION));

            mContributorFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FollowerListEvent(context, contributorModel, mContributorFollowButton).followContributor();
                }
            });
            mContributorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FollowerListEvent(context, contributorModel, mContributorFollowButton).viewProfile();
                }
            });

            boolean isFollowingAuthor = author.getInt(Contributor.IS_FOLLOWING) == 1;
            if (isFollowingAuthor) {
                mContributorFollowButton.setImageResource(R.drawable.btn_unfollow);
            } else {
                mContributorFollowButton.setImageResource(R.drawable.btn_follow);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if there is result from profile activity, if so update button follow state.
     *
     * @param requestCode code request when profile activity called
     * @param resultCode  result state for now just catch RESULT_OK
     * @param data        data from activity called is follow or unfollow
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new FollowerListEvent(this).handleProfileResult(requestCode, resultCode, data);
    }

    /**
     * Create option menu for post like share, reload and open web version.
     *
     * @param menu inflate
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    /**
     * Select action when user hit option menu.
     *
     * @param item selected menu item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_refresh) {
            retrieveArticle();
        } else if (id == R.id.action_share) {
            new ArticleListEvent(this, articleModel).shareArticle();
        } else if (id == R.id.action_browse) {
            new ArticleListEvent(this, articleModel).browseArticle();
        } else if (id == R.id.action_comment) {
            new ArticleListEvent(this, articleModel).leaveComment();
        }

        return super.onOptionsItemSelected(item);
    }
}
