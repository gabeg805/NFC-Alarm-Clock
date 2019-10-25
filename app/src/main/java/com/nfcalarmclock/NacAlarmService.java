package com.nfcalarmclock;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Binder;
import android.os.IBinder;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import androidx.annotation.NonNull;

import androidx.core.app.JobIntentService;

/**
 * Receive the signal from the AlarmManager that it's time for the alarm to go
 * off, which in turn start the NacAlarmActivity.
 */
public class NacAlarmService
	extends JobIntentService
	//extends Service
{
 
	/**
	 */
	//@Override
	//public void onReceive(Context context, Intent intent)
	//{
	//	NacUtility.quickToast(context, "Alarm Broadcast Receiver!");
	//	Bundle bundle = NacIntent.getAlarmBundle(intent);
	//	Intent newIntent = NacIntent.createAlarmActivity(context, bundle);

	//	NacUtility.quickToast(context, "Starting alarm activity!");
	//	//newIntent.setFlag(Intent.FLAG_ACTIVITY_NEW_TASK);
	//	context.startActivity(newIntent);
	//}

    //private NotificationManager mNM;
	private NacNotification mNotification;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    //private int NOTIFICATION = R.string.local_service_started;

	private NacAlarm mAlarm;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
		NacAlarm alarm = NacIntent.getAlarm(work);
		int id = (alarm != null) ? alarm.getId() : -1;
		NacUtility.printf("enque work Id : %d");

        enqueueWork(context, NacAlarmService.class, id, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        //Log.i("SimpleJobIntentService", "Executing work: " + intent);
		NacUtility.printf("NacAlarmService onHandleWork!");
		Bundle bundle = NacIntent.getAlarmBundle(intent);
		Intent newIntent = NacIntent.createAlarmActivity(this, bundle);

		NacUtility.printf("Starting alarm activity!");
		//newIntent.setFlag(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(newIntent);

        //String label = intent.getStringExtra("label");
        //if (label == null) {
        //    label = intent.toString();
        //}
        //toast("Executing: " + label);
        //for (int i = 0; i < 5; i++) {
        //    Log.i("SimpleJobIntentService", "Running service " + (i + 1)
        //            + "/5 @ " + SystemClock.elapsedRealtime());
        //    try {
        //        Thread.sleep(1000);
        //    } catch (InterruptedException e) {
        //    }
        //}
        //Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("All work complete");
    }

    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
				NacUtility.quickToast(getApplicationContext(), text.toString());
                //Toast.makeText(SimpleJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    ///**
    // * Class for clients to access.  Because we know this service always
    // * runs in the same process as its clients, we don't need to deal with
    // * IPC.
    // */
    //public class LocalBinder extends Binder {
    //    NacAlarmService getService() {
    //        return NacAlarmService.this;
    //    }
    //}

    //@Override
    //public void onCreate() {
	//	NacUtility.printf("Alarm service onCreate()!");
    //    //mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	//	this.mNotification = new NacNotification(this);
	//	this.mAlarm = null;

    //    // Display a notification about us starting.  We put an icon in the status bar.
    //    //showNotification();
    //}

    //@Override
    //public int onStartCommand(Intent intent, int flags, int startId) {
    //    //Log.i("LocalService", "Received start id " + startId + ": " + intent);
	//	NacUtility.printf("Alarm service onStartCommand()!");
	//	this.mAlarm = NacIntent.getAlarm(intent);
	//	showNotification();
	//	Bundle bundle = NacIntent.getAlarmBundle(intent);
	//	Intent newIntent = NacIntent.createAlarmActivity(this, bundle);

	//	NacUtility.printf("Starting alarm activity!");
	//	//newIntent.setFlag(Intent.FLAG_ACTIVITY_NEW_TASK);
	//	startActivity(newIntent);

	//	// Maybe change the following. I'm not sure
    //    return START_NOT_STICKY;
    //}

    //@Override
    //public void onDestroy() {
    //    // Cancel the persistent notification.
    //    //mNM.cancel(NOTIFICATION);

    //    // Tell the user we stopped.
    //    //Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    //    NacUtility.quickToast(this, "Local service stopped!");
    //}

    //@Override
    //public IBinder onBind(Intent intent) {
    //    return mBinder;
    //}

    //// This is the object that receives interactions from clients.  See
    //// RemoteService for a more complete example.
    //private final IBinder mBinder = new LocalBinder();

    ///**
    // * Show a notification while this service is running.
    // */
    //private void showNotification() {
	//	//NacNotification notification = new NacNotification(this);

	//	this.mNotification.show(this.mAlarm);
    //    //// In this sample, we'll use the same text for the ticker and the expanded notification
    //    //CharSequence text = getText(R.string.local_service_started);

    //    //// The PendingIntent to launch our activity if the user selects this notification
    //    //PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
    //    //        new Intent(this, NacMainActivity.class), 0);
    //    //        //new Intent(this, LocalServiceActivities.Controller.class), 0);

    //    //// Set the info for the views that show in the notification panel.
    //    //Notification notification = new Notification.Builder(this)
    //    //        .setSmallIcon(R.drawable.stat_sample)  // the status icon
    //    //        .setTicker(text)  // the status text
    //    //        .setWhen(System.currentTimeMillis())  // the time stamp
    //    //        .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
    //    //        .setContentText(text)  // the contents of the entry
    //    //        .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
    //    //        .build();

    //    //// Send the notification.
    //    //mNM.notify(NOTIFICATION, notification);
    //}

}
