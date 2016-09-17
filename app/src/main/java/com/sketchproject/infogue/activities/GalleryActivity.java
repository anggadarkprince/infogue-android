package com.sketchproject.infogue.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.GalleryFragment;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Image;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleyMultipartRequest;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;
import com.sketchproject.infogue.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity implements GalleryFragment.OnImageInteractionListener {
    public static final int PICK_IMAGE = 1;
    public static final String CALLED_FROM_MAIN = "formMain";

    private ConnectionDetector connectionDetector;
    private SessionManager sessionManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private GalleryFragment galleryFragment;

    private boolean isCalledFromMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isCalledFromMain = extras.getBoolean(CALLED_FROM_MAIN, false);
        }

        connectionDetector = new ConnectionDetector(getBaseContext());
        sessionManager = new SessionManager(getBaseContext());
        galleryFragment = (GalleryFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    galleryFragment.refreshGallery(swipeRefreshLayout);
                }
            });
        }

        Button buttonUpload = (Button) findViewById(R.id.btn_upload);
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionDetector.isNetworkAvailable()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_image)), PICK_IMAGE);
                } else {
                    connectionDetector.snackbarDisconnectNotification(findViewById(R.id.btn_upload), null);
                }
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                byte[] image = Helper.getFileDataFromBitmap(bitmap);
                uploadImage(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Upload and passing image to server and return the image data,
     * then insert into adapter and update the gallery.
     *
     * @param image image which parsed to byte array
     */
    public void uploadImage(final byte[] image) {
        final ProgressDialog progress = new ProgressDialog(GalleryActivity.this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.setMessage(getString(R.string.label_upload_image_progress));
        progress.show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST, APIBuilder.URL_API_GALLERY_UPLOAD, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    JSONObject imageData = result.getJSONObject("image");
                    String status = result.getString(APIBuilder.RESPONSE_STATUS);
                    String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                    Log.i("Infogue/Image", message);

                    if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                        Image image = new Image();
                        image.setId(imageData.getInt(Image.ID));
                        image.setSource(imageData.getString(Image.SOURCE));
                        galleryFragment.insertImage(image);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                String errorMessage = new Logger().networkRequestError(getBaseContext(), error, "Image");
                Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Contributor.API_TOKEN, sessionManager.getSessionData(SessionManager.KEY_TOKEN, null));
                params.put(Contributor.FOREIGN, String.valueOf(sessionManager.getSessionData(SessionManager.KEY_ID, 0)));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put(Image.SOURCE, new DataPart("image.jpg", image, "image/jpeg"));
                return params;
            }
        };
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_LONG,
                APIBuilder.NO_RETRY,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }

    @Override
    public void onImageClicked(Image image) {
        if (isCalledFromMain) {

        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Image.SOURCE, image.getSource());
            setResult(AppCompatActivity.RESULT_OK, returnIntent);
            finish();
        }

    }

    @Override
    public void onDeleteImage(final Image image) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builder.setTitle(R.string.label_dialog_delete_image);
        builder.setMessage(R.string.message_delete_image);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (connectionDetector.isNetworkAvailable()) {
                    deleteImage(image.getId());
                } else {
                    connectionDetector.snackbarDisconnectNotification(findViewById(R.id.btn_upload), null);
                }
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog confirmationDialog = builder.create();
        confirmationDialog.show();
        Helper.setDialogButtonTheme(this, confirmationDialog);
    }

    /**
     * Delete image from server and remove the adapter immediately without waiting delete progress
     * and ignoring the status of the action.
     *
     * @param id image unique identity
     */
    public void deleteImage(final int id) {
        galleryFragment.deleteImage(id);
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIBuilder.URL_API_GALLERY_DELETE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString(APIBuilder.RESPONSE_STATUS);
                            String message = result.getString(APIBuilder.RESPONSE_MESSAGE);

                            if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Image", "[Delete] Success : " + message);
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
                        String errorMessage = new Logger().networkRequestError(getBaseContext(), error, "Image");
                        Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                SessionManager sessionManager = new SessionManager(getBaseContext());
                Map<String, String> params = new HashMap<>();
                params.put(APIBuilder.METHOD, APIBuilder.METHOD_DELETE);
                params.put(Contributor.API_TOKEN, sessionManager.getSessionData(SessionManager.KEY_TOKEN, null));
                params.put(Contributor.FOREIGN, String.valueOf(sessionManager.getSessionData(SessionManager.KEY_ID, 0)));
                params.put(Image.ID, String.valueOf(id));
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                APIBuilder.TIMEOUT_MEDIUM,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);
    }
}
