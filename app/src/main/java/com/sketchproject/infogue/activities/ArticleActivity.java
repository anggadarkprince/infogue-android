package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.dummy.DummyArticleContent;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.Constant;

public class ArticleActivity extends AppCompatActivity implements ArticleFragment.OnArticleFragmentInteractionListener {
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        session = new SessionManager(getBaseContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createArticleIntent = new Intent(getBaseContext(), ArticleCreateActivity.class);
                startActivity(createArticleIntent);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String username = extras.getString(SessionManager.KEY_USERNAME);
            if(session.isLoggedIn()){
                if(session.getSessionData(SessionManager.KEY_USERNAME, "").equals(username)){
                    fab.setVisibility(View.VISIBLE);
                }
            }

            Fragment fragment = ArticleFragment.newInstance(username);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_feedback) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_FEEDBACK));
            startActivity(browserIntent);
        } else if (id == R.id.action_help) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_HELP));
            startActivity(browserIntent);
        } else if (id == R.id.action_rating) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_APP));
            startActivity(browserIntent);
        } else if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(getBaseContext(), AboutActivity.class);
            startActivity(aboutActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArticleFragmentInteraction(View view, Article article) {
        Log.i("INFOGUE/Article", article.getId() + " " + article.getSlug() + " " + article.getTitle());
    }

    @Override
    public void onArticlePopupInteraction(final View view, final Article article) {
        PopupMenu popup = new PopupMenu(new ContextThemeWrapper(view.getContext(), R.style.AppTheme_PopupOverlay), view);
        popup.inflate(R.menu.article);
        popup.setGravity(Gravity.END);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_view) {
                    Toast.makeText(view.getContext(), "view " + article.getTitle(), Toast.LENGTH_LONG).show();
                } else if (id == R.id.action_browse) {
                    Toast.makeText(view.getContext(), "browse " + article.getTitle(), Toast.LENGTH_LONG).show();
                } else if (id == R.id.action_share) {
                    Toast.makeText(view.getContext(), "share " + article.getTitle(), Toast.LENGTH_LONG).show();
                } else if (id == R.id.action_rate) {
                    Toast.makeText(view.getContext(), "rate 5 " + article.getTitle(), Toast.LENGTH_LONG).show();
                }

                return false;
            }
        });
        popup.show();
    }
}
