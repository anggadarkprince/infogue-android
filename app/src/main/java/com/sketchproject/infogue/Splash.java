package com.sketchproject.infogue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.sketchproject.infogue.activities.ApplicationActivity;
import com.sketchproject.infogue.activities.FeaturedActivity;
import com.sketchproject.infogue.modules.SessionManager;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        final int SPLASH_TIME_OUT = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SessionManager sessionManager = new SessionManager(getBaseContext());
                boolean openFirstTime = !sessionManager.getSessionData(SessionManager.KEY_USER_LEARNED, false);
                if (openFirstTime) {
                    Intent featuredIntent = new Intent(getBaseContext(), FeaturedActivity.class);
                    startActivity(featuredIntent);
                } else {
                    Intent applicationIntent = new Intent(getBaseContext(), ApplicationActivity.class);
                    startActivity(applicationIntent);
                }

                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
