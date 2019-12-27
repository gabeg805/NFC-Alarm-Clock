package com.nfcalarmclock;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

/**
 * The application's main activity.
 */
public class NacMainActivity
	extends AppCompatActivity
	implements View.OnClickListener,
		NacDialog.OnDismissListener
{

	/**
	 * Result code when requesting SYSTEM_ALERT_WINDOW permission.
	 */
	public static int DRAW_OVERLAY_REQUEST = 1234;

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
			alarm.print();
			adapter.add(alarm);
			//adapter.setWasAddedWithFloatingButton(false);
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
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == DRAW_OVERLAY_REQUEST)
		{
			if (!NacPermissions.hasDrawOverlay(this))
			{
				// You don't have permission
				//checkPermission();
				NacUtility.printf("Alarms may not go off as expected if app is not open.");
			} else {
				NacUtility.printf("Alarms will go off as expected!");
				// Do as per your logic
			}

		}

	}

	/**
	 * Add a new alarm when the floating action button is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		NacCardAdapter adapter = this.getCardAdapter();

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

		if (this.shouldShowDrawOverlayDialog())
		{
			this.showDrawOverlayDialog();
		}

		NacSharedPreferences shared = new NacSharedPreferences(this);
		Drawable drawable = ContextCompat.getDrawable(this,
			R.drawable.card_divider);
		DividerItemDecoration divider = new DividerItemDecoration(this,
			LinearLayoutManager.VERTICAL);
		NacLayoutManager layoutManager = new NacLayoutManager(this);

		this.mSharedPreferences = shared;
		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingButton = (FloatingActionButton) findViewById(
			R.id.fab_add_alarm);
		this.mRecyclerView = (RecyclerView) findViewById(
			R.id.content_alarm_list);

		divider.setDrawable(drawable);
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
		case R.id.menu_sort:
			NacCardAdapter adapter = this.getCardAdapter();

			adapter.sort();
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Show Android prompt where the user can enable the SYSTEM_DIALOG_WINDOW
	 * permission.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacPermissions.requestDrawOverlay(this, DRAW_OVERLAY_REQUEST);
		return true;
	}
	/**
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		this.setupAlarmCardAdapter();
		this.setupFloatingActionButton();
		this.addSetAlarmFromIntent();
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
	 * @return True if the draw overlay permission dialog should be show, and
	 *         False otherwise.
	 */
	private boolean shouldShowDrawOverlayDialog()
	{
		return ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			&& !NacPermissions.hasDrawOverlay(this)
			&& !NacIntent.isSetAlarmAction(getIntent()));
	}

	/**
	 * Show the draw overlay dialog.
	 */
	private void showDrawOverlayDialog()
	{
		NacPermissionsDrawOverlayDialog dialog =
			new NacPermissionsDrawOverlayDialog();

		dialog.addOnDismissListener(this);
		dialog.build(this);
		dialog.show();
	}

	/**
	 * Display a snackbar showing the next scheduled alarm.
	 */
	private void showNextAlarm()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacCardAdapter adapter = this.getCardAdapter();
		List<NacAlarm> alarms = adapter.getAlarms();
		NacAlarm nextAlarm = NacCalendar.getNextAlarm(alarms);
		String message = NacCalendar.getNextMessage(shared, nextAlarm);

		NacUtility.snackbar(this, message, "DISMISS", null);
	}

}
