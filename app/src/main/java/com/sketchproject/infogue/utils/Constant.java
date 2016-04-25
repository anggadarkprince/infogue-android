package com.sketchproject.infogue.utils;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 10.17.
 */
public interface Constant {
    String BASE_URL = "http://www.infogue.id/";
    String BASE_URL_API = "http://192.168.43.141:8000/api/";
    String SHORT_NAME = "info-gue";
    String URL_FEEDBACK = BASE_URL + "contact";
    String URL_FORGOT = BASE_URL + "auth/forgot";
    String URL_HELP = BASE_URL + "faq";
    String URL_APP = "http://play.google.com/store/apps/details?id=com.sketchproject.infogue";

    String URL_AVATAR_DEFAULT = "http://infogue.id/images/contributors/noavatar.jpg";
    String URL_COVER_DEFAULT = "http://infogue.id/images/covers/noavatar.jpg";

    String URL_DISQUS_TEMPLATE = "http://infogue.angga-ari.com/mobiletemplate.html";

    String[] jokes = {"Syahrini", "Jupe", "Depe", "Nabilah JKT48", "Raisa"};

    String REQUEST_EXIST = "exist"; // code 400 bad request
    String REQUEST_SUCCESS = "success"; // code 200 ok
    String REQUEST_UNREGISTERED = "unregistered"; // 403 forbidden
    String REQUEST_RESTRICT = "restrict"; // code 403 forbidden
    String REQUEST_GRANTED = "granted"; // code 200  ok
    String REQUEST_MISMATCH = "mismatch"; // code 401 unauthorized
    String REQUEST_FAILURE = "failure"; // code 500 internal error
    String REQUEST_DENIED = "denied"; // code 400 bad request

    String URL_API_REGISTER = BASE_URL_API + "account/register";
    String URL_API_LOGIN = BASE_URL_API + "account/login";
    String URL_API_CATEGORY = BASE_URL_API + "category";
    String URL_API_FEATURED = BASE_URL_API + "featured";

}
