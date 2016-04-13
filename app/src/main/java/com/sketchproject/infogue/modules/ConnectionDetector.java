package com.sketchproject.infogue.modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.sketchproject.infogue.R;

public class ConnectionDetector extends BroadcastReceiver {
    private Context mContext;
    private OnLostConnectionListener mLostListener;
    private OnConnectionEstablished mEstablishedListener;

    private Snackbar snackbar;

    public ConnectionDetector(Context context) {
        mContext = context;
    }

    /**
     * Check network is available or not
     *
     * @return boolean
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if (info != null && info.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    public void setLostConnectionListener(OnLostConnectionListener listener) {
        mLostListener = listener;
    }

    public void setEstablishedConnectionListener(OnConnectionEstablished listener) {
        mEstablishedListener = listener;
    }

    @SuppressWarnings("deprecation")
    public void snackbarDisconnectNotification(View view, View.OnClickListener callbackRetry) {
        buildSnackNotification(view, callbackRetry, "No Internet Connection!", "RETRY", Resources.getSystem().getColor(R.color.color_danger));
    }

    @SuppressWarnings("deprecation")
    public void snackbarDisconnectNotification(View view, View.OnClickListener callbackRetry, String message, String action) {
        buildSnackNotification(view, callbackRetry, message, action, Resources.getSystem().getColor(R.color.color_danger));
    }

    @SuppressWarnings("deprecation")
    public void snackbarConnectedNotification(View view, View.OnClickListener callbackRetry) {
        buildSnackNotification(view, callbackRetry, "Connection Established.", "OK", Resources.getSystem().getColor(R.color.color_success));
    }

    @SuppressWarnings("deprecation")
    public void snackbarConnectedNotification(View view, View.OnClickListener callbackRetry, String message, String action) {
        buildSnackNotification(view, callbackRetry, message, action, Resources.getSystem().getColor(R.color.color_success));
    }

    private void buildSnackNotification(View view, View.OnClickListener callbackRetry, String message, String action, int backgroundColor) {
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Resources.getSystem().getColor(R.color.light));
        if (callbackRetry != null) {
            snackbar.setAction(action, callbackRetry).show();
        }

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(backgroundColor);
    }

    public void dismissNotification() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    public void defaultToastNotification(Context context) {
        Toast toast = Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if (!isConnected && mLostListener != null) {
            mLostListener.onLostConnectionNotified(mContext);
        } else if (isConnected && mEstablishedListener != null) {
            mEstablishedListener.onConnectionEstablished(mContext);
        }
    }

    public interface OnLostConnectionListener {
        void onLostConnectionNotified(Context context);
    }

    public interface OnConnectionEstablished {
        void onConnectionEstablished(Context context);
    }
}
