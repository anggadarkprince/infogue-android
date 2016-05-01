package com.sketchproject.infogue.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.CommentFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Comment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A {@link AppCompatActivity} subclass contain comment list.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 25/04/2016 10.37.
 */
public class CommentActivity extends AppCompatActivity implements CommentFragment.OnCommentInteractionListener {
    private SessionManager session;
    private ConnectionDetector connectionDetector;

    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog formCommentDialog;
    private EditText mCommentInput;
    private ProgressDialog progress;

    private int articleId;

    /**
     * Perform initialization of CommentActivity.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        connectionDetector = new ConnectionDetector(getBaseContext());
        session = new SessionManager(getBaseContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progress = new ProgressDialog(CommentActivity.this);
        progress.setMessage(getString(R.string.label_submit_comment));
        progress.setIndeterminate(true);

        @SuppressLint("InflateParams")
        View mFormComment = getLayoutInflater().inflate(R.layout.fragment_comment_form, null);
        mCommentInput = (EditText) mFormComment.findViewById(R.id.input_comment);

        Button mCommentButton = (Button) mFormComment.findViewById(R.id.btn_comment);
        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionDetector.isNetworkAvailable()) {
                    if (mCommentInput.getText().toString().trim().isEmpty()) {
                        Helper.toastColor(getBaseContext(), R.string.error_comment_required, R.color.color_danger_transparent);
                    } else {
                        submitComment();
                    }
                } else {
                    connectionDetector.snackbarDisconnectNotification(v, null);
                }
            }
        });

        Button mDismissButton = (Button) mFormComment.findViewById(R.id.btn_dismiss);
        mDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formCommentDialog.dismiss();
            }
        });

        CircleImageView avatar = (CircleImageView) mFormComment.findViewById(R.id.avatar);
        if (avatar != null) {
            Glide.with(getBaseContext())
                    .load(session.getSessionData(SessionManager.KEY_AVATAR, null))
                    .placeholder(R.drawable.placeholder_square)
                    .dontAnimate()
                    .into(avatar);
        }

        TextView name = (TextView) mFormComment.findViewById(R.id.name);
        if (name != null) {
            name.setText(session.getSessionData(SessionManager.KEY_NAME, ""));
        }

        TextView location = (TextView) mFormComment.findViewById(R.id.location);
        if (location != null) {
            location.setText(session.getSessionData(SessionManager.KEY_LOCATION, ""));
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
        builder.setTitle(R.string.prompt_leave_a_comment);
        builder.setView(mFormComment);
        formCommentDialog = builder.create();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (session.isLoggedIn()) {
                        launchCommentForm();
                    } else {
                        Intent authIntent = new Intent(getBaseContext(), AuthenticationActivity.class);
                        startActivity(authIntent);
                    }
                }
            });
        }

        ImageButton submitCommentButton = (ImageButton) findViewById(R.id.btn_submit_comment);
        if (submitCommentButton != null) {
            submitCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (session.isLoggedIn()) {
                        launchCommentForm();
                    } else {
                        Intent authIntent = new Intent(getBaseContext(), AuthenticationActivity.class);
                        startActivity(authIntent);
                    }
                }
            });
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            articleId = extras.getInt(Article.ID);
            String articleSlug = extras.getString(Article.SLUG);

            Fragment fragment = CommentFragment.newInstance(1, articleId, articleSlug);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        } else {
            Helper.toastColor(getBaseContext(), R.string.message_invalid_comment, R.color.color_danger);
            finish();
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    CommentFragment fragment = (CommentFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    fragment.refreshCommentList(swipeRefreshLayout);
                }
            });
        }
    }

    /**
     * Set swipe to refresh enable or disable, user could swipe when reach top.
     *
     * @param state enable or not
     */
    public void setSwipeEnable(boolean state) {
        swipeRefreshLayout.setEnabled(state);
    }

    /**
     * Perform submit comment request to server.
     */
    private void submitComment() {
        progress.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_COMMENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progress.dismiss();
                        formCommentDialog.dismiss();
                        mCommentInput.setText("");

                        swipeRefreshLayout.setRefreshing(true);
                        CommentFragment fragment = (CommentFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                        fragment.refreshCommentList(swipeRefreshLayout);

                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);

                            Log.e("Infogue/Comment", "[Submit] Success : " + status);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                Helper.toastColor(getBaseContext(), getString(R.string.message_comment_posted), R.color.color_success_transparent);
                            } else {
                                Log.w("Infogue/Comment", "[Submit] 200 Code : " + getString(R.string.error_unknown));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_unknown);
                        if (networkResponse == null) {
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

                                Log.e("Infogue/Comment", "[Submit] Error : " + message);

                                if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                    errorMessage = getString(R.string.error_unauthorized);
                                } else if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = message;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }
                        Helper.toastColor(getBaseContext(), errorMessage, R.color.color_danger_transparent);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Article.COMMENT, mCommentInput.getText().toString());
                params.put(Article.FOREIGN, String.valueOf(articleId));
                params.put(Contributor.FOREIGN, String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                params.put(Contributor.API_TOKEN, session.getSessionData(SessionManager.KEY_TOKEN, null));
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
    }

    /**
     * Show comment form
     */
    private void launchCommentForm() {
        formCommentDialog.show();
        Helper.setDialogButtonTheme(getBaseContext(), formCommentDialog);
    }

    /**
     * Create option menu, default info action related web app.
     *
     * @param menu being inflate
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    /**
     * Perform action when user select menu.
     *
     * @param item selected current menu
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_feedback) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_FEEDBACK));
            startActivity(browserIntent);
        } else if (id == R.id.action_help) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_HELP));
            startActivity(browserIntent);
        } else if (id == R.id.action_rating) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_APP));
            startActivity(browserIntent);
        } else if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(getBaseContext(), AboutActivity.class);
            startActivity(aboutActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Interaction listener when user click view holder of comment.
     *
     * @param comment model data
     */
    @Override
    public void onCommentListClicked(Comment comment) {
        // interact with comment list, for now do nothing! because we doesn't need to perform more further
    }
}
