package com.sketchproject.infogue.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment;
import com.sketchproject.infogue.fragments.dummy.DummyFollowerContent;

public class FollowerActivity extends AppCompatActivity implements FollowerFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onListFragmentInteraction(DummyFollowerContent.DummyItem item) {
        Log.i("RESULT", item.username);
    }
}
