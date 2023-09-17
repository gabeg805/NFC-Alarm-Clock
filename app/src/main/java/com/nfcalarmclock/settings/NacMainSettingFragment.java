package com.nfcalarmclock.settings;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedKeys;
import com.nfcalarmclock.statistics.NacStatisticsSettingFragment;

/**
 * Main setting fragment.
 */
public class NacMainSettingFragment
	extends NacGenericSettingFragment
	implements Preference.OnPreferenceClickListener
{

	/**
	 * Get the title of the fragment.
	 *
	 * @return The title of the fragment.
	 */
	public String getTitle()
	{
		return getString(R.string.settings);
	}

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState,
		String rootKey)
	{
		addPreferencesFromResource(R.xml.main_preferences);

		NacSharedKeys keys = this.getSharedKeys();
		Preference appearance = findPreference(keys.getAppearance());
		Preference general = findPreference(keys.getGeneral());
		Preference statistics = findPreference(keys.getStatistics());
		Preference about = findPreference(keys.getAbout());
		Preference support = findPreference(keys.getSupport());

		// Set icons
		appearance.setIcon(this.createIconDrawable(R.mipmap.palette));
		general.setIcon(this.createIconDrawable(R.mipmap.settings));
		statistics.setIcon(this.createIconDrawable(R.mipmap.analytics));
		about.setIcon(this.createIconDrawable(R.mipmap.about));
		support.setIcon(this.createIconDrawable(R.mipmap.favorite));

		// Set on click listeners
		appearance.setOnPreferenceClickListener(this);
		general.setOnPreferenceClickListener(this);
		statistics.setOnPreferenceClickListener(this);
		about.setOnPreferenceClickListener(this);
		support.setOnPreferenceClickListener(this);
	}

	/**
	 * @return The icon drawable.
	 */
	private BitmapDrawable createIconDrawable(int id)
	{
		Resources res = getResources();
		int size = (int) res.getDimension(R.dimen.isz_large);
		Bitmap bitmap = BitmapFactory.decodeResource(res, id);
		Bitmap scaled = Bitmap.createScaledBitmap(bitmap, size, size, true);

		return new BitmapDrawable(res, scaled);
	}

	/**
	 * Called when a preference is slicked
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		NacSharedKeys keys = this.getSharedKeys();
		String preferenceKey = preference.getKey();
		FragmentManager manager = getParentFragmentManager();
		Fragment fragment;
		String title;

		// General
		if (preferenceKey.equals(keys.getGeneral()))
		{
			fragment = new NacGeneralSettingFragment();
			title = keys.getGeneralTitle();

		}
		// Appearance
		else if (preferenceKey.equals(keys.getAppearance()))
		{
			fragment = new NacAppearanceSettingFragment();
			title = keys.getAppearanceTitle();
		}
		// Statistics
		else if (preferenceKey.equals(keys.getStatistics()))
		{
			fragment = new NacStatisticsSettingFragment();
			title = keys.getStatisticsTitle();
		}
		// About
		else if (preferenceKey.equals(keys.getAbout()))
		{
			fragment = new NacAboutSettingFragment();
			title = keys.getAboutTitle();
		}
		// Support
		else if (preferenceKey.equals(keys.getSupport()))
		{
			FragmentActivity fragmentActivity = requireActivity();
			NacSupportSetting support = new NacSupportSetting(fragmentActivity);

			support.connect();
			return false;
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
