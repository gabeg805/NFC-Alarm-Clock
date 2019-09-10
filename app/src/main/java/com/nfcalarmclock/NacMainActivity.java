package com.nfcalarmclock;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.DividerItemDecoration;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
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
	extends NacActivity
	implements View.OnClickListener
{

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

		adapter.add();
	}

	/**
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		NacSharedPreferences shared = new NacSharedPreferences(this);
		Drawable drawable = ContextCompat.getDrawable(this,
			R.drawable.card_divider);
		DividerItemDecoration divider = new DividerItemDecoration(this,
			LinearLayoutManager.VERTICAL);
		NacLayoutManager layoutManager = new NacLayoutManager(this);
		ColorStateList color = ColorStateList.valueOf(shared.getThemeColor());

		this.mSharedPreferences = shared;
		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingButton = (FloatingActionButton) findViewById(
			R.id.fab_add_alarm);
		this.mRecyclerView = (RecyclerView) findViewById(
			R.id.content_alarm_list);

		divider.setDrawable(drawable);
		this.mFloatingButton.setBackgroundTintList(color);
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
		case R.id.menu_next_alarm:
			showNextAlarm();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, NacSettingsActivity.class));
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

		NacSharedPreferences shared = this.getSharedPreferences();
		NacCardAdapter adapter = this.getCardAdapter();
		String message = shared.getAutoDismissMessage();

		adapter.build();

		if (!message.isEmpty())
		{
			NacUtility.toast(this, message);

			shared.editAutoDismissMessage("");
		}
	}

	/**
	 * Display a snackbar showing the next scheduled alarm.
	 */
	private void showNextAlarm()
	{
		NacScheduler scheduler = new NacScheduler(this);
		AlarmClockInfo next = scheduler.getNext();
		String message = "No scheduled alarms.";

		if (next != null)
		{
			NacSharedPreferences shared = this.getSharedPreferences();
			int nextAlarmFormat = shared.getNextAlarmFormat();
			long millis = next.getTriggerTime();
			message = NacCalendar.getNextMessage(millis, nextAlarmFormat);
		}

		NacUtility.snackbar(this, message, "DISMISS", null);
	}

}
