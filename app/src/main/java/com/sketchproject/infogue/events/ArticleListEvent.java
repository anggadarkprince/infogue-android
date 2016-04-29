package com.sketchproject.infogue.events;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ArticleCreateActivity;
import com.sketchproject.infogue.activities.ArticleEditActivity;
import com.sketchproject.infogue.activities.PostActivity;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 10.37.
 */
public class ArticleListEvent {
    private Context context;
    private Article article;

    /**
     * Initialized Article event list
     *
     * @param context parent context
     * @param article model data of article
     */
    public ArticleListEvent(Context context, Article article) {
        Log.i("Infogue/Article", "ID : " + article.getId() + " Title : " + article.getTitle());
        this.context = context;
        this.article = article;
    }

    /**
     * Rate article silently, just give feedback if fail.
     */
    public void rateArticle() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_RATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Rate", "Success : Average rating for article id " + article.getId() + " is " + message);
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
                        String errorMessage = context.getString(R.string.error_unknown);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = context.getString(R.string.error_timeout);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.getString(APIBuilder.RESPONSE_STATUS);
                                String message = response.getString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Article", "Error : " + message);

                                if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = context.getString(R.string.error_not_found);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = context.getString(R.string.error_server);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = context.getString(R.string.error_maintenance);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        String rateMessage = errorMessage + "\r\nYour rating was discarded";
                        Helper.toastColor(context, rateMessage, ContextCompat.getColor(context, R.color.color_danger));
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

        VolleySingleton.getInstance(context).addToRequestQueue(postRequest);

        String successMessage = "Awesome!, you give 5 Stars on \n\r\"" + article.getTitle() + "\"";
        Helper.toastColor(context, successMessage, ContextCompat.getColor(context, R.color.primary));
    }

    /**
     * Share article link to another providers.
     */
    public void shareArticle() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, APIBuilder.getShareArticleText(article.getSlug()));
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.label_intent_share)));
    }

    /**
     * Open article url to browser.
     */
    public void browseArticle() {
        String articleUrl = APIBuilder.getArticleUrl(article.getSlug());
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
        context.startActivity(browserIntent);
    }

    /**
     * Launch post activity and passing article identity.
     */
    public void viewArticle() {
        Intent postIntent = new Intent(context, PostActivity.class);
        postIntent.putExtra(Article.ARTICLE_ID, article.getId());
        postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
        postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
        postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
        context.startActivity(postIntent);
    }

    /**
     * Launch EditArticle activity and passing some necessary extras data.
     */
    public void editArticle() {
        Intent editIntent = new Intent(context, ArticleEditActivity.class);
        editIntent.putExtra(Article.ARTICLE_ID, article.getId());
        editIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
        editIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
        editIntent.putExtra(ArticleCreateActivity.CALLED_FROM_MAIN, false);
        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).startActivityForResult(editIntent, ArticleCreateActivity.ARTICLE_FORM_CODE);
        } else {
            throwInstanceException();
        }
    }

    /**
     * Delete article data.
     */
    public void deleteArticle() {
        Helper.createDialog(context,
                R.string.action_long_delete,
                R.string.message_delete_article,
                R.string.action_delete,
                R.string.action_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performDelete();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Perform request delete to the server.
     */
    private void performDelete() {
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.setMessage(context.getString(R.string.label_delete_article_progress));
        progress.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_ARTICLE + "/" + article.getSlug(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            Log.i("Infogue/Article", "Success : " + message);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                if (context instanceof FragmentActivity) {
                                    FragmentActivity parent = ((FragmentActivity) context);
                                    ArticleFragment fragment = (ArticleFragment) parent.getSupportFragmentManager()
                                            .findFragmentById(R.id.fragment);
                                    fragment.deleteArticleRow(article.getId());

                                    String successMessage = "You have deleted article \r\n\"" + article.getTitle() + "\"";
                                    Helper.toastColor(context, successMessage,
                                            ContextCompat.getColor(context, R.color.color_warning));
                                } else {
                                    throwInstanceException();
                                }


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Helper.toastColor(context, context.getString(R.string.error_parse_data),
                                    ContextCompat.getColor(context, R.color.primary));
                        }

                        progress.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = context.getString(R.string.error_unknown);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = context.getString(R.string.error_timeout);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Article", "Error : " + message);

                                if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                    errorMessage = context.getString(R.string.error_unauthorized);
                                } else if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = context.getString(R.string.error_not_found);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = context.getString(R.string.error_server);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = context.getString(R.string.error_maintenance);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = context.getString(R.string.error_parse_data);
                            }
                        }
                        Helper.toastColor(context, errorMessage,
                                ContextCompat.getColor(context, R.color.color_danger));

                        progress.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(APIBuilder.METHOD, APIBuilder.METHOD_DELETE);
                params.put(Contributor.CONTRIBUTOR_API, new SessionManager(context).getSessionData(SessionManager.KEY_TOKEN, null));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(postRequest);
    }

    /**
     * @throws IllegalStateException
     */
    private void throwInstanceException() throws IllegalStateException {
        throw new IllegalStateException(context.getClass().getSimpleName() +
                " must extends FragmentActivity class.");
    }
}
