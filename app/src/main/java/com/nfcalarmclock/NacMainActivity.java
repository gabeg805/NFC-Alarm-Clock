package com.nfcalarmclock;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
		ColorStateList color = ColorStateList.valueOf(shared.getThemeColor());
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
		this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		this.mRecyclerView.addOnScrollListener(new ScrollListener());
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
	 */
	@Override
	public void onClick(View view)
	{
		this.mAdapter.add();
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
	 * Display a snackbar showing the next scheduled alarm.
	 */
	private void showNextAlarm()
	{
		NacScheduler scheduler = new NacScheduler(this);
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

	/**
	 * RecyclerView scroll listener to show/hide the floating action button.
	 */
	public class ScrollListener
		extends RecyclerView.OnScrollListener
	{

		/**
		 */
		public ScrollListener()
		{
		}

		/**
		 * Show the floating action button when scrolling up and hide it when
		 * scrolling down.
		 *
		 * @param  rv  The recycler view.
		 * @param  dx  The change in scrolling in the x-direction.
		 * @param  dy  The change in scrolling in the y-direction.
		 */
		@Override
		public void onScrolled(RecyclerView rv, int dx, int dy)
		{
			super.onScrolled(rv, dx, dy);

			if ((dy < 0) && !mFloatingButton.isShown())
			{
				mFloatingButton.show();
			}
			else if ((dy > 0) && mFloatingButton.isShown())
			{
				mFloatingButton.hide();
			}
		}

		/**
		 * Display the floating button when at the bottom of the list.
		 *
		 * @param  rv  The recycler view.
		 * @param  state The scroll state (Idle, Dragging, or Settling).
		 */
		@Override
		public void onScrollStateChanged(RecyclerView rv, int state)
		{
			super.onScrollStateChanged(rv, state);

			if ((state == 0) && !rv.canScrollVertically(1))
			{
				//if (!mFloatingButton.isShown())
				if (mFloatingButton.isShown())
				{
					mFloatingButton.hide();
				}
			}
		}

	}

}
