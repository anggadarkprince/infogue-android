package com.sketchproject.infogue.utils;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 17.33.
 */
public class APIBuilder {
    public static final String BASE_URL = "http://192.168.43.141:8000/";
    public static final String BASE_URL_API = BASE_URL + "api/";
    public static final String ASSET_IMAGES_URL = BASE_URL + "images/";
    public static final String URL_HELP = BASE_URL + "faq";
    public static final String URL_FORGOT = BASE_URL + "auth/forgot";
    public static final String URL_FEEDBACK = BASE_URL + "contact";
    public static final String URL_API_COMMENT = BASE_URL_API + "comment";
    public static final String URL_API_UNFOLLOW = BASE_URL_API + "unfollow";
    public static final String URL_API_FOLLOW = BASE_URL_API + "follow";
    public static final String URL_API_MESSAGE = BASE_URL_API + "message";
    public static final String URL_API_WALLET = BASE_URL_API + "wallet/transaction";
    public static final String URL_API_WALLET_WITHDRAW = BASE_URL_API + "wallet/withdraw";
    public static final String URL_API_WALLET_DELETE = BASE_URL_API + "wallet/withdraw";
    public static final String URL_API_GALLERY = BASE_URL_API + "image/gallery";
    public static final String URL_API_GALLERY_UPLOAD = BASE_URL_API + "image/upload";
    public static final String URL_API_GALLERY_DELETE = BASE_URL_API + "image/delete";
    public static final String URL_API_ARTICLE = BASE_URL_API + "article";
    public static final String URL_API_HIT = BASE_URL_API + "article/hit";
    public static final String URL_API_RATE = BASE_URL_API + "article/rate";
    public static final String URL_API_FEATURED = BASE_URL_API + "featured";
    public static final String URL_API_CATEGORY = BASE_URL_API + "category";
    public static final String URL_API_BANK = BASE_URL_API + "bank";
    public static final String URL_API_SETTING = BASE_URL_API + "account";
    public static final String URL_API_LOGIN = BASE_URL_API + "account/login";
    public static final String URL_API_REGISTER = BASE_URL_API + "account/register";
    public static final String URL_API_OAUTH_FACEBOOK = BASE_URL_API + "oauth/facebook";
    public static final String URL_API_OAUTH_TWITTER = BASE_URL_API + "oauth/twitter";
    public static final String URL_API_GCM_REGISTER = BASE_URL_API + "gcm/register";

    public static final String URL_APP = "http://play.google.com/store/apps/details?id=com.sketchproject.infogue";
    public static final String URL_DISQUS_TEMPLATE = "http://infogue.angga-ari.com/mobiletemplate.html";
    public static final String URL_FACEBOOK_DEVELOPER = "https://facebook.com/sketchproject";
    public static final String URL_TWITTER_DEVELOPER = "https://twitter.com/sketchproject";

    public static final String REQUEST_EXIST = "exist";                 // code 400 bad request
    public static final String REQUEST_DENIED = "denied";               // code 400 bad request
    public static final String REQUEST_SUCCESS = "success";             // code 200 ok
    public static final String REQUEST_UNREGISTERED = "unregistered";   // code 403 forbidden
    public static final String REQUEST_RESTRICT = "restrict";           // code 403 forbidden
    public static final String REQUEST_GRANTED = "granted";             // code 200  ok
    public static final String REQUEST_MISMATCH = "mismatch";           // code 401 unauthorized
    public static final String REQUEST_FAILURE = "failure";             // code 500, 503, 404 server error, maintenance, not found
    public static final String REQUEST_NOT_FOUND = "not found";         // code 400 bad request

    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_LOGIN = "login";

    public static final String METHOD = "_method";
    public static final String METHOD_PUT = "put";
    public static final String METHOD_DELETE = "delete";

    public static final String SEARCH_BOTH = "both";
    public static final String SEARCH_CONTRIBUTOR = "contributor";
    public static final String SEARCH_ARTICLE = "article";

    public static final int TIMEOUT_SHORT = 15000;
    public static final int TIMEOUT_MEDIUM = 30000;
    public static final int TIMEOUT_LONG = 50000;

    public static final int NO_RETRY = 0;

