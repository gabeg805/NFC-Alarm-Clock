package com.nfcalarmclock.alarm.options.mediapicker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentOnAttachListener
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.media.NacMedia
import com.nfcalarmclock.alarm.options.mediapicker.music.NacMusicPickerFragment
import com.nfcalarmclock.alarm.options.mediapicker.ringtone.NacRingtonePickerFragment
import com.nfcalarmclock.system.permission.readmediaaudio.NacReadMediaAudioPermission
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.util.getMediaArtist
import com.nfcalarmclock.util.getMediaBundle
import com.nfcalarmclock.util.getMediaPath
import com.nfcalarmclock.util.getMediaTitle
import com.nfcalarmclock.util.getMediaType
import com.nfcalarmclock.util.getRecursivelyPlayMedia
import com.nfcalarmclock.util.getShuffleMedia
import dagger.hilt.android.AndroidEntryPoint

/**
 * Media activity.
 */
@UnstableApi
@AndroidEntryPoint
class NacMediaPickerActivity

	// Constructor
	: FragmentActivity(),

	// Interfaces
	OnRequestPermissionsResultCallback
{

	/**
	 * View pager.
	 */
	private lateinit var viewPager: ViewPager2

	/**
	 * Tab layout.
	 */
	private lateinit var tabLayout: TabLayout

	/**
	 * Adapter for the view pager.
	 */
	private lateinit var pagerAdapter: NacPagerAdapter

	/**
	 * List of fragments.
	 */
	private val allFragments: Array<NacMediaPickerFragment?> = arrayOfNulls(2)

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Media path.
	 */
	private var mediaPath: String = ""

	/**
	 * Media artist.
	 */
	private var mediaArtist: String = ""

	/**
	 * Media title.
	 */
	private var mediaTitle: String = ""

	/**
	 * Media type.
	 */
	private var mediaType: Int = NacMedia.TYPE_NONE

	/**
	 * Whether to shuffle the media.
	 */
	private var shuffleMedia: Boolean = false

	/**
	 * Whether to recursively play the media in a directory.
	 */
	private var recursivelyPlayMedia: Boolean = false

	/**
	 * Current tab position.
	 */
	private var position: Int = 0

	/**
	 * The tab titles: Browse and Ringtone
	 */
	private val titles: Array<String> = Array(2) { "" }

	/**
	 * Listener for when a fragment is attached.
	 */
	private val onFragmentAttachListener: FragmentOnAttachListener = FragmentOnAttachListener { _, fragment ->

		// Set the music fragment
		if (fragment is NacMusicPickerFragment)
		{
			allFragments[0] = fragment
		}
		// Set the ringtone fragment
		else if (fragment is NacRingtonePickerFragment)
		{
			allFragments[1] = fragment
		}

		// Count the number of fragments that have been attached
		val count = allFragments.filterNotNull().size

		// Check if this is the first fragment that was attached
		if (count == 1)
		{
			// Check if permission should be requested
			if (shouldRequestReadMediaAudioPermission())
			{
				// Request permission to read audio files
				NacReadMediaAudioPermission.requestPermission(this@NacMediaPickerActivity, NacMusicPickerFragment.READ_REQUEST_CODE)
			}
			else
			{
				// Select the tab
				selectTabByIndex(position)
			}
		}
	}

	/**
	 * Listener for when a tab is selected.
	 */
	private val onTabSelectedListener: OnTabSelectedListener = object: OnTabSelectedListener {

		/**
		 * Called when a tab is selected.
		 */
		override fun onTabSelected(tab: TabLayout.Tab)
		{
			// Set the current position
			position = tab.position

			// The user is on the music fragment (where they can select from their
			// own music to set as the media for an alarm), but the app has not
			// been given the permission to read audio files on the phone
			if (shouldRequestReadMediaAudioPermission())
			{
				// Request permission to read audio files
				NacReadMediaAudioPermission.requestPermission(this@NacMediaPickerActivity, NacMusicPickerFragment.READ_REQUEST_CODE)
				return
			}
		}

		/**
		 */
		override fun onTabUnselected(tab: TabLayout.Tab?)
		{
		}

		/**
		 */
		override fun onTabReselected(tab: TabLayout.Tab?)
		{
		}

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

		// Get the media bundle from the intent
		val bundle = intent.getMediaBundle()

		// Set the member variables
		alarm = NacIntent.getAlarm(intent)
		mediaPath = bundle.getMediaPath()
		mediaArtist = bundle.getMediaArtist()
		mediaTitle = bundle.getMediaTitle()
		mediaType = bundle.getMediaType()
		//val mediaType = alarm?.mediaType ?: NacMedia.getType(this, mediaPath)
		shuffleMedia = bundle.getShuffleMedia()
		recursivelyPlayMedia = bundle.getRecursivelyPlayMedia()
		viewPager = findViewById(R.id.act_sound)
		tabLayout = findViewById(R.id.tab_layout)
		pagerAdapter = NacPagerAdapter(this)

		// Setup the tab titles
		titles[0] = getString(R.string.action_browse)
		titles[1] = resources.getStringArray(R.array.audio_sources)[4]

		// Get the media type

		// Set the initial position based on the media path
		// None
		position = if (NacMedia.isNone(mediaType))
		{
			1
		}
		// File or directory
		else if (NacMedia.isFile(mediaType) || NacMedia.isDirectory(mediaType))
		{
			0
		}
		// Ringtone
		else if (NacMedia.isRingtone(mediaType))
		{
			1
		}
		// Unknown
		else
		{
			1
		}
		// Spotify
		//else if (NacMedia.isSpotify(mediaType))
		//{
		//	2
		//}

		// Set the pager adapter
		viewPager.adapter = pagerAdapter

		// Set the tab layout mediator
		TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
			tab.text = titles[position]
		}.attach()

		// Setup the colors
		setupTabColors()

		// Set the listener when the back button is pressed
		onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true)
		{
			override fun handleOnBackPressed()
			{
				// Get the current position and the music fragment
				val musicFragment = allFragments[0] as NacMusicPickerFragment?

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

				// Check the number of items in the back stack is greater than 0
				if (supportFragmentManager.backStackEntryCount > 0)
				{
					// Pop the back stack
					// TODO: IndexOutOfBoundsException from crashes and ANRs could be happening here?
					// Change to ...Immedate() so hopefully this does away with the crashes
					supportFragmentManager.popBackStackImmediate()
				}
				else
				{
					// Finish the activity
					finish()
				}
			}
		})
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
		if (requestCode != NacMusicPickerFragment.READ_REQUEST_CODE)
		{
			return
		}

		// Get the music fragment
		val musicFragment = allFragments[0]

		// Permission was granted
		if ((musicFragment != null) && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			// Create a new pager adapter in order to refresh the fragments
			// in the view pager. Not sure if this is the way to do it, but
			// this is the only way that I found works
			pagerAdapter = NacPagerAdapter(this)

			// Set pager adapter on the view pager
			viewPager.adapter = pagerAdapter
		}
		// Permission was denied
		else
		{
			selectTabByIndex(1)
		}
	}

	/**
	 * Called when the activity is started.
	 */
	@Suppress("deprecation")
	override fun onStart()
	{
		// Super
		super.onStart()

		// Add a listener for when a fragment is attached
		supportFragmentManager.addFragmentOnAttachListener(onFragmentAttachListener)

		// Set the tab selected listener based on the Android API level
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			tabLayout.addOnTabSelectedListener(onTabSelectedListener)
		}
		// API < 26
		else
		{
			tabLayout.setOnTabSelectedListener(onTabSelectedListener)
		}
	}

	/**
	 * Called when the activity is stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Remove the listener for when a fragment is attached
		supportFragmentManager.removeFragmentOnAttachListener(onFragmentAttachListener)

		// Remove the tab selected listener
		tabLayout.removeOnTabSelectedListener(onTabSelectedListener)
	}

	/**
	 * Select the tab at the given index.
	 *
	 * @param  index  The index of the tab.
	 */
	private fun selectTabByIndex(index: Int)
	{
		// Get the tab at the index
		val tab = tabLayout.getTabAt(index) ?: return

		// Select the tab
		tab.select()
	}

	/**
	 * Setup the tab colors.
	 */
	private fun setupTabColors()
	{
		val shared = NacSharedPreferences(this)
		val defaultColor = resources.getInteger(R.integer.default_color)

		// Set the color
		tabLayout.setSelectedTabIndicatorColor(shared.themeColor)
		tabLayout.setTabTextColors(defaultColor, shared.themeColor)
	}

	/**
	 * Check if should request read media audio permission.
	 */
	private fun shouldRequestReadMediaAudioPermission(): Boolean
	{
		// Current tab is on the Browse tab, but the app does not have
		// permission to read media so that the user can actually browse
		return (position == 0) && !NacReadMediaAudioPermission.hasPermission(this)
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
		}

		/**
		 * Create a music fragment.
		 */
		private fun createMusicFragment(): Fragment
		{
			// Check that alarm is not null
			return if (alarm != null)
			{
				NacMusicPickerFragment.newInstance(alarm)
			}
			// Use the media path
			else
			{
				NacMusicPickerFragment.newInstance(mediaPath, mediaArtist, mediaTitle,
					mediaType, shuffleMedia, recursivelyPlayMedia)
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
				NacRingtonePickerFragment.newInstance(alarm)
			}
			// Use the media path
			else
			{
				NacRingtonePickerFragment.newInstance(mediaPath, mediaArtist, mediaTitle,
					mediaType, shuffleMedia, recursivelyPlayMedia)
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

	companion object
	{

		/**
		 * Create an intent that will be used to start the media activity with
		 * an alarm attached.
		 *
		 * @param context A context.
		 * @param alarm   An alarm.
		 *
		 * @return An intent that will be used to start the media activity with
		 *         an alarm attached.
		 */
		fun getStartIntentWithAlarm(
			context: Context,
			alarm: NacAlarm
		): Intent
		{
			// Create an intent
			val intent = Intent(context, NacMediaPickerActivity::class.java)

			// Add the alarm to the intent
			return NacIntent.addAlarm(intent, alarm)
		}

	}

}