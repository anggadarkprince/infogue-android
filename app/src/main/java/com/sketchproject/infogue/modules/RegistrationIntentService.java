package com.sketchproject.infogue.modules;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.utils.APIBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sketch Project Studio
 * Created by Angga on 07/05/2016 14.56.
 */
public class RegistrationIntentService extends IntentService {
    private static final String[] TOPICS = {"article", "message"};
    private SessionManager session;

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        session = new SessionManager(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            InstanceID myID = InstanceID.getInstance(this);
            String registrationToken = myID.getToken(
                    getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null
            );
            Log.d("Registration Token", registrationToken);

            sendRegistrationToServer(registrationToken);

            // Subscribe to topic channels
            subscribeTopics(registrationToken);

            sharedPreferences.edit().putBoolean(SessionManager.SENT_TOKEN_TO_SERVER, true).apply();
            session.setSessionData(SessionManager.SENT_TOKEN_TO_SERVER, true);
            session.setSessionData(SessionManager.KEY_TOKEN_GCM, registrationToken);
        } catch (IOException e) {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(SessionManager.SENT_TOKEN_TO_SERVER, false).apply();
            session.setSessionData(SessionManager.SENT_TOKEN_TO_SERVER, false);
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(SessionManager.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final String token) {
        // Add custom implementation, as needed.
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_GCM_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Gcm", "[Gcm Register] Success registered " + message);
                            } else {
                                Log.w("Infogue/Gcm", "[Gcm Register] " + getBaseContext().getString(R.string.error_unknown));
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

                        String errorMessage = "[Gcm Register] Error : ";
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage += getBaseContext().getString(R.string.error_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage += getBaseContext().getString(R.string.error_no_connection);
                            } else {
                                errorMessage += getBaseContext().getString(R.string.error_unknown);
                            }
                        } else {
                            if (networkResponse.statusCode == 404) {
                                errorMessage = getBaseContext().getString(R.string.error_not_found);
                            } else if (networkResponse.statusCode == 500) {
                                errorMessage = getBaseContext().getString(R.string.error_server);
                            } else if (networkResponse.statusCode == 503) {
                                errorMessage = getBaseContext().getString(R.string.error_maintenance);
                            } else {
                                errorMessage += getBaseContext().getString(R.string.error_unknown);
                            }
                        }
                        Log.e("Infogue/Gcm", errorMessage);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(APIBuilder.METHOD, APIBuilder.METHOD_PUT);
                params.put(Contributor.GCM_TOKEN, token);
                params.put(Contributor.ID, String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_SHORT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
