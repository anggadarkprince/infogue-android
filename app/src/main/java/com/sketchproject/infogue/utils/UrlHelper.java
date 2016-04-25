package com.sketchproject.infogue.utils;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 17.33.
 */
public class UrlHelper {
    public static String getApiFeaturedUrl(String slugFeatured, int page) {
        String url = Constant.URL_API_FEATURED + "/" + slugFeatured;
        if (page > 0) {
            url += "?page=" + page;
        }

        return url;
    }

    public static String getApiCategoryUrl(String slugCategory, String slugSubCategory, int page) {
        String url = Constant.URL_API_CATEGORY + "/" + slugCategory;
        if (slugSubCategory != null) {
            url += "/" + slugSubCategory;
        }

        if (page > 0) {
            url += "?page=" + page;
        }

        return url;
    }

    public static String getApiFollowerUrl(String type, int relatedId, String username, String query) {
        String related = "contributor_id=" + relatedId;
        String search = "query=" + query;
        String optionalQuery = "";
        String url = Constant.BASE_URL_API + "contributor/" + username + "/" + type.toLowerCase();

        optionalQuery += related;
        if (query != null) {
            if (!optionalQuery.isEmpty()) {
                optionalQuery += "&";
            }
            optionalQuery += search;
        }

        if (!optionalQuery.isEmpty()) {
            url += "?" + optionalQuery;
        }
        return url;
    }

    public static String getApiArticleUrl(int authorId, String authorUsername, boolean isMyArticle, String query) {
        String author = "contributor_id=" + authorId;
        String myArticle = "my_article=" + isMyArticle;
        String search = "query=" + query;
        String optionalQuery = "";
        String url = Constant.BASE_URL_API + "contributor/" + authorUsername + "/article";

        optionalQuery += author;
        optionalQuery += "&"+myArticle;
        if (query != null) {
            if (!optionalQuery.isEmpty()) {
                optionalQuery += "&";
            }
            optionalQuery += search;
        }

        if (!optionalQuery.isEmpty()) {
            url += "?" + optionalQuery;
        }
        return url;
    }

    public static String getContributorDetailUrl(String username) {
        return Constant.BASE_URL + "contributor/" + username + "/detail";
    }

    public static String getArticleUrl(String slug) {
        return Constant.BASE_URL + slug;
    }

    public static String getContributorUrl(String username) {
        return Constant.BASE_URL + "contributor/" + username;
    }

    public static String getShareArticleText(String slug) {
        return "Hey checkout infogue.id article " + Constant.BASE_URL + slug + " via infogue.id";
    }

    public static String getShareContributorText(String username) {
        return "Hey checkout infogue.id contributor " + Constant.BASE_URL + "contributor/" + username + " via infogue.id";
    }

    public static String getDisqusUrl(int idPost, String slug, String title, String shortName) {
        return Constant.URL_DISQUS_TEMPLATE + "?shortname=" + shortName + "&identifier=" + idPost + "&url=" + Constant.BASE_URL + slug + "&title=" + title;
    }
}
