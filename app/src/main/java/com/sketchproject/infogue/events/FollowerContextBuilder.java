package com.sketchproject.infogue.events;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 16.42.
 */
public class FollowerContextBuilder {
    private Context context;
    private Contributor contributor;
    private View followButton;
    private AlertDialog alert;
    private String[] items;

    public FollowerContextBuilder() {
        items = context.getResources().getStringArray(R.array.items_article);
    }

    public FollowerContextBuilder(Context dialogContext, Contributor data, final View followControl) {
        context = dialogContext;
        contributor = data;
        followButton = followControl;
        items = context.getResources().getStringArray(R.array.items_article);
    }

    public AlertDialog buildContext(){
        return buildContext(context, contributor, followButton);
    }

    public AlertDialog buildContext(final Context dialogContext, final Contributor data, final View followControl) {
        if (dialogContext == null || data == null || followControl == null) {
            throw new IllegalArgumentException(FollowerContextBuilder.class.getSimpleName() +
                    " Context, data, followControl is not initialized. Make sure use"+
                    " FollowerContextBuilder(Context dialogContext, Contributor data, final View followControl)");
        }

        int menuRes;
        if (new SessionManager(dialogContext).isMe(data.getId()) || !data.getStatus().equals(Contributor.STATUS_ACTIVATED)) {
            menuRes = R.array.items_follow_profile;
            items = dialogContext.getResources().getStringArray(R.array.items_follow_profile);
        }
        else if(data.isFollowing()){
            menuRes = R.array.items_unfollow_people;
            items = dialogContext.getResources().getStringArray(R.array.items_unfollow_people);
        }
        else {
            menuRes = R.array.items_follow_people;
            items = dialogContext.getResources().getStringArray(R.array.items_follow_people);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setItems(menuRes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String menu = items[which];
                FollowerListEvent event = new FollowerListEvent(dialogContext, data, followControl);
                if (menu.equals(dialogContext.getString(R.string.action_long_open))) {
                    event.viewProfile();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_browse))) {
                    event.browseContributor();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_share_contributor))) {
                    event.shareContributor();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_follow))) {
                    event.followContributor();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_unfollow))) {
                    event.followContributor();
                }
            }
        });
        alert = builder.create();
        return alert;
    }

    public void show() {
        if (alert == null) {
            throw new IllegalStateException(FollowerContextBuilder.class.getSimpleName() +
                    " is not initialized, call buildContext(..) method.");
        }
        alert.show();
    }

    public void dismiss() {
        if (alert == null) {
            throw new IllegalStateException(FollowerContextBuilder.class.getSimpleName() +
                    " is not initialized, call buildContext(..) method.");
        }
        alert.dismiss();
    }
}
