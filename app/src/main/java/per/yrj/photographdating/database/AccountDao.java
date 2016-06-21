package per.yrj.photographdating.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import per.yrj.photographdating.domain.Account;

/**
 * Created by YiRenjie on 2016/6/12.
 */
public class AccountDao {
    private DBOpenHelper mHelper;

    public AccountDao(Context context) {
        mHelper = DBOpenHelper.getInstance(context);
    }

    public Account getCurrentAccount() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String sql = "select * from " + SQL.Account.TABLE_NAME + " where "
                + SQL.Account.COLUMN_CURRENT + "=1";
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            Account account = new Account();
            account.setAccount(cursor.getString(cursor
                    .getColumnIndex(SQL.Account.COLUMN_ACCOUNT)));
            account.setArea(cursor.getString(cursor
                    .getColumnIndex(SQL.Account.COLUMN_AREA)));
            account.setCurrent(cursor.getInt(cursor
                    .getColumnIndex(SQL.Account.COLUMN_CURRENT)) == 1);
            account.setIcon(cursor.getString(cursor
                    .getColumnIndex(SQL.Account.COLUMN_ICON)));
            account.setName(cursor.getString(cursor
                    .getColumnIndex(SQL.Account.COLUMN_NAME)));
            account.setSex(cursor.getInt(cursor
                    .getColumnIndex(SQL.Account.COLUMN_SEX)));
            account.setSign(cursor.getString(cursor
                    .getColumnIndex(SQL.Account.COLUMN_SIGN)));
            account.setToken(cursor.getString(cursor
                    .getColumnIndex(SQL.Account.COLUMN_TOKEN)));

            return account;
        }

        return null;
    }

    public List<Account> getAllAccount() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String sql = "select * from " + SQL.Account.TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        List<Account> list = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (list == null) {
                    list = new ArrayList<Account>();
                }
                Account account = new Account();

                account.setAccount(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_ACCOUNT)));
                account.setArea(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_AREA)));
                account.setCurrent(cursor.getInt(cursor
                        .getColumnIndex(SQL.Account.COLUMN_CURRENT)) == 1);
                account.setIcon(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_ICON)));
                account.setName(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_NAME)));
                account.setSex(cursor.getInt(cursor
                        .getColumnIndex(SQL.Account.COLUMN_SEX)));
                account.setSign(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_SIGN)));
                account.setToken(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_TOKEN)));
                list.add(account);
            }
        }
        return list;
    }

    public void addAccount(Account account) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SQL.Account.COLUMN_ACCOUNT, account.getAccount());
        values.put(SQL.Account.COLUMN_AREA, account.getArea());
        values.put(SQL.Account.COLUMN_ICON, account.getIcon());
        values.put(SQL.Account.COLUMN_NAME, account.getName());
        values.put(SQL.Account.COLUMN_SEX, account.getSex());
        values.put(SQL.Account.COLUMN_SIGN, account.getSign());
        values.put(SQL.Account.COLUMN_TOKEN, account.getToken());
        values.put(SQL.Account.COLUMN_CURRENT, account.isCurrent() ? 1 : 0);

        db.insert(SQL.Account.TABLE_NAME, null, values);
    }

    public void updateAccount(Account account) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SQL.Account.COLUMN_AREA, account.getArea());
        values.put(SQL.Account.COLUMN_ICON, account.getIcon());
        values.put(SQL.Account.COLUMN_NAME, account.getName());
        values.put(SQL.Account.COLUMN_SEX, account.getSex());
        values.put(SQL.Account.COLUMN_SIGN, account.getSign());
        values.put(SQL.Account.COLUMN_TOKEN, account.getToken());
        values.put(SQL.Account.COLUMN_CURRENT, account.isCurrent() ? 1 : 0);

        String whereClause = SQL.Account.COLUMN_ACCOUNT + "=?";
        String[] whereArgs = new String[]{account.getAccount()};
        db.update(SQL.Account.TABLE_NAME, values, whereClause, whereArgs);
    }

    public Account getByAccount(String account) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String sql = "select * from " + SQL.Account.TABLE_NAME + " where "
                + SQL.Account.COLUMN_ACCOUNT + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{account});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Account a = new Account();

                a.setAccount(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_ACCOUNT)));
                a.setArea(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_AREA)));
                a.setCurrent(cursor.getInt(cursor
                        .getColumnIndex(SQL.Account.COLUMN_CURRENT)) == 1);
                a.setIcon(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_ICON)));
                a.setName(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_NAME)));
                a.setSex(cursor.getInt(cursor
                        .getColumnIndex(SQL.Account.COLUMN_SEX)));
                a.setSign(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_SIGN)));
                a.setToken(cursor.getString(cursor
                        .getColumnIndex(SQL.Account.COLUMN_TOKEN)));
                return a;
            }
        }
        return null;
    }
}
