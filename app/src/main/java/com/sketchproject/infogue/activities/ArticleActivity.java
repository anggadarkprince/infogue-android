package com.sketchproject.infogue.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.IconizedMenu;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.Constant;

public class ArticleActivity extends AppCompatActivity implements ArticleFragment.OnArticleFragmentInteractionListener {
    private SessionManager session;

    private String username;

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
            username = extras.getString(SessionManager.KEY_USERNAME);
            if (session.isLoggedIn()) {
                if (session.getSessionData(SessionManager.KEY_USERNAME, "").equals(username)) {
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
        IconizedMenu popup = new IconizedMenu(new ContextThemeWrapper(view.getContext(), R.style.AppTheme_PopupOverlay), view);
        if (session.getSessionData(SessionManager.KEY_USERNAME, "").equals(username)) {
            popup.inflate(R.menu.post);
        }
        else{
            popup.inflate(R.menu.article);
        }

        popup.setGravity(Gravity.END);
        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_view) {
                    Toast.makeText(view.getContext(), "view " + article.getTitle(), Toast.LENGTH_LONG).show();
                } else if (id == R.id.action_publish) {
                    Toast.makeText(view.getContext(), "publish " + article.getTitle(), Toast.LENGTH_LONG).show();
                } else if (id == R.id.action_draft) {
                    Toast.makeText(view.getContext(), "draft " + article.getTitle(), Toast.LENGTH_LONG).show();
                } else if (id == R.id.action_edit) {
                    Toast.makeText(view.getContext(), "edit " + article.getTitle(), Toast.LENGTH_LONG).show();
                } else if (id == R.id.action_delete) {
                    Toast.makeText(view.getContext(), "delete " + article.getTitle(), Toast.LENGTH_LONG).show();
                }

                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onArticleLongClickInteraction(final View view, final Article article) {
        final CharSequence[] postItems = {
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse), getString(R.string.action_long_share),
                getString(R.string.action_long_publish),
                getString(R.string.action_long_draft),
                getString(R.string.action_long_edit),
                getString(R.string.action_long_delete)
        };

        final CharSequence[] articleItems = {
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
                getString(R.string.action_long_rate)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (session.getSessionData(SessionManager.KEY_USERNAME, "").equals(username)) {
            builder.setItems(postItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Toast.makeText(view.getContext(), postItems[item] + article.getTitle(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            builder.setItems(articleItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Toast.makeText(view.getContext(), articleItems[item] + article.getTitle(), Toast.LENGTH_LONG).show();
                }
            });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }
}
