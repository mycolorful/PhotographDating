package per.yrj.photographdating.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YiRenjie on 2016/6/12.
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    private DBOpenHelper(Context context) {
        super(context, SQL.NAME, null, SQL.VERSION);
    }

    private static DBOpenHelper mInstance;

    public static DBOpenHelper getInstance(Context context){
        if (mInstance == null){
            synchronized (DBOpenHelper.class){
                if (mInstance == null){
                    mInstance = new DBOpenHelper(context);
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL.Account.SQL_CREATE_TABLE);
        db.execSQL(SQL.Conversation.SQL_CREATE_TABLE);
        db.execSQL(SQL.Message.SQL_CREATE_TABLE);
        db.execSQL(SQL.Friend.SQL_CREATE_TABLE);
        db.execSQL(SQL.Invitation.SQL_CREATE_TABLE);
        db.execSQL(SQL.BackTask.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
