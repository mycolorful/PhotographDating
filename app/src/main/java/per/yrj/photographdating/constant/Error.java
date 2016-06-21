package per.yrj.photographdating.constant;

/**
 * Created by YiRenjie on 2016/6/11.
 */
public interface Error {
    int ERROR_SERVER = 1;
    int ERROR_CLIENT_NET = 2;

    public interface Login {
        int PASSWORD_ERROR = 100;
        int ACCOUNT_MISS = 101;
    }

    public interface Register {
        int ACCOUNT_EXIST = 150;
    }
}
