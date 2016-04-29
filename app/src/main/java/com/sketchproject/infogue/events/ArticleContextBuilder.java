package com.sketchproject.infogue.events;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 10.55.
 */
public class ArticleContextBuilder {
    private Context context;
    private Article article;
    private AlertDialog alert;
    private String[] items;

    public ArticleContextBuilder() {
        items = context.getResources().getStringArray(R.array.items_article);
    }

    public ArticleContextBuilder(Context dialogContext, Article data) {
        context = dialogContext;
        article = data;
        items = context.getResources().getStringArray(R.array.items_article);
    }

    public AlertDialog buildContext(){
        return buildContext(context, article);
    }

    public AlertDialog buildContext(final Context dialogContext, final Article data) {
        if (dialogContext == null || data == null) {
            throw new IllegalArgumentException(ArticlePopupBuilder.class.getSimpleName() +
                    " Context, Article is not initialized. Make sure use"+
                    " ArticleContextBuilder(Context dialogContext, Article data) instead");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setItems(R.array.items_article, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String menu = items[which];
                ArticleListEvent event = new ArticleListEvent(dialogContext, data);
                if (menu.equals(dialogContext.getString(R.string.action_long_open))) {
                    event.viewArticle();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_browse))) {
                    event.browseArticle();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_share))) {
                    event.shareArticle();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_rate))) {
                    event.rateArticle();
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
