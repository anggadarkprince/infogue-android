package com.sketchproject.infogue.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.IconizedMenu;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ArticleActivity extends AppCompatActivity implements
        ArticleFragment.OnArticleFragmentInteractionListener,
        ArticleFragment.OnArticleEditableFragmentInteractionListener,
        ConnectionDetector.OnLostConnectionListener,
        ConnectionDetector.OnConnectionEstablished {

    public static final String DISCARD_ARTICLE = "discard";
    public static final String SAVE_ARTICLE = "save";

    private SessionManager session;
    private ConnectionDetector connectionDetector;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progress;
    private int authorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        session = new SessionManager(getBaseContext());
        connectionDetector = new ConnectionDetector(getBaseContext());
        connectionDetector.setLostConnectionListener(this);
        connectionDetector.setEstablishedConnectionListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progress = new ProgressDialog(ArticleActivity.this);
        progress.setIndeterminate(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent createArticleIntent = new Intent(getBaseContext(), ArticleCreateActivity.class);
                    startActivityForResult(createArticleIntent, ArticleCreateActivity.CALL_ARTICLE_FORM_CODE);
                }
            });
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    ArticleFragment fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    fragment.refreshArticleList(swipeRefreshLayout);
                }
            });
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            authorId = extras.getInt(SessionManager.KEY_ID);
            String authorUsername = extras.getString(SessionManager.KEY_USERNAME);
            String query = extras.getString(SearchActivity.QUERY_STRING);

            if (getSupportActionBar() != null) {
                if (query != null) {
                    getSupportActionBar().setTitle("All result for " + query);
                }
            }

            // this extras is sent from Article create/edit form activity
            boolean saveResult = extras.getBoolean(ArticleActivity.SAVE_ARTICLE);
            boolean isCalledFromMain = extras.getBoolean(ArticleCreateActivity.CALLED_FROM_MAIN);
            int resultCode = extras.getInt(ArticleCreateActivity.RESULT_CODE);

            if (isCalledFromMain) {
                handleResult(resultCode, saveResult);
            }

            Boolean isMyArticle = false;
            if (session.isLoggedIn()) {
                if (session.getSessionData(SessionManager.KEY_ID, 0) == authorId) {
                    if (fab != null) {
                        fab.setVisibility(View.VISIBLE);
                    }
                    isMyArticle = true;
                }
            }

            Fragment fragment = ArticleFragment.newInstanceAuthor(1, authorId, authorUsername, isMyArticle, query);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        }
    }

    public void setSwipeEnable(boolean state) {
        swipeRefreshLayout.setEnabled(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ArticleCreateActivity.CALL_ARTICLE_FORM_CODE) {
            boolean saveResult = data.getBooleanExtra(SAVE_ARTICLE, false);
            handleResult(resultCode, saveResult);
        }
    }

    @SuppressWarnings("deprecation")
    private void handleResult(int resultCode, boolean saveResult) {
        final Snackbar snackbar = Snackbar.make(swipeRefreshLayout, "Article successfully saved!", Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(getResources().getColor(R.color.light));
        snackbar.setAction(R.string.action_ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        View snackbarView = snackbar.getView();

        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (saveResult) {
                snackbarView.setBackgroundResource(R.color.color_success);
                swipeRefreshLayout.setRefreshing(true);
                ArticleFragment fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                fragment.refreshArticleList(swipeRefreshLayout);
            } else {
                snackbar.setText(R.string.error_server);
                snackbarView.setBackgroundResource(R.color.color_danger);
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            snackbarView.setBackgroundResource(R.color.color_warning);
            snackbar.setText("Article is discarded!");
        } else {
            snackbarView.setBackgroundResource(R.color.color_danger);
            snackbar.setText("Invalid Article Result!");
        }

        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    private void viewArticle(Article article) {
        if (connectionDetector.isNetworkAvailable()) {
            Log.i("INFOGUE/Article", article.getId() + " " + article.getSlug() + " " + article.getTitle());
            Intent postIntent = new Intent(getBaseContext(), PostActivity.class);
            postIntent.putExtra(Article.ARTICLE_ID, article.getId());
            postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
            postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
            postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
            startActivity(postIntent);
            connectionDetector.dismissNotification();
        } else {
            onLostConnectionNotified(getBaseContext());
        }
    }

    private void browseArticle(Article article) {
        String articleUrl = UrlHelper.getArticleUrl(article.getSlug());
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
        startActivity(browserIntent);
    }

    private void shareArticle(Article article) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareArticleText(article.getSlug()));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
    }

    private void rateArticle(final Article article) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_RATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(Constant.RESPONSE_STATUS);
                            String message = result.getString(Constant.RESPONSE_MESSAGE);

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Rate", "Success::Average rating for article id " + article.getId() + " is " + message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                                String status = response.getString(Constant.RESPONSE_STATUS);
                                String message = response.getString(Constant.RESPONSE_MESSAGE);

                                Log.i("Infogue/Article", "Error::" + message);

                                if (status.equals(Constant.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        AppHelper.toastColored(getBaseContext(), errorMessage + "\r\nYour rating was discarded", ContextCompat.getColor(getBaseContext(), R.color.color_danger));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Article.ARTICLE_FOREIGN, String.valueOf(article.getId()));
                params.put(Article.ARTICLE_RATE, String.valueOf(5));
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);

        String successMessage = "Awesome!, you give 5 Stars on \n\r\"" + article.getTitle() + "\"";
        AppHelper.toastColored(getBaseContext(), successMessage, ContextCompat.getColor(getBaseContext(), R.color.primary));
    }

    private void editArticle(Article article) {
        Intent editIntent = new Intent(getBaseContext(), ArticleEditActivity.class);
        editIntent.putExtra(Article.ARTICLE_ID, article.getId());
        editIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
        startActivityForResult(editIntent, ArticleCreateActivity.CALL_ARTICLE_FORM_CODE);
    }

    private void deleteArticle(View view, final Article article) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.AppTheme_NoActionBar));
        builder.setTitle(R.string.action_long_delete);
        builder.setMessage(getString(R.string.message_delete_confirm) + " \"" + article.getTitle() + "\"?");
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteArticleRequest(article);
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialogDelete = builder.create();
        dialogDelete.show();
        AppHelper.dialogButtonTheme(getBaseContext(), dialogDelete);
    }

    private void deleteArticleRequest(final Article article) {
        progress.setMessage(getString(R.string.label_delete_article_progress));
        progress.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_ARTICLE + "/" + article.getSlug(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(Constant.RESPONSE_STATUS);
                            String message = result.getString(Constant.RESPONSE_MESSAGE);

                            Log.i("Infogue/Article", "Success::" + message);

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                ArticleFragment fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                                fragment.deleteArticleRow(article.getId());

                                String successMessage = "You have deleted article \r\n\"" + article.getTitle() + "\"";
                                AppHelper.toastColored(getBaseContext(), successMessage, ContextCompat.getColor(getBaseContext(), R.color.color_warning));
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
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.getString(Constant.RESPONSE_STATUS);
                                String message = response.getString(Constant.RESPONSE_MESSAGE);

                                Log.i("Infogue/Article", "Error::" + message);

                                if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                    errorMessage = message + ", please login again!";
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
                        AppHelper.toastColored(getBaseContext(), errorMessage, ContextCompat.getColor(getBaseContext(), R.color.color_danger));

                        progress.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("_method", "delete");
                params.put(Contributor.CONTRIBUTOR_API, session.getSessionData(SessionManager.KEY_TOKEN, null));
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
    }

    @Override
    public void onArticleFragmentInteraction(View view, Article article) {
        viewArticle(article);
    }

    @Override
    public void onArticlePopupInteraction(final View view, final Article article) {
        IconizedMenu popup = new IconizedMenu(new ContextThemeWrapper(view.getContext(), R.style.AppTheme_PopupOverlay), view);
        if (session.getSessionData(SessionManager.KEY_ID, 0) == authorId) {
            popup.inflate(R.menu.post);
        } else {
            popup.inflate(R.menu.article);
        }

        popup.setGravity(Gravity.END);
        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (connectionDetector.isNetworkAvailable()) {
                    if (id == R.id.action_view) {
                        viewArticle(article);
                    } else if (id == R.id.action_browse) {
                        browseArticle(article);
                    } else if (id == R.id.action_share) {
                        shareArticle(article);
                    } else if (id == R.id.action_rate) {
                        rateArticle(article);
                    } else if (id == R.id.action_edit) {
                        editArticle(article);
                    } else if (id == R.id.action_delete) {
                        deleteArticle(view, article);
                    }

                    connectionDetector.dismissNotification();
                } else {
                    onLostConnectionNotified(getBaseContext());
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
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
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
        if (session.getSessionData(SessionManager.KEY_ID, 0) == authorId) {
            builder.setItems(postItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (postItems[item].toString().equals(getString(R.string.action_long_open))) {
                        viewArticle(article);
                    } else if (postItems[item].toString().equals(getString(R.string.action_long_browse))) {
                        browseArticle(article);
                    } else if (postItems[item].toString().equals(getString(R.string.action_long_share))) {
                        shareArticle(article);
                    } else if (postItems[item].toString().equals(getString(R.string.action_long_edit))) {
                        editArticle(article);
                    } else if (postItems[item].toString().equals(getString(R.string.action_long_delete))) {
                        deleteArticle(view, article);
                    }
                }
            });
        } else {
            builder.setItems(articleItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (articleItems[item].toString().equals(getString(R.string.action_long_open))) {
                        viewArticle(article);
                    } else if (articleItems[item].toString().equals(getString(R.string.action_long_browse))) {
                        browseArticle(article);
                    } else if (articleItems[item].toString().equals(getString(R.string.action_long_share))) {
                        shareArticle(article);
                    } else if (articleItems[item].toString().equals(getString(R.string.action_long_rate))) {
                        rateArticle(article);
                    }
                }
            });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBrowseClicked(View view, Article article) {
        browseArticle(article);
    }

    @Override
    public void onShareClicked(View view, Article article) {
        shareArticle(article);
    }

    @Override
    public void onEditClicked(View view, Article article) {
        editArticle(article);
    }

    @Override
    public void onDeleteClicked(View view, Article article) {
        deleteArticle(view, article);
    }

    @Override
    public void onLostConnectionNotified(Context context) {
        connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();

                if (!connectionDetector.isNetworkAvailable()) {
                    connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onLostConnectionNotified(getBaseContext());
                        }
                    }, Constant.jokes[(int) Math.floor(Math.random() * Constant.jokes.length)] + " stole my internet T_T", getString(R.string.action_retry));
                } else {
                    connectionDetector.snackbarConnectedNotification(findViewById(android.R.id.content), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectionDetector.dismissNotification();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onConnectionEstablished(Context context) {
        connectionDetector.snackbarConnectedNotification(findViewById(android.R.id.content), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();
            }
        });
    }
}
