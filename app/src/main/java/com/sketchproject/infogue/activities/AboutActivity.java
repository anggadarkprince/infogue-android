package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.sketchproject.infogue.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageButton mFacebookButton = (ImageButton) findViewById(R.id.btn_facebook);
        if (mFacebookButton != null) {
            mFacebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/sketchproject"));
                    startActivity(browserIntent);
                }
            });
        }

        ImageButton mTwitterButton = (ImageButton) findViewById(R.id.btn_twitter);
        if (mTwitterButton != null) {
            mTwitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/sketchproject"));
                    startActivity(browserIntent);
                }
            });
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
