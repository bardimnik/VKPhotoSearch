package com.opiumfive.vkphotosearch;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.vk.sdk.VKSdk;
import com.crashlytics.android.answers.Answers;
import io.fabric.sdk.android.Fabric;

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
        Fabric.with(this, new Answers(), new Crashlytics());
        VKSdk.initialize(this);
    }
}
