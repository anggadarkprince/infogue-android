package com.sketchproject.infogue.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.infogue.R;

import java.io.ByteArrayOutputStream;

/**
 * Static helper function collection.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 14.27.
 */
public class Helper {

    /**
     * Create dialog with positive and negative button.
     *
     * @param context          dialog
     * @param titleId          title resource id
     * @param messageId        message resource id
     * @param textIdPositive   text button positive
     * @param textIdNegative   text button negative
     * @param positiveListener button positive event handler
     * @param negativeListener button negative event handler
     * @return Alert dialog
     */
    public static AlertDialog createDialog(Context context, int titleId, int messageId, int textIdPositive, int textIdNegative,
                                           DialogInterface.OnClickListener positiveListener,
                                           DialogInterface.OnClickListener negativeListener) {

        return createDialog(context, titleId, messageId, textIdPositive, textIdNegative, 0, positiveListener, negativeListener, null);
    }

    /**
     * Create dialog with positive, negative and neutral button.
     *
     * @param context          dialog
     * @param titleId          title resource id
     * @param messageId        message resource id
     * @param textIdPositive   text button positive
     * @param textIdNegative   text button negative
     * @param textIdNeutral    text button neutral
     * @param positiveListener button positive event handler
     * @param negativeListener button negative event handler
     * @param neutralListener  button neutral event handler
     * @return Alert dialog
     */
    public static AlertDialog createDialog(Context context, int titleId, int messageId,
                                           int textIdPositive, int textIdNegative, int textIdNeutral,
                                           DialogInterface.OnClickListener positiveListener,
                                           DialogInterface.OnClickListener negativeListener,
                                           DialogInterface.OnClickListener neutralListener) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme_NoActionBar));
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(textIdPositive, positiveListener);
        builder.setNegativeButton(textIdNegative, negativeListener);
        if (textIdNeutral != 0 && neutralListener != null) {
            builder.setNeutralButton(textIdNegative, negativeListener);
        }

        AlertDialog dialog = builder.create();

        dialog.show();
        Helper.setDialogButtonTheme(context, dialog);
        dialog.hide();

        return dialog;
    }

    /**
     * Force button dialog to match with current theme.
     * I have no idea to fix button dialog theme so i figure out this approach works perfectly
     * but not cool right,,, i'm working on it :)
     *
     * @param context parent context
     * @param dialog  which has the button
     * @return the dialog itself
     */
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

    /**
     * Create colored toast, I'm pretty sure it must be separated into single class that
     * extend Toast and change the make(...) method to paint the view, but i prefer easy way,
     * still try to figure out how to keep rounded view, but yeah,, i'm still work fucking on it,
     * ops, sorry for my bad language.
     *
     * @param context parent context
     * @param message resource toast message id
     * @param color   resource color id
     * @return custom toast
     */
    public static Toast toastColor(Context context, @StringRes int message, @ColorRes int color) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(color);
        view.setPadding(20, 20, 20, 20);
        TextView tv = (TextView) view.findViewById(android.R.id.message);
        tv.setShadowLayer(0, 0, 0, ContextCompat.getColor(context, R.color.transparent));
        tv.setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();

        return toast;
    }

    /**
     * Create colored toast, like the other toastColor(...) method but it passed plain string
     * instead resource string id, more useful with custom message from server :)
     *
     * @param context parent context
     * @param message string message
     * @param color   resource color id
     * @return custom toast
     */
    public static Toast toastColor(Context context, String message, @ColorRes int color) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(color);
        view.setPadding(20, 20, 20, 20);
        TextView tv = (TextView) view.findViewById(android.R.id.message);
        tv.setShadowLayer(0, 0, 0, ContextCompat.getColor(context, R.color.transparent));
        tv.setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();

        return toast;
    }

    /**
     * Build slug from string title like "The beautiful day in 1992" turns "the-beautiful-day-in-1992"
     * or "Super massive black hole O'creaz MO on July" turns "super-massive-black-hole-ocreaz-mo-on-july"
     *
     * @param title article title
     * @return slug string
     */
    public static String createSlug(String title) {
        String trimmed = title.trim();
        String slug = trimmed
                .replaceAll("[^a-zA-Z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.toLowerCase();
    }

    /**
     * Wrap article in post activity with simple style to wrap image and make it fit
     * with its container.
     *
     * @param html string content
     * @return string wrapped html
     */
    public static String wrapHtmlString(String html) {
        return "<html>" +
                "<head>" +
                "<style>img{display: inline; height: auto; max-width: 100%;}</style>" +
                "</head>" +
                "<body>" + html + "</body>" +
                "</html>";
    }

    /**
     * Turn drawable resource into byte array.
     *
     * @param context parent context
     * @param id      drawable resource id
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, @DrawableRes int id) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Turn drawable into byte array.
     *
     * @param drawable data
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