    /**
     * Build featured article url.
     * http://infogue.id/api/featured/{latest}
     * http://infogue.id/api/featured/{headline}?page={3}
     *
     * @param slugFeatured slug featured like headline, trending, latest, random, etc
     * @param page         current page request
     * @return string url
     */
    public static String getApiFeaturedUrl(String slugFeatured, int page) {
        String url = URL_API_FEATURED + "/" + slugFeatured;
        if (page > 0) {
            url += "?page=" + page;
        }

        return url;
    }

    /**
     * Build category article url.
     * http://infogue.id/api/category/{sport}
     * http://infogue.id/api/category/{sport}page={1}
     * http://infogue.id/api/category/{entertainment}/{music}?page={3}
     *
     * @param slugCategory    slug category slug like entertainment, sport, tech-computer, etc
     * @param slugSubCategory slug subcategory related with category slug
     * @param page            current page request
     * @return string url
     */
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

    /**
     * Build tag article url.
     * http://infogue.id/api/tag/{sport}
     *
     * @param tag article tag
     * @return string url
     */
    public static String getApiTagUrl(String tag) {
        return BASE_URL_API + "tag/" + tag;
    }

    /**
     * Build follower list url by contributor username.
     * http://infogue.id/api/contributor/{anggadarkprince}/{followers}
     * http://infogue.id/api/contributor/{anggadarkprince}/{following}
     * http://infogue.id/api/contributor/{anggadarkprince}/{following}?contributor_id={3}
     * <p/>
     * page query not defined here because next request retrieve url from server
     * http://infogue.id/api/contributor/{anggadarkprince}/{following}?contributor_id={3}&page={2}
     *
     * @param type      followers or following list
     * @param relatedId related with logged user to check if they has following this username
     * @param username  owner data list
     * @return string url
     */
    public static String getApiFollowerUrl(String type, int relatedId, String username) {
        String optionalQuery = "";
        String url = BASE_URL_API + "contributor/" + username + "/" + type.toLowerCase();

        if (relatedId > 0) {
            String related = "contributor_id=" + relatedId;
            optionalQuery += related;
        }

        if (!optionalQuery.isEmpty()) {
            url += "?" + optionalQuery;
        }

        return url;
    }

