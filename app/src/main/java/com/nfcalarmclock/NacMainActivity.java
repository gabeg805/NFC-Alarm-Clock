package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.lang.StringBuilder;
import java.lang.System;

/**
 * The application's main activity.
 */
public class NacMainActivity
	extends AppCompatActivity
	implements View.OnClickListener,
		NacCardAdapter.OnUseNfcChangeListener,
		NacDialog.OnCancelListener,
		NacDialog.OnDismissListener
{

	/**
	 * Wait time between a notification posting and running the alarm activity
	 * for that notification.
	 */
	private static final long ACTIVE_ALARM_POST_DURATION = 2000;

	/**
	 * Wait time between a notification posting and running the alarm activity
	 * for that notification.
	 */
	private static final long SHOW_ALARM_ACTIVITY_DELAY = 2000;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Recycler view containing the alarm cards.
	 */
	private RecyclerView mRecyclerView;

	/**
	 * Floating action button to add new alarms.
	 */
	private FloatingActionButton mFloatingActionButton;

	/**
	 * Alarm card adapter.
	 */
	private NacCardAdapter mAdapter;

	/**
	 * Scan an NFC tag dialog.
	 */
	private NacScanNfcTagDialog mScanNfcTagDialog;

	/**
	 * Alarm activity delay handler.
	 */
	private Handler mActivityDelayHandler;

	/**
	 * Add an alarm that was created from the SET_ALARM intent.
	 */
	private void addSetAlarmFromIntent()
	{
		Intent intent = getIntent();
		NacCardAdapter cardAdapter = this.getCardAdapter();
		NacAlarm alarm = NacIntent.getSetAlarm(this, intent);

		if (alarm != null)
		{
			int id = cardAdapter.getUniqueId();

			alarm.setId(id);
			cardAdapter.add(alarm);
			cardAdapter.setWasAddedWithFloatingActionButton(true);
		}
	}

	/**
	 * Cleanup the show activity delay handler.
	 */
	private void cleanupShowActivityDelayHandler()
	{
		Handler handler = this.getShowActivityDelayHandler();
		if (handler != null)
		{
			handler.removeCallbacksAndMessages(null);
			this.mActivityDelayHandler = null;
		}
	}

	/**
	 * Setup an NFC scan checker, which checks if this activity was started by an
	 * NFC tag being scanned.
	 */
	private void dismissActiveAlarm()
	{
		Intent intent = getIntent();
		NacAlarm alarm = NacDatabase.findActiveAlarm(this);
		//NacAlarm alarm = NacNotificationHelper.findActiveAlarm(this);

		if (alarm == null)
		{
			return;
		}

		boolean success = NacContext.dismissForegroundServiceFromNfcScan(this,
			intent, alarm);

		if (!success)
		{
			this.showAlarmActivity(alarm);
		}
	}

	/**
	 * Show activity handler.
	 */
	private Handler getShowActivityDelayHandler()
	{
		return mActivityDelayHandler;
	}

	/**
	 * @return The alarm card adapter.
	 */
	private NacCardAdapter getCardAdapter()
	{
		return this.mAdapter;
	}

	/**
	 * @return The floating action button.
	 */
	private FloatingActionButton getFloatingActionButton()
	{
		return this.mFloatingActionButton;
	}

	/**
	 * @return The recycler view.
	 */
	private RecyclerView getRecyclerView()
	{
		return this.mRecyclerView;
	}

	/**
	 * @return Scan NFC tag dialog.
	 */
	private NacScanNfcTagDialog getScanNfcTagDialog()
	{
		return this.mScanNfcTagDialog;
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * Uncheck the NFC button when the dialog is canceled.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		NacAlarm alarm = (NacAlarm) dialog.getData();
		NacCardAdapter cardAdapter = this.getCardAdapter();
		NacCardHolder cardHolder = cardAdapter.getCardHolder(alarm);

		if (cardHolder != null)
		{
			cardHolder.doNfcButtonClick();
		}

		this.mScanNfcTagDialog = null;
		return true;
	}

	/**
	 * Add a new alarm when the floating action button is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		NacCardAdapter cardAdapter = this.getCardAdapter();

		cardAdapter.add();
		cardAdapter.setWasAddedWithFloatingActionButton(true);
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingActionButton = (FloatingActionButton) findViewById(
			R.id.fab_add_alarm);
		this.mRecyclerView = (RecyclerView) findViewById(
			R.id.content_alarm_list);
		this.mScanNfcTagDialog = null;

		if (this.wasNfcScannedForAlarm())
		{
			this.dismissActiveAlarm();
		}

		this.setupRecyclerView();
	}

	/**
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_action_bar, menu);
		return true;
	}

	/**
	 * Set the default (empty) NFC tag ID.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacSharedConstants cons = new NacSharedConstants(this);
		NacAlarm alarm = (NacAlarm) dialog.getData();

		alarm.setNfcTagId("");
		alarm.changed();
		NacUtility.quickToast(this, cons.getMessageNfcRequired());

		this.mScanNfcTagDialog = null;
		return true;
	}

	/**
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		setIntent(intent);

		if (this.wasNfcScannedForDialog())
		{
			NacSharedConstants cons = new NacSharedConstants(this);
			this.saveNfcTagId();
			NacUtility.quickToast(this, cons.getMessageNfcRequired());
		}
		else if (this.wasNfcScannedForAlarm())
		{
			this.dismissActiveAlarm();
		}
	}

	/**
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		switch (id)
		{
			case R.id.menu_settings:
				Intent settingsIntent = new Intent(this, NacSettingsActivity.class);
				startActivity(settingsIntent);
				return true;
			case R.id.menu_show_next_alarm:
				NacCardAdapter cardAdapter = this.getCardAdapter();
				cardAdapter.showNextAlarm();
				return true;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		this.setupActiveAlarmActivity();
		this.setupAlarmCardAdapter();
		this.setupFloatingActionButton();
		this.setupGoogleRatingDialog();
		this.addSetAlarmFromIntent();
		NacNfc.start(this);
	}

	/**
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		NacNfc.stop(this);
		this.cleanupShowActivityDelayHandler();
	}

	/**
	 */
	@Override
	public void onUseNfcChange(NacAlarm alarm)
	{
		if (!alarm.getUseNfc())
		{
			return;
		}

		NacScanNfcTagDialog dialog = new NacScanNfcTagDialog();

		dialog.build(this);
		dialog.saveData(alarm);
		dialog.addOnCancelListener(this);
		dialog.addOnDismissListener(this);
		dialog.show();

		this.mScanNfcTagDialog = dialog;
	}

	/**
	 * Save the scanned NFC tag ID.
	 */
	private void saveNfcTagId()
	{
		if (!this.wasNfcScannedForDialog())
		{
			return;
		}

		Intent intent = getIntent();
		Tag nfcTag = NacNfc.getTag(intent);
		if (nfcTag == null)
		{
			return;
		}

		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
		NacAlarm alarm = (NacAlarm) dialog.getData();
		String id = NacNfc.parseId(nfcTag);

		alarm.setNfcTagId(id);
		alarm.changed();
		dialog.dismissDialog();
	}

	/**
	 * Setup the alarm activity for any active alarms.
	 */
	private void setupActiveAlarmActivity()
	{
		//StatusBarNotification notification = NacNotificationHelper
		//	.getActiveNotification(this);
		//NacAlarm alarm = NacNotificationHelper.findAlarm(this, notification);
		NacAlarm alarm = NacDatabase.findActiveAlarm(this);

		if (this.shouldShowAlarmActivity(alarm))
		{
			if (this.shouldShowAlarmActivityDelayed(alarm))
			{
				this.showAlarmActivityDelayed(SHOW_ALARM_ACTIVITY_DELAY);
			}
			else
			{
				this.showAlarmActivity(alarm);
				//this.showAlarmActivity(notification);
			}
		}
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private void setupAlarmCardAdapter()
	{
		NacCardAdapter cardAdapter = this.getCardAdapter();
		cardAdapter.build();
		cardAdapter.setOnUseNfcChangeListener(this);
	}

	/**
	 * Setup the floating action button.
	 */
	private void setupFloatingActionButton()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		FloatingActionButton floatingButton = this.getFloatingActionButton();
		ColorStateList color = ColorStateList.valueOf(shared.getThemeColor());

		floatingButton.setOnClickListener(this);
		floatingButton.setBackgroundTintList(color);
	}

	/**
	 * Setup the Google rating dialog.
	 */
	private void setupGoogleRatingDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int counter = shared.getRateMyAppCounter();

		if (shared.isRateMyAppRated())
		{
			return;
		}
		else if (shared.isRateMyAppLimit())
		{
			NacRateMyAppDialog dialog = new NacRateMyAppDialog();
			dialog.build(this);
			dialog.show();
		}
		else
		{
			shared.incrementRateMyApp();
		}
	}

	/**
	 * Setup the recycler view.
	 */
	private void setupRecyclerView()
	{
		RecyclerView recyclerView = this.getRecyclerView();
		NacCardAdapter cardAdapter = this.getCardAdapter();
		int padding = getResources().getDimensionPixelSize(R.dimen.sp_main);

		Drawable drawable = ContextCompat.getDrawable(this,
			R.drawable.card_divider);
		InsetDrawable insetDrawable = new InsetDrawable(drawable, padding, 0,
			padding, 0);
		DividerItemDecoration divider = new DividerItemDecoration(this,
			LinearLayoutManager.VERTICAL);
		NacLayoutManager layoutManager = new NacLayoutManager(this);

		//divider.setDrawable(drawable);
		divider.setDrawable(insetDrawable);
		recyclerView.addItemDecoration(divider);
		recyclerView.setAdapter(cardAdapter);
		recyclerView.setLayoutManager(layoutManager);
	}

	///**
	// * @return True if should delay the start of the alarm activity, and False
	// *         otherwise.
	// */
	//private boolean shouldDelayAlarmActivity(StatusBarNotification notification)
	//{
	//	NacAlarm alarm = NacNotificationHelper.findAlarm(this, notification);
	//	return this.shouldDelayAlarmActivity(alarm);
	//}

	///**
	// * @return True if should start the alarm activity, and False otherwise.
	// */
	//private boolean shouldShowAlarmActivity(StatusBarNotification notification)
	//{
	//	NacAlarm alarm = NacNotificationHelper.findAlarm(this, notification);
	//	return this.shouldShowAlarmActivity(alarm);
	//}

	/**
	 * @see shouldShowAlarmActivity
	 */
	private boolean shouldShowAlarmActivity(NacAlarm alarm)
	{
		Intent intent = getIntent();
		return (alarm != null) && !NacNfc.wasScanned(this, intent);
	}

	/**
	 * @return True if should delay the start of the alarm activity, and False
	 *         otherwise.
	 */
	private boolean shouldShowAlarmActivityDelayed(NacAlarm alarm)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		return this.shouldShowAlarmActivity(alarm) && alarm.getUseNfc();
		//	&& !shared.getPreventAppFromClosing();
	}

	/**
	 * @see showAlarmActivity
	 */
	private void showAlarmActivity()
	{
		NacAlarm activeAlarm = NacDatabase.findActiveAlarm(this);
		this.showAlarmActivity(activeAlarm);
		//StatusBarNotification notification = NacNotificationHelper
		//	.getActiveNotification(this);
		//this.showAlarmActivity(notification);
	}

	/**
	 * Show the alarm activity.
	 */
	private void showAlarmActivity(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		Intent intent = NacIntent.createAlarmActivity(this, alarm);
		startActivity(intent);
	}

	//private void showAlarmActivity(StatusBarNotification statusBarNotification)
	//{
	//	if (statusBarNotification == null)
	//	{
	//		return;
	//	}

	//	Notification notification = statusBarNotification.getNotification();
	//	if (notification == null)
	//	{
	//		return;
	//	}

	//	try
	//	{
	//		PendingIntent pending = notification.contentIntent;
	//		if (pending != null)
	//		{
	//			pending.send();
	//		}
	//	}
	//	catch (PendingIntent.CanceledException e)
	//	{
	//		NacUtility.printf("Caught canceled exception for pending intent!");
	//	}
	//}

	/**
	 * Show the alarm activity after some delay.
	 */
	private void showAlarmActivityDelayed(long delay)
	{
		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				//StatusBarNotification notification = NacNotificationHelper
				//	.getActiveNotification(NacMainActivity.this);
				//if (shouldShowAlarmActivity(notification))
				NacAlarm activeAlarm = NacDatabase.findActiveAlarm(NacMainActivity.this);
				if (shouldShowAlarmActivity(activeAlarm))
				{
					showAlarmActivity(activeAlarm);
					//showAlarmActivity(notification);
				}
			}
		}, delay);

		this.mActivityDelayHandler = handler;
	}

	/**
	 * @return True if an NFC tag was scanned to dismiss an alarm, and False
	 *         otherwise. This is to say that if an NFC tag was scanned for the
	 *         dialog, this would return False.
	 */
	private boolean wasNfcScannedForAlarm()
	{
		Intent intent = getIntent();
		return NacNfc.wasScanned(this, intent) && !this.wasNfcScannedForDialog();
	}

	/**
	 * @return True if an NFC tag was scanned while the Scan NFC Tag dialog was
	 *         open, and False otherwise.
	 */
	private boolean wasNfcScannedForDialog()
	{
		Intent intent = getIntent();
		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
		return (dialog != null) && NacNfc.wasScanned(this, intent);
	}

}
