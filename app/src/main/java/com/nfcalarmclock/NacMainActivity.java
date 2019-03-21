package com.nfcalarmclock;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.lang.System;

/**
 * The application's main activity.
 */
public class NacMainActivity
	extends NacActivity
	implements View.OnClickListener
{

	/**
	 * Alarm scheduler.
	 */
	private NacAlarmScheduler mScheduler;

	/**
	 * The database.
	 */
	private NacDatabase mDatabase;

	/**
	 * Recycler view containing the alarm cards.
	 */
	private NacRecyclerView mRecyclerView;

	/**
	 * Floating button to add new alarms.
	 */
	private NacFloatingButton mFloatingButton;

	/**
	 * Alarm card adapter.
	 */
	private NacCardAdapter mAdapter;

	/**
	 * Create the application.
	 *
	 * @param  savedInstanceState  The saved instance state.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingButton = new NacFloatingButton(this);
		this.mRecyclerView = new NacRecyclerView(this);

		this.mRecyclerView.init();
		this.mFloatingButton.init();
		this.mRecyclerView.setAdapter(this.mAdapter);
		this.mRecyclerView.setLayoutManager(this);
		this.mRecyclerView.setScrollListener(this.mFloatingButton);
	}

	/**
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		this.mAdapter.build();
	}

	/**
	 * Add a new alarm when the floating action button is clicked.
	 *
	 * @param  v  The view that was clicked.
	 */
	@Override
	public void onClick(View view)
	{
		this.mAdapter.add();
	}

	/**
	 * Create the options menu in the action bar.
	 *
	 * @param  menu  The menu view.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_action_bar, menu);
		return true;
	}

	/**
	 * A menu item was selected. Determine which action to take depending on
	 * the item selected.
	 *
	 * @param  item  The menu item that was selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		switch (id)
		{
		case R.id.menu_next_alarm:
			showNextAlarm();
			return true;
		case R.id.menu_settings:
			startSettingsActivity();
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Start the settings activity.
	 */
	private void startSettingsActivity()
	{
		Intent intent = new Intent(this, NacSettingsActivity.class);

		startActivity(intent);
	}

	/**
	 * Display a snackbar showing the next scheduled alarm.
	 */
	private void showNextAlarm()
	{
		NacAlarmScheduler scheduler = new NacAlarmScheduler(this);
		AlarmClockInfo next = scheduler.getNext();
		String msg = "No scheduled alarms.";

		if (next != null)
		{
			msg = "Next alarm in ";
			long time = (next.getTriggerTime() - System.currentTimeMillis())
				/ 1000;
			long day = (time / (60*60*24)) % 365;
			long hr = (time / (60*60)) % 24;
			long min = (time / 60) % 60;
			long sec = time % 60;
			String dayunit = (day != 1) ? " days " : " day ";
			String hrunit = (hr != 1) ? " hours " : " hour ";
			String minunit = (min != 1) ? " minutes " : " minute ";
			String secunit = (sec != 1) ? " seconds " : " second ";

			if (day > 0)
			{
				msg += String.valueOf(day)+dayunit+String.valueOf(hr)+hrunit;
			}
			else
			{
				if (hr > 0)
				{
					msg += String.valueOf(hr)+hrunit+String.valueOf(min)+minunit;
				}
				else
				{
					msg += String.valueOf(min)+minunit+String.valueOf(sec)+secunit;
				}
			}
		}

		NacUtility.snackbar(this, msg, "DISMISS", null);
	}

}
