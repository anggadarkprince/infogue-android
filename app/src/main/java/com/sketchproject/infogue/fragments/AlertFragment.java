package com.sketchproject.infogue.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
 * A simple {@link Fragment} subclass. show alert by categorizing to danger, warning, success
 * and info type and messages.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 21/04/2016 19.09.
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

    /**
     * Default constructor
     */
    public AlertFragment() {
        // Required empty public constructor
    }

    /**
     * Called after onCreate() and before onCreated()
     *
     * @param inflater           The LayoutInflater object that can be used to inflate view
     * @param container          If non-null, this is the parent view that the fragment's attached
     * @param savedInstanceState If non-null, this fragment is being re-constructed from previous
     * @return return the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    /**
     * Method when parent activity was created.
     *
     * @param savedInstanceState saved last state
     */
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

    /**
     * Add custom listener when user click button close.
     *
     * @param listener close button listener
     */
    @SuppressWarnings("unused")
    public void setOnDismissListener(View.OnClickListener listener) {
        mAlertDismiss.setOnClickListener(listener);
    }

    /**
     * Fadeout and hide alert view.
     */
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

    /**
     * Animate and show alert view.
     */
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

    /**
     * Custom alert title.
     *
     * @param title string title
     */
    public void setAlertTitle(String title) {
        mAlertTitle.setText(title);
    }

    /**
     * Display single line message.
     *
     * @param message single text message
     */
    public void setAlertMessage(String message) {
        mAlertMessage.setText(message);
    }

    /**
     * Render html alert message.
     *
     * @param message html string
     */
    @SuppressWarnings("unused")
    public void setAlertMessageHtml(String message) {
        mAlertMessage.setText(Html.fromHtml(message));
        mAlertMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Build multiple alert messages.
     *
     * @param messages text info for alert
     */
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

    /**
     * Determine alert type.
     *
     * @param type of alert
     */
    public void setAlertType(int type) {
        switch (type) {
            case ALERT_SUCCESS:
                mAlertContainer.setBackgroundResource(R.color.color_success);
                mAlertIcon.setImageResource(R.drawable.ic_check);
                mAlertTitle.setText(R.string.placeholder_alert_success_title);
                mAlertMessage.setText(R.string.placeholder_alert_success_message);
                break;
            case ALERT_INFO:
                mAlertContainer.setBackgroundResource(R.color.color_info);
                mAlertIcon.setImageResource(R.drawable.ic_info);
                mAlertTitle.setText(R.string.placeholder_alert_info_title);
                mAlertMessage.setText(R.string.placeholder_alert_info_message);
                break;
            case ALERT_WARNING:
                mAlertContainer.setBackgroundResource(R.color.color_warning);
                mAlertIcon.setImageResource(R.drawable.ic_warning);
                mAlertTitle.setText(R.string.placeholder_alert_warning_title);
                mAlertMessage.setText(R.string.placeholder_alert_warning_message);
                break;
            case ALERT_DANGER:
                mAlertContainer.setBackgroundResource(R.color.color_danger);
                mAlertIcon.setImageResource(R.drawable.ic_error);
                mAlertTitle.setText(R.string.placeholder_alert_danger_title);
                mAlertMessage.setText(R.string.placeholder_alert_danger_message);
                break;
            default:
                mAlertContainer.setBackgroundResource(R.color.primary);
                mAlertIcon.setImageResource(R.drawable.ic_whatshot);
                mAlertTitle.setText(R.string.placeholder_alert_default_title);
                mAlertMessage.setText(R.string.placeholder_alert_default_message);
                break;
        }
    }
}
