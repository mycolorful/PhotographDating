package per.yrj.photographdating.request;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.callback.ChatCallBack;
import per.yrj.photographdating.constant.Action;

/**
 * 三种request的封装：auth,invitation,text
 * Created by YiRenjie on 2016/6/18.
 */
public class RequestBean {
    private final String type;
    private static String sequence;
    private String action;
    private String sender;
    private String token;
    private String receiver;
    private String content;
    private ChatCallBack chatCallBack;

    private Map<String, Object> map;

    public RequestBean(ChatCallBack chatCallBack, @NonNull String action) {
        this(chatCallBack, action, null, null);
        if (!action.equals(Action.Request.AUTH)){
            throw new IllegalArgumentException("action只能为auth");
        }
    }

    public RequestBean(ChatCallBack chatCallBack, @NonNull String action, @NonNull String receiver, @NonNull String content) {
        this.chatCallBack = chatCallBack;
        this.action = action;
        type = "request";
        sequence = UUID.randomUUID().toString();
        token = MyApplication.getCurrentAccount().getToken();
        sender = MyApplication.getCurrentAccount().getAccount();

        map = new HashMap<>();
        map.put("type", type);
        map.put("sequence", sequence);
        map.put("sender", sender);
        map.put("token", token);
        map.put("action", action);
        if (!action.equals(Action.Request.AUTH)) {
            map.put("receiver", receiver);
            map.put("content", content);
        }
        this.receiver = receiver;
        this.content = content;
    }

    public Map<String, Object> getMap() {
        return map;
    }


    public String getTransport() {
        Log.d("", "" + map.toString());

        return new Gson().toJson(map);
    }

    public String getSequence() {
        return sequence;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ChatCallBack getChatCallBack() {
        return chatCallBack;
    }
}
