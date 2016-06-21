package per.yrj.photographdating.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.action.Action;
import per.yrj.photographdating.callback.ChatCallBack;
import per.yrj.photographdating.debug.Debug;
import per.yrj.photographdating.debug.Debugable;
import per.yrj.photographdating.domain.Account;
import per.yrj.photographdating.network.NetWorkRequestManager;
import per.yrj.photographdating.utils.CommonUtils;

/**
 * Created by YiRenjie on 2016/6/18.
 */
public class ChatCoreService extends Service implements NetWorkRequestManager.NetConnectListener, Debugable, NetWorkRequestManager.OnPushListener {
    private NetWorkRequestManager chatManager;

    private int reconnectCount = 0;// 重连次数

    private Map<String, Action> actionMaps = new HashMap<String, Action>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            connectServer();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        chatManager = NetWorkRequestManager.getInstance();
        chatManager.addConnectionListener(this);
        chatManager.setPushListener(this);

        // 注册网络监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);

        scanClass();
    }

    private void connectServer() {
        Account account = MyApplication.getCurrentAccount();
        if (account != null) {
            chatManager.auth(new ChatCallBack() {
                @Override
                public void onSuccess() {
                    Debug.i(ChatCoreService.this, "认证成功");
                }

                @Override
                public void onProgress() {

                }

                @Override
                public void onError(int errCode, String msg) {

                }
            });
        }
    }

    /**
     * 加载action类
     */
    private void scanClass() {
        String[] array = getResources().getStringArray(R.array.actions);

        if (array == null) {
            return;
        }

        String packageName = getPackageName();
        ClassLoader classLoader = getClassLoader();

        for (int i = 0; i < array.length; i++) {
            try {

                Class<?> clazz = classLoader.loadClass(packageName + "."
                        + array[i]);

                Class<?> superclass = clazz.getSuperclass();

                if (superclass != null
                        && Action.class.getName().equals(superclass.getName())) {

                    Action action = (Action) clazz.newInstance();
                    actionMaps.put(action.getAction(), action);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatManager.removeConnectionListener(this);
        unregisterReceiver(mReceiver);

        // 断开连接
        chatManager.closeSocket();
    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

        if (CommonUtils.isNetConnected()) {
            // 有网络的
            Log.d("Core", "网络断开重连");
            reconnectCount++;

            if (reconnectCount < 10) {
                connectServer();
            }
        }
    }

    @Override
    public void onReconnecting() {

    }

    @Override
    public boolean shouldDebug() {
        return true;
    }

    @Override
    public String tag() {
        return "ChatCoreService";
    }

    @Override
    public boolean onPush(String action, Map<String, Object> data) {
        Debug.i(this, data.toString());
        Action actioner = actionMaps.get(action);
        if (actioner != null) {
            actioner.doAction(this, data);
        }

        return true;
    }
}
