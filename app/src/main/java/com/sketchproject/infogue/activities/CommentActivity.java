package com.sketchproject.infogue.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

public class CommentActivity extends AppCompatActivity {

    private ProgressDialog progress;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progress = new ProgressDialog(this);
        progress.setMessage("Loading Article Data");
        progress.setIndeterminate(true);
        progress.show();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt(Article.ARTICLE_ID);
            String slug = extras.getString(Article.ARTICLE_SLUG);
            String title = extras.getString(Article.ARTICLE_TITLE);
            final String urlDisqus = UrlHelper.getDisqusUrl(id, slug, title, Constant.SHORT_NAME);

            final String[] patterns = {"disqus.com/next/login-success", "disqus.com/_ax/google/complete", "disqus.com/_ax/twitter/complete", "disqus.com/_ax/facebook/complete"};

            final WebView webDisqus = (WebView) findViewById(R.id.disqus_comment);
            WebSettings webSettings = webDisqus.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(false);
            webSettings.setDisplayZoomControls(false);
            webDisqus.requestFocusFromTouch();
            webDisqus.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Log.i("INFOGUE/Comment", url);
                    for (String pattern : patterns) {
                        if (url.matches("^" + pattern)) {
                            webDisqus.loadUrl(urlDisqus);
                        }
                    }
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progress.dismiss();
                }
            });
            webDisqus.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress == 100) {
                        progress.dismiss();
                        Log.i("INFOGUE/Comment", view.getUrl());
                        for (String pattern : patterns) {
                            if (view.getUrl().matches("^" + pattern)) {
                                webDisqus.loadUrl(urlDisqus);
                            }
                        }
                    }
                }
            });
            webDisqus.loadUrl(urlDisqus);
        } else {
            Toast.makeText(getBaseContext(), "Invalid comment data", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public String getHtmlComment(int idPost, int idSlug, int idTitle, String shortName) {

        return "<div id='disqus_thread'></div>"
                + "<script type='text/javascript'>"
                + "var disqus_identifier = '"
                + idPost
                + "';"
                + "var disqus_shortname = '"
                + shortName
                + "';"
                + " (function() { var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;"
                + "dsq.src = 'http://' + disqus_shortname + '.disqus.com/embed.js';"
                + "(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq); })();"
                + "</script>";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
