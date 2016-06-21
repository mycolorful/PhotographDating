package per.yrj.photographdating.action;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.File;
import java.util.Map;

import per.yrj.photographdating.database.InvitationDao;
import per.yrj.photographdating.domain.Invitation;
import per.yrj.photographdating.network.NetWorkRequestManager;
import per.yrj.photographdating.receiver.PushReceiver;

public class InvitationAction extends Action {

	@Override
	public String getAction() {
		return "invitation";
	}

	@Override
	public void doAction(Context context, Map<String, Object> data) {
		if (data == null) {
			return;
		}

		String receiver = data.get("receiver").toString();
		String sender = data.get("sender").toString();

		String name = null;
		String icon = null;

		Object nameObj = data.get("invitor_name");
		if (nameObj != null) {
			name = (String) nameObj;
		}

		Object iconObj = data.get("invitor_icon");
		if (iconObj != null) {
			icon = (String) iconObj;
		}

		// 存取数据
		InvitationDao invitationDao = new InvitationDao(context);
		Invitation invitation = invitationDao.queryInvitation(receiver, sender);
		if (invitation == null) {
			invitation = new Invitation();
			invitation.setAccount(sender);
			invitation.setOwner(receiver);
			invitation.setAgree(false);
			if (icon != null) {
				invitation.setIcon(icon);
			}
			invitation.setName(name);
			invitationDao.addInvitation(invitation);
		} else {
			invitation.setAgree(false);
			if (icon != null) {
				invitation.setIcon(icon);
			}
			invitationDao.updateInvitation(invitation);
		}

		String friendIcon = invitation.getIcon();
		if (!TextUtils.isEmpty(friendIcon)) {
			// todo 下载朋友icon

		}

		// 发送广播
		Intent intent = new Intent(PushReceiver.ACTION_INVATION);
		intent.putExtra(PushReceiver.KEY_FROM, sender);
		intent.putExtra(PushReceiver.KEY_TO, receiver);
		context.sendBroadcast(intent);
	}
}
