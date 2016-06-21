package per.yrj.photographdating.action;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


import java.util.Map;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.activities.ChatActivity;
import per.yrj.photographdating.activities.MainActivity;
import per.yrj.photographdating.database.MessageDao;
import per.yrj.photographdating.domain.Message;
import per.yrj.photographdating.receiver.PushReceiver;
import per.yrj.photographdating.utils.CommonUtils;

public class TextAction extends Action {

    @Override
    public String getAction() {
        return "text";
    }

    @Override
    public void doAction(Context context, Map<String, Object> data) {
        if (data == null) {
            return;
        }

        String receiver = data.get("receiver").toString();
        String sender = data.get("sender").toString();

        String content = data.get("content").toString();

        // 数据存储
        MessageDao dao = new MessageDao(context);
        Message message = new Message();
        message.setAccount(sender);
        message.setContent(content);
        message.setCreateTime(System.currentTimeMillis());
        message.setDirection(1);
        message.setOwner(receiver);
        message.setRead(false);
        message.setType(0);
        dao.addMessage(message);

        // 发送广播
        Intent intent = new Intent(PushReceiver.ACTION_TEXT);
        intent.putExtra(PushReceiver.KEY_FROM, sender);
        intent.putExtra(PushReceiver.KEY_TO, receiver);
        intent.putExtra(PushReceiver.KEY_TEXT_CONTENT, content);
        context.sendBroadcast(intent);

        if (CommonUtils.shouldNotify()) {
            // 发送消息推送
            Intent[] intents = new Intent[2];
            intents[0] = new Intent(context, MainActivity.class);
            intents[1] = new Intent(context, ChatActivity.class);
            intents[1].putExtra(context.getPackageName() + "chatWithAccount", message.getAccount());
            PendingIntent pendingIntent = PendingIntent.getActivities(context,
                    0, intents, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.getContext());
            builder.setContentTitle(message.getAccount())
                    .setContentText(message.getContent())
                    .setTicker(message.getContent())
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
//                    .setPriority(Notification.PRIORITY_MAX)
                    .setAutoCancel(true);

            notificationManager.notify(0, builder.build());
        }
    }

}
