package com.sketchproject.infogue.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    public int PRIVATE_MODE = 0;

    private SharedPreferences pref;
    private Editor editor;

    // Shared pref unique file name
    private static final String PREF_NAME = "INFOGUE_PREF";

    // All Shared Preferences keys status
    private static final String IS_LOGIN = "isLoggedIn";

    // User pref key
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
    public static final String KEY_IS_FOLLOWING = "followed";
    public static final String KEY_USER_LEARNED = "learned";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    /**
     * Create login session
     */
    public boolean createLoginSession(HashMap<String, Object> session) {
        // Storing login data
        editor.putBoolean(IS_LOGIN, true);

        editor.putInt(KEY_ID, Integer.parseInt(String.valueOf(session.get(KEY_ID))));
        editor.putString(KEY_USERNAME, String.valueOf(session.get(KEY_USERNAME)));
        editor.putString(KEY_TOKEN, String.valueOf(session.get(KEY_TOKEN)));
        editor.putString(KEY_NAME, String.valueOf(session.get(KEY_NAME)));
        editor.putString(KEY_LOCATION, String.valueOf(session.get(KEY_LOCATION)));
        editor.putString(KEY_ABOUT, String.valueOf(session.get(KEY_ABOUT)));
        editor.putString(KEY_AVATAR, String.valueOf(session.get(KEY_AVATAR)));
        editor.putString(KEY_COVER, String.valueOf(session.get(KEY_COVER)));
        editor.putInt(KEY_ARTICLE, Integer.parseInt(String.valueOf(session.get(KEY_ARTICLE))));
        editor.putInt(KEY_FOLLOWER, Integer.parseInt(String.valueOf(session.get(KEY_FOLLOWER))));
        editor.putInt(KEY_FOLLOWING, Integer.parseInt(String.valueOf(session.get(KEY_FOLLOWING))));

        // commit changes
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
    @Nullable
    public String getSessionData(String key, @Nullable String defValue) {
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
     * Get stored session data
     *
     * @return HashMap
     */
    @SuppressWarnings("unused")
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();

        // Populate user data
        user.put(KEY_ID, pref.getString(KEY_ID, null));
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_LOCATION, pref.getString(KEY_LOCATION, null));
        user.put(KEY_AVATAR, pref.getString(KEY_AVATAR, null));
        user.put(KEY_COVER, pref.getString(KEY_COVER, null));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        return user;
    }

    /**
     * Retrieve all session
     *
     * @return Map
     */
    @SuppressWarnings("unused")
    public Map<String, ?> getAllSession() {
        return pref.getAll();
    }

    /**
     * Retrieve size of session
     *
     * @return int
     */
    @SuppressWarnings("unused")
    public int getSesionSize() {
        return pref.getAll().size();
    }

    /**
     * Clear session details
     */
    public boolean logoutUser() {
        // Clearing all data from Shared Preferences
        editor.remove(KEY_ID);
        editor.remove(KEY_NAME);
        editor.remove(KEY_LOCATION);
        editor.remove(KEY_AVATAR);
        editor.remove(KEY_COVER);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_TOKEN);
        editor.remove(IS_LOGIN);

        // editor.clear();

        return editor.commit();
    }

    /**
     * Quick check for login
     **/
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

}
