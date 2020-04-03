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
	 * Floating button to add new alarms.
	 */
	private FloatingActionButton mFloatingButton;

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
		NacCardAdapter adapter = this.getCardAdapter();
		NacAlarm alarm = NacIntent.getSetAlarm(this, intent);

		if (alarm != null)
		{
			alarm.setId(adapter.getUniqueId());
			adapter.add(alarm);
			adapter.setWasAddedWithFloatingButton(true);
		}
	}

	/**
	 * @return The notification for the most recent (if more than 1) active
	 *         alarm.
	 */
	@TargetApi(Build.VERSION_CODES.M)
	private StatusBarNotification getActiveAlarmNotification()
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
	private FloatingActionButton getFloatingButton()
	{
		return this.mFloatingButton;
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
		NacCardAdapter adapter = this.getCardAdapter();

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		adapter.add();
		adapter.setWasAddedWithFloatingButton(true);
	}

	/**
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		NacSharedPreferences shared = new NacSharedPreferences(this);
		DividerItemDecoration divider = new DividerItemDecoration(this,
			LinearLayoutManager.VERTICAL);
		NacLayoutManager layoutManager = new NacLayoutManager(this);
		Drawable drawable = ContextCompat.getDrawable(this,
			R.drawable.card_divider);
		int padding = getResources().getDimensionPixelSize(R.dimen.sp_main);
		InsetDrawable insetDrawable = new InsetDrawable(drawable, padding, 0,
			padding, 0);

		this.mSharedPreferences = shared;
		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingButton = (FloatingActionButton) findViewById(
			R.id.fab_add_alarm);
		this.mRecyclerView = (RecyclerView) findViewById(
			R.id.content_alarm_list);

		divider.setDrawable(insetDrawable);
		//divider.setDrawable(drawable);
		this.mFloatingButton.setOnClickListener(this);
		this.mRecyclerView.addItemDecoration(divider);
		this.mRecyclerView.setAdapter(this.mAdapter);
		this.mRecyclerView.setLayoutManager(layoutManager);
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
				startActivity(new Intent(this, NacSettingsActivity.class));
				return true;
			case R.id.menu_show_next_alarm:
				NacCardAdapter adapter = this.getCardAdapter();
				adapter.showNextAlarm();
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
	}

	/**
	 * Setup the alarm activity for any active alarms.
	 */
	private void setupActiveAlarmActivity()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		StatusBarNotification activeNotification =
			this.getActiveAlarmNotification();

		if (!shared.getPreventAppFromClosing() || (activeNotification == null))
		{
			return;
		}

		long currentTime = System.currentTimeMillis();
		long postTime = activeNotification.getPostTime();

		if ((currentTime-postTime) >= ACTIVE_ALARM_POST_DURATION)
		{
			this.showAlarmActivity(activeNotification);
		}
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private void setupAlarmCardAdapter()
	{
		NacCardAdapter adapter = this.getCardAdapter();
		adapter.build();
	}

	/**
	 * Setup the floating action button.
	 */
	private void setupFloatingActionButton()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		FloatingActionButton floatingButton = this.getFloatingButton();
		ColorStateList color = ColorStateList.valueOf(shared.getThemeColor());

		floatingButton.setBackgroundTintList(color);
	}

	/**
	 * Setup the Google rating dialog.
	 */
	private void setupGoogleRatingDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int counter = shared.getRateMyAppCounter();

		if (counter == NacSharedPreferences.DEFAULT_RATE_MY_APP_RATED)
		{
			return;
		}
		else if ((counter+1) >= NacSharedPreferences.DEFAULT_RATE_MY_APP_LIMIT)
		{
			NacRateMyAppDialog dialog = new NacRateMyAppDialog(this);

			dialog.build();
			dialog.show();
		}
		else
		{
			shared.editRateMyAppCounter(counter+1);
		}
	}

	/**
	 * Show the alarm activity.
	 */
	private void showAlarmActivity(StatusBarNotification activeNotification)
	{
		if (activeNotification == null)
		{
			return;
		}

		Notification noti = activeNotification.getNotification();

		if (noti != null)
		{
			PendingIntent pending = noti.contentIntent;

			try
			{
				pending.send();
			}
			catch (PendingIntent.CanceledException e)
			{
				NacUtility.printf("Caught canceled exception for pending intent!");
			}
		}
	}

}
