package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.AppHelper;

import java.util.ArrayList;
import java.util.List;

public class ArticleEditActivity extends ArticleCreateActivity {
    private int articleId;
    private String articleSlug;

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
        } else {
            AppHelper.toastColored(getBaseContext(), "Invalid article!", Color.parseColor("#ddd9534f"));
            finish();
        }

        demoOnly();

        mSaveButton.setText(R.string.action_update_article);
    }

    protected void saveData() {
        if (connectionDetector.isNetworkAvailable()) {
            progress.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress.dismiss();

                    if (isCalledFromMainActivity) {
                        Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                        articleIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                        articleIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));
                        // add some information for notification
                        articleIntent.putExtra(ArticleActivity.SAVE_ARTICLE, Math.random() < 0.5);
                        articleIntent.putExtra(CALLED_FROM_MAIN, isCalledFromMainActivity);
                        articleIntent.putExtra(RESULT_CODE, AppCompatActivity.RESULT_OK);

                        startActivity(articleIntent);
                    } else {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(ArticleActivity.SAVE_ARTICLE, Math.random() < 0.5);
                        returnIntent.putExtra(CALLED_FROM_MAIN, isCalledFromMainActivity);
                        setResult(AppCompatActivity.RESULT_OK, returnIntent);
                    }

                    finish();
                }
            }, 2000);
        } else {
            connectionDetector.snackbarDisconnectNotification(mSelectButton, null);
        }
    }

    private void demoOnly() {
        List<String> tags = new ArrayList<>();
        tags.add("sunday");
        tags.add("holiday");
        tags.add("spirit");
        tags.add("smile");
        tags.add("welcome home");
        tags.add("new album");
        tags.add("music2016");

        article = new Article(articleId, articleSlug);
        article.setTitle("Hari ini indah sekali");
        article.setSlug("hari-ini-indah-sekali");
        article.setCategory("Entertainment");
        article.setCategoryId(1);
        article.setSubcategory("Music");
        article.setSubcategoryId(2);
        article.setAuthorId(session.getSessionData(SessionManager.KEY_ID, 0));
        article.setContent(getString(R.string.large_text));
        article.setTags(tags);
        article.setFeatured("http://infogue.id/images/featured/featured_1.jpg");
        article.setExcerpt("This is simple excerpt");

        mTitleInput.setText(article.getTitle());
        mSlugInput.setText(article.getSlug());
        mCategorySpinner.setSelection(article.getCategoryId());
        mSubcategorySpinner.setSelection(article.getSubcategoryId());
        mContentEditor.setHtml(article.getContent());
        mTagsInput.setTags(article.getTags());
        mExcerptInput.setText(article.getExcerpt());
        mFeaturedImage.setVisibility(View.VISIBLE);
        Glide.with(getBaseContext())
                .load(article.getFeatured())
                .placeholder(R.drawable.placeholder_logo_wide)
                .crossFade()
                .into(mFeaturedImage);
    }
}
