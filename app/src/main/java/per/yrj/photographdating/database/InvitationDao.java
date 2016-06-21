package per.yrj.photographdating.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import per.yrj.photographdating.domain.Invitation;


public class InvitationDao {
	private DBOpenHelper helper;

	public InvitationDao(Context context) {
		helper = DBOpenHelper.getInstance(context);
	}

	public Cursor queryCursor(String owner) {
		SQLiteDatabase db = helper.getReadableDatabase();

		String sql = "select * from " + SQL.Invitation.TABLE_NAME + " where "
				+ SQL.Invitation.COLUMN_OWNER + "=?";
		return db.rawQuery(sql, new String[] { owner });
	}

	public void addInvitation(Invitation invitation) {
		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(SQL.Invitation.COLUMN_OWNER, invitation.getOwner());
		values.put(SQL.Invitation.COLUMN_INVITATOR_ACCOUNT,
				invitation.getAccount());
		values.put(SQL.Invitation.COLUMN_INVITATOR_NAME, invitation.getName());
		values.put(SQL.Invitation.COLUMN_INVITATOR_ICON, invitation.getIcon());
		values.put(SQL.Invitation.COLUMN_CONTENT, invitation.getContent());
		values.put(SQL.Invitation.COLUMN_AGREE, invitation.isAgree() ? 1 : 0);
		db.insert(SQL.Invitation.TABLE_NAME, null, values);
	}

	public void updateInvitation(Invitation invitation) {
		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(SQL.Invitation.COLUMN_INVITATOR_NAME, invitation.getName());
		values.put(SQL.Invitation.COLUMN_INVITATOR_ICON, invitation.getIcon());
		values.put(SQL.Invitation.COLUMN_CONTENT, invitation.getContent());
		values.put(SQL.Invitation.COLUMN_AGREE, invitation.isAgree() ? 1 : 0);

		String whereClause = SQL.Invitation.COLUMN_OWNER + "=? and "
				+ SQL.Invitation.COLUMN_INVITATOR_ACCOUNT + "=?";
		String[] whereArgs = new String[] { invitation.getOwner(),
				invitation.getAccount() };

		db.update(SQL.Invitation.TABLE_NAME, values, whereClause, whereArgs);
	}

	public Invitation queryInvitation(String owner, String account) {
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select * from " + SQL.Invitation.TABLE_NAME + " where "
				+ SQL.Invitation.COLUMN_OWNER + "=? and "
				+ SQL.Invitation.COLUMN_INVITATOR_ACCOUNT + "=?";

		Cursor cursor = db.rawQuery(sql, new String[] { owner, account });
		Invitation invitation = null;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				// String account = cursor
				// .getString(cursor
				// .getColumnIndex(SQL.Invitation.COLUMN_INVITATOR_ACCOUNT));
				String name = cursor.getString(cursor
						.getColumnIndex(SQL.Invitation.COLUMN_INVITATOR_NAME));
				String icon = cursor.getString(cursor
						.getColumnIndex(SQL.Invitation.COLUMN_INVITATOR_ICON));
				boolean agree = cursor.getInt(cursor
						.getColumnIndex(SQL.Invitation.COLUMN_AGREE)) == 1;
				String content = cursor.getString(cursor
						.getColumnIndex(SQL.Invitation.COLUMN_CONTENT));
				// String owner = cursor.getString(cursor
				// .getColumnIndex(SQL.Invitation.COLUMN_OWNER));
				long id = cursor.getLong(cursor
						.getColumnIndex(SQL.Invitation.COLUMN_ID));

				invitation = new Invitation();
				invitation.setAccount(account);
				invitation.setAgree(agree);
				invitation.setContent(content);
				invitation.setIcon(icon);
				invitation.setName(name);
				invitation.setOwner(owner);
				invitation.setId(id);
			}
			cursor.close();
		}
		return invitation;
	}

	public boolean hasUnagree(String owner) {
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select count(" + SQL.Invitation.COLUMN_ID + ") from "
				+ SQL.Invitation.TABLE_NAME + " where "
				+ SQL.Invitation.COLUMN_AGREE + "=0";
		Cursor cursor = db.rawQuery(sql, null);

		int count = 0;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}
		return count != 0;
	}
}
