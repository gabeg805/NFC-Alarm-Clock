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
public class FragmentPagerSupport
	extends FragmentActivity
	implements TabLayout.OnTabSelectedListener,
		ActivityCompat.OnRequestPermissionsResultCallback
{

	/**
	 * Request value for Read permissions.
	 */
	private static final int NAC_MUSIC_DIALOG_READ_REQUEST = 1;

	/**
	 * The view pager.
	 */
	private ViewPager mPager;

	/**
	 * The adapter for the view pager.
	 */
	private MyAdapter mAdapter;

	/**
	 * The tab layout.
	 */
	private TabLayout mTabLayout;

	/**
	 * The alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * The tab titles.
	 */
	private static final String[] mTitles = new String[] { "Ringtone",
		"Browse...", "Spotify" };

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
		FragmentManager manager = getSupportFragmentManager();

		for (Fragment f : manager.getFragments())
		{
			if (((position == 0) && (f instanceof NacRingtoneFragment))
				|| ((position == 1) && (f instanceof NacMusicFragment))
				|| ((position == 2) && (f instanceof NacSpotifyFragment)))
			{
				return f;
			}
		}

		return null;
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
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_sound);

		Intent intent = getIntent();
		FragmentManager manager = getSupportFragmentManager();
		NacSharedPreferences shared = new NacSharedPreferences(this);
		this.mAlarm = NacAlarmParcel.getAlarm(intent);
		this.mPager = (ViewPager) findViewById(R.id.act_sound);
		this.mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
		this.mAdapter = new MyAdapter(manager, this.mTitles.length);

		this.mPager.setAdapter(this.mAdapter);
		this.mTabLayout.setupWithViewPager(this.mPager);
		this.selectTab();
		this.mTabLayout.setSelectedTabIndicatorColor(shared.getThemeColor());
		this.mTabLayout.setTabTextColors(NacSharedPreferences.DEFAULT_COLOR,
			shared.getThemeColor());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			this.mTabLayout.addOnTabSelectedListener(this);
		}
		else
		{
			this.mTabLayout.setOnTabSelectedListener(this);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
		String[] permissions, int[] grantResults)
	{
		if (requestCode == NAC_MUSIC_DIALOG_READ_REQUEST)
		{
			if ((grantResults.length > 0)
				&& (grantResults[0] == PackageManager.PERMISSION_GRANTED))
			{
				NacMusicFragment fragment = (NacMusicFragment)
					this.getFragment(1);

				getSupportFragmentManager().beginTransaction()
					.detach(fragment)
					.attach(fragment)
					.commitAllowingStateLoss();
			}
			else
			{
				this.mTabLayout.getTabAt(0).select();
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

		if ((position == 1) && !NacPermissions.hasRead(this))
		{
			NacPermissions.request(this,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				NAC_MUSIC_DIALOG_READ_REQUEST);
		}
		else if (position == 2)
		{
			NacSpotifyFragment fragment = (NacSpotifyFragment)
				this.getFragment(position);

			fragment.setupSpotify();
		}
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
		String sound = this.getAlarm().getSound();

		for (int i=0; i < titles.length; i++)
		{
			boolean selected = false;

			if (((i == 0) && NacMedia.isRingtone(sound))
				|| ((i == 1) && NacMedia.isFile(sound))
				|| ((i == 2) && false))
			{
				this.mTabLayout.getTabAt(i).select();
				return;
			}
		}
	}

	/**
	 */
	public class MyAdapter
		extends FragmentPagerAdapter
	{

		/**
		 * The number of items to swipe through.
		 */
		private int mCount;

		/**
		 */
		public MyAdapter(FragmentManager fragmentManager, int count)
		{
			super(fragmentManager);

			this.mCount = count;
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
			NacAlarm alarm = getAlarm();

			if (position == 0)
			{
				return NacRingtoneFragment.newInstance(alarm);
			}
			else if (position == 1)
			{
				return NacMusicFragment.newInstance(alarm);
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
			return getTitles()[position];
		}

	}

}

