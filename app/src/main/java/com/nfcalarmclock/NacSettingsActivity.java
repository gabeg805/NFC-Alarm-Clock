package com.nfcalarmclock;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * Display all the configurable settings for the app.
 */
public class NacSettingsActivity
	extends NacActivity
	implements FragmentManager.OnBackStackChangedListener
{

	/**
	 * @return The stack count.
	 */
	private int getStackCount()
	{
		return getFragmentManager().getBackStackEntryCount();
	}

	/**
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		FragmentManager manager = getFragmentManager();

		manager.beginTransaction()
			.replace(android.R.id.content, new SettingsFragment())
			.commit();
		manager.addOnBackStackChangedListener(this);
	}

	/**
	 */
	@Override
	public void onBackStackChanged()
	{
		int count = this.getStackCount();
		String title = "Settings";

		if (count > 0)
		{
			BackStackEntry entry = getFragmentManager().getBackStackEntryAt(0);
			title = entry.getName();
		}

		setTitle(title);
	}

	/**
	 */
	@Override
	public boolean onSupportNavigateUp()
	{
		int count = this.getStackCount();

		if (count > 0)
		{
			getFragmentManager().popBackStack();
			return false;
		}
		else
		{
			finish();
			return true;
		}
	}

	/**
	 * Settings fragment.
	 */
	public static class SettingsFragment
		extends NacSettingsFragment
		implements Preference.OnPreferenceClickListener
	{

		/**
		 */
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.main_preferences);

			NacSharedKeys keys = this.getKeys();
			Resources res = getResources();
			Preference general = findPreference(keys.getGeneralScreen());
			Preference alarm = findPreference(keys.getDefaultAlarmScreen());
			Preference color = findPreference(keys.getColorScreen());
			Preference miscellaneous = findPreference(
				keys.getMiscellaneousScreen());

			general.setIcon(this.createIconDrawable(R.mipmap.settings));
			alarm.setIcon(this.createIconDrawable(R.mipmap.alarm));
			color.setIcon(this.createIconDrawable(R.mipmap.palette));
			miscellaneous.setIcon(this.createIconDrawable(R.mipmap.extension));
			general.setOnPreferenceClickListener(this);
			alarm.setOnPreferenceClickListener(this);
			color.setOnPreferenceClickListener(this);
			miscellaneous.setOnPreferenceClickListener(this);
		}

		/**
		 * @return The icon drawable.
		 */
		private BitmapDrawable createIconDrawable(int id)
		{
			Resources res = getResources();
			int size = (int) res.getDimension(R.dimen.isz_pref_icon);
			Bitmap bitmap = BitmapFactory.decodeResource(res, id);
			Bitmap scaled = Bitmap.createScaledBitmap(bitmap, size, size, true);

			return new BitmapDrawable(res, scaled);
		}

		/**
		 */
		@Override
		public boolean onPreferenceClick(Preference preference)
		{
			NacSharedKeys keys = this.getKeys();
			String preferenceKey = preference.getKey();
			FragmentManager manager = getFragmentManager();
			Fragment fragment;
			String title;

			if (preferenceKey.equals(keys.getGeneralScreen()))
			{
				fragment = new NacGeneralSettings();
				title = keys.getGeneralScreenTitle();

			}
			else if (preferenceKey.equals(keys.getDefaultAlarmScreen()))
			{
				fragment = new NacDefaultAlarmSettings();
				title = keys.getDefaultAlarmScreenTitle();
			}
			else if (preferenceKey.equals(keys.getColorScreen()))
			{
				fragment = new NacColorSettings();
				title = keys.getColorScreenTitle();
			}
			else if (preferenceKey.equals(keys.getMiscellaneousScreen()))
			{
				fragment = new NacMiscellaneousSettings();
				title = keys.getMiscellaneousScreenTitle();
			}
			else
			{
				return false;
			}

			manager.beginTransaction()
				.replace(android.R.id.content, fragment)
				.addToBackStack(title)
				.commit();

			return true;
		}

	}

}
