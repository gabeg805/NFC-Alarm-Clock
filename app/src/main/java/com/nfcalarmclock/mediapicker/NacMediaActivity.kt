package com.nfcalarmclock.mediapicker

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.mediapicker.music.NacMusicFragment
import com.nfcalarmclock.mediapicker.ringtone.NacRingtoneFragment
import com.nfcalarmclock.permission.readmediaaudio.NacReadMediaAudioPermission.hasPermission
import com.nfcalarmclock.permission.readmediaaudio.NacReadMediaAudioPermission.requestPermission
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacIntent.getAlarm
import com.nfcalarmclock.util.NacIntent.getMedia
import dagger.hilt.android.AndroidEntryPoint

/**
 * Media activity.
 */
@AndroidEntryPoint
class NacMediaActivity
	: FragmentActivity(),
	FragmentOnAttachListener,
	OnTabSelectedListener,
	OnRequestPermissionsResultCallback
{

	/**
	 * View pager.
	 */
	private var viewPager: ViewPager2? = null

	/**
	 * Adapter for the view pager.
	 */
	private var pagerAdapter: NacPagerAdapter? = null

	/**
	 * Tab layout.
	 */
	private var tabLayout: TabLayout? = null

	/**
	 * List of fragments.
	 */
	private val fragments: Array<Fragment?>

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Media path.
	 */
	private var media: String? = null

	/**
	 * Current tab position.
	 */
	private var position = 0

	/**
	 * The tab titles.
	 */
	private val titles = arrayOfNulls<String>(2)

	/**
	 * The media type.
	 */
	private val mediaType: Int
		get()
		{
			return alarm?.mediaType
				?: if (media != null)
					{
						NacMedia.getType(this, media!!)
					}
					else
					{
						NacMedia.TYPE_NONE
					}
		}

	/**
	 * Constructor
	 */
	init
	{
		// Setup the fragment list
		fragments = arrayOfNulls(titles.size)

		// Add a listener for when a fragment is attached
		supportFragmentManager.addFragmentOnAttachListener(this)
	}

	/**
	 * Select a fragment.
	 */
	private fun fragmentSelected(selectedFragment: Fragment?)
	{
		val tabFragment = fragments[position]

		// The selected fragment is undefined or does not match the position of
		// the fragment that the user should be on
		if (selectedFragment == null || selectedFragment !== tabFragment)
		{
			return
		}

		// The user is on the music fragment (where they can select from their
		// own music to set as the media for an alarm), but the app has not
		// been given the permission to read audio files on the phone
		if (selectedFragment is NacMusicFragment
			&& !hasPermission(this))
		{
			// Request permission to read audio files
			// TODO: Stack overflow error here
			requestPermission(this, NacMusicFragment.READ_REQUEST_CODE)
		}

		// Select the fragment
		(selectedFragment as NacMediaFragment).onSelected()
	}

	/**
	 * Called when the fragment is attached.
	 */
	override fun onAttachFragment(manager: FragmentManager,
		frag: Fragment)
	{
		// Music fragment
		if (frag is NacMusicFragment)
		{
			fragments[0] = frag
		}
		// Ringtone fragment
		else if (frag is NacRingtoneFragment)
		{
			fragments[1] = frag
		}

		// Select the gragment
		fragmentSelected(frag)
	}

	/**
	 * Called when the back button is pressed
	 */
	override fun onBackPressed()
	{
		// Get the current position and the music fragment
		val musicFragment = fragments[0] as NacMusicFragment?

		// Check if at position 0 and that the music fragment is defined. The
		// music fragment (where the user can browse for music to play for
		// an alarm, instead of a ringtone) is at position 0. Do custom stuff
		// if back is pressed for this fragment
		if (position == 0 && musicFragment != null)
		{
			val fileBrowser = musicFragment.fileBrowser ?: return

			// Browser is undefined. Do nothing

			// Go to previous directory if not already at root level,
			// otherwise, do a normal back press which will exit the activity
			if (!fileBrowser.isAtRoot)
			{
				fileBrowser.previousDirectory()
				return
			}
		}

		// Normal back press which should exit the activity
		super.onBackPressed()
	}

	/**
	 * Called when the activity is created.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Set the layout
		setContentView(R.layout.act_sound)

		// Get all the audio sources
		val audioSources = resources.getStringArray(R.array.audio_sources)

		// Set the member variables
		alarm = getAlarm(intent)
		media = getMedia(intent)
		viewPager = findViewById(R.id.act_sound)
		tabLayout = findViewById(R.id.tab_layout)
		titles[0] = getString(R.string.action_browse)
		titles[1] = audioSources[3]
		pagerAdapter = NacPagerAdapter(this)
		position = 0

		// Setup the views
		setupViewPager()
		setupTabColors()
		selectTabByMediaType(mediaType)
	}

	/**
	 * Called when the permission request receives a result.
	 */
	override fun onRequestPermissionsResult(requestCode: Int,
		permissions: Array<String>, grantResults: IntArray)
	{
		// Super
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		// Request code does not match the one that this activity sent
		if (requestCode != NacMusicFragment.READ_REQUEST_CODE)
		{
			return
		}

		// Get the music fragment
		val musicFragment = fragments[0]

		// Permission was granted
		if (musicFragment != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{

			// Create a new pager adapter in order to refresh the fragments
			// in the view pager. Not sure if this is the way to do it, but
			// this is the only way that I found works
			val adapter = NacPagerAdapter(this)

			// Set pager adapter on the view pager
			viewPager!!.adapter = adapter

			// Set the new pager adapter member variable
			pagerAdapter = adapter
		}
		// Permission was denied
		else
		{
			selectTabByIndex(1)
		}
	}

	/**
	 * Called when a tab is reselected.
	 */
	override fun onTabReselected(tab: TabLayout.Tab)
	{
	}

	/**
	 * Called when a tab is selected.
	 */
	override fun onTabSelected(tab: TabLayout.Tab)
	{
		// Set the current position
		position = tab.position

		// Get the fragment at that position
		val fragment = fragments[position]

		// Select the fragment
		fragmentSelected(fragment)
	}

	/**
	 * Called when a tab is unselected.
	 */
	override fun onTabUnselected(tab: TabLayout.Tab)
	{
	}

	/**
	 * Select the tab at the given index.
	 *
	 * @param  index  The index of the tab.
	 */
	private fun selectTabByIndex(index: Int)
	{
		// Check if the tab layout is not defined yet
		if (tabLayout == null)
		{
			return
		}

		// Get the tab at the index
		val tab = tabLayout!!.getTabAt(index)

		// Check if the tab is not null
		if (tab != null)
		{
			// Select the tab
			tab.select()
			onTabSelected(tab)
		}
	}

	/**
	 * Select the tab that the fragment activity should start on.
	 *
	 * @param  mediaType  The media type for when the alarm goes off.
	 */
	private fun selectTabByMediaType(mediaType: Int)
	{
		// None
		if (NacMedia.isNone(mediaType))
		{
			selectTabByIndex(1)
		}
		// File or directory
		else if (NacMedia.isFile(mediaType) || NacMedia.isDirectory(mediaType))
		{
			selectTabByIndex(0)
		}
		// Ringtone
		else if (NacMedia.isRingtone(mediaType))
		{
			selectTabByIndex(1)
		}
		// Spotify
		//else if (NacMedia.isSpotify(mediaType))
		//{
		//	selectTabByIndex(2)
		//}
	}

	/**
	 * Setup the tab colors.
	 */
	private fun setupTabColors()
	{
		val shared = NacSharedPreferences(this)
		val defaultColor = resources.getInteger(R.integer.default_color)

		// Set the color
		tabLayout!!.setSelectedTabIndicatorColor(shared.themeColor)
		tabLayout!!.setTabTextColors(defaultColor, shared.themeColor)
	}

	/**
	 * Setup the view pager.
	 */
	@Suppress("deprecation")
	private fun setupViewPager()
	{
		// Set the pager adapter
		viewPager!!.adapter = pagerAdapter

		// Set the tab layout mediator
		TabLayoutMediator(tabLayout!!, viewPager!!) { tab: TabLayout.Tab, position: Int ->
			tab.text = titles[position]
		}.attach()

		// Set the tab selected listener based on the Android API level
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			tabLayout!!.addOnTabSelectedListener(this)
		}
		// API < 26
		else
		{
			tabLayout!!.setOnTabSelectedListener(this)
		}
	}

	/**
	 * Pager adapter.
	 */
	inner class NacPagerAdapter(fa: FragmentActivity)
		: FragmentStateAdapter(fa)
	{

		/**
		 * Create the fragment.
		 */
		@UnstableApi
		override fun createFragment(position: Int): Fragment
		{
			return when (position)
			{

				// Music fragment
				0 ->
				{
					createMusicFragment()
				}

				// Ringtone fragment
				1 ->
				{
					createRingtoneFragment()
				}

				// Unknown
				else ->
				{
					createRingtoneFragment()
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
		}

		/**
		 * Create a music fragment.
		 */
		private fun createMusicFragment(): Fragment
		{
			// Check that alarm is not null
			return if (alarm != null)
			{
				NacMusicFragment.newInstance(alarm)
			}
			// Check that media is not null
			else if (media != null)
			{
				NacMusicFragment.newInstance(media)
			}
			// Return an empty music fragment
			else
			{
				NacMusicFragment()
			}
		}

		/**
		 * Create a ringtone fragment.
		 */
		@UnstableApi
		private fun createRingtoneFragment(): Fragment
		{
			// Check that alarm is not null
			return if (alarm != null)
			{
				NacRingtoneFragment.newInstance(alarm)
			}
			// Check that media is not null
			else if (media != null)
			{
				NacRingtoneFragment.newInstance(media)
			}
			// Return an empty music fragment
			else
			{
				NacRingtoneFragment()
			}
		}

		/**
		 * Get the number of items to swipe through.
		 *
		 * @return The number of items to swipe through.
		 */
		override fun getItemCount(): Int
		{
			return titles.size
		}

	}

}