package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
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
import java.lang.System;

/**
 * The application's main activity.
 */
public class NacMainActivity
	extends AppCompatActivity
	implements View.OnClickListener
{

	/**
	 * Wait time between a notification posting and running the alarm activity
	 * for that notification.
	 */
	private static final long ACTIVE_ALARM_POST_DURATION = 2000;

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
	 * @return NacAlarm that is found by using the information in the currently
	 *         active notification
	 */
	private NacAlarm findAlarmFromNotification(StatusBarNotification notification)
	{
		if (notification == null)
		{
			return null;
		}

		int id = notification.getId();
		return NacDatabase.findAlarm(this, id);
	}

	/**
	 * @return The notification for the current active alarm.
	 */
	@TargetApi(Build.VERSION_CODES.M)
	private StatusBarNotification getActiveStatusBarNotification()
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			return null;
		}

		NotificationManager manager = getSystemService(
			NotificationManager.class);
		StatusBarNotification[] statusbar = manager.getActiveNotifications();
		StatusBarNotification activeNotification = null;

		for (StatusBarNotification sb : statusbar)
		{
			Notification notification = sb.getNotification();
			String group = notification.getGroup();
			long posted = sb.getPostTime();

			if (!group.equals(NacActiveAlarmNotification.GROUP))
			{
				continue;
			}

			if ((activeNotification == null)
				|| (activeNotification.getPostTime() > posted))
			{
				activeNotification = sb;
				continue;
			}
		}

		return activeNotification;
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
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
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
	public void onPause()
	{
		super.onPause();
		NacNfc.stop(this);
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
	}

	/**
	 * Setup the alarm activity for any active alarms.
	 */
	private void setupActiveAlarmActivity()
	{
		StatusBarNotification notification = this.getActiveStatusBarNotification();
		NacAlarm alarm = this.findAlarmFromNotification(notification);

		if ((notification == null) || (alarm == null))
		{
		}
		else if (this.shouldShowAlarmActivity(alarm))
		{
			NacUtility.quickToast(this, "Showing alarm activity!");
			this.showAlarmActivity(notification);
		}
		else if (this.shouldStartNfc(alarm))
		{
			NacUtility.quickToast(this, "Starting NFC dispatch!");
			NacNfc.start(this);
		}
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private void setupAlarmCardAdapter()
	{
		NacCardAdapter cardAdapter = this.getCardAdapter();
		cardAdapter.build();
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

	/**
	 * @return True if should start the alarm activity, and False otherwise.
	 */
	private boolean shouldShowAlarmActivity(NacAlarm alarm)
	{
		return (alarm != null) && !this.shouldStartNfc(alarm);
	}

	/**
	 * @return True if should start NFC, and False otherwise.
	 */
	private boolean shouldStartNfc(NacAlarm alarm)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		return (alarm != null) && alarm.getUseNfc() && shared.getPreventAppFromClosing();
	}

	/**
	 * Show the alarm activity.
	 */
	private void showAlarmActivity(StatusBarNotification notification)
	{
		if (notification == null)
		{
			return;
		}

		try
		{
			PendingIntent pending = notification.getNotification().contentIntent;
			if (pending != null)
			{
				pending.send();
			}
		}
		catch (PendingIntent.CanceledException e)
		{
			NacUtility.printf("Caught canceled exception for pending intent!");
		}
	}

}
