package com.sketchproject.infogue.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.infogue.R;

import java.io.ByteArrayOutputStream;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 14.27.
 */
public class Helper {

    public static AlertDialog setDialogButtonTheme(Context context, AlertDialog dialog) {
        Button mButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (mButton != null) {
            mButton.setTextColor(ContextCompat.getColor(context, R.color.primary));
        }
        Button mButton2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (mButton2 != null) {
            mButton2.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }
        Button mButton3 = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if (mButton3 != null) {
            mButton3.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        return dialog;
    }

    public static Toast toastColor(Context context, String message, @ColorInt int color) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundColor(color);
        view.setPadding(20, 20, 20, 20);
        TextView tv = (TextView) view.findViewById(android.R.id.message);
        tv.setShadowLayer(0, 0, 0, ContextCompat.getColor(context, R.color.transparent));
        tv.setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();

        return toast;
    }

    public static Toast toastColorResource(Context context, String message, @ColorRes int color) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, color));
        view.setPadding(20, 20, 20, 20);
        TextView tv = (TextView) view.findViewById(android.R.id.message);
        tv.setShadowLayer(0, 0, 0, ContextCompat.getColor(context, R.color.transparent));
        tv.setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();

        return toast;
    }

    public static String createSlug(String title) {
        String trimmed = title.trim();
        String slug = trimmed
                .replaceAll("[^a-zA-Z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.toLowerCase();
    }

    public static String wrapHtmlString(String html) {
        return "<html>" +
                "<head>" +
                "<style>img{display: inline; height: auto; max-width: 100%;}</style>" +
                "</head>" +
                "<body>" + html + "</body>" +
                "</html>";
    }

    public static byte[] getFileDataFromDrawable(Context context, int id) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] getFileDataFromDrawable(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
