package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ConversationFragment;
import com.sketchproject.infogue.fragments.SuggestionFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Message;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.ObjectPooling;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity implements SuggestionFragment.OnContributorInteractionListener {
    public static final String NEW_CONVERSATION = "new";
    public static final int NEW_CONVERSATION_CODE = 111;

    private Validator validator;
    private ConnectionDetector connectionDetector;
    private SessionManager sessionManager;
    private ObjectPooling objectPooling;

    private EditText receiverEdit;
    private ImageButton reselectButton;
    private CircleImageView avatarImage;

    private EditText chatMessage;
    private ImageButton buttonSend;
    private int userId;
    private boolean conversationUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        validator = new Validator();
        connectionDetector = new ConnectionDetector(ConversationActivity.this);
        sessionManager = new SessionManager(ConversationActivity.this);
        objectPooling = new ObjectPooling();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("New Conversation");
        }
        conversationUpdated = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean newConversation = extras.getBoolean(ConversationActivity.NEW_CONVERSATION);

            receiverEdit = (EditText) findViewById(R.id.username);
            reselectButton = (ImageButton) findViewById(R.id.reselect);
            avatarImage = (CircleImageView) findViewById(R.id.avatar);

            chatMessage = (EditText) findViewById(R.id.chat_message);
            chatMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (validator.isEmpty(s.toString())) {
                        buttonSend.setEnabled(false);
                        buttonSend.setBackgroundResource(R.drawable.circle_featured_inactive);
                    } else {
                        buttonSend.setEnabled(true);
                        buttonSend.setBackgroundResource(R.drawable.circle_featured_active);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            buttonSend = (ImageButton) findViewById(R.id.btn_send);
            buttonSend.setEnabled(false);
            buttonSend.setBackgroundResource(R.drawable.circle_featured_inactive);
            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message = chatMessage.getText().toString();
                    sendMessage(message, view);
                }
            });

            if (newConversation) {
                // setup chat box status
                chatMessage.setEnabled(false);
                chatMessage.setHint(R.string.prompt_receiver_empty);

                // add suggestion fragment
                final SuggestionFragment suggestionFragment;
                Object objectFragment = objectPooling.find(SuggestionFragment.class.getSimpleName());
                if (objectFragment == null) {
                    suggestionFragment = new SuggestionFragment();
                    objectPooling.pool(suggestionFragment, SuggestionFragment.class.getSimpleName());
                } else {
                    suggestionFragment = (SuggestionFragment) objectFragment;
                }
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, suggestionFragment);
                fragmentTransaction.commit();

                // setup remove receiver event and status
                reselectButton.setVisibility(View.GONE);
                reselectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reselectButton.setVisibility(View.GONE);
                        receiverEdit.setEnabled(true);

                        chatMessage.setEnabled(false);
                        chatMessage.setHint(R.string.prompt_receiver_empty);

                        // add suggestion fragment
                        final SuggestionFragment suggestionFragment;
                        Object objectFragment = objectPooling.find(SuggestionFragment.class.getSimpleName());
                        if (objectFragment == null) {
                            suggestionFragment = new SuggestionFragment();
                            objectPooling.pool(suggestionFragment, SuggestionFragment.class.getSimpleName());
                        } else {
                            suggestionFragment = (SuggestionFragment) objectFragment;
                        }
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, suggestionFragment);
                        fragmentTransaction.commit();

                        receiverEdit.setText("");
                        receiverEdit.requestFocus();
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("New Conversation");
                        }
                    }
                });

                // set receiver text edit status and event
                receiverEdit.setEnabled(true);
                receiverEdit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        suggestionFragment.fetchSuggestion(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            } else {
                // fetch info from extras
                userId = extras.getInt(Message.CONTRIBUTOR_ID);
                String username = extras.getString(Message.USERNAME);
                String name = extras.getString(Message.NAME);
                String avatar = extras.getString(Message.AVATAR);
                reselectButton.setVisibility(View.GONE);

                // setup title and user who interact with
                setupConversation(userId, username, name, avatar);
            }
        } else {
            Helper.toastColor(getBaseContext(), R.string.message_invalid_message, R.color.color_danger_transparent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (conversationUpdated) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(ConversationActivity.NEW_CONVERSATION, true);
            setResult(AppCompatActivity.RESULT_OK, returnIntent);
            Log.i("Infogue/Conversation", "Updated Return");
        }
        super.onBackPressed();
    }

    /**
     * @param message conversation chat
     * @param view    anchor parent for snackbar
     */
    private void sendMessage(final String message, View view) {
        if (connectionDetector.isNetworkAvailable()) {
            final ConversationFragment conversationFragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            conversationFragment.insertLoading();

            chatMessage.setEnabled(false);
            buttonSend.setEnabled(false);
            buttonSend.setBackgroundResource(R.drawable.circle_featured_inactive);
            StringRequest sendMessageRequest = new StringRequest(Request.Method.POST, APIBuilder.getApiMessageUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject result = new JSONObject(response);
                                String status = result.getString(APIBuilder.RESPONSE_STATUS);

                                if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                    conversationFragment.insertNewMessage(sessionManager, message);
                                    chatMessage.setText("");
                                    conversationUpdated = true;
                                } else {
                                    Log.w("Infogue", "[Conversation] " + getString(R.string.error_unknown));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                conversationFragment.removeLoading();
                            }
                            chatMessage.setEnabled(true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMessage = new Logger().networkRequestError(getBaseContext(), error, "Conversation");
                            String errMessage = errorMessage + "\r\nYour message was discarded";
                            Helper.toastColor(getBaseContext(), errMessage, R.color.color_danger);
                            chatMessage.setEnabled(true);
                            buttonSend.setEnabled(true);
                            buttonSend.setBackgroundResource(R.drawable.circle_featured_active);
                            conversationFragment.removeLoading();
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> messageParams = new HashMap<>();
                    messageParams.put(Contributor.API_TOKEN, sessionManager.getSessionData(SessionManager.KEY_TOKEN, null));
                    messageParams.put("contributor_id", String.valueOf(sessionManager.getSessionData(SessionManager.KEY_ID, 0)));
                    messageParams.put("receiver_id", String.valueOf(userId));
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

    @Override
    public void onContributorInteraction(View view, Contributor contributor) {
        reselectButton.setVisibility(View.VISIBLE);
        setupConversation(contributor.getId(),
                contributor.getUsername(),
                contributor.getName(),
                contributor.getAvatar());
    }

    private void setupConversation(int userId, String username, String name, String avatar) {
        this.userId = userId;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(username);
        }

        receiverEdit.setText(name);
        receiverEdit.setEnabled(false);
        Glide.with(getBaseContext())
                .load(avatar)
                .dontAnimate()
                .placeholder(R.drawable.placeholder_square)
                .into(avatarImage);

        // add conversation fragment
        ConversationFragment conversationFragment = ConversationFragment.newInstance(username);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, conversationFragment);
        fragmentTransaction.commit();

        chatMessage.setEnabled(true);
        chatMessage.setHint(R.string.prompt_message);
    }
}
