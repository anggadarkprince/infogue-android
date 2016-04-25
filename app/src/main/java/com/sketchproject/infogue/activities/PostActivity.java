package com.sketchproject.infogue.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Category;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Subcategory;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.gujun.android.taggroup.TagGroup;

public class PostActivity extends AppCompatActivity {
    private int id;
    private String slug;
    private String title;

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
    private TagGroup mArticleTags;

    private ImageView mContributorAvatar;
    private TextView mContributorName;
    private TextView mContributorLocation;

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
        mArticleTags = (TagGroup) findViewById(R.id.article_tags);
        mArticleExcerpt = (TextView) findViewById(R.id.article_excerpt);
        mArticleDetail = (TextView) findViewById(R.id.article_detail);

        mContributorAvatar = (ImageView) findViewById(R.id.contributor_avatar);
        mContributorName = (TextView) findViewById(R.id.contributor_name);
        mContributorLocation = (TextView) findViewById(R.id.contributor_location);

        Button mCommentButton = (Button) findViewById(R.id.btn_comment);
        if (mCommentButton != null) {
            mCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent commentIntent = new Intent(getBaseContext(), CommentActivity.class);
                    commentIntent.putExtra(Article.ARTICLE_ID, id);
                    commentIntent.putExtra(Article.ARTICLE_SLUG, slug);
                    commentIntent.putExtra(Article.ARTICLE_TITLE, title);
                    startActivity(commentIntent);
                }
            });
        }

        progress = new ProgressDialog(PostActivity.this);
        progress.setMessage("Loading Article Data");
        progress.setIndeterminate(true);
        progress.show();

        buildArticle();
    }

    @SuppressLint("SetTextI18n")
    private void buildArticle() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getInt(Article.ARTICLE_ID);
            slug = extras.getString(Article.ARTICLE_SLUG);
            title = extras.getString(Article.ARTICLE_TITLE);

            mArticleWrapper.setVisibility(View.GONE);
            Glide.with(getBaseContext())
                    .load(extras.getString(Article.ARTICLE_FEATURED))
                    .placeholder(R.drawable.placeholder_rectangle)
                    .centerCrop()
                    .crossFade()
                    .into(mArticleFeatured);
            mArticleTitle.setText(extras.getString(Article.ARTICLE_TITLE));

            JsonObjectRequest articleRequest = new JsonObjectRequest(Request.Method.GET, UrlHelper.getApiPostUrl(slug), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");

                                if (status.equals(Constant.REQUEST_SUCCESS)) {
                                    JSONObject article = response.getJSONObject("article");
                                    JSONObject author = article.getJSONObject("contributor");
                                    JSONObject subcategory = article.getJSONObject("subcategory");
                                    JSONObject category = subcategory.getJSONObject("category");
                                    JSONArray tags = article.getJSONArray("tags");

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
                                    mArticleDetail.setText(sub + "  |  " + view + "X Views  |  " + stars + "Stars");
                                    mArticleRating.setRating(stars);

                                    List<String> tagList = new ArrayList<>();
                                    for (int i = 0; i < tags.length(); i++) {
                                        tagList.add(tags.getJSONObject(i).getString("tag"));
                                    }
                                    mArticleTags.setTags(tagList);

                                    Glide.with(getBaseContext())
                                            .load(Constant.BASE_URL + "images/contributors/" + author.getString(Contributor.CONTRIBUTOR_AVATAR))
                                            .placeholder(R.drawable.placeholder_square)
                                            .centerCrop()
                                            .dontAnimate()
                                            .into(mContributorAvatar);
                                    mContributorName.setText(author.getString(Contributor.CONTRIBUTOR_NAME));
                                    mContributorLocation.setText(author.getString(Contributor.CONTRIBUTOR_LOCATION));
                                    mArticleWrapper.setVisibility(View.VISIBLE);
                                } else {
                                    AppHelper.toastColored(getBaseContext(), getString(R.string.error_server), Color.parseColor("#ddd1205e"));
                                }

                                progress.dismiss();
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
            id = 0;
            slug = "";
            progress.dismiss();
            AppHelper.toastColored(getBaseContext(), "Invalid article data", Color.parseColor("#ddd1205e"));
            finish();
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
            sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareArticleText(slug));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
        } else if (id == R.id.action_refresh) {
            buildArticle();
        } else if (id == R.id.action_browse) {
            String articleUrl = UrlHelper.getArticleUrl(slug);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
            startActivity(browserIntent);
        } else if (id == R.id.action_comment) {
            Intent commentIntent = new Intent(getBaseContext(), CommentActivity.class);
            commentIntent.putExtra(Article.ARTICLE_ID, id);
            commentIntent.putExtra(Article.ARTICLE_SLUG, slug);
            commentIntent.putExtra(Article.ARTICLE_TITLE, title);
            startActivity(commentIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}
