package per.yrj.photographdating.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.apache.mina.core.session.IoSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.callback.ChatCallBack;
import per.yrj.photographdating.debug.Debug;
import per.yrj.photographdating.debug.Debugable;
import per.yrj.photographdating.callback.CallBackObject;
import per.yrj.photographdating.constant.Action;
import per.yrj.photographdating.constant.Error;
import per.yrj.photographdating.constant.Url;
import per.yrj.photographdating.domain.Account;
import per.yrj.photographdating.domain.Message;
import per.yrj.photographdating.future.HttpFuture;
import per.yrj.photographdating.request.RequestBean;

/**
 * Created by YiRenjie on 2016/6/11.
 */
public class NetWorkRequestManager implements Debugable, Connector.ConnectListener, Connector.IoListener {
    private OkHttpClient mOkHttpClient;
    private static NetWorkRequestManager mInstance;
    private Handler mHandler;
    private Connector mConnector;
    private OnPushListener pushListener;
    private List<NetConnectListener> connectListeners = new LinkedList<>();
    private String mAuthSequence;
    private Map<String, String> headers = new HashMap<>();
    /**
     * 保存所有的request请求，为后面请求回调做准备
     */
    private Map<String, RequestBean> mRequests;

    /**
     * 表示是登录还是注册
     */
    private boolean isLogin = true;

    private NetWorkRequestManager() {
        mOkHttpClient = new OkHttpClient();
        mHandler = new Handler(Looper.getMainLooper());
        mRequests = new HashMap<>();
    }

    public static NetWorkRequestManager getInstance() {
        if (mInstance == null) {
            synchronized (NetWorkRequestManager.class) {
                if (mInstance == null) {
                    mInstance = new NetWorkRequestManager();
                }
            }
        }
        return mInstance;
    }

    public HttpFuture loginOrRegister(String account, String psd, CallBackObject callBack) {
        return loginOrRegister(account, psd, callBack, Url.URL_HTTP_LOGIN);
    }

