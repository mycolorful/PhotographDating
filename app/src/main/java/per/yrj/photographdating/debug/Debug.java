package per.yrj.photographdating.debug;

import android.content.Context;
import android.util.Log;

import per.yrj.photographdating.MyApplication;

/**
 * Created by YiRenjie on 2016/6/11.
 */
public class Debug {
    private static Context mContext = MyApplication.getContext();
    private static final boolean DEBUG = true;

    public static void i(Debugable debugable, String msg){
        if (!DEBUG){
            return;
        }
        if (debugable.shouldDebug()) {
            Log.i(debugable.tag(), msg);
        }
    }

    public static void e(Debugable debugable, String msg){
        if (!DEBUG){
            return;
        }
        if (debugable.shouldDebug()){
            Log.e(debugable.tag(), msg);
        }
    }
}
