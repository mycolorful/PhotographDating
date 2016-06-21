package per.yrj.photographdating.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.activities.ChatActivity;
import per.yrj.photographdating.activities.MainActivity;

/**
 * Created by YiRenjie on 2016/6/18.
 */
public class CommonUtils {
    private static Context mContext;
    static {
        mContext = MyApplication.getContext();
    }

    /**
     * 判断服务是否运行
     *
     * @param clazz
     *            要判断的服务的class
     * @return
     */
    public static boolean isServiceRunning(Class<? extends Service> clazz) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(100);
        for (int i = 0; i < services.size(); i++) {
            String className = services.get(i).service.getClassName();
            if (className.equals(clazz.getName())) {
                return true;
            }
        }
        return false;
    }

    public static String getFirstAlpha(String inputString) {
        // String pinYin = getPinYin(inputString);
        // if (pinYin != null && pinYin.length() > 0) {
        // return pinYin.substring(0, 1).toUpperCase();
        // }

        if (inputString != null) {

            String[] array = PinyinHelper.toHanyuPinyinStringArray(inputString
                    .charAt(0));

            if (array == null) {
                return inputString.substring(0, 1).toUpperCase();
            } else {
                return array[0].toUpperCase();
            }
        }

        return "";
    }

    public static String getDateFormat(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static boolean shouldNotify(){
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo task = manager.getRunningTasks(1).get(0);
        String shortClassName = task.topActivity.getShortClassName();
        if (shortClassName.equals(".activities.MainActivity")||shortClassName.equals(".activities.ChatActivity")) {
            return false;
        }
        return true;
    }

    /**
     * 判断当前网络是否连接
     *
     * @return
     */
    public static boolean isNetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 关闭软键盘
     * @param activity 当前的activity
     */
    public static void hideSoftKeyboard(Activity activity) {

        Log.i("test==hideSoftKeyboard", activity.toString());
        //1.得到InputMethodManager对象
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
        //2.调用hideSoftInputFromWindow方法隐藏软键盘
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0); //强制隐藏键盘
    }

    public static String string2MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}