    /**
     * @param account  用户名
     * @param psd      密码
     * @param callBack 灰调方法
     * @param url      请求的地址
     */
    private HttpFuture loginOrRegister(final String account, final String psd, final CallBackObject callBack, String url) {
        // 如果是请求登录，那就设为true
        isLogin = url.equals(Url.URL_HTTP_LOGIN);

        FormBody fromBody = new FormBody.Builder()
                .add("account", account)
                .add("password", psd)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(fromBody)
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Debug.i(NetWorkRequestManager.this, "登录请求失败" + e.getMessage());
                if (callBack != null) {
                    postFailure(callBack, Error.ERROR_SERVER, "服务器异常:" + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //如果callBack为null，则不对返回结果进行解析。
                if (callBack == null) {
                    return;
                }

                String json = response.body().string();
                Debug.i(NetWorkRequestManager.this, json);
                if (response.code() == 200) {
                    JsonParser parser = new JsonParser();
                    JsonObject root = parser.parse(json).getAsJsonObject();
                    if (root != null) {
                        JsonPrimitive flagObj = root.getAsJsonPrimitive("flag");
                        boolean flag = flagObj.getAsBoolean();
                        //判断返回的flag值
                        if (flag) {
                            JsonObject dataObj = root.getAsJsonObject("data");
                            // 判断data是否为空
                            if (dataObj != null) {
                                Object data = new Gson().fromJson(dataObj, callBack.getClazz());
                                postSuccess(callBack, data);

                            } else {
                                postSuccess(callBack, null);
                            }
                        } else {
                            // 如果返回错误
                            // 获得错误code
                            JsonPrimitive errorCodeObj = root
                                    .getAsJsonPrimitive("errorCode");
                            // 获得错误string
                            JsonPrimitive errorStringObj = root
                                    .getAsJsonPrimitive("errorString");

                            int errorCode = errorCodeObj.getAsInt();
                            String errorString = errorStringObj
                                    .getAsString();

                            //如果用户名不存在，自动注册一个
                            if (errorCode == Error.Login.ACCOUNT_MISS) {
                                loginOrRegister(account, psd, callBack, Url.URL_HTTP_REGISTER);
                            } else {
                                postFailure(callBack, errorCode, errorString);
                            }
                        }
                    } else {
                        postFailure(callBack, Error.ERROR_SERVER, "服务器异常");
                    }
                } else {
                    postFailure(callBack, Error.ERROR_SERVER, "服务器异常");
                }
            }
        });
        return new HttpFuture(call);
    }

    private void postSuccess(final CallBackObject callBack, final Object obj) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //判断是登录成功还是注册成功
                if (isLogin) {
                    callBack.onSuccess(obj, Action.LOGIN);
                } else {
                    callBack.onSuccess(obj, Action.REGISTER);
                }
            }
        });
    }

    private void postFailure(final CallBackObject callBack, final int errorCode, final String errorString) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onFailure(errorCode, errorString);
            }
        });
    }

    /**
     * 连接认证
     */
    public void auth(final ChatCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBean authRequest = new RequestBean(callBack, Action.Request.AUTH);
                if (mConnector == null) {
                    mConnector = new Connector(Url.BASE_HOST, Url.BASE_PORT, 3);
                }

                mAuthSequence = authRequest.getSequence();
                connectServer();
                addRequest(authRequest);
            }
        }).start();

    }

    /**
     * 将请求加入connector的工作队列，同时自己保存一份
     *
     * @param request 请求
     */
    private void addRequest(RequestBean request) {
        mConnector.addRequest(request);
        mRequests.put(request.getSequence(), request);
    }

    private void connectServer() {
        if (mConnector != null) {
            mConnector.connect();
            mConnector.setConnectListener(this);
            mConnector.setIOListener(this);
        }
    }

    /**
     * 发送消息
     *
     * @param message  要发送的消息
     * @param callBack 回调方法
     */
    public void sendMessage(final Message message, final String action, final ChatCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBean sendRequest = new RequestBean(callBack, action
                        , message.getAccount(), message.getContent());
                if (mConnector == null) {
                    mConnector = new Connector(Url.BASE_HOST, Url.BASE_PORT, 3);
                }
                connectServer();
                addRequest(sendRequest);

            }
        }).start();
    }

    /**
     * 搜索用户
     *
     * @param callBack
     * @return
     */
    @SuppressWarnings("rawtypes")
    public HttpFuture searchContact(String search,
                                    final CallBackObject callBack) {
        Account curAccount = MyApplication.getCurrentAccount();

        FormBody fromBody = new FormBody.Builder()
                .add("search", search)
                .build();

        Request request = new Request.Builder()
                .url(Url.URL_HTTP_SEARCH)
                .addHeader("account", curAccount.getAccount())
                .addHeader("token", curAccount.getToken())
                .post(fromBody)
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Debug.i(NetWorkRequestManager.this, "搜索请求失败" + e.getMessage());
                if (callBack != null) {
                    postFailure(callBack, Error.ERROR_SERVER, "服务器异常:" + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //如果callBack为null，则不对返回结果进行解析。
                if (callBack == null) {
                    return;
                }

                String json = response.body().string();
                Debug.i(NetWorkRequestManager.this, json);
                if (response.code() == 200) {
                    JsonParser parser = new JsonParser();
                    JsonObject root = parser.parse(json).getAsJsonObject();
                    if (root != null) {
                        JsonPrimitive flagObj = root.getAsJsonPrimitive("flag");
                        boolean flag = flagObj.getAsBoolean();
                        //判断返回的flag值
                        if (flag) {
                            JsonObject dataObj = root.getAsJsonObject("data");
                            // 判断data是否为空
                            if (dataObj != null) {
                                Object data = new Gson().fromJson(dataObj, callBack.getClazz());
                                postSuccess(callBack, data);

                            } else {
                                postSuccess(callBack, null);
                            }
                        } else {
                            // 如果返回错误
                            // 获得错误code
                            JsonPrimitive errorCodeObj = root
                                    .getAsJsonPrimitive("errorCode");
                            // 获得错误string
                            JsonPrimitive errorStringObj = root
                                    .getAsJsonPrimitive("errorString");

                            int errorCode = errorCodeObj.getAsInt();
                            String errorString = errorStringObj
                                    .getAsString();

                            postFailure(callBack, errorCode, errorString);
                        }
                    } else {
                        postFailure(callBack, Error.ERROR_SERVER, "服务器异常");
                    }
                } else {
                    postFailure(callBack, Error.ERROR_SERVER, "服务器异常");
                }
            }
        });

        return new HttpFuture(call);
    }

    @Override
    public void connecting() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onReconnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onOutPutFailed(String msg, Exception e) {

    }

    @Override
    public void onInputComed(IoSession session, Object msg) {
        Debug.i(this, "onInputComed收到消息：" + msg);
        if (!(msg instanceof String)) {
            return;
        }
        String json = (String) msg;
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(json).getAsJsonObject();

        // 获得方向:是请求还是response
        JsonPrimitive typeJson = root.getAsJsonPrimitive("type");
        String type = typeJson.getAsString();

        // 获得序列号
        JsonPrimitive sequenceJson = root
                .getAsJsonPrimitive("sequence");
        String sequence = sequenceJson.getAsString();
        if ("request".equalsIgnoreCase(type)) {
            // 服务器推送消息
            JsonPrimitive actionJson = root.getAsJsonPrimitive("action");
            String action = actionJson.getAsString();
            if (pushListener != null) {
                Debug.i(this, json);
                boolean pushed = pushListener.onPush(action, (Map<String, Object>) new Gson().fromJson(root, new TypeToken<Map<String, Object>>() {
                }.getType()));
                if (pushed) {
                    session.write("{type:'response',sequence:'"
                            + sequence + "',flag:" + true + "}");
                } else {
                    session.write("{type:'response',sequence:'"
                            + sequence + "',flag:" + false
                            + ",errorCode:1,errorString:'客户端未处理成功!'}");
                }
            }
        } else if ("response".equalsIgnoreCase(type)) {
            // 请求返回response
            JsonPrimitive flagJson = root.getAsJsonPrimitive("flag");
            boolean flag = flagJson.getAsBoolean();
            // 消息发送结果只有 成功或者 失败,不需要返回对象
            if (flag) {
                // 如果返回的序列号是auth发出的，则表示身份验证成功。
                if (sequence.equals(mAuthSequence)) {
                    RequestBean authRequest = mRequests.remove(mAuthSequence);
                    Debug.i(this, "onInputCom:认证成功");
                    authRequest.getChatCallBack().onSuccess();
                    return;
                }

                // 如果不是auth发出的，则表示消息成功发送
                final RequestBean request = mRequests.remove(sequence);
                if (request != null) {
                    if (request.getSequence().equals(sequence)) {
                        final ChatCallBack callBack = request.getChatCallBack();
                        if (callBack != null) {
                            // 在主线程中调用
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onSuccess();
                                }
                            });
                        }
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.getChatCallBack().onError(0, "序列号不匹配");
                            }
                        });
                    }
                }
            } else {
                // flag为false，则表示失败
                if (sequence.equals(mAuthSequence)) {
                    ListIterator<NetConnectListener> iterator = connectListeners.listIterator();
                    final RequestBean request = mRequests.remove(sequence);
                    if (request != null) {
                        if (request.getSequence().equals(sequence)) {
                            final ChatCallBack callBack = request.getChatCallBack();
                            if (callBack != null) {
                                // 在主线程中调用
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callBack.onError(1, "服务器处理失败");
                                    }
                                });
                            }
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    request.getChatCallBack().onError(2, "服务器处理失败，序列号不匹配");
                                }
                            });
                        }
                    }
                }

            }
        }
    }

    public boolean post(String path, Map<String, String> paramaters){
        String url = Url.BASE_HTTP + path;

        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> me : paramaters.entrySet()){
            builder.add(me.getKey(), me.getValue());
        }
        FormBody fromBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(fromBody)
                .build();

        String result = null;
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            result = response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
        }
            return parseResult(result);

    }

    private boolean parseResult(String result) {
        if ("error".equals(result)) {
            return false;
        } else {
            JsonParser parser = new JsonParser();
            try {
                JsonObject root = parser.parse(result).getAsJsonObject();
                JsonPrimitive flagObject = root.getAsJsonPrimitive("flag");
                return flagObject.getAsBoolean();
            } catch (Exception e) {
                return false;
            }
        }
    }

    /*public HttpFuture downloadFile(String path, File file, final HMFileCallBack callBack) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30 * 1000);
        client.setMaxRetriesAndTimeout(5, 30 * 1000);
        client.setResponseTimeout(30 * 1000);
        String url = HMURL.BASE_HTTP + path;

        for (Map.Entry<String, String> me : headers.entrySet()) {
            client.addHeader(me.getKey(), me.getValue());
        }

        return new HttpFuture(client.get(url, new FileAsyncHttpResponseHandler(
                file) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                callBack.onSuccess(file);
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                super.onProgress(bytesWritten, totalSize);

                callBack.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, File file) {
                callBack.onError(HMError.ERROR_SERVER,
                        "服务器异常 : " + throwable.getMessage());
            }
        }));
    }*/

    /**
     * 移除连接监听
     *
     * @param listener
     */
    public void removeConnectionListener(NetConnectListener listener) {
        if (connectListeners.contains(listener)) {
            connectListeners.remove(listener);
        }
    }

    /**
     * 添加连接监听
     *
     * @param listener
     */
    public void addConnectionListener(NetConnectListener listener) {
        if (!connectListeners.contains(listener)) {
            connectListeners.add(listener);
        }
    }

    public void closeSocket() {
        if (mConnector != null && mConnector.isConnected()) {
            mConnector.disconnect();
            mConnector = null;
        }
    }

    public interface NetConnectListener {
        /**
         * 正在连接
         */
        void onConnecting();

        /**
         * 已经连接
         */
        void onConnected();

        /**
         * 已经断开连接
         */
        void onDisconnected();

        /**
         * 正在重试连接
         */
        void onReconnecting();

    }

    public interface OnPushListener {
        /**
         * @param type 消息类型
         * @param data
         * @return
         */
        boolean onPush(String type, Map<String, Object> data);
    }

    /**
     * 添加消息推送监听
     *
     * @param listener
     */
    public void setPushListener(OnPushListener listener) {
        this.pushListener = listener;
    }

    @Override
    public boolean shouldDebug() {
        return true;
    }

    @Override
    public String tag() {
        return "NetWorkRequestManager";
    }
}
