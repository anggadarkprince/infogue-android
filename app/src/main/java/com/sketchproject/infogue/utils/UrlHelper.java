package com.sketchproject.infogue.utils;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 17.33.
 */
public class UrlHelper {
    public static String getContributorDetailUrl(String username) {
        return Constant.BASE_URL + "contributor/" + username + "/detail";
    }

    public static String getArticleUrl(String slug) {
        return Constant.BASE_URL + slug;
    }

    public static String getContributorUrl(String username) {
        return Constant.BASE_URL + "contributor/"+username;
    }

    public static String getShareArticleText(String slug) {
        return "Hey checkout infogue.id article " + Constant.BASE_URL + slug + " via infogue.id";
    }

    public static String getShareContributorText(String username) {
        return "Hey checkout infogue.id contributor " + Constant.BASE_URL + "contributor/"+ username + " via infogue.id";
    }

    public static String getDisqusUrl(int idPost, String slug, String title, String shortName) {
        return Constant.URL_DISQUS_TEMPLATE + "?shortname=" + shortName + "&identifier=" + idPost + "&url=" + Constant.BASE_URL + slug + "&title=" + title;
    }
}
