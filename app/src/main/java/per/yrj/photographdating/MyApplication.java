package per.yrj.photographdating;

import android.app.Application;
import android.content.Context;

/**
 * Created by YiRenjie on 2016/5/23.
 */
public class MyApplication extends Application {
    private static MyApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }

    public static Context getContext(){
        return mApplication;
    }
}
