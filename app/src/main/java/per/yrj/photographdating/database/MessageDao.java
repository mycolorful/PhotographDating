package per.yrj.photographdating.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import per.yrj.photographdating.domain.Conversation;
import per.yrj.photographdating.domain.Message;


public class MessageDao {
	private DBOpenHelper helper;

	public MessageDao(Context context) {
		helper = DBOpenHelper.getInstance(context);
	}

	public void addMessage(Message message) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SQL.Message.COLUMN_ACCOUNT, message.getAccount());
		values.put(SQL.Message.COLUMN_CONTENT, message.getContent());
		values.put(SQL.Message.COLUMN_CREATE_TIME, message.getCreateTime());
		values.put(SQL.Message.COLUMN_DIRECTION, message.getDirection());
		values.put(SQL.Message.COLUMN_OWNER, message.getOwner());
		values.put(SQL.Message.COLUMN_STATE, message.getState());
		values.put(SQL.Message.COLUMN_TYPE, message.getType());
		values.put(SQL.Message.COLUMN_URL, message.getUrl());
		values.put(SQL.Message.COLUMN_READ, message.isRead() ? 1 : 0);

		message.setId(db.insert(SQL.Message.TABLE_NAME, null, values));

		String sql = "select * from " + SQL.Conversation.TABLE_NAME
				+ " where " + SQL.Conversation.COLUMN_ACCOUNT + "=? and "
				+ SQL.Conversation.COLUMN_OWNER + "=?";
		Cursor cursor = db.rawQuery(sql, new String[] { message.getAccount(),
				message.getOwner() });
		if (cursor != null && cursor.moveToNext()) {
			// String account = cursor.getString(cursor
			// .getColumnIndex(SQL.Conversation.COLUMN_ACCOUNT));
			// String content = cursor.getString(cursor
			// .getColumnIndex(SQL.Conversation.COLUMN_CONTENT));
			// String icon = cursor.getString(cursor
			// .getColumnIndex(SQL.Conversation.COLUMN_ICON));
			// String name = cursor.getString(cursor
			// .getColumnIndex(SQL.Conversation.COLUMN_NAME));
			// String owner = cursor.getString(cursor
			// .getColumnIndex(SQL.Conversation.COLUMN_OWNER));
			// int unread = cursor.getInt(cursor
			// .getColumnIndex(SQL.Conversation.COLUMN_UNREAD));
			// long updateTime = cursor.getLong(cursor
			// .getColumnIndex(SQL.Conversation.COLUMN_UPDATE_TIME));
			//

			// 关闭cursor
			cursor.close();
			cursor = null;

			int unread = 0;

			sql = "select count(_id) from " + SQL.Message.TABLE_NAME
					+ " where " + SQL.Message.COLUMN_READ + "=0 and "
					+ SQL.Message.COLUMN_ACCOUNT + "=? and "
					+ SQL.Message.COLUMN_OWNER + "=?";
			cursor = db.rawQuery(sql, new String[] { message.getAccount(),
					message.getOwner() });
			if (cursor != null && cursor.moveToNext()) {
				unread = cursor.getInt(0);
			}

			values = new ContentValues();
			values.put(SQL.Conversation.COLUMN_ACCOUNT, message.getAccount());

			int type = message.getType();
			if (type == 0) {
				values.put(SQL.Conversation.COLUMN_CONTENT,
						message.getContent());
			} else if (type == 1) {
				values.put(SQL.Conversation.COLUMN_CONTENT, "图片");
			}
			// values.put(SQL.Conversation.COLUMN_ICON,
			// conversation.getIcon());
			// values.put(SQL.Conversation.COLUMN_NAME,
			// conversation.getName());
			values.put(SQL.Conversation.COLUMN_OWNER, message.getOwner());
			values.put(SQL.Conversation.COLUMN_UNREAD, unread);
			values.put(SQL.Conversation.COLUMN_UPDATE_TIME,
					System.currentTimeMillis());

			String whereClause = SQL.Conversation.COLUMN_OWNER + "=? and "
					+ SQL.Conversation.COLUMN_ACCOUNT + "=?";
			String[] whereArgs = new String[] { message.getOwner(),
					message.getAccount() };

			db.update(SQL.Conversation.TABLE_NAME, values, whereClause,
					whereArgs);

		} else {
			Conversation conversation = new Conversation();
			conversation.setAccount(message.getAccount());
			int type = message.getType();
			if (type == 0) {
				conversation.setContent(message.getContent());
			} else if (type == 1) {
				conversation.setContent("图片");
			}
			// conversation.setIcon(message.get);
			// conversation.setName(message.get);
			conversation.setOwner(message.getOwner());
			conversation.setUnread(message.isRead() ? 0 : 1);
			conversation.setUpdateTime(System.currentTimeMillis());

			values = new ContentValues();
			values.put(SQL.Conversation.COLUMN_ACCOUNT,
					conversation.getAccount());
			values.put(SQL.Conversation.COLUMN_CONTENT,
					conversation.getContent());
			values.put(SQL.Conversation.COLUMN_ICON, conversation.getIcon());
			values.put(SQL.Conversation.COLUMN_NAME, conversation.getName());
			values.put(SQL.Conversation.COLUMN_OWNER, conversation.getOwner());
			values.put(SQL.Conversation.COLUMN_UNREAD,
					conversation.getUnread());
			values.put(SQL.Conversation.COLUMN_UPDATE_TIME,
					conversation.getUpdateTime());

			db.insert(SQL.Conversation.TABLE_NAME, null, values);
		}
	}

	public void updateMessage(Message message) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SQL.Message.COLUMN_CONTENT, message.getContent());
		values.put(SQL.Message.COLUMN_CREATE_TIME, message.getCreateTime());
		values.put(SQL.Message.COLUMN_DIRECTION, message.getDirection());
		values.put(SQL.Message.COLUMN_STATE, message.getState());
		values.put(SQL.Message.COLUMN_TYPE, message.getType());
		values.put(SQL.Message.COLUMN_URL, message.getUrl());
		values.put(SQL.Message.COLUMN_READ, message.isRead() ? 1 : 0);

		String whereClause = SQL.Message.COLUMN_ID + "=?";
		String[] whereArgs = new String[] { message.getId() + "" };
		db.update(SQL.Message.TABLE_NAME, values, whereClause, whereArgs);
	}

	public Cursor queryMessage(String owner, String account) {
		String sql = "select * from " + SQL.Message.TABLE_NAME + " where "
				+ SQL.Message.COLUMN_OWNER + "=? and "
				+ SQL.Message.COLUMN_ACCOUNT + "=? order by "
				+ SQL.Message.COLUMN_CREATE_TIME + " asc";
		SQLiteDatabase db = helper.getReadableDatabase();
		return db.rawQuery(sql, new String[] { owner, account });
	}

	public Cursor queryConversation(String owner) {
		String sql = "select * from " + SQL.Conversation.TABLE_NAME
				+ " where " + SQL.Conversation.COLUMN_OWNER + "=? order by "
				+ SQL.Conversation.COLUMN_UPDATE_TIME + " desc";
		SQLiteDatabase db = helper.getReadableDatabase();
		return db.rawQuery(sql, new String[] { owner });
	}

	public void clearUnread(String owner, String account) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SQL.Message.COLUMN_READ, 1);
		String whereClause = SQL.Message.COLUMN_OWNER + "=? and "
				+ SQL.Message.COLUMN_ACCOUNT + "=?";
		String[] whereArgs = new String[] { owner, account };
		db.update(SQL.Message.TABLE_NAME, values, whereClause, whereArgs);

		values = new ContentValues();
		values.put(SQL.Conversation.COLUMN_UNREAD, 0);
		whereClause = SQL.Conversation.COLUMN_OWNER + "=? and "
				+ SQL.Conversation.COLUMN_ACCOUNT + "=?";
		db.update(SQL.Conversation.TABLE_NAME, values, whereClause, whereArgs);
	}

	public int getAllUnread(String owner) {
		String sql = "select sum(" + SQL.Conversation.COLUMN_UNREAD
				+ ") from " + SQL.Conversation.TABLE_NAME + " where "
				+ SQL.Conversation.COLUMN_OWNER + "=?";

		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, new String[] { owner });
		int sum = 0;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				sum = cursor.getInt(0);
			}
			cursor.close();
		}
		return sum;
	}
}
