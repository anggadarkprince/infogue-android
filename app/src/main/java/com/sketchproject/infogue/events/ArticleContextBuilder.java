package com.sketchproject.infogue.events;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.SessionManager;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 10.55.
 */
public class ArticleContextBuilder {
    private Context context;
    private Article article;
    private AlertDialog alert;

    /**
     * Initialize ArticleContextBuilder.
     *
     * @param dialogContext parent context
     * @param data          model data of article
     */
    public ArticleContextBuilder(Context dialogContext, Article data) {
        context = dialogContext;
        article = data;
    }

    /**
     * Build new context menu dialog for article by passing field attribute.
     *
     * @return AlertDialog
     */
    public AlertDialog buildContext() {
        return buildContext(context, article);
    }

    /**
     * Build context menu dialog by information which passed.
     *
     * @param dialogContext parent context
     * @param data          model data of article
     * @return AlertDialog
     */
    public AlertDialog buildContext(final Context dialogContext, final Article data) {
        if (dialogContext == null || data == null) {
            throw new IllegalArgumentException(ArticlePopupBuilder.class.getSimpleName() +
                    " Context, Article is not initialized. Make sure use" +
                    " ArticleContextBuilder(Context dialogContext, Article data) instead");
        }

        final int menu = new SessionManager(dialogContext).isMe(data.getAuthorId()) ? R.array.items_article_editable : R.array.items_article;
        final String[] items = context.getResources().getStringArray(R.array.items_article);

        AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setItems(menu, new DialogInterface.OnClickListener() {
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
                    event.rateArticle(5);
                } else if (menu.equals(dialogContext.getString(R.string.action_long_edit))) {
                    event.editArticle();
                } else if (menu.equals(dialogContext.getString(R.string.action_long_delete))) {
                    event.deleteArticle();
                }
            }
        });
        alert = builder.create();
        return alert;
    }

    /**
     * Show alert if initialized.
     */
    public void show() {
        if (alert == null) {
            throw new IllegalStateException(ArticleContextBuilder.class.getSimpleName() +
                    " is not initialized, call buildContext(..) method.");
        }
        alert.show();
    }

    /**
     * Dismiss alert if initialized.
     */
    public void dismiss() {
        if (alert == null) {
            throw new IllegalStateException(ArticleContextBuilder.class.getSimpleName() +
                    " is not initialized, call buildContext(..) method.");
        }
        alert.dismiss();
    }
}
