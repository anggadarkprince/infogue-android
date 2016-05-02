package com.sketchproject.infogue.fragments.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sketchproject.infogue.R;

/**
 * Info view holder to display message like end, empty or error messages.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 16/04/2016 18.19.
 */
public class ListInfoViewHolder extends RecyclerView.ViewHolder {
    public ImageView mLogoImage;
    public TextView mMessageView;

    public ListInfoViewHolder(View view) {
        super(view);
        mLogoImage = (ImageView) view.findViewById(R.id.logo);
        mMessageView = (TextView) view.findViewById(R.id.message);
    }

    @Override
    public String toString() {
        return super.toString() + " Comment : " + mMessageView.getText();
    }
}
