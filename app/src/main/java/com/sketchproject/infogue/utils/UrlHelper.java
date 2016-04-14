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
}
