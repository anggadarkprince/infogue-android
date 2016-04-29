package com.sketchproject.infogue.events;

import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.IconizedMenu;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 10.46.
 */
public class ArticlePopupBuilder {
    private Context context;
    private View view;
    private Article article;
    private IconizedMenu popupIconized;

    public ArticlePopupBuilder() {
    }

    public IconizedMenu buildPopup(final Context popupContext, View viewAnchor, final Article data) {
        context = popupContext;
        view = viewAnchor;
        article = data;

        popupIconized = new IconizedMenu(new ContextThemeWrapper(context, R.style.AppTheme_PopupOverlay), view);
        popupIconized.inflate(R.menu.article);
        popupIconized.setGravity(Gravity.END);
        popupIconized.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_view) {
                    new ArticleListEvent(context, article).viewArticle();
                } else if (id == R.id.action_browse) {
                    new ArticleListEvent(context, article).browseArticle();
                } else if (id == R.id.action_share) {
                    new ArticleListEvent(context, article).shareArticle();
                } else if (id == R.id.action_rate) {
                    new ArticleListEvent(context, article).rateArticle();
                }

                return false;
            }
        });

        return popupIconized;
    }

    public void show() {
        if (popupIconized == null) {
            throw new IllegalStateException(ArticlePopupBuilder.class.getSimpleName() +
                    " is not initialized, call buildPopup(..) method.");
        }
        popupIconized.show();
    }

    public void dismiss() {
        if (popupIconized == null) {
            throw new IllegalStateException(ArticlePopupBuilder.class.getSimpleName() +
                    " is not initialized, call buildPopup(..) method.");
        }
        popupIconized.dismiss();
    }
}
