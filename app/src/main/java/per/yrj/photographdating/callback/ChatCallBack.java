package per.yrj.photographdating.callback;

/**
 * Created by YiRenjie on 2016/6/18.
 */
public interface ChatCallBack {

    void onSuccess();

    void onProgress();

    void onError(int errCode, String msg);

}
