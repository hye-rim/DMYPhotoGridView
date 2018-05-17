package com.hackday.dmyphotogridview_parkhyerim;

import android.app.Application;

import com.bumptech.glide.Glide;

/**
 * Created by hyerim on 2018. 5. 18....
 */
public class BaseApplication extends Application {
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }
}
