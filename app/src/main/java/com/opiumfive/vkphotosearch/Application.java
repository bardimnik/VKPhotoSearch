package com.opiumfive.vkphotosearch;

import com.vk.sdk.VKSdk;

/**
 * Created by allsw on 11.07.2016.
 */

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
