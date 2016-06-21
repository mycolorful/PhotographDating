package per.yrj.photographdating.constant;

/**
 * Created by YiRenjie on 2016/6/11.
 */
public class Url {
    public static String BASE_HTTP = "http://119.29.24.200:8080/ChatServer";
    public static String BASE_HOST = "119.29.24.200";
    public static int BASE_PORT = 9090;

    /**
     * 登录部分的url地址
     */
    public final static String URL_HTTP_LOGIN = BASE_HTTP + "/login";
    public final static String URL_HTTP_REGISTER = BASE_HTTP + "/register";
    public final static String URL_HTTP_LOGOUT = BASE_HTTP + "/logout";


    public final static String URL_HTTP_SEARCH = BASE_HTTP + "/user/search";
}
