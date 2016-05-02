package com.sketchproject.infogue.modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.sketchproject.infogue.R;

/**
 * Handle detect connection status.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 10/04/2016 18.17.
 */
@SuppressWarnings("unused")
public class ConnectionDetector extends BroadcastReceiver {
    private OnLostConnectionListener mLostListener;
    private OnConnectionEstablished mEstablishedListener;
    private Context mContext;
    private Snackbar snackbar;
    private Toast toast;

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

    /**
     * Listener triggered when connection lost via broadcast.
     *
     * @param listener on connection lost listener
     */
    public void setLostConnectionListener(OnLostConnectionListener listener) {
        mLostListener = listener;
    }

    /**
     * Listener triggered when connection established via broadcast.
     *
     * @param listener on connection established
     */
    public void setEstablishedConnectionListener(OnConnectionEstablished listener) {
        mEstablishedListener = listener;
    }

    /**
     * Create snackbar no internet notification.
     *
     * @param view          anchor parent snackbar
     * @param callbackRetry callback handler when action triggered
     */
    public void snackbarDisconnectNotification(View view, View.OnClickListener callbackRetry) {
        buildSnackNotification(view, callbackRetry,
                mContext.getString(R.string.message_no_internet),
                mContext.getString(R.string.action_retry),
                R.color.color_danger, Snackbar.LENGTH_INDEFINITE);
    }

    /**
     * Create snackbar internet connect and established.
     *
     * @param view          anchor parent snackbar
     * @param callbackRetry callback handler when action triggered
     */
    public void snackbarConnectedNotification(View view, View.OnClickListener callbackRetry) {
        buildSnackNotification(view, callbackRetry,
                mContext.getString(R.string.message_internet_established),
                mContext.getString(R.string.action_ok),
                R.color.color_success, Snackbar.LENGTH_LONG);
    }

    /**
     * Build and show snackbar immediately.
     *
     * @param view            anchor parent snackbar
     * @param callbackRetry   callback handler when action triggered
     * @param message         content message text
     * @param action          action button text
     * @param backgroundColor resource color background
     * @param duration        duration show notification
     */
    private void buildSnackNotification(View view, View.OnClickListener callbackRetry, String message, String action, int backgroundColor, int duration) {
        snackbar = Snackbar.make(view, message, duration);
        snackbar.setActionTextColor(ContextCompat.getColor(mContext, R.color.light));
        if (callbackRetry != null) {
            snackbar.setAction(action, callbackRetry);
        } else {
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
        }
        snackbar.show();

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundResource(backgroundColor);
    }

    /**
     * Dismiss all notifications if exist (snackbar and toast).
     */
    public void dismissNotification() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
        if (toast != null) {
            toast.cancel();
        }
    }

    /**
     * Prefer toast notification when lost connection.
     *
     * @param context parent context
     */
    public void toastConnectedNotification(Context context) {
        toast = Toast.makeText(context, R.string.message_no_internet, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Receive broadcast information when lost connection and passing into custom listener.
     *
     * @param context parent context
     * @param intent  receive intent data
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if (!isConnected && mLostListener != null) {
            mLostListener.onLostConnectionNotified(mContext);
        } else if (isConnected && mEstablishedListener != null) {
            mEstablishedListener.onConnectionEstablished(mContext);
        }
    }

    /**
     * Interface handle lost connection event.
     */
    public interface OnLostConnectionListener {
        void onLostConnectionNotified(Context context);
    }

    /**
     * Interface handle established connection event.
     */
    public interface OnConnectionEstablished {
        void onConnectionEstablished(Context context);
    }
}
