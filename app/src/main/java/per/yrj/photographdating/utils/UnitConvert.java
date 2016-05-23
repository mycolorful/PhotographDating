package per.yrj.photographdating.utils;

import android.content.Context;

import per.yrj.photographdating.MyApplication;

/**
 * Created by Administrator on 2016/5/10.
 */
public class UnitConvert {
    public static Context mContext = MyApplication.getContext();

    public static int dip2px(int dip){
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dip*scale);
    }

    public static int px2dip(int px){
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (px/scale);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(float pxValue) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(float spValue) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
