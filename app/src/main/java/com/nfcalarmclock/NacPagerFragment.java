package com.nfcalarmclock;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

/**
 */
public class NacPagerFragment
	extends FragmentActivity
	implements TabLayout.OnTabSelectedListener,
		ActivityCompat.OnRequestPermissionsResultCallback
{

	/**
	 * The view pager.
	 */
	private ViewPager mPager;

	/**
	 * The adapter for the view pager.
	 */
	private NacPagerAdapter mAdapter;

	/**
	 * The tab layout.
	 */
	private TabLayout mTabLayout;

	/**
	 * The alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Current tab position.
	 */
	private int mPosition;

	/**
	 * The tab titles.
	 */
	private static final String[] mTitles = new String[] { "Browse",
		"Ringtone", "Spotify" };

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The fragment at the given position.
	 */
	private Fragment getFragment(int position)
	{
		return this.mFragments[position];
	}

	/**
	 * @return The current tab position.
	 */
	private int getPosition()
	{
		return this.mPosition;
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
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_sound);

		Intent intent = getIntent();
		FragmentManager manager = getSupportFragmentManager();
		this.mAlarm = NacAlarmParcel.getAlarm(intent);
		this.mPager = (ViewPager) findViewById(R.id.act_sound);
		this.mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
		this.mAdapter = new NacPagerAdapter(manager, this.mAlarm, this.mTitles);
		this.mFragments = new Fragment[3];
		this.mPosition = 0;

		this.setupPager();
		this.setTabColors();
		this.selectTab();
	}

	private Fragment[] mFragments;

	@Override
	public void onAttachFragment(Fragment fragment)
	{
		super.onAttachFragment(fragment);

		if (fragment instanceof NacMusicFragment)
		{
			this.mFragments[0] = fragment;
		}
		else if (fragment instanceof NacRingtoneFragment)
		{
			this.mFragments[1] = fragment;
		}
		else if (fragment instanceof NacSpotifyFragment)
		{
			this.mFragments[2] = fragment;
		}

		this.fragmentSelected(fragment);
	}

	private void fragmentSelected(Fragment selectedFragment)
	{
		int position = this.getPosition();
		Fragment tabFragment = this.getFragment(position);

		if ((tabFragment == null) || (selectedFragment == null)
			|| (selectedFragment != tabFragment))
		{
			return;
		}

		((NacMediaFragment)selectedFragment).onSelected();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
		String[] permissions, int[] grantResults)
	{
		if (requestCode == NacMusicFragment.READ_REQUEST)
		{
			if ((grantResults.length > 0)
				&& (grantResults[0] == PackageManager.PERMISSION_GRANTED))
			{
				Fragment fragment = this.getFragment(0);

				getSupportFragmentManager().beginTransaction()
					.detach(fragment)
					.attach(fragment)
					.commitAllowingStateLoss();
			}
			else
			{
				this.selectTab(1);
			}
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
		Fragment fragment = this.getFragment(position);
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
	 * Select the tab that the fragment activity should start on.
	 */
	private void selectTab()
	{
		String[] titles = this.getTitles();
		int type = this.getAlarm().getSoundType();

		if (NacSound.isNone(type))
		{
			this.selectTab(1);
		}
		else if (NacSound.isFile(type))
		{
			this.selectTab(0);
		}
		else if (NacSound.isRingtone(type))
		{
			this.selectTab(1);
		}
		else if (NacSound.isSpotify(type))
		{
			this.selectTab(2);
		}
	}

	/**
	 * Select the tab at the given index.
	 */
	private void selectTab(int position)
	{
		Tab tab = this.mTabLayout.getTabAt(position);

		tab.select();
		onTabSelected(tab);
	}

	/**
	 * Set the tab colors.
	 */
	private void setTabColors()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);

		this.mTabLayout.setSelectedTabIndicatorColor(shared.getThemeColor());
		this.mTabLayout.setTabTextColors(NacSharedPreferences.DEFAULT_COLOR,
			shared.getThemeColor());
	}

	/**
	 * Setup the pager.
	 */
	@SuppressWarnings("deprecation")
	private void setupPager()
	{
		this.mPager.setAdapter(this.mAdapter);
		this.mTabLayout.setupWithViewPager(this.mPager);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			this.mTabLayout.addOnTabSelectedListener(this);
		}
		else
		{
			this.mTabLayout.setOnTabSelectedListener(this);
		}
	}

	/**
	 */
	public static class NacPagerAdapter
		extends FragmentPagerAdapter
	{

		/**
		 * Alarm.
		 */
		private final NacAlarm mAlarm;

		/**
		 * Tab titles.
		 */
		private final String[] mTitles;

		/**
		 * The number of items to swipe through.
		 */
		private final int mCount;

		/**
		 */
		public NacPagerAdapter(FragmentManager fragmentManager, NacAlarm alarm,
			String[] titles)
		{
			super(fragmentManager);

			this.mAlarm = alarm;
			this.mTitles = titles;
			this.mCount = titles.length;
		}

		/**
		 * @return The number of items to swipe through.
		 */
		@Override
		public int getCount()
		{
			return this.mCount;
		}

		/**
		 */
		@Override
		public Fragment getItem(int position)
		{
			NacAlarm alarm = this.mAlarm;

			if (position == 0)
			{
				return NacMusicFragment.newInstance(alarm);
			}
			else if (position == 1)
			{
				return NacRingtoneFragment.newInstance(alarm);
			}
			else if (position == 2)
			{
				return NacSpotifyFragment.newInstance(alarm);
			}
			else
			{
				NacUtility.printf("Trying to display position : %d", position);
				return null;
			}
		}

		/**
		 */
		@Override
		public CharSequence getPageTitle(int position)
		{
			return this.mTitles[position];
		}

	}

}

