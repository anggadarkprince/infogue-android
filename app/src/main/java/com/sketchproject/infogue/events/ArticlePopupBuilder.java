package com.sketchproject.infogue.events;

import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.IconizedMenu;
import com.sketchproject.infogue.modules.SessionManager;

/**
 * Sketch Project Studio
 * Created by Angga on 29/04/2016 10.46.
 */
public class ArticlePopupBuilder {
    private Context context;
    private View view;
    private Article article;
    private IconizedMenu popupIconized;

    /**
     * Initialize ArticlePopupBuilder.
     *
     * @param popupContext model data of contributor
     * @param viewAnchor anchor view to display popup
     * @param data model data of Article
     */
    public ArticlePopupBuilder(Context popupContext, View viewAnchor, Article data) {
        context = popupContext;
        view = viewAnchor;
        article = data;
    }

    /**
     * Build popup by passing data from field.
     *
     * @return IconizedMenu
     */
    public IconizedMenu buildPopup(){
        return buildPopup(context, view, article);
    }

    /**
     * Build popup menu by information which passed.
     *
     * @param popupContext model data of contributor
     * @param viewAnchor anchor view to display popup
     * @param data model data of Article
     * @return IconizedMenu
     */
    public IconizedMenu buildPopup(final Context popupContext, View viewAnchor, final Article data) {
        if (popupContext == null || viewAnchor == null || data == null) {
            throw new IllegalArgumentException(ArticlePopupBuilder.class.getSimpleName() +
                    " Context, viewAnchor, Article is not initialized. Make sure use"+
                    " ArticlePopupBuilder(Context popupContext, View viewAnchor, Article data) instead");
        }

        int menu = new SessionManager(popupContext).isMe(data.getAuthorId()) ? R.menu.editable : R.menu.article;
        popupIconized = new IconizedMenu(new ContextThemeWrapper(popupContext, R.style.AppTheme_PopupOverlay), viewAnchor);
        popupIconized.inflate(menu);
        popupIconized.setGravity(Gravity.END);
        popupIconized.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                ArticleListEvent event = new ArticleListEvent(popupContext, data);
                switch (id) {
                    case R.id.action_view:
                        event.viewArticle();
                        break;
                    case R.id.action_browse:
                        event.browseArticle();
                        break;
                    case R.id.action_share:
                        event.shareArticle();
                        break;
                    case R.id.action_rate:
                        event.rateArticle();
                        break;
                    case R.id.action_edit:
                        event.editArticle();
                        break;
                    case R.id.action_delete:
                        event.deleteArticle();
                        break;
                }

                return false;
            }
        });

        return popupIconized;
    }

    /**
     * Show popup if initialized.
     */
    public void show() {
        if (popupIconized == null) {
            throw new IllegalStateException(ArticlePopupBuilder.class.getSimpleName() +
                    " is not initialized, call buildPopup(..) method.");
        }
        popupIconized.show();
    }

    /**
     * Dismiss popup if initialized.
     */
    public void dismiss() {
        if (popupIconized == null) {
            throw new IllegalStateException(ArticlePopupBuilder.class.getSimpleName() +
                    " is not initialized, call buildPopup(..) method.");
        }
        popupIconized.dismiss();
    }
}
