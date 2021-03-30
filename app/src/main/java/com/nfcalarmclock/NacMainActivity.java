package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
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
	private static final long SHOW_ALARM_ACTIVITY_DELAY = 1000;

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
	 * Shutdown broadcast receiver.
	 */
	private NacShutdownBroadcastReceiver mShutdownBroadcastReceiver;

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
			cardAdapter.addAlarm(alarm);
			cardAdapter.setWasAddedWithFloatingActionButton(true);
		}
	}

	/**
	 * Cleanup the show activity delay handler.
	 */
	private void cleanupScanNfcTagDialog()
	{
		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
		if (dialog != null)
		{
			dialog.getAlertDialog().dismiss();
			this.mScanNfcTagDialog = null;
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
	 * Cleanup the shutdown broadcast receiver.
	 */
	private void cleanupShutdownBroadcastReceiver()
	{
		NacShutdownBroadcastReceiver receiver = this.getShutdownBroadcastReceiver();
		unregisterReceiver(receiver);
	}

	/**
	 * Setup an NFC scan checker, which checks if this activity was started by an
	 * NFC tag being scanned.
	 */
	private void dismissActiveAlarm(Intent intent)
	{
		NacAlarm alarm = NacDatabase.findActiveAlarm(this);
		if (alarm == null)
		{
			return;
		}

		boolean success = NacContext.dismissForegroundServiceFromNfcScan(this,
			intent, alarm);

		if (success)
		{
			recreate();
		}
		else
		{
			this.showAlarmActivity(alarm);
		}
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
	 * Show activity handler.
	 */
	private Handler getShowActivityDelayHandler()
	{
		return mActivityDelayHandler;
	}


	/**
	 * Get the shutdown broadcast receiver.
	 */
	private NacShutdownBroadcastReceiver getShutdownBroadcastReceiver()
	{
		return this.mShutdownBroadcastReceiver;
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
			cardHolder.getNfcButton().setChecked(false);
			cardHolder.doNfcButtonClick();
		}

		this.cleanupScanNfcTagDialog();
		return true;
	}

	/**
	 * Add a new alarm when the floating action button is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		NacCardAdapter cardAdapter = this.getCardAdapter();

		cardAdapter.addAlarm();
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

		Intent intent = getIntent();
		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingActionButton = findViewById(R.id.fab_add_alarm);
		this.mRecyclerView = findViewById(R.id.content_alarm_list);
		this.mScanNfcTagDialog = null;
		this.mShutdownBroadcastReceiver = new NacShutdownBroadcastReceiver();

		if (this.wasNfcScannedForAlarm(intent))
		{
			this.dismissActiveAlarm(intent);
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
		this.cleanupScanNfcTagDialog();
		NacUtility.quickToast(this, cons.getMessageNfcRequired());
		return true;
	}

	/**
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		if (this.wasNfcScannedForDialog(intent))
		{
			NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
			NacSharedConstants cons = new NacSharedConstants(this);

			this.saveNfcTagId(intent);
			dialog.saveData(null);
			dialog.cancel();
			NacUtility.quickToast(this, cons.getMessageNfcRequired());
		}
		else if (this.wasNfcScannedForAlarm(intent))
		{
			this.dismissActiveAlarm(intent);
		}
	}

	/**
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.menu_settings)
		{
			Intent settingsIntent = new Intent(this, NacSettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		else if (id == R.id.menu_show_next_alarm)
		{
			NacCardAdapter cardAdapter = this.getCardAdapter();
			cardAdapter.showNextAlarm();
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		this.setupRefreshMainActivity();
		this.setupActiveAlarmActivity();
		this.setupAlarmCardAdapter();
		this.setupFloatingActionButton();
		this.setupGoogleRatingDialog();
		this.addSetAlarmFromIntent();
		this.setupShutdownBroadcastReceiver();
		NacNfc.start(this);
	}

	/**
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		this.cleanupShowActivityDelayHandler();
		this.cleanupShutdownBroadcastReceiver();
		NacNfc.stop(this);
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
		this.mScanNfcTagDialog = dialog;

		dialog.build(this);
		dialog.saveData(alarm);
		dialog.addOnCancelListener(this);
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Save the scanned NFC tag ID.
	 */
	private void saveNfcTagId(Intent intent)
	{
		if (!this.wasNfcScannedForDialog(intent))
		{
			return;
		}

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
	}

	/**
	 * Setup the alarm activity for any active alarms.
	 */
	private void setupActiveAlarmActivity()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = NacDatabase.findActiveAlarm(this);
		long delay = SHOW_ALARM_ACTIVITY_DELAY;

		if (this.shouldShowAlarmActivity(alarm))
		{
			if (this.shouldShowAlarmActivityDelayed(alarm))
			{
				delay = shared.getPreventAppFromClosing() ? delay/4 : delay;
				this.showAlarmActivityDelayed(delay);
			}
			else
			{
				this.showAlarmActivity(alarm);
			}
		}
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private void setupAlarmCardAdapter()
	{
		NacCardAdapter cardAdapter = this.getCardAdapter();

		if (cardAdapter.size() == 0)
		{
			cardAdapter.build();
		}

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
		if (shared.isRateMyAppRated())
		{
			return;
		}

		if (shared.isRateMyAppLimit())
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
		int padding = getResources().getDimensionPixelSize(R.dimen.normal);

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

	/**
	 * Setup whether the main activity should be refreshed or not.
	 */
	private void setupRefreshMainActivity()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		if (shared.getShouldRefreshMainActivity())
		{
			shared.editShouldRefreshMainActivity(false);
			recreate();
		}
	}

	/**
	 * Setup the shutdown broadcast receiver.
	 */
	private void setupShutdownBroadcastReceiver()
	{
		NacShutdownBroadcastReceiver receiver = this.getShutdownBroadcastReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);

		registerReceiver(receiver, filter);
	}

	/**
	 * @return True if the alarm activity should be shown, and False otherwise.
	 */
	private boolean shouldShowAlarmActivity(NacAlarm alarm)
	{
		Intent intent = getIntent();
		return (alarm != null) && !NacNfc.wasScanned(intent);
	}

	/**
	 * @return True if should delay the start of the alarm activity, and False
	 *     otherwise.
	 */
	private boolean shouldShowAlarmActivityDelayed(NacAlarm alarm)
	{
		return this.shouldShowAlarmActivity(alarm) && alarm.getUseNfc();
	}

	/**
	 * Show the alarm activity.
	 *
	 * @param  alarm  The alarm that should be attached to the intent when the
	 *     alarm activity is started.
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
				NacAlarm activeAlarm = NacDatabase.findActiveAlarm(NacMainActivity.this);
				if (shouldShowAlarmActivity(activeAlarm))
				{
					showAlarmActivity(activeAlarm);
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
	private boolean wasNfcScannedForAlarm(Intent intent)
	{
		return NacNfc.wasScanned(intent) && !this.wasNfcScannedForDialog(intent);
	}

	/**
	 * @return True if an NFC tag was scanned while the Scan NFC Tag dialog was
	 *         open, and False otherwise.
	 */
	private boolean wasNfcScannedForDialog(Intent intent)
	{
		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
		return (dialog != null) && NacNfc.wasScanned(intent);
	}

}
