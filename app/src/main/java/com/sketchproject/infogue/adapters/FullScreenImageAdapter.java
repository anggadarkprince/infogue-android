package com.sketchproject.infogue.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Image;
import com.sketchproject.infogue.modules.TouchImageView;

import java.util.ArrayList;

/**
 * Sketch Project Studio
 * Created by angga on 17/09/16.
 */
public class FullScreenImageAdapter extends PagerAdapter {
    private AppCompatActivity activity;
    private ArrayList<Image> imagePaths;

    public FullScreenImageAdapter(AppCompatActivity activity, ArrayList<Image> imagePaths) {
        this.activity = activity;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this.imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.fragment_image_fullscreen, container, false);

        TouchImageView imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.image);
        Glide.with(activity.getBaseContext())
                .load(imagePaths.get(position).getSource())
                .placeholder(R.color.dark)
                .fitCenter()
                .crossFade()
                .into(imgDisplay);

        container.addView(viewLayout);
        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
