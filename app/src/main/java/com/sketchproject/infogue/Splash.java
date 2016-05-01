package com.sketchproject.infogue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.sketchproject.infogue.activities.ApplicationActivity;
import com.sketchproject.infogue.activities.FeaturedActivity;
import com.sketchproject.infogue.database.DBHelper;
import com.sketchproject.infogue.database.DatabaseManager;
import com.sketchproject.infogue.fragments.LoginFragment;
import com.sketchproject.infogue.modules.SessionManager;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        TwitterAuthConfig authConfig = new TwitterAuthConfig(LoginFragment.TWITTER_KEY, LoginFragment.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        DatabaseManager.initializeInstance(dbHelper);

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
