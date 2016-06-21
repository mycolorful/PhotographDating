package per.yrj.photographdating.callback;

import java.lang.reflect.ParameterizedType;

/**
 * Created by YiRenjie on 2016/6/11.
 */
public abstract class CallBackObject<T> {
    private Class<T> clazz;

    @SuppressWarnings("unchecked")
    public CallBackObject() {
        ParameterizedType type = (ParameterizedType) this.getClass()
                .getGenericSuperclass();
        this.clazz = (Class<T>) type.getActualTypeArguments()[0];
    }

    public abstract void onSuccess(T t, String action);

    public abstract void onFailure(int error, String msg);

    public Class<T> getClazz() {
        return clazz;
    }
}
