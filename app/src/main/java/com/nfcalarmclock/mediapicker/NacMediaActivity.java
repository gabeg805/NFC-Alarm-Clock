package com.nfcalarmclock.mediapicker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayoutMediator;

import com.nfcalarmclock.R;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.media.NacMedia;
import com.nfcalarmclock.mediapicker.music.NacMusicFragment;
import com.nfcalarmclock.mediapicker.ringtone.NacRingtoneFragment;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedDefaults;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.system.NacIntent;

/**
 */
public class NacMediaActivity
	extends FragmentActivity
	implements FragmentOnAttachListener,
		TabLayout.OnTabSelectedListener,
		ActivityCompat.OnRequestPermissionsResultCallback
{

	/**
	 * View pager.
	 */
	private ViewPager2 mViewPager;

	/**
	 * Adapter for the view pager.
	 */
	private NacPagerAdapter mAdapter;

	/**
	 * Tab layout.
	 */
	private TabLayout mTabLayout;

	/**
	 * List of fragments.
	 */
	private final Fragment[] mFragments;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Media path.
	 */
	private String mMediaPath;

	/**
	 * Current tab position.
	 */
	private int mPosition;

	/**
	 * The tab titles.
	 */
	private final String[] mTitles = new String[2];

	/**
	 */
	public NacMediaActivity()
	{
		super();

		FragmentManager manager = getSupportFragmentManager();
		int length = this.getTitles().length;

		this.mFragments = new Fragment[length];

		manager.addFragmentOnAttachListener(this);
	}

	/**
	 * Select a fragment.
	 */
	private void fragmentSelected(Fragment selectedFragment)
	{
		int position = this.getPosition();
		Fragment tabFragment = this.getFragments()[position];

		if ((selectedFragment == null) || (selectedFragment != tabFragment))
		{
			return;
		}

		((NacMediaFragment)selectedFragment).onSelected();
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The list of fragments.
	 */
	private Fragment[] getFragments()
	{
		return this.mFragments;
	}

	/**
	 * @return The view page adapter.
	 */
	private NacPagerAdapter getPagerAdapter()
	{
		return this.mAdapter;
	}

	/**
	 * @return The current tab position.
	 */
	private int getPosition()
	{
		return this.mPosition;
	}

	/**
	 * @return The media path.
	 */
	private String getMedia()
	{
		return this.mMediaPath;
	}

	/**
	 * @return The media type.
	 */
	private int getMediaType()
	{
		NacAlarm alarm = this.getAlarm();
		String media = this.getMedia();

		if (alarm != null)
		{
			return alarm.getMediaType();
		}
		else if (media != null)
		{
			return NacMedia.getType(this, media);
		}
		else
		{
			return NacMedia.TYPE_NONE;
		}
	}

	/**
	 * @return The tab layout.
	 */
	private TabLayout getTabLayout()
	{
		return this.mTabLayout;
	}

	/**
	 * @return The array of titles.
	 */
	private String[] getTitles()
	{
		return this.mTitles;
	}

	/**
	 * @return The view pager.
	 */
	private ViewPager2 getViewPager()
	{
		return this.mViewPager;
	}

	/**
	 */
	@Override
	public void onAttachFragment(@NonNull FragmentManager manager,
		@NonNull Fragment fragment)
	{
		Fragment[] list = this.getFragments();

		//if (list == null)
		//{
		//	this.mFragments = new Fragment[this.mTitles.length];
		//	list = this.mFragments;
		//}

		if (fragment instanceof NacMusicFragment)
		{
			list[0] = fragment;
		}
		else if (fragment instanceof NacRingtoneFragment)
		{
			list[1] = fragment;
		}
		//else if (fragment instanceof NacSpotifyFragment)
		//{
		//	list[2] = fragment;
		//}

		this.fragmentSelected(fragment);
	}

	/**
	 */
	@Override
	public void onBackPressed()
	{
		int position = this.getPosition();
		NacMusicFragment musicFragment = (NacMusicFragment)
			this.getFragments()[0];

		if ((position == 0) && (musicFragment != null))
		{
			musicFragment.backPressed();
			return;
		}

		super.onBackPressed();
	}

	/**
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_sound);

		Intent intent = getIntent();
		FragmentManager manager = getSupportFragmentManager();
		NacSharedConstants cons = new NacSharedConstants(this);

		this.mAlarm = NacIntent.getAlarm(intent);
		this.mMediaPath = NacIntent.getMedia(intent);
		this.mViewPager = findViewById(R.id.act_sound);
		this.mTabLayout = findViewById(R.id.tab_layout);
		this.mTitles[0] = cons.getActionBrowse();
		this.mTitles[1] = cons.getAudioSources().get(3);
		this.mAdapter = new NacPagerAdapter(this);
		this.mPosition = 0;

		int mediaType = this.getMediaType();

		this.setupViewPager();
		this.setupTabColors();
		this.selectTabByMediaType(mediaType);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
		@NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == NacMusicFragment.READ_REQUEST_CODE)
		{
			if ((grantResults.length > 0)
				&& (grantResults[0] == PackageManager.PERMISSION_GRANTED))
			{
				Fragment fragment = this.getFragments()[0];

				if (fragment != null)
				{
					getSupportFragmentManager().beginTransaction()
						.detach(fragment)
						.attach(fragment)
						.commitAllowingStateLoss();
					return;
				}
			}

			this.selectTabByIndex(1);
		}
	}

	/**
	 */
	@Override
	public void onTabReselected(Tab tab)
	{
	}

	/**
	 */
	@Override
	public void onTabSelected(Tab tab)
	{
		int position = tab.getPosition();
		Fragment fragment = this.getFragments()[position];
		this.mPosition = position;

		this.fragmentSelected(fragment);
	}

	/**
	 */
	@Override
	public void onTabUnselected(Tab tab)
	{
	}

	/**
	 * Select the tab at the given index.
	 *
	 * @param  index  The index of the tab.
	 */
	private void selectTabByIndex(int index)
	{
		TabLayout tabLayout = this.getTabLayout();

		if (tabLayout != null)
		{
			Tab tab = tabLayout.getTabAt(index);

			if (tab != null)
			{
				tab.select();
				onTabSelected(tab);
			}
		}
	}

	/**
	 * Select the tab that the fragment activity should start on.
	 *
	 * @param  mediaType  The media type for when the alarm goes off.
	 */
	private void selectTabByMediaType(int mediaType)
	{
		if (NacMedia.isNone(mediaType))
		{
			this.selectTabByIndex(1);
		}
		else if (NacMedia.isFile(mediaType) || NacMedia.isDirectory(mediaType))
		{
			this.selectTabByIndex(0);
		}
		else if (NacMedia.isRingtone(mediaType))
		{
			this.selectTabByIndex(1);
		}
		else if (NacMedia.isSpotify(mediaType))
		{
			this.selectTabByIndex(2);
		}
	}

	/**
	 * Setup the tab colors.
	 */
	private void setupTabColors()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		NacSharedDefaults defaults = shared.getDefaults();
		TabLayout tabLayout = this.getTabLayout();

		tabLayout.setSelectedTabIndicatorColor(shared.getThemeColor());
		tabLayout.setTabTextColors(defaults.getColor(),
			shared.getThemeColor());
	}

	/**
	 * Setup the view pager.
	 */
	@SuppressWarnings("deprecation")
	private void setupViewPager()
	{
		ViewPager2 viewPager = this.getViewPager();
		NacPagerAdapter adapter = this.getPagerAdapter();
		TabLayout tabLayout = this.getTabLayout();

		viewPager.setAdapter(adapter);

		new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
			tab.setText(this.getTitles()[position])).attach();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			tabLayout.addOnTabSelectedListener(this);
		}
		else
		{
			tabLayout.setOnTabSelectedListener(this);
		}
	}

	/**
	 */
	//public static class NacPagerAdapter
	public class NacPagerAdapter
		extends FragmentStateAdapter
	{

		///**
		// * Alarm.
		// */
		//private final NacAlarm mAlarm;

		///**
		// * Tab titles.
		// */
		//private final String[] mTitles;

		/**
		 */
		public NacPagerAdapter(FragmentActivity fa)
		{
			super(fa);
		}

		/**
		 */
		//@NonNull
        @Override
		public Fragment createFragment(int position)
		{
			NacAlarm alarm = getAlarm();
			String media = getMedia();

			if (position == 0)
			{
				if (alarm != null)
				{
					return NacMusicFragment.newInstance(alarm);
				}
				else if (media != null)
				{
					return NacMusicFragment.newInstance(media);
				}
			}
			else if (position == 1)
			{
				if (alarm != null)
				{
					return NacRingtoneFragment.newInstance(alarm);
				}
				else if (media != null)
				{
					return NacRingtoneFragment.newInstance(media);
				}
			}
			//else if (position == 2)
			//{
			//	if (alarm != null)
			//	{
			//		return NacSpotifyFragment.newInstance(alarm);
			//	}
			//	else if (media!= null)
			//	{
			//		return NacSpotifyFragment.newInstance(media);
			//	}
			//}

			return null;
		}

		/**
		 * @return The number of items to swipe through.
		 */
		@Override
		public int getItemCount()
		{
			return getTitles().length;
		}

	}

}

