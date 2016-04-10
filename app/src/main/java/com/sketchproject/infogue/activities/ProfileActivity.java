package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sketchproject.infogue.R;

public class ProfileActivity extends AppCompatActivity {

    public static final String FOLLOWER_ACTIVITY = "follower-activity";

    private TextView mNameView;
    private TextView mLocationView;
    private TextView mAboutView;
    private TextView mArticleView;
    private TextView mFollowerView;
    private TextView mFollowingView;
    private ImageView mAvatarImage;
    private ImageView mCoverImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNameView = (TextView) findViewById(R.id.name);
        mLocationView = (TextView) findViewById(R.id.location);
        mAboutView = (TextView) findViewById(R.id.about);
        mArticleView = (TextView) findViewById(R.id.valueArticle);
        mFollowerView = (TextView) findViewById(R.id.valueFollower);
        mFollowingView = (TextView) findViewById(R.id.valueFollowing);
        mAvatarImage = (ImageView) findViewById(R.id.avatar);
        mCoverImage = (ImageView) findViewById(R.id.cover);

        final Bundle extras = getIntent().getExtras();
        if(extras != null){
            mNameView.setText(extras.getString("name"));
            mLocationView.setText(extras.getString("location"));
            mArticleView.setText(String.valueOf((int)(Math.random() * 50)));
            mFollowerView.setText(String.valueOf((int)(Math.random() * 50)));
            mFollowingView.setText(String.valueOf((int) (Math.random() * 50)));
            mAvatarImage.setImageResource(extras.getInt("avatar"));

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(extras.getString("username"));
            }

            View mArticleButton = findViewById(R.id.btn_article);
            mArticleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                    articleIntent.putExtra("id", extras.getInt("id"));
                    articleIntent.putExtra("username", extras.getString("username"));
                    startActivity(articleIntent);
                }
            });

            View mFollowerButton = findViewById(R.id.btn_followers);
            mFollowerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent followerIntent = new Intent(getBaseContext(), FollowerActivity.class);
                    followerIntent.putExtra(FOLLOWER_ACTIVITY, FollowerActivity.FOLLOWER_SCREEN);
                    followerIntent.putExtra("id", extras.getInt("id"));
                    followerIntent.putExtra("username", extras.getString("username"));
                    startActivity(followerIntent);
                }
            });

            View mFollowingButton = findViewById(R.id.btn_following);
            mFollowingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent followingIntent = new Intent(getBaseContext(), FollowerActivity.class);
                    followingIntent.putExtra(FOLLOWER_ACTIVITY, FollowerActivity.FOLLOWING_SCREEN);
                    followingIntent.putExtra("id", extras.getInt("id"));
                    followingIntent.putExtra("username", extras.getString("username"));
                    startActivity(followingIntent);
                }
            });

            Button mDetailButton = (Button) findViewById(R.id.btn_detail);
            mDetailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://infogue.id/contributor/"+extras.getString("username")+"/detail"));
                    startActivity(browserIntent);
                }
            });
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
}
