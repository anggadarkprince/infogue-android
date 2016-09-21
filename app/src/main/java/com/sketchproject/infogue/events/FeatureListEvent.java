package com.sketchproject.infogue.events;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ApplicationActivity;
import com.sketchproject.infogue.models.Bank;
import com.sketchproject.infogue.models.Repositories.BankRepository;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Sketch Project Studio
 * Created by angga on 21/09/16.
 */
public class FeatureListEvent {
    private Context context;
    private BankRepository bankRepository;
    private Bank bank;

    public FeatureListEvent(Context context) {
        this.context = context;
        bankRepository = new BankRepository();
    }

    public boolean needDownloadBankData() {
        return bankRepository.isEmpty();
    }

    public void downloadBankData(final ProgressDialog progress) {
        progress.setMessage(context.getString(R.string.label_retrieve_feature_progress));
        progress.show();
        JsonObjectRequest menuRequest = new JsonObjectRequest(Request.Method.GET, APIBuilder.URL_API_BANK, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString(APIBuilder.RESPONSE_STATUS);
                            JSONArray banks = response.getJSONArray(Bank.TABLE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                bankRepository.clearData();

                                List<Bank> banksData = new ArrayList<>();

                                for (int i = 0; i < banks.length(); i++) {
                                    JSONObject bankObject = banks.getJSONObject(i);

                                    bank = new Bank();
                                    bank.setId(bankObject.getInt(Bank.ID));
                                    bank.setBank(bankObject.getString(Bank.BANK));
                                    bank.setLogo(bankObject.getString(Bank.LOGO));
                                    banksData.add(bank);
                                }
                                bankRepository.createAllData(banksData);
                            } else {
                                if (context instanceof ApplicationActivity) {
                                    ((ApplicationActivity) context).confirmRetry();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (context instanceof ApplicationActivity) {
                                ((ApplicationActivity) context).confirmRetry();
                            }
                        }
                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (context instanceof ApplicationActivity) {
                            ((ApplicationActivity) context).confirmRetry();
                        }
                    }
                }
        );
        menuRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_SHORT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(menuRequest);
    }
}
