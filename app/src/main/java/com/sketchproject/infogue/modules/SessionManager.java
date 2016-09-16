package com.sketchproject.infogue.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.sketchproject.infogue.fragments.LoginFragment;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Handle session data and preferences.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 10/04/2016 18.16 18.17.
 */
@SuppressWarnings("unused")
public class SessionManager {
    public int PRIVATE_MODE = 0;

    private SharedPreferences pref;
    private Editor editor;
    private Context mContext;

    private static final String PREF_NAME = "com.sketchproject.infogue";
    private static final String IS_LOGIN = "isLoggedIn";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_ARTICLE = "article";
    public static final String KEY_FOLLOWER = "follower";
    public static final String KEY_FOLLOWING = "following";
    public static final String KEY_ABOUT = "about";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_COVER = "cover";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_TOKEN_GCM = "gcm";
    public static final String KEY_IS_FOLLOWING = "followed";
    public static final String KEY_STATUS = "status";
    public static final String KEY_NOTIFICATION = "notification";
    public static final String KEY_USER_LEARNED = "learned";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    /**
     * Default constructor of SessionManager
     *
     * @param context parent context
     */
    public SessionManager(Context context) {
        mContext = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    /**
     * Create login session
     */
    public boolean createLoginSession(HashMap<String, Object> session) {
        editor.putBoolean(IS_LOGIN, true);

        editor.putInt(KEY_ID, Integer.parseInt(String.valueOf(session.get(KEY_ID))));
        editor.putString(KEY_USERNAME, String.valueOf(session.get(KEY_USERNAME)));
        editor.putString(KEY_TOKEN, String.valueOf(session.get(KEY_TOKEN)));
        editor.putString(KEY_TOKEN_GCM, String.valueOf(session.get(KEY_TOKEN_GCM)));
        editor.putString(KEY_NAME, String.valueOf(session.get(KEY_NAME)));
        editor.putString(KEY_LOCATION, String.valueOf(session.get(KEY_LOCATION)));
        editor.putString(KEY_ABOUT, String.valueOf(session.get(KEY_ABOUT)));
        editor.putString(KEY_AVATAR, String.valueOf(session.get(KEY_AVATAR)));
        editor.putString(KEY_COVER, String.valueOf(session.get(KEY_COVER)));
        editor.putString(KEY_STATUS, String.valueOf(session.get(KEY_STATUS)));
        editor.putInt(KEY_ARTICLE, Integer.parseInt(String.valueOf(session.get(KEY_ARTICLE))));
        editor.putInt(KEY_FOLLOWER, Integer.parseInt(String.valueOf(session.get(KEY_FOLLOWER))));
        editor.putInt(KEY_FOLLOWING, Integer.parseInt(String.valueOf(session.get(KEY_FOLLOWING))));

        return editor.commit();
    }

    /**
     * Set String session
     *
     * @param key   session
     * @param value session
     * @return boolean
     */
    public boolean setSessionData(String key, String value) {
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * Set boolean session
     *
     * @param key   session
     * @param value session
     * @return boolean
     */
    public boolean setSessionData(String key, boolean value) {
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * Set integer session
     *
     * @param key   session
     * @param value session
     * @return boolean
     */
    public boolean setSessionData(String key, int value) {
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * Retrieve session with return data String
     *
     * @param key      session
     * @param defValue session
     * @return boolean
     */
    public String getSessionData(String key, String defValue) {
        return pref.getString(key, defValue);
    }

    /**
     * Retrieve session with return data boolean
     *
     * @param key      session
     * @param defValue session
     * @return boolean
     */
    public boolean getSessionData(String key, boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    /**
     * Retrieve session with return data integer
     *
     * @param key      session
     * @param defValue session
     * @return boolean
     */
    public int getSessionData(String key, int defValue) {
        return pref.getInt(key, defValue);
    }

    /**
     * Quick check if id which given matched with active session.
     *
     * @param id reference id of session data
     * @return boolean indicate is me or not
     */
    public boolean isMe(int id) {
        return id != 0 && getSessionData(SessionManager.KEY_ID, 0) == id;
    }

    /**
     * Quick check if username which given matched with active session.
     *
     * @param username reference unique user identity of session data
     * @return boolean indicate is me or not
     */
    public boolean isMe(String username) {
        return getSessionData(SessionManager.KEY_ID, "").equals(username);
    }

    /**
     * Get stored session data.
     *
     * @return HashMap
     */
    public HashMap<String, Object> getUserDetails() {
        HashMap<String, Object> user = new HashMap<>();
        user.put(KEY_ID, pref.getInt(KEY_ID, 0));
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_LOCATION, pref.getString(KEY_LOCATION, null));
        user.put(KEY_AVATAR, pref.getString(KEY_AVATAR, null));
        user.put(KEY_COVER, pref.getString(KEY_COVER, null));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        user.put(KEY_TOKEN_GCM, pref.getString(KEY_TOKEN_GCM, null));
        user.put(KEY_ARTICLE, pref.getInt(KEY_ARTICLE, 0));
        user.put(KEY_FOLLOWER, pref.getInt(KEY_FOLLOWER, 0));
        user.put(KEY_FOLLOWING, pref.getInt(KEY_FOLLOWING, 0));
        user.put(KEY_STATUS, pref.getString(KEY_STATUS, null));
        return user;
    }

    /**
     * Retrieve all session.
     *
     * @return Map
     */
    public Map<String, ?> getAllSession() {
        return pref.getAll();
    }

    /**
     * Retrieve size of session
     *
     * @return int
     */
    public int getSessionSize() {
        return pref.getAll().size();
    }

    /**
     * Clear session details
     */
    public boolean logoutUser() {
        editor.remove(KEY_ID);
        editor.remove(KEY_NAME);
        editor.remove(KEY_LOCATION);
        editor.remove(KEY_AVATAR);
        editor.remove(KEY_COVER);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_TOKEN_GCM);
        editor.remove(KEY_ARTICLE);
        editor.remove(KEY_FOLLOWER);
        editor.remove(KEY_FOLLOWING);
        editor.remove(KEY_STATUS);
        editor.remove(IS_LOGIN);

        FacebookSdk.sdkInitialize(mContext);
        LoginManager.getInstance().logOut();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(LoginFragment.TWITTER_KEY, LoginFragment.TWITTER_SECRET);
        Fabric.with(mContext, new Twitter(authConfig));
        Twitter.logOut();

        // editor.clear();

        return editor.commit();
    }

    /**
     * Quick check for login.
     **/
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

}
