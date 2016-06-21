package per.yrj.photographdating.future;

/**
 * Created by YiRenjie on 2016/6/16.
 */
public abstract class Future {
    abstract boolean isCanceled ();
    abstract void cancel();
    abstract boolean isExecuted();
}
