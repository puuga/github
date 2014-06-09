package com.appspace.roomlinktest;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context ctx;

	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		ctx = context;
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			sendNotification("Send error", "Send error: "
					+ intent.getExtras().toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
				.equals(messageType)) {
			sendNotification("Send error", "Deleted messages on server: "
					+ intent.getExtras().toString());
		} else {
			// sendNotification("Received: " + intent.getExtras().toString());
			// Log.d("roomlinktest", "Received: " +
			// intent.getExtras().toString());
			// Log.d("roomlinktest", "Received id: " +
			// intent.getExtras().getString("id"));
			// Log.d("roomlinktest", "Received title: " +
			// intent.getExtras().getString("title"));
			// Log.d("roomlinktest", "Received message: " +
			// intent.getExtras().getString("message"));
			System.out.println("Received Received: "
					+ intent.getExtras().toString());
			System.out.println("Received id: "
					+ intent.getExtras().getString("id"));
			System.out.println("Received title: "
					+ intent.getExtras().getString("title"));
			System.out.println("Received message: "
					+ intent.getExtras().getString("message"));
			System.out.println("Received messageIdId: "
					+ intent.getExtras().getString("messageIdId"));

			String id = intent.getExtras().getString("id");
			String title = intent.getExtras().getString("title");
			String message = intent.getExtras().getString("message");
			String messageIdId = intent.getExtras().getString("messageIdId");
			if (message == null)
				sendNotification(title, message);
			else
				sendNotification(title, message, messageIdId);
		}
		// Log.d("roomlinktest", "onReceive");
		System.out.println("onReceive");
		setResultCode(Activity.RESULT_OK);
	}

	private void sendNotification(String title, String msg) {
		// Log.d("roomlinktest", "sendNotification");
		System.out.println("sendNotification");
		mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
		// new Intent(ctx, MainActivity.class), 0);
		Intent notificationIntent = new Intent(ctx, MainActivity.class);
		notificationIntent.putExtra("item_id", "1001");
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				notificationIntent, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				ctx).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private void sendNotification(String title, String msg, String messageIdId) {
		// Log.d("roomlinktest", "sendNotification");
		System.out.println("sendNotification");
		mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
		// new Intent(ctx, MainActivity.class), 0);
		Intent notificationIntent = new Intent(ctx, MainActivity.class);
		notificationIntent.putExtra("messageIdId", messageIdId);
		// PendingIntent.FLAG_UPDATE_CURRENT
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				notificationIntent, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				ctx).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
