package per.yrj.photographdating.future;

import okhttp3.Call;

/**
 * Created by YiRenjie on 2016/6/16.
 */
public class HttpFuture extends Future{
    private Call mCall;

    public HttpFuture(Call call){
        this.mCall = call;
    }

    @Override
    public boolean isCanceled() {
        return mCall.isCanceled();
    }

    @Override
    public void cancel() {
        if (mCall!=null && !mCall.isCanceled() && mCall.isExecuted()){
            mCall.cancel();
        }
    }

    @Override
    public boolean isExecuted() {
        return mCall.isExecuted();
    }
}
