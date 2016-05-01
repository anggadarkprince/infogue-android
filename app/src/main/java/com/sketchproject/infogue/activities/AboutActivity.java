package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.utils.APIBuilder;

/**
 * A simple {@link AppCompatActivity} subclass to show credit title.
 *
 * Sketch Project Studio
 * Created by Angga on 1/04/2016 10.37.
 */
public class AboutActivity extends AppCompatActivity {

    /**
     * Perform initialization of AboutActivity.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageButton mFacebookButton = (ImageButton) findViewById(R.id.btn_facebook);
        if (mFacebookButton != null) {
            mFacebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_FACEBOOK_DEVELOPER));
                    startActivity(browserIntent);
                }
            });
        }

        ImageButton mTwitterButton = (ImageButton) findViewById(R.id.btn_twitter);
        if (mTwitterButton != null) {
            mTwitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIBuilder.URL_TWITTER_DEVELOPER));
                    startActivity(browserIntent);
                }
            });
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
