package com.sketchproject.infogue.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ConversationFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Message;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.Validator;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;
import com.sketchproject.infogue.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ConversationActivity extends AppCompatActivity {
    private Validator validator;
    private ConnectionDetector connectionDetector;
    private SessionManager sessionManager;
    private EditText chatMessage;
    private FloatingActionButton buttonSend;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        validator = new Validator();
        connectionDetector = new ConnectionDetector(ConversationActivity.this);
        sessionManager = new SessionManager(ConversationActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String username = extras.getString(Message.USERNAME);
            id = extras.getInt(Message.CONTRIBUTOR_ID);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(username);
            }

            // add fragment
            Fragment fragment = ConversationFragment.newInstance(username);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        } else {
            Helper.toastColor(getBaseContext(), R.string.message_invalid_message, R.color.color_danger_transparent);
            finish();
        }

        chatMessage = (EditText) findViewById(R.id.chat_message);
        buttonSend = (FloatingActionButton) findViewById(R.id.btn_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = chatMessage.getText().toString();
                if (!validator.isEmpty(message)) {
                    chatMessage.setEnabled(false);
                    buttonSend.setEnabled(false);
                    sendMessage(message, view);
                } else {
                    final Snackbar snackbar = Snackbar.make(view, getString(R.string.error_message_required), Snackbar.LENGTH_SHORT);
                    snackbar.getView().setBackgroundResource(R.color.color_danger);
                    snackbar.setActionTextColor(ContextCompat.getColor(getBaseContext(), R.color.light));
                    snackbar.setAction(getString(R.string.action_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

    /**
     * @param message conversation chat
     * @param view    anchor parent for snackbar
     */
    private void sendMessage(final String message, View view) {
        if (connectionDetector.isNetworkAvailable()) {
            StringRequest sendMessageRequest = new StringRequest(Request.Method.POST, APIBuilder.getApiMessageUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject result = new JSONObject(response);
                                String status = result.getString(APIBuilder.RESPONSE_STATUS);

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    ConversationFragment conversationFragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                                    conversationFragment.insertNewMessage(sessionManager, message);
                                    chatMessage.setText("");
                                } else {
                                    Log.w("Infogue", "[Conversation] " + getString(R.string.error_unknown));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            chatMessage.setEnabled(true);
                            buttonSend.setEnabled(true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMessage = new Logger().networkRequestError(getBaseContext(), error, "Conversation");
                            String rateMessage = errorMessage + "\r\nYour message was discarded";
                            Helper.toastColor(getBaseContext(), rateMessage, R.color.color_danger);
                            chatMessage.setEnabled(true);
                            buttonSend.setEnabled(true);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> messageParams = new HashMap<>();
                    messageParams.put(Contributor.API_TOKEN, sessionManager.getSessionData(SessionManager.KEY_TOKEN, null));
                    messageParams.put("contributor_id", String.valueOf(sessionManager.getSessionData(SessionManager.KEY_ID, 0)));
                    messageParams.put("receiver_id", String.valueOf(id));
                    messageParams.put("message", message);
                    return messageParams;
                }
            };

            sendMessageRequest.setRetryPolicy(new DefaultRetryPolicy(
                    APIBuilder.TIMEOUT_MEDIUM,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(sendMessageRequest);
        } else {
            connectionDetector.snackbarDisconnectNotification(view, null);
        }
    }
}
