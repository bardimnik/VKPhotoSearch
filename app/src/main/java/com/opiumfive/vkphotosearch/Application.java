package com.opiumfive.vkphotosearch;

import android.content.Context;

import com.vk.sdk.VKSdk;

/**
 * Created by allsw on 11.07.2016.
 */

public class Application extends android.app.Application {

    private static Application mInstance;

    public Application() {
        mInstance = this;
    }

    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
