package com.sketchproject.infogue.utils;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import com.sketchproject.infogue.R;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 14.27.
 */
public class DialogStyleHelper {

    public static void buttonTheme(AlertDialog dialog){
        Button mButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (mButton != null) {
            //noinspection deprecation
            mButton.setTextColor(Resources.getSystem().getColor(R.color.colorPrimary));
        }
        Button mButton2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (mButton2 != null) {
            //noinspection deprecation
            mButton2.setTextColor(Resources.getSystem().getColor(R.color.colorPrimary));
        }
    }
}
