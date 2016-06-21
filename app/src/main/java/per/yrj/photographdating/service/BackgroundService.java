package per.yrj.photographdating.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.database.BackTaskDao;
import per.yrj.photographdating.database.SQL;
import per.yrj.photographdating.domain.Account;
import per.yrj.photographdating.domain.NetTask;
import per.yrj.photographdating.network.NetWorkRequestManager;
import per.yrj.photographdating.utils.SerializableUtil;

public class BackgroundService extends IntentService {

	private Account account;

	public BackgroundService() {
		super("background");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		account = MyApplication.getCurrentAccount();
		String owner = account.getAccount();

		final BackTaskDao dao = new BackTaskDao(this);

		Map<Long, String> map = new HashMap<Long, String>();

		Cursor cursor = dao.query(owner, 0);
		if (cursor != null) {

			while (cursor.moveToNext()) {
				final long id = cursor.getLong(cursor
						.getColumnIndex(SQL.BackTask.COLUMN_ID));
				String filePath = cursor.getString(cursor
						.getColumnIndex(SQL.BackTask.COLUMN_PATH));

				map.put(id, filePath);
			}
			cursor.close();
		}

		for (Map.Entry<Long, String> me : map.entrySet()) {
			try {
				final Long id = me.getKey();
				String filePath = me.getValue();

				NetTask task = (NetTask) SerializableUtil.read(filePath);

				int type = task.getType();
				if (type == NetTask.TYPE_NORMAL) {
					doNormalTask(dao, id, task);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void doNormalTask(final BackTaskDao dao, final Long id, NetTask task) {
		boolean result = NetWorkRequestManager.getInstance().post(task.getPath(),
				task.getParams());

		if (result) {
			dao.updateState(id, 2);
		}
	}

}
