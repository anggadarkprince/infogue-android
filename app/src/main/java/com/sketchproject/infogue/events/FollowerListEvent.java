package com.sketchproject.infogue.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ProfileActivity;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.APIBuilder;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 16.50.
 */
public class FollowerListEvent {
    private Context context;
    private Contributor contributor;

    public static Contributor mContributor;
    public static ImageButton mControlButton;

    public FollowerListEvent(Context context, Contributor contributor){
        this.context = context;
        this.contributor = contributor;
    }

    public void shareContributor(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, APIBuilder.getShareContributorText(contributor.getUsername()));
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.label_intent_share)));
    }

    public void browseContributor(){
        String articleUrl = APIBuilder.getContributorUrl(contributor.getUsername());
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
        context.startActivity(browserIntent);
    }

    public void viewProfile(FragmentActivity parent, Contributor contributor, View buttonControl){
        Log.i("INFOGUE/Contributor", contributor.getId() + " " + contributor.getUsername());
        mControlButton = (ImageButton) buttonControl;
        mContributor = contributor;

        Intent profileIntent = new Intent(context, ProfileActivity.class);
        profileIntent.putExtra(SessionManager.KEY_ID, contributor.getId());
        profileIntent.putExtra(SessionManager.KEY_USERNAME, contributor.getUsername());
        profileIntent.putExtra(SessionManager.KEY_NAME, contributor.getName());
        profileIntent.putExtra(SessionManager.KEY_LOCATION, contributor.getLocation());
        profileIntent.putExtra(SessionManager.KEY_ABOUT, contributor.getAbout());
        profileIntent.putExtra(SessionManager.KEY_AVATAR, contributor.getAvatar());
        profileIntent.putExtra(SessionManager.KEY_COVER, contributor.getCover());
        profileIntent.putExtra(SessionManager.KEY_STATUS, contributor.getStatus());
        profileIntent.putExtra(SessionManager.KEY_ARTICLE, contributor.getArticle());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWER, contributor.getFollowers());
        profileIntent.putExtra(SessionManager.KEY_FOLLOWING, contributor.getFollowing());
        profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, contributor.isFollowing());
        parent.startActivityForResult(profileIntent, ProfileActivity.PROFILE_RESULT_CODE);
    }

    public void viewProfileResult(int requestCode, int resultCode, Intent data){
        if (requestCode == ProfileActivity.PROFILE_RESULT_CODE && data != null) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                boolean isFollowing = data.getBooleanExtra(SessionManager.KEY_IS_FOLLOWING, false);
                mContributor.setIsFollowing(isFollowing);

                if (isFollowing) {
                    mControlButton.setImageResource(R.drawable.btn_unfollow);
                } else {
                    mControlButton.setImageResource(R.drawable.btn_follow);
                }
            }
        }
    }
}
