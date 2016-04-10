package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.fragments.dummy.DummyFollowerContent;

public class FollowerActivity extends AppCompatActivity implements FollowerFragment.OnListFragmentInteractionListener {

    public static final String FOLLOWER_SCREEN = "Followers";
    public static final String FOLLOWING_SCREEN = "Following";

    String activityTitle = "Followers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            activityTitle = extras.getString(ProfileActivity.FOLLOWER_ACTIVITY);
            Log.i(activityTitle, String.valueOf(extras.getInt("id")) + extras.getString("username"));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(activityTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(DummyFollowerContent.DummyItem item) {
        Log.i("RESULT", item.username);
        Intent intentProfile = new Intent(getBaseContext(), ProfileActivity.class);
        intentProfile.putExtra("id", item.id);
        intentProfile.putExtra("username", item.username);
        intentProfile.putExtra("name", item.name);
        intentProfile.putExtra("location", item.location);
        intentProfile.putExtra("avatar", item.avatar);
        startActivity(intentProfile);
    }
}
