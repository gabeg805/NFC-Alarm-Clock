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
	 * List of fragments.
	 */
	private Fragment[] mFragments;

	/**
	 * The alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * The sound.
	 */
	private NacSound mSound;

	/**
	 * Current tab position.
	 */
	private int mPosition;

	/**
	 * The tab titles.
	 */
	//private static final String[] mTitles = new String[] { "Browse",
	//	"Ringtone", "Spotify" };
	private static final String[] mTitles = new String[] { "Browse",
		"Ringtone" };

	/**
	 * Select a fragment.
	 */
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
		return this.getFragments()[position];
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
	 * @return The sound.
	 */
	private NacSound getSound()
	{
		return this.mSound;
	}

	/**
	 * @return The sound type.
	 */
	private int getSoundType()
	{
		NacAlarm alarm = this.getAlarm();
		NacSound sound = this.getSound();

		if (alarm != null)
		{
			return alarm.getSoundType();
		}
		else if (sound != null)
		{
			return sound.getType();
		}
		else
		{
			return NacSound.TYPE_NONE;
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
	private ViewPager getViewPager()
	{
		return this.mPager;
	}

	/**
	 */
	@Override
	public void onAttachFragment(Fragment fragment)
	{
		super.onAttachFragment(fragment);

		Fragment[] list = this.getFragments();

		if (fragment instanceof NacMusicFragment)
		{
			list[0] = fragment;
		}
		else if (fragment instanceof NacRingtoneFragment)
		{
			list[1] = fragment;
		}
		else if (fragment instanceof NacSpotifyFragment)
		{
			list[2] = fragment;
		}

		this.fragmentSelected(fragment);
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
		this.mAlarm = NacIntent.getAlarm(intent);
		this.mSound = NacIntent.getSound(intent);
		this.mPager = (ViewPager) findViewById(R.id.act_sound);
		this.mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
		//this.mAdapter = new NacPagerAdapter(manager, this.mAlarm, this.mTitles);
		this.mAdapter = new NacPagerAdapter(manager);
		this.mFragments = new Fragment[this.mTitles.length];
		this.mPosition = 0;

		this.setupPager();
		this.setTabColors();
		this.selectTab();
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
		//int type = this.getAlarm().getSoundType();
		int type = this.getSoundType();

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
		TabLayout tabLayout = this.getTabLayout();
		Tab tab = tabLayout.getTabAt(position);

		tab.select();
		onTabSelected(tab);
	}

	/**
	 * Set the tab colors.
	 */
	private void setTabColors()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		TabLayout tabLayout = this.getTabLayout();

		tabLayout.setSelectedTabIndicatorColor(shared.getThemeColor());
		tabLayout.setTabTextColors(NacSharedPreferences.DEFAULT_COLOR,
			shared.getThemeColor());
	}

	/**
	 * Setup the pager.
	 */
	@SuppressWarnings("deprecation")
	private void setupPager()
	{
		ViewPager pager = this.getViewPager();
		NacPagerAdapter adapter = this.getPagerAdapter();
		TabLayout tabLayout = this.getTabLayout();

		pager.setAdapter(adapter);
		tabLayout.setupWithViewPager(pager);

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
		extends FragmentPagerAdapter
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
		//public NacPagerAdapter(FragmentManager fragmentManager, NacAlarm alarm,
		//	String[] titles)
		public NacPagerAdapter(FragmentManager fragmentManager)
		{
			super(fragmentManager);
		}

		/**
		 * @return The number of items to swipe through.
		 */
		@Override
		public int getCount()
		{
			//return this.getTitles().length;
			return getTitles().length;
		}

		/**
		 */
		@Override
		public Fragment getItem(int position)
		{
			NacAlarm alarm = getAlarm();
			NacSound sound = getSound();

			if (position == 0)
			{
				if (alarm != null)
				{
					return NacMusicFragment.newInstance(alarm);
				}
				else if (sound != null)
				{
					return NacMusicFragment.newInstance(sound);
				}
			}
			else if (position == 1)
			{
				if (alarm != null)
				{
					return NacRingtoneFragment.newInstance(alarm);
				}
				else if (sound != null)
				{
					return NacRingtoneFragment.newInstance(sound);
				}
			}
			else if (position == 2)
			{
				if (alarm != null)
				{
					return NacSpotifyFragment.newInstance(alarm);
				}
				else if (sound != null)
				{
					return NacSpotifyFragment.newInstance(sound);
				}
			}

			return null;
		}

		/**
		 */
		@Override
		public CharSequence getPageTitle(int position)
		{
			//return this.getTitles()[position];
			return getTitles()[position];
		}

		///**
		// * @return The tab titles.
		// */
		//private String[] getTitles()
		//{
		//	return this.mTitles;
		//}

	}

}

