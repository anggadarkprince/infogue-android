package com.sketchproject.infogue.utils;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 17.33.
 */
public class UrlHelper {
    public static String getContributorUrl(String username) {
        return Constant.BASE_URL + "contributor/" + username + "/detail";
    }

    public static String getShareText(String slug) {
        return "Hey checkout infogue.id article " + Constant.BASE_URL + slug + " via @infogue";
    }

    public static String getBrowseArticleUrl(String slug) {
        return Constant.BASE_URL + slug;
    }

    public static String wrapHtmlString(String html) {
        return "<html><head><style>img{display:block; width:100%}</style></head><body>" + html + "</body></html>";
    }

    public static String getDisqusUrl(int idPost, String slug, String title, String shortName) {
        return Constant.URL_DISQUS_TEMPLATE + "?shortname=" + shortName + "&identifier=" + idPost + "&url=" + Constant.BASE_URL + slug + "&title=" + title;
    }
}
