package com.sketchproject.infogue.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.MessageFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Message;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MessageActivity extends AppCompatActivity implements MessageFragment.OnMessageInteractionListener {
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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
                    MessageFragment fragment = (MessageFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    fragment.refreshMessageList(swipeRefreshLayout);
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

    @Override
    public void onMessageListClicked(Message message) {
        Log.i("Infogue/Message", message.getName());
    }

    @Override
    public void onDeleteMessage(final Message message) {
        Helper.createDialog(this,
                R.string.action_delete_message,
                R.string.message_delete_message,
                R.string.action_delete,
                R.string.action_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(message.getId(), message.getName());
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void deleteMessage(final int id, final String name) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.setMessage(getString(R.string.label_delete_article_progress));
        progress.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_MESSAGE + "/" + id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            Log.i("Infogue/Message", "[Delete] Success : " + message);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                MessageFragment fragment = (MessageFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                                fragment.deleteMessageRow(id);

                                String successMessage = "You have deleted conversation with \r\n\"" + name + "\"";
                                Helper.toastColor(MessageActivity.this, successMessage, R.color.color_warning_transparent);
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
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = getString(R.string.error_no_connection);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);

                                String status = response.optString(APIBuilder.RESPONSE_STATUS);
                                String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                                Log.e("Infogue/Message", "[Delete] Error : " + message);

                                if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                    errorMessage = getString(R.string.error_unauthorized);
                                } else if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.error_not_found);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = getString(R.string.error_server);
                                } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                    errorMessage = getString(R.string.error_maintenance);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = getString(R.string.error_parse_data);
                            }
                        }
                        Helper.toastColor(MessageActivity.this, errorMessage, R.color.color_danger_transparent);

                        progress.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(APIBuilder.METHOD, APIBuilder.METHOD_DELETE);
                params.put(Contributor.API_TOKEN, new SessionManager(MessageActivity.this).getSessionData(SessionManager.KEY_TOKEN, null));
                params.put(Contributor.FOREIGN, String.valueOf(new SessionManager(MessageActivity.this).getSessionData(SessionManager.KEY_ID, 0)));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_MEDIUM,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(MessageActivity.this).addToRequestQueue(postRequest);
    }
}
