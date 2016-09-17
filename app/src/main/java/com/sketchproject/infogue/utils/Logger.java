package com.sketchproject.infogue.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.sketchproject.infogue.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sketch Project Studio
 * Created by angga on 13/09/16.
 */
public class Logger {
    public String networkRequestError(Context context, VolleyError error) {
        return networkRequestError(context, error, String.valueOf(error.networkResponse.statusCode));
    }

    public String networkRequestError(Context context, VolleyError error, String label) {
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

                Log.e("Infogue", "[" + label + "] Error : " + message);

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

        return errorMessage;
    }
}
