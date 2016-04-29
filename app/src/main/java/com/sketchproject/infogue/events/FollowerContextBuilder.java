package com.sketchproject.infogue.events;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.modules.SessionManager;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 16.42.
 */
public class FollowerContextBuilder {
    private Context context;
    private Contributor contributor;
    private AlertDialog alert;
    private String[] items;

    public FollowerContextBuilder() {
    }

    public AlertDialog buildContext(Context dialogContext, Contributor data) {
        context = dialogContext;
        contributor = data;

        int menuRes;
        if (new SessionManager(dialogContext).isMe(contributor.getId())) {
            menuRes = R.array.items_follow_profile;
            items = context.getResources().getStringArray(R.array.items_follow_profile);
        }
        else if(contributor.isFollowing()){
            menuRes = R.array.items_unfollow_people;
            items = context.getResources().getStringArray(R.array.items_unfollow_people);
        }
        else {
            menuRes = R.array.items_follow_people;
            items = context.getResources().getStringArray(R.array.items_follow_people);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(menuRes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String menu = items[which];
                if (menu.equals(context.getString(R.string.action_long_open))) {
                    //new ArticleListEvent(context, contributor).viewArticle();
                } else if (menu.equals(context.getString(R.string.action_long_browse))) {
                    //new ArticleListEvent(context, contributor).browseArticle();
                } else if (menu.equals(context.getString(R.string.action_long_share_contributor))) {
                    //new ArticleListEvent(context, contributor).shareArticle();
                } else if (menu.equals(context.getString(R.string.action_long_follow))) {
                    //new ArticleListEvent(context, contributor).rateArticle();
                } else if (menu.equals(context.getString(R.string.action_long_unfollow))) {
                    //new ArticleListEvent(context, contributor).rateArticle();
                }
            }
        });
        alert = builder.create();
        return alert;
    }

    public void show() {
        if (alert == null) {
            throw new IllegalStateException(ArticleContextBuilder.class.getSimpleName() +
                    " is not initialized, call buildContext(..) method.");
        }
        alert.show();
    }

    public void dismiss() {
        if (alert == null) {
            throw new IllegalStateException(ArticleContextBuilder.class.getSimpleName() +
                    " is not initialized, call buildContext(..) method.");
        }
        alert.dismiss();
    }
}
