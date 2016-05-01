package com.sketchproject.infogue.events;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ArticleCreateActivity;
import com.sketchproject.infogue.activities.ArticleEditActivity;
import com.sketchproject.infogue.activities.CommentActivity;
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
 * Article events list.
 * <p>
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
        this.context = context;
        this.article = article;
    }

    /**
     * Rate article silently, just give feedback if fail.
     */
    public void rateArticle(final int rate) {
        Log.i("Infogue/Article", "Rate article ID : " + article.getId() + " Title : " + article.getTitle() + " with " + rate + " Stars");
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_RATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Article", "[Rate] Success : Average rating for article id " + article.getId() + " is " + message);
                            } else {
                                Log.w("Infogue/Article", "[Rate] " + context.getString(R.string.error_unknown));
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

                        String errorMessage = context.getString(R.string.error_unknown);
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = context.getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = context.getString(R.string.error_no_connection);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.getString(APIBuilder.RESPONSE_STATUS);
                                String message = response.getString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Article", "[Rate] Error : " + message);

                                if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
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
                        String rateMessage = errorMessage + "\r\nYour rating was discarded";
                        Helper.toastColor(context, rateMessage, R.color.color_danger);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Article.FOREIGN, String.valueOf(article.getId()));
                params.put(Article.RATE, String.valueOf(rate));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_SHORT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(postRequest);

        String rateMessage;
        if (rate > 3) {
            rateMessage = "Awesome!, you give 5 Stars on \n\r\"" + article.getTitle() + "\"";
        } else {
            rateMessage = "Too bad!, you give under 3 Stars on \n\r\"" + article.getTitle() + "\"";
        }

        int color = R.color.color_hazard_transparent;
        if (rate == 5) {
            color = R.color.color_success_transparent;
        } else if (rate == 4) {
            color = R.color.color_info_transparent;
        } else if (rate == 3) {
            color = R.color.color_warning_transparent;
        } else if (rate == 2) {
            color = R.color.color_caution_transparent;
        }
        Helper.toastColor(context, rateMessage, color);
    }

    /**
     * Increment viewer article.
     */
    public void countViewer() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_HIT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Article", "[Hit] Current viewer article id : " + article.getId() + " is " + message);
                            } else {
                                Log.w("Infogue/Article", "[Hit] " + context.getString(R.string.error_unknown));
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

                        String errorMessage = "[Hit] Error : ";
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage += context.getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage += context.getString(R.string.error_no_connection);
                            } else {
                                errorMessage += context.getString(R.string.error_unknown);
                            }
                        } else {
                            if (networkResponse.statusCode == 404) {
                                errorMessage = context.getString(R.string.error_not_found);
                            } else if (networkResponse.statusCode == 500) {
                                errorMessage = context.getString(R.string.error_server);
                            } else if (networkResponse.statusCode == 503) {
                                errorMessage = context.getString(R.string.error_maintenance);
                            } else {
                                errorMessage += context.getString(R.string.error_unknown);
                            }
                        }
                        Log.e("Infogue/Hit", errorMessage);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Article.FOREIGN, String.valueOf(article.getId()));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_SHORT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(postRequest);
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

    public void leaveComment() {
        Intent commentIntent = new Intent(context, CommentActivity.class);
        commentIntent.putExtra(Article.ID, article.getId());
        commentIntent.putExtra(Article.SLUG, article.getSlug());
        commentIntent.putExtra(Article.TITLE, article.getTitle());
        context.startActivity(commentIntent);
    }

    /**
     * Launch post activity and passing article identity.
     */
    public void viewArticle() {
        Intent postIntent = new Intent(context, PostActivity.class);
        postIntent.putExtra(Article.ID, article.getId());
        postIntent.putExtra(Article.SLUG, article.getSlug());
        postIntent.putExtra(Article.FEATURED, article.getFeatured());
        postIntent.putExtra(Article.TITLE, article.getTitle());
        context.startActivity(postIntent);
    }

    /**
     * Launch EditArticle activity and passing some necessary extras data.
     */
    public void editArticle() {
        Intent editIntent = new Intent(context, ArticleEditActivity.class);
        editIntent.putExtra(Article.ID, article.getId());
        editIntent.putExtra(Article.SLUG, article.getSlug());
        editIntent.putExtra(Article.FEATURED, article.getFeatured());
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

                            Log.i("Infogue/Article", "[Delete] Success : " + message);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                if (context instanceof FragmentActivity) {
                                    FragmentActivity parent = ((FragmentActivity) context);
                                    ArticleFragment fragment = (ArticleFragment) parent.getSupportFragmentManager()
                                            .findFragmentById(R.id.fragment);
                                    fragment.deleteArticleRow(article.getId());

                                    String successMessage = "You have deleted article \r\n\"" + article.getTitle() + "\"";
                                    Helper.toastColor(context, successMessage, R.color.color_warning_transparent);
                                } else {
                                    throwInstanceException();
                                }
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
                        String errorMessage = context.getString(R.string.error_unknown);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = context.getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = context.getString(R.string.error_no_connection);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);

                                String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Article", "[Delete] Error : " + message);

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
                        Helper.toastColor(context, errorMessage, R.color.color_danger_transparent);

                        progress.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(APIBuilder.METHOD, APIBuilder.METHOD_DELETE);
                params.put(Contributor.API_TOKEN, new SessionManager(context).getSessionData(SessionManager.KEY_TOKEN, null));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_MEDIUM,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
