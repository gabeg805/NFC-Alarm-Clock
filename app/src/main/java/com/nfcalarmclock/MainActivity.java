package com.nfcalarmclock;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.lang.System;

/**
 * @brief The application's main activity.
 */
public class MainActivity
	extends AppCompatActivity
	implements View.OnClickListener
{

	/**
	 * @brief Recycler view containing the alarm cards.
	 */
	private NacRecyclerView mRecyclerView;

	/**
	 * @brief Floating button to add new alarms.
	 */
	private NacFloatingButton mFloatingButton;

	/**
	 * @brief Alarm card adapter.
	 */
	private NacCardAdapter mAdapter;

	/**
	 * @brief Create the application.
	 *
	 * @param  savedInstanceState  The saved instance state.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		NacUtility.printf("onCreate() in MainActivity.");
		setContentView(R.layout.act_main);
		//this.deleteDatabase(NacDatabaseContract.DATABASE_NAME);

		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingButton = new NacFloatingButton(this);
		this.mRecyclerView = new NacRecyclerView(this);

		this.mRecyclerView.init();
		this.mFloatingButton.init();
		this.mRecyclerView.setAdapter(this.mAdapter);
		this.mRecyclerView.setLayoutManager(this);
		this.mRecyclerView.setScrollListener(this.mFloatingButton);
		//this.mAdapter.build();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		NacUtility.printf("onStart() in MainActivity.");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		NacUtility.printf("onResume() in MainActivity.");
		this.mAdapter.build();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		NacUtility.printf("onRestart() in MainActivity.");
	}

	/**
	 * @brief Add a new alarm when the floating action button is clicked.
	 *
	 * @param  v  The view that was clicked.
	 */
	@Override
	public void onClick(View v)
	{
		this.mAdapter.add();
	}

	/**
	 * @brief Create the options menu in the action bar.
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
	 * @brief A menu item was selected. Determine which action to take depending on
	 *		  the item selected.
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
			Toast.makeText(this, "Yo this is the default thing",
				Toast.LENGTH_LONG).show();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * @brief Start the settings activity.
	 */
	private void startSettingsActivity()
	{
		Intent intent = new Intent(this, SettingsActivity.class);

		startActivity(intent);
	}

	/**
	 * @brief Display a toast showing the next scheduled alarm.
	 */
	private void showNextAlarm()
	{
		NacAlarmScheduler scheduler = new NacAlarmScheduler(this);
		AlarmClockInfo next = scheduler.getNext();
		String msg = "";

		if (next == null)
		{
			msg = "No scheduled alarms.";
		}
		else
		{
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

			msg = "Time remaining: ";

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

		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

}
