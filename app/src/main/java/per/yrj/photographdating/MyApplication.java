package per.yrj.photographdating;

import android.app.Application;
import android.content.Context;

import per.yrj.photographdating.database.AccountDao;
import per.yrj.photographdating.domain.Account;

/**
 * Created by YiRenjie on 2016/5/23.
 */
public class MyApplication extends Application {
    private static MyApplication mApplication;
    private static Account mCurrentAccount;
    private static AccountDao mAccountDao;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        mAccountDao = new AccountDao(mApplication);
    }

    public static Context getContext(){
        return mApplication;
    }

    public static Account getCurrentAccount(){
        if (mCurrentAccount == null){
            mCurrentAccount = mAccountDao.getCurrentAccount();
        }
        return mCurrentAccount;
    }

    /**
     * 传入一个新的账户替代当前使用的账户，如果传入的账户不存在则会创建一个
     * @param account 新的账户
     * @return true如果替换成功,false如果没有替换
     */
    public boolean changeCurrentAccount(Account account){
        // 先获取要被替换掉的账户，将current设置为false
        if (mCurrentAccount != null) {
            // 如果要新的账户和当前账户是同一个账户，则返回false。
            if (mCurrentAccount.getAccount().equals(account.getAccount())) {
                return false;
            }
            mCurrentAccount.setCurrent(false);
            mAccountDao.updateAccount(mCurrentAccount);
        }

        // 将新的账户current设置为true，如果不存在就添加到数据库，否则更新
        account.setCurrent(true);
        if (mAccountDao.getByAccount(account.getAccount()) == null){
            mAccountDao.addAccount(account);
        }else {
            mAccountDao.updateAccount(account);
        }
        mCurrentAccount = account;
        return true;
    }

}
