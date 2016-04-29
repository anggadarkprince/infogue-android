package com.sketchproject.infogue.utils;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 17.33.
 */
public class APIBuilder {
    public static final String SEARCH_BOTH = "both";
    public static final String SEARCH_CONTRIBUTOR = "contributor";
    public static final String SEARCH_ARTICLE = "article";
    public static final String BASE_URL = "http://www.infogue.id/";
    public static final String URL_HELP = BASE_URL + "faq";
    public static final String URL_FORGOT = BASE_URL + "auth/forgot";
    public static final String URL_FEEDBACK = BASE_URL + "contact";
    public static final String BASE_URL_API = "http://192.168.43.141:8000/api/";
    public static final String URL_API_COMMENT = BASE_URL_API + "comment";
    public static final String URL_API_UNFOLLOW = BASE_URL_API + "unfollow";
    public static final String URL_API_FOLLOW = BASE_URL_API + "follow";
    public static final String URL_API_ARTICLE = BASE_URL_API + "article";
    public static final String URL_API_HIT = BASE_URL_API + "article/hit";
    public static final String URL_API_RATE = BASE_URL_API + "article/rate";
    public static final String URL_API_FEATURED = BASE_URL_API + "featured";
    public static final String URL_API_CATEGORY = BASE_URL_API + "category";
    public static final String URL_API_SETTING = BASE_URL_API + "account";
    public static final String URL_API_LOGIN = BASE_URL_API + "account/login";
    public static final String URL_API_REGISTER = BASE_URL_API + "account/register";

    public static final String URL_APP = "http://play.google.com/store/apps/details?id=com.sketchproject.infogue";
    public static final String URL_DISQUS_TEMPLATE = "http://infogue.angga-ari.com/mobiletemplate.html";

    public static final String REQUEST_EXIST = "exist"; // code 400 bad request
    public static final String REQUEST_DENIED = "denied"; // code 400 bad request
    public static final String REQUEST_SUCCESS = "success"; // code 200 ok
    public static final String REQUEST_UNREGISTERED = "unregistered"; // 403 forbidden
    public static final String REQUEST_RESTRICT = "restrict"; // code 403 forbidden
    public static final String REQUEST_GRANTED = "granted"; // code 200  ok
    public static final String REQUEST_MISMATCH = "mismatch"; // code 401 unauthorized
    public static final String REQUEST_FAILURE = "failure"; // code 500 internal error
    public static final String REQUEST_NOT_FOUND = "not found"; // code 400 bad request
    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_MESSAGE = "message";

    public static String getApiFeaturedUrl(String slugFeatured, int page) {
        String url = URL_API_FEATURED + "/" + slugFeatured;
        if (page > 0) {
            url += "?page=" + page;
        }

        return url;
    }

    public static String getApiCategoryUrl(String slugCategory, String slugSubCategory, int page) {
        String url = URL_API_CATEGORY + "/" + slugCategory;
        if (slugSubCategory != null) {
            url += "/" + slugSubCategory;
        }

        if (page > 0) {
            url += "?page=" + page;
        }

        return url;
    }

    public static String getApiTagUrl(String tag) {
        return BASE_URL_API + "tag/" + tag;
    }

    public static String getApiFollowerUrl(String type, int relatedId, String username) {
        String optionalQuery = "";
        String url = BASE_URL_API + "contributor/" + username + "/" + type.toLowerCase();

        if(relatedId > 0){
            String related = "contributor_id=" + relatedId;
            optionalQuery += related;
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
        String url = BASE_URL_API + "contributor/" + authorUsername + "/article";

        optionalQuery += author;
        optionalQuery += "&" + myArticle;
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

    public static String getApiPostUrl(String slug) {
        return BASE_URL_API + "article/" + slug;
    }

    public static String getApiPostUrl(String slug, int loggedId) {
        return BASE_URL_API + "article/" + slug + "?contributor_id=" + loggedId;
    }

    public static String getApiContributorUrl(String username) {
        return BASE_URL_API + "contributor/" + username;
    }

    public static String getApiSearchUrl(String query, String type, int loggedId){
        String url = BASE_URL_API+"search/";
        if(type == null || type.equals(SEARCH_BOTH)){
            url += "?query="+query;
        } else {
            url += type+"?query="+query;
        }

        if(loggedId > 0){
            url += "&contributor_id="+loggedId;
        }

        return url;
    }

    public static String getApiCommentUrl(String slug) {
        return BASE_URL_API + "article/" + slug + "/comment";
    }

    public static String getContributorDetailUrl(String username) {
        return BASE_URL + "contributor/" + username + "/detail";
    }

    public static String getContributorConversationUrl(String username) {
        return BASE_URL + "account/message/conversation/" + username;
    }

    public static String getArticleUrl(String slug) {
        return BASE_URL + slug;
    }

    public static String getContributorUrl(String username) {
        return BASE_URL + "contributor/" + username;
    }

    public static String getShareArticleText(String slug) {
        return "Hey checkout infogue.id article " + BASE_URL + slug + " via infogue.id";
    }

    public static String getShareContributorText(String username) {
        return "Hey checkout infogue.id contributor " + BASE_URL + "contributor/" + username + " via infogue.id";
    }

    public static String getDisqusUrl(int idPost, String slug, String title, String shortName) {
        return URL_DISQUS_TEMPLATE +
                "?shortname=" + shortName +
                "&identifier=" + idPost +
                "&url=" + BASE_URL + slug +
                "&title=" + title;
    }
}
