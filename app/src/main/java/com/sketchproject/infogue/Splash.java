package com.sketchproject.infogue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.sketchproject.infogue.activities.FeaturedActivity;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        final int SPLASH_TIME_OUT = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent featuredScreen = new Intent(Splash.this, FeaturedActivity.class);
                featuredScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(featuredScreen);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
