package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.BackStackEntry;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

/**
 * Display all the configurable settings for the app.
 */
public class NacSettingsActivity
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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FragmentManager manager = getSupportFragmentManager();

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
			BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
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
			getSupportFragmentManager().popBackStack();
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
		public void onCreatePreferences(Bundle savedInstanceState,
			String rootKey)
		{
			addPreferencesFromResource(R.xml.main_preferences);

			NacSharedKeys keys = this.getKeys();
			Resources res = getResources();
			Preference appearance = findPreference(keys.getAppearanceScreen());
			Preference general = findPreference(keys.getGeneralScreen());
			Preference miscellaneous = findPreference(
				keys.getMiscellaneousScreen());
			Preference about = findPreference(keys.getAboutScreen());

			appearance.setIcon(this.createIconDrawable(R.mipmap.palette));
			general.setIcon(this.createIconDrawable(R.mipmap.settings));
			miscellaneous.setIcon(this.createIconDrawable(R.mipmap.extension));
			about.setIcon(this.createIconDrawable(R.mipmap.about));
			appearance.setOnPreferenceClickListener(this);
			general.setOnPreferenceClickListener(this);
			miscellaneous.setOnPreferenceClickListener(this);
			about.setOnPreferenceClickListener(this);
		}

		/**
		 * @return The icon drawable.
		 */
		private BitmapDrawable createIconDrawable(int id)
		{
			Resources res = getResources();
			int size = (int) res.getDimension(R.dimen.isz_main_custom);
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
			//FragmentManager manager = getFragmentManager();
			FragmentManager manager = getParentFragmentManager();
			Fragment fragment;
			String title;

			if (preferenceKey.equals(keys.getGeneralScreen()))
			{
				fragment = new NacGeneralSettings();
				title = keys.getGeneralScreenTitle();

			}
			else if (preferenceKey.equals(keys.getAppearanceScreen()))
			{
				fragment = new NacAppearanceSettings();
				title = keys.getAppearanceScreenTitle();
			}
			else if (preferenceKey.equals(keys.getMiscellaneousScreen()))
			{
				fragment = new NacMiscellaneousSettings();
				title = keys.getMiscellaneousScreenTitle();
			}
			else if (preferenceKey.equals(keys.getAboutScreen()))
			{
				fragment = new NacAboutSettings();
				title = keys.getAboutScreenTitle();
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
