package com.sketchproject.infogue.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.utils.UrlHelper;

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
        mArticleContent.getSettings().setBuiltInZoomControls(false);
        mArticleContent.getSettings().setDisplayZoomControls(false);
        mArticleContent.setScrollContainer(false);
        mArticleContent.setVerticalScrollBarEnabled(false);
        mArticleRating = (RatingBar) findViewById(R.id.article_rating);
        mArticleTags = (TagGroup) findViewById(R.id.article_tags);
        mArticleExcerpt = (TextView) findViewById(R.id.article_excerpt);
        mArticleDetail = (TextView) findViewById(R.id.article_detail);

        mContributorAvatar = (ImageView) findViewById(R.id.contributor_avatar);
        mContributorName = (TextView) findViewById(R.id.contributor_name);
        mContributorLocation = (TextView) findViewById(R.id.contributor_location);

        Button mCommentButton = (Button) findViewById(R.id.btn_comment);
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

        progress = new ProgressDialog(this);
        progress.setMessage("Loading Article Data");
        progress.setIndeterminate(true);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getInt(Article.ARTICLE_ID);
            slug = extras.getString(Article.ARTICLE_SLUG);
            title = extras.getString(Article.ARTICLE_TITLE);
            progress.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    buildArticle(extras);
                }
            }, 2000);
        } else {
            id = 0;
            slug = "";
            Toast.makeText(getBaseContext(), "Invalid article data", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("SetTextI18n")
    private void buildArticle(Bundle extras) {
        mArticleWrapper.setVisibility(View.GONE);
        Glide.with(getBaseContext())
                .load(extras.getString(Article.ARTICLE_FEATURED))
                .placeholder(R.drawable.placeholder_logo)
                .centerCrop()
                .crossFade()
                .into(mArticleFeatured);
        mArticleTitle.setText(extras.getString(Article.ARTICLE_TITLE));
        mArticleCategory.setText("ENTERTAINMENT");
        mArticleContributor.setText("Angga Ari Wijaya");
        mArticlePublished.setText("15 January 2016");
        mArticleContent.loadData(getString(R.string.large_text), "text/html", "UTF-8");
        mArticleExcerpt.setText("This is example of excerpt content with fancy data around the solid article text");
        mArticleDetail.setText("International  |  345X Views  |  3.4 Stars");
        mArticleRating.setRating(3);
        mArticleTags.setTags("Trending", "Super", "Modern", "Delicious", "Long Dress", "Awesome", "Gentle");

        Glide.with(getBaseContext())
                .load("http://infogue.id/images/contributors/avatar_1.jpg")
                .placeholder(R.drawable.placeholder_square)
                .centerCrop()
                .dontAnimate()
                .into(mContributorAvatar);
        mContributorName.setText("Angga Ari Wijaya");
        mContributorLocation.setText("Jakarta, Indonesia");
        mArticleWrapper.setVisibility(View.VISIBLE);

        progress.dismiss();
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
            sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareText(slug));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_send_to)));
        } else if (id == R.id.action_refresh) {
            Snackbar snackbar = Snackbar.make(mArticleContent, "Refreshing article data...", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(getResources().getColor(R.color.light));
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
            snackbar.show();
        } else if (id == R.id.action_browse) {
            String articleUrl = UrlHelper.getBrowseArticleUrl(slug);
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
