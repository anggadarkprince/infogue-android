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

    public AlertDialog buildContext(Context dialogContext, Article data) {
        context = dialogContext;
        article = data;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(R.array.items_article, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String menu = items[which];
                if (menu.equals(context.getString(R.string.action_long_open))) {
                    new ArticleListEvent(context, article).viewArticle();
                } else if (menu.equals(context.getString(R.string.action_long_browse))) {
                    new ArticleListEvent(context, article).browseArticle();
                } else if (menu.equals(context.getString(R.string.action_long_share))) {
                    new ArticleListEvent(context, article).shareArticle();
                } else if (menu.equals(context.getString(R.string.action_long_rate))) {
                    new ArticleListEvent(context, article).rateArticle();
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
