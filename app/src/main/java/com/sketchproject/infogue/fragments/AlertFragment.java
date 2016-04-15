package com.sketchproject.infogue.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sketchproject.infogue.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlertFragment extends Fragment {
    public static final int ALERT_DEFAULT = 0;
    public static final int ALERT_SUCCESS = 1;
    public static final int ALERT_WARNING = 2;
    public static final int ALERT_INFO = 3;
    public static final int ALERT_DANGER = 4;

    private RelativeLayout mAlertContainer;
    private ImageView mAlertIcon;
    private TextView mAlertTitle;
    private TextView mAlertMessage;
    private ImageButton mAlertDismiss;

    public AlertFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAlertContainer = (RelativeLayout) getView();
        if (mAlertContainer != null) {
            mAlertIcon = (ImageView) mAlertContainer.findViewById(R.id.alert_icon);
        }
        if (mAlertContainer != null) {
            mAlertTitle = (TextView) mAlertContainer.findViewById(R.id.alert_title);
        }
        if (mAlertContainer != null) {
            mAlertMessage = (TextView) mAlertContainer.findViewById(R.id.alert_message);
        }
        if (mAlertContainer != null) {
            mAlertDismiss = (ImageButton) mAlertContainer.findViewById(R.id.alert_dismiss);
            mAlertDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        setAlertType(ALERT_DEFAULT);
        if (mAlertContainer != null) {
            mAlertContainer.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unused")
    public void setOnDismissListener(View.OnClickListener listener) {
        mAlertDismiss.setOnClickListener(listener);
    }

    public void dismiss() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mAlertContainer.setVisibility(View.GONE);
        mAlertContainer.animate()
                .setDuration(shortAnimTime)
                .alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAlertContainer.setVisibility(View.GONE);
                    }
                });
    }

    public void show() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        final View alert = getView();
        if (alert != null) {
            alert.setVisibility(View.VISIBLE);
            alert.animate()
                    .setDuration(shortAnimTime)
                    .alpha(1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            alert.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public void setAlertTitle(String title) {
        mAlertTitle.setText(title);
    }

    public void setAlertMessage(String message) {
        mAlertMessage.setText(message);
    }

    public void setAlertMessage(List<String> messages) {
        String allMessages = "";
        for (int i = 0; i < messages.size(); i++) {
            allMessages += "• " + messages.get(i);
            if (messages.size() > 1 && i < messages.size() - 1) {
                allMessages += "\n";
            }
        }
        setAlertMessage(allMessages);
    }

    @SuppressLint("SetTextI18n")
    public void setAlertType(int type) {
        switch (type) {
            case ALERT_SUCCESS:
                mAlertContainer.setBackgroundResource(R.color.color_success);
                mAlertIcon.setImageResource(R.drawable.ic_check);
                mAlertTitle.setText("Action was perform successfully!");
                mAlertMessage.setText("Success message appear here...");
                break;
            case ALERT_INFO:
                mAlertContainer.setBackgroundResource(R.color.color_info);
                mAlertIcon.setImageResource(R.drawable.ic_info);
                mAlertTitle.setText("Hey, take a look!");
                mAlertMessage.setText("Info message appear here...");
                break;
            case ALERT_WARNING:
                mAlertContainer.setBackgroundResource(R.color.color_warning);
                mAlertIcon.setImageResource(R.drawable.ic_warning);
                mAlertTitle.setText("Caution, look at this!");
                mAlertMessage.setText("Warning message appear here...");
                break;
            case ALERT_DANGER:
                mAlertContainer.setBackgroundResource(R.color.color_danger);
                mAlertIcon.setImageResource(R.drawable.ic_error);
                mAlertTitle.setText("Whops, something is getting wrong!");
                mAlertMessage.setText("Error message appear here...");
                break;
            default:
                mAlertContainer.setBackgroundResource(R.color.primary);
                mAlertIcon.setImageResource(R.drawable.ic_whatshot);
                mAlertTitle.setText("Alert Default!");
                mAlertMessage.setText("This is an alert message...");
                break;
        }
    }
}
