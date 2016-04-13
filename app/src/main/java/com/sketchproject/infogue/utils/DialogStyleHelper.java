package com.sketchproject.infogue.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import com.sketchproject.infogue.R;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 14.27.
 */
public class DialogStyleHelper {

    @SuppressWarnings("deprecation")
    public static AlertDialog buttonTheme(Context context, AlertDialog dialog){
        Button mButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (mButton != null) {
            mButton.setTextColor(context.getResources().getColor(R.color.primary));
        }
        Button mButton2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (mButton2 != null) {
            mButton2.setTextColor(context.getResources().getColor(R.color.gray));
        }
        Button mButton3 = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if (mButton3 != null) {
            mButton3.setTextColor(context.getResources().getColor(R.color.primary));
        }

        return dialog;
    }
}
