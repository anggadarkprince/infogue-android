package com.sketchproject.infogue.fragments.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.sketchproject.infogue.R;

/**
 * Sketch Project Studio
 * Created by Angga on 16/04/2016 18.19.
 */
public class LoadingViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar mProgressBar;

    public LoadingViewHolder(View view) {
        super(view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.load_more_progress);
    }

    @Override
    public String toString() {
        return super.toString() + " Loading list items";
    }
}
