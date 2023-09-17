package com.nfcalarmclock.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.BackStackEntry;

import com.nfcalarmclock.shared.NacSharedConstants;

/**
 * Display all the configurable settings for the app.
 */
public class NacMainSettingActivity
	extends AppCompatActivity
	implements FragmentManager.OnBackStackChangedListener
{

	/**
	 * @return The stack count.
	 */
	private int getStackCount()
	{
		return getSupportFragmentManager().getBackStackEntryCount();
	}

	/**
	 */
	@Override
	public void onBackStackChanged()
	{
		NacSharedConstants cons = new NacSharedConstants(this);
		int count = this.getStackCount();
		String title = cons.getSettings();

		if (count > 0)
		{
			BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
			title = entry.getName();
		}

		setTitle(title);
	}

	/**
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FragmentManager manager = getSupportFragmentManager();

		// Add the main setting fragment to the activity
		manager.beginTransaction()
			.replace(android.R.id.content, new NacMainSettingFragment())
			.commit();
		manager.addOnBackStackChangedListener(this);
	}

	/**
	 */
	@Override
	public boolean onSupportNavigateUp()
	{
		int count = this.getStackCount();

		// There are still items in the back stack. Keep popping them
		if (count > 0)
		{
			getSupportFragmentManager().popBackStack();
			return false;
		}
		// No more items in the back stack. Finish the Settings activity
		else
		{
			finish();
			return true;
		}
	}

}
