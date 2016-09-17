package com.sketchproject.infogue.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.adapters.FullScreenImageAdapter;
import com.sketchproject.infogue.models.Image;

import java.util.ArrayList;

public class ImagePreviewActivity extends AppCompatActivity {

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        Bundle intent = getIntent().getExtras();
        ArrayList<Image> allImages = (ArrayList<Image>) intent.getSerializable("images");
        int position = intent.getInt("position", 0);

        FullScreenImageAdapter adapter = new FullScreenImageAdapter(ImagePreviewActivity.this, allImages);
        ViewPager viewPager = (ViewPager) findViewById(R.id.image_pager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
    }
}
