package com.opiumfive.vkphotosearch;

import android.content.Context;
import android.content.SharedPreferences;


public class SPmanager {

    private volatile static SPmanager mInstance;

    private static final String USER_PREFERENCES_NAME = "user_preferences";
    private final static String KEY_TOOLTIPS_SHOWN = "key_tooltips_shown";
    private final static String KEY_LAST_LAT = "key_lat";
    private final static String KEY_LAST_LONG = "key_long";

    private boolean mTooltipsShown;
    private float mLastLat;
    private float mLastLong;


    public boolean getTooltipsShown() {
        return mTooltipsShown;
    }

    public void setTooltipsShown(boolean tooltipsShown) {
        this.mTooltipsShown = tooltipsShown;
        persistBoolean(KEY_TOOLTIPS_SHOWN, this.mTooltipsShown);
    }

    public float getLastLat() {
        return mLastLat;
    }

    public void setLastLat(float lastLat) {
        this.mLastLat = lastLat;
        persistFloat(KEY_LAST_LAT, this.mLastLat);
    }

    public float getLastLong() {
        return mLastLong;
    }

    public void setLastLong(float lastLong) {
        this.mLastLong = lastLong;
        persistFloat(KEY_LAST_LONG, this.mLastLong);
    }

    private SPmanager() {
        SharedPreferences preferences;
        final Context context = Application.getAppContext();
        preferences = context.getSharedPreferences(USER_PREFERENCES_NAME, context.MODE_PRIVATE);
        mTooltipsShown = preferences.getBoolean(KEY_TOOLTIPS_SHOWN, false);
        mLastLat = preferences.getFloat(KEY_LAST_LAT, 0.0f);
        mLastLong = preferences.getFloat(KEY_LAST_LONG, 0.0f);
    }

    public static synchronized SPmanager getInstance() {
        if (mInstance == null) {
            mInstance = new SPmanager();
        }
        return mInstance;
    }

    private void persistBoolean(final String key, final boolean value) {
        SharedPreferences preferences;
        final Context context = Application.getAppContext();
        preferences = context.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(key, value).commit();
    }

    private void persistFloat(final String key, final float value) {
        SharedPreferences preferences;
        final Context context = Application.getAppContext();
        preferences = context.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putFloat(key, value).commit();
    }
}