    /**
     * Build article list url.
     * http://infogue.id/api/contributor/{anggadarkprince}/article
     * http://infogue.id/api/contributor/{anggadarkprince}/article?contributor_id={2}&my_article=true&query={keywords}
     *
     * @param loggedId       article author id used as user reference to check if they has following this username
     * @param authorUsername article author username
     * @param isMyArticle    find out if it is my article
     * @param query          keyword when try to filter and search article
     * @return string url
     */
    public static String getApiArticleUrl(int loggedId, String authorUsername, boolean isMyArticle, String query) {
        String author = "contributor_id=" + loggedId;
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

    /**
     * Build contributor url.
     * http://infogue.id/api/contributor/{anggadarkprince}
     *
     * @param username contributor identity
     * @return string url
     */
    public static String getApiContributorUrl(String username) {
        return BASE_URL_API + "contributor/" + username;
    }

    /**
     * Build conversation url.
     * http://infogue.id/api/message/conversation/{anggadarkprince}
     *
     * @param username contributor who interact with
     * @return string url
     */
    public static String getApiConversationUrl(String username) {
        return BASE_URL_API + "message/conversation/" + username;
    }

    /**
     * Build contributor suggestion.
     *
     * @param query keyword name or username
     * @return string url
     */
    public static String getApiContributorSuggestionUrl(String query) {
        return BASE_URL_API + "contributor/search?query=" + query;
    }

    /**
     * Build conversation url.
     * http://infogue.id/api/message
     *
     * @return string url
     */
    public static String getApiMessageUrl() {
        return BASE_URL_API + "message";
    }

    /**
     * Build contributor url.
     * http://infogue.id/api/contributor/{anggadarkprince}
     *
     * @param username contributor identity
     * @return string url
     */
    public static String getApiContributorUrl(String username, int loggedId) {
        return BASE_URL_API + "contributor/" + username + "?contributor_id=" + loggedId;
    }

    /**
     * Build contributor stream url.
     * http://infogue.id/api/contributor/{anggadarkprince}/stream
     *
     * @param username contributor identity
     * @param loggedId logged user if exist
     * @return string url
     */
    public static String getApiStreamUrl(String username, int loggedId) {
        return BASE_URL_API + "contributor/" + username + "/stream?contributor_id=" + loggedId;
    }

    /**
     * Build article post url request for edit just retrieve the article.
     * http://infogue.id/api/article/brand-new-day
     *
     * @param slug unique article identity
     * @return string url
     */
    public static String getApiPostUrl(String slug) {
        return BASE_URL_API + "article/" + slug;
    }

    /**
     * Build article post url request for show post retrieve along with author.
     * http://infogue.id/api/article/brand-new-day?contributor_id={2}
     *
     * @param slug     unique article identity
     * @param loggedId with related
     * @return string url
     */
    public static String getApiPostUrl(String slug, int loggedId) {
        return BASE_URL_API + "article/" + slug + "?contributor_id=" + loggedId;
    }

    /**
     * Build search url.
     * http://infogue.id/api/search?query={something awesome}&contributor_id={2}
     * http://infogue.id/api/search/contributor?query={angga}
     * http://infogue.id/api/search/article?query={news}
     *
     * @param query    keywords
     * @param type     filter result for contributor, article or both
     * @param loggedId related url for searching contributor to check if they has following this username
     * @return string url
     */
    public static String getApiSearchUrl(String query, String type, int loggedId) {
        String url = BASE_URL_API + "search";
        if (type == null || type.equals(SEARCH_BOTH)) {
            url += "?query=" + query;
        } else {
            url += "/" + type + "?query=" + query;
        }

        if (loggedId > 0) {
            url += "&contributor_id=" + loggedId;
        }

        return url;
    }

    /**
     * Build comment url.
     * http://infogue.id/api/article/brand-new-day/comment
     *
     * @param slug article slug related with comment
     * @return string url
     */
    public static String getApiCommentUrl(String slug) {
        return BASE_URL_API + "article/" + slug + "/comment";
    }

    /**
     * Build resend confirmation email url.
     *
     * @param token user token activation
     * @return string url
     */
    public static String getResendEmail(String token) {
        return APIBuilder.BASE_URL + "auth/resend/" + token;
    }

    /**
     * Build web url for article detail.
     * http://infogue.id/contributor/{anggadarkprince}/detail
     *
     * @param username contributor identity
     * @return string url
     */
    public static String getContributorDetailUrl(String username) {
        return BASE_URL + "contributor/" + username + "/detail";
    }

    /**
     * Build conversation url.
     * http://infogue.id/account/message/conversation/{anggadarkprince}
     *
     * @param username conversation with
     * @return url string
     */
    public static String getContributorConversationUrl(String username) {
        return BASE_URL + "account/message/conversation/" + username;
    }

    /**
     * Build web version of article url.
     * http://infogue.id/brand-new-day
     *
     * @param slug unique article identity
     * @return string url
     */
    public static String getArticleUrl(String slug) {
        return BASE_URL + slug;
    }

    /**
     * Build web version of contributor profile url.
     * http://infogue.id/contributor/{anggadarkprince}
     *
     * @param username unique identity
     * @return string url
     */
    public static String getContributorUrl(String username) {
        return BASE_URL + "contributor/" + username;
    }

    /**
     * Build share article text.
     *
     * @param slug unique article identity.
     * @return string url
     */
    public static String getShareArticleText(String slug) {
        return BASE_URL + slug;
    }

    /**
     * Build share contributor text.
     *
     * @param username unique article identity
     * @return string url
     */
    public static String getShareContributorText(String username) {
        return "Hey checkout infogue.id contributor " + BASE_URL + "contributor/" + username + " via infogue.id";
    }

    /**
     * Build url request disqus url form.
     *
     * @param idPost    identity to tell disqus find related comments
     * @param slug      unique article identity along id to find related comments
     * @param title     article title
     * @param shortName short name of disqus forum (info-gue)
     * @return string url
     */
    public static String getDisqusUrl(int idPost, String slug, String title, String shortName) {
        return URL_DISQUS_TEMPLATE +
                "?shortname=" + shortName +
                "&identifier=" + idPost +
                "&url=" + BASE_URL + slug +
                "&title=" + title;
    }
}
