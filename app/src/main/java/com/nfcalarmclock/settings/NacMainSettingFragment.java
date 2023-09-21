package com.nfcalarmclock.settings;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import com.android.billingclient.api.ProductDetails;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedKeys;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.statistics.NacStatisticsSettingFragment;
import com.nfcalarmclock.util.NacUtility;

/**
 * Main setting fragment.
 */
public class NacMainSettingFragment
	extends NacGenericSettingFragment
{

	/**
	 * Setup the Support preference icon.
	 */
	private void animateSupportIcon()
	{
		Resources res = getResources();
		Preference preference = findPreference(getString(R.string.support_setting_key));

		// Create the icons
		BitmapDrawable whiteDrawable = this.createIconDrawable(R.mipmap.favorite);
		BitmapDrawable redDrawable = this.createIconDrawable(R.mipmap.favorite);

		// Create the transition icon
		TransitionDrawable transitionDrawable = new TransitionDrawable(
			new BitmapDrawable[]{whiteDrawable, redDrawable});
		int color = res.getColor(R.color.red);

		// Color the icon that will be transitioned to
		redDrawable.setTint(color);

		// Set the icon
		preference.setIcon(transitionDrawable);

		// Start the transition
		transitionDrawable.startTransition(1000);
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
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState,
		String rootKey)
	{
		addPreferencesFromResource(R.xml.main_preferences);

		// Set icons
		this.setupAppearanceIcon();
		this.setupGeneralIcon();
		this.setupStatisticsIcon();
		this.setupAboutIcon();
		this.setupSupportIcon();
	}

	/**
	 * A preference in the tree was clicked.
	 */
	@Override
	public boolean onPreferenceTreeClick(Preference preference)
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
		else
		{
			// Support
			if (preferenceKey.equals(keys.getSupport()))
			{
				// Start the billing flow
				this.startBillingFlow();
			}

			// Default return
			return super.onPreferenceTreeClick(preference);
		}

		// Show the fragment that was selected above
		manager.beginTransaction()
			.replace(android.R.id.content, fragment)
			.addToBackStack(title)
			.commit();

		// Default return
		return super.onPreferenceTreeClick(preference);
	}

	/**
	 * Setup the About preference icon.
	 */
	private void setupAboutIcon()
	{
		Preference preference = findPreference(getString(R.string.about_setting_key));
		BitmapDrawable drawable = this.createIconDrawable(R.mipmap.about);

		preference.setIcon(drawable);
	}

	/**
	 * Setup the Appearance preference icon.
	 */
	private void setupAppearanceIcon()
	{
		Preference preference = findPreference(getString(R.string.appearance_setting_key));
		BitmapDrawable drawable = this.createIconDrawable(R.mipmap.palette);

		preference.setIcon(drawable);
	}

	/**
	 * Setup the General preference icon.
	 */
	private void setupGeneralIcon()
	{
		Preference preference = findPreference(getString(R.string.general_setting_key));
		BitmapDrawable drawable = this.createIconDrawable(R.mipmap.settings);

		preference.setIcon(drawable);
	}

	/**
	 * Setup the Statistics preference icon.
	 */
	private void setupStatisticsIcon()
	{
		Preference preference = findPreference(getString(R.string.stats_setting_key));
		BitmapDrawable drawable = this.createIconDrawable(R.mipmap.analytics);

		preference.setIcon(drawable);
	}

	/**
	 * Setup the Support preference icon.
	 */
	private void setupSupportIcon()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		Preference preference = findPreference(getString(R.string.support_setting_key));

		// Create the icon
		BitmapDrawable drawable = this.createIconDrawable(R.mipmap.favorite);

		// Check if the user has shown their support
		if (shared.getWasAppSupported())
		{
			Resources res = getResources();
			int color = res.getColor(R.color.red);

			// Change the color of the heart to show that the user has shown their
			// support
			drawable.setTint(color);
		}

		// Set the icon
		preference.setIcon(drawable);
	}

	/**
	 * Start the billing flow.
	 */
	private void startBillingFlow()
	{
		FragmentActivity fragmentActivity = requireActivity();
		NacSupportSetting support = new NacSupportSetting(fragmentActivity);

		// Set the billing event listener
		support.setOnBillingEventListener(new NacSupportSetting.OnBillingEventListener()
		{

			/**
			 * There was a billing error.
			 */
			@Override
			public void onBillingError()
			{
				String message = getString(R.string.error_message_google_play_billing);

				// Show a toast indicating there was an error. Make sure this is run
				// on the UI thread
				fragmentActivity.runOnUiThread(() ->
					NacUtility.quickToast(fragmentActivity, message)
				);
			}

			/**
			 * The billing flow is ready to be launched.
			 */
			@Override
			public void onPrepareToLaunchBillingFlow(ProductDetails productDetails)
			{
				// Launch billing flow, passing in the activity
				support.launchBillingFlow(fragmentActivity, productDetails);
			}

			/**
			 * Support has been purchased.
			 */
			@Override
			public void onSupportPurchased()
			{
				NacSharedPreferences shared = getSharedPreferences();
				NacSharedKeys keys = getSharedKeys();
				String message = getString(R.string.message_support_thank_you);

				// Make sure the following things are run on the UI thread
				fragmentActivity.runOnUiThread(() ->
					{
						// Show a toast saying thank you
						NacUtility.quickToast(fragmentActivity, message);

						// Save that the app was supported in shared preferences
						shared.editWasAppSupported(true);

						// Re-draw the support icon
						animateSupportIcon();
					}
				);
			}
		});

		// Connect to Google Play
		support.connect();
	}

}
