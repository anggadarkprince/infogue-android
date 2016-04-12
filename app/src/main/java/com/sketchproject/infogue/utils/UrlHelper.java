package com.sketchproject.infogue.utils;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 17.33.
 */
public class UrlHelper {
    public static String getContributorUrl(String username){
        return Constant.BASE_URL+"contributor/" + username + "/detail";
    }
}
