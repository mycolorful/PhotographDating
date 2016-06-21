package per.yrj.photographdating.request;

import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import per.yrj.photographdating.callback.ChatCallBack;
import per.yrj.photographdating.domain.Message;

/**
 * Created by YiRenjie on 2016/6/18.
 */
public class Request2Server {
    private ChatCallBack chatCallBack;
    private Message message;
    private Map<String, Object> map;

    public Request2Server(ChatCallBack chatCallBack, RequestBean request){
        this.chatCallBack = chatCallBack;

        map = new HashMap<>();
        if (request != null){
            map.putAll(request.getMap());
        }
    }

    public String getTransport() {
        Log.d("", "" + map.toString());

        return new Gson().toJson(map);
    }
}
