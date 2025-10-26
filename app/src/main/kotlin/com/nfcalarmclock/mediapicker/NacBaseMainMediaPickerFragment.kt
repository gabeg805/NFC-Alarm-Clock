package com.nfcalarmclock.mediapicker

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.mediapicker.music.NacMusicPickerFragment
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.getMediaArtist
import com.nfcalarmclock.system.getMediaPath
import com.nfcalarmclock.system.getMediaTitle
import com.nfcalarmclock.system.getMediaType
import com.nfcalarmclock.system.getRecursivelyPlayMedia
import com.nfcalarmclock.system.getShuffleMedia
import com.nfcalarmclock.system.media.NacMedia
import com.nfcalarmclock.system.media.isMediaDirectory
import com.nfcalarmclock.system.media.isMediaFile
import com.nfcalarmclock.system.media.isMediaNone
import com.nfcalarmclock.system.media.isMediaRingtone
import com.nfcalarmclock.system.permission.readmediaaudio.NacReadMediaAudioPermission
import com.nfcalarmclock.view.setupThemeColor

/**
 * Base main media picker fragment that will contain the child fragments.
 */
abstract class NacBaseMainMediaPickerFragment<T: NacAlarm>
	: Fragment()
{

	/**
	 * Pager adapter.
	 */
	inner class NacPagerAdapter(
		fragmentManager: FragmentManager,
		lifecycle: Lifecycle
	) : FragmentStateAdapter(fragmentManager, lifecycle)
	{

		/**
		 * Create the fragment.
		 */
		@UnstableApi
		override fun createFragment(position: Int): Fragment
		{
			println("Creating fragment at position : $position")
			return when (position)
			{

				// Music fragment
				0 -> createMusicFragment()

				// Ringtone fragment
				1 -> createRingtoneFragment()

				// Unknown
				else -> createRingtoneFragment()

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

	/**
	 * Create a music fragment.
	 */
	protected abstract fun createMusicFragment(): Fragment

	/**
	 * Create a music fragment.
	 */
	protected abstract fun createRingtoneFragment(): Fragment

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
	 * Item.
	 */
	protected var item: T? = null

	/**
	 * Media path.
	 */
	protected var mediaPath: String = ""

	/**
	 * Media artist.
	 */
	protected var mediaArtist: String = ""

	/**
	 * Media title.
	 */
	protected var mediaTitle: String = ""

	/**
	 * Media type.
	 */
	protected var mediaType: Int = NacMedia.TYPE_NONE

	/**
	 * Whether to shuffle the media.
	 */
	protected var shuffleMedia: Boolean = false

	/**
	 * Whether to recursively play the media in a directory.
	 */
	protected var recursivelyPlayMedia: Boolean = false

	/**
	 * Current tab position.
	 */
	private var position: Int = 0

	/**
	 * The tab titles: Browse and Ringtone
	 */
	private val titles: Array<String> = Array(2) { "" }

	/**
	 * Back pressed callback
	 */
	private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true)
	{
		@OptIn(UnstableApi::class)
		override fun handleOnBackPressed()
		{
			println("Back pressed! AYYYYY")
			//// Get the current destination
			//val destinationId = navController.currentDestination?.id

			//// Current destination is the music picker fragment
			//if (destinationId == R.id.nacMusicPickerFragment)
			//{
			//	// Get the file browser or do nothing if unable to
			//	val musicFragment = childFragmentManager.findFragmentById(destinationId) as NacMusicPickerFragment?
			//	val fileBrowser = musicFragment?.fileBrowser ?: return

			//	// Go to previous directory if not already at root level,
			//	// otherwise, do a normal back press which will exit the activity
			//	if (!fileBrowser.isAtRoot)
			//	{
			//		println("Previous directory!!!!!")
			//		fileBrowser.previousDirectory()
			//		return
			//	}
			//}

			//val x = navController.popBackStack()
			//println("GOING back to jank : $x")
			//// Get the current position and the music fragment
			//val musicFragment = allFragments[0] as NacMusicPickerFragment?

			//// Check if at position 0 and that the music fragment is defined. The
			//// music fragment (where the user can browse for music to play for
			//// an item, instead of a ringtone) is at position 0. Do custom stuff
			//// if back is pressed for this fragment
			//if (position == 0 && musicFragment != null)
			//{
			//	val fileBrowser = musicFragment.fileBrowser ?: return

			//	// Browser is undefined. Do nothing

			//	// Go to previous directory if not already at root level,
			//	// otherwise, do a normal back press which will exit the activity
			//	if (!fileBrowser.isAtRoot)
			//	{
			//		fileBrowser.previousDirectory()
			//		return
			//	}
			//}

			//// Check the number of items in the back stack is greater than 0
			//if (supportFragmentManager.backStackEntryCount > 0)
			//{
			//	// Pop the back stack
			//	// TODO: IndexOutOfBoundsException from crashes and ANRs could be happening here?
			//	// Change to ...Immedate() so hopefully this does away with the crashes
			//	supportFragmentManager.popBackStackImmediate()
			//}
			//else
			//{
			//	// Finish the activity
			//	finish()
			//}
		}
	}

	/**
	 * Listener for when a tab is selected.
	 */
	private val onTabSelectedListener: OnTabSelectedListener = object: OnTabSelectedListener {

		/**
		 * Tab is selected.
		 */
		@OptIn(UnstableApi::class)
		override fun onTabSelected(tab: TabLayout.Tab)
		{
			println("Tab selected! $position")
			// Set the current position
			position = tab.position

			// The user is on the music fragment (where they can select from their
			// own music to set as the media for an item), but the app has not
			// been given the permission to read audio files on the phone
			if ((position == 0) && shouldRequestReadMediaAudioPermission())
			{
				// Request permission to read audio files
				NacReadMediaAudioPermission.requestPermission(requireActivity(), NacMusicPickerFragment.READ_REQUEST_CODE)
				return
			}
		}

		/**
		 * Tab unselected.
		 */
		override fun onTabUnselected(tab: TabLayout.Tab?) {}

		/**
		 * Tab reselected.
		 */
		override fun onTabReselected(tab: TabLayout.Tab?) {}

	}

	/**
	 * Create the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		// TODO: Rename this layout
		println("Media nav fragment onCreasteView()")
		return inflater.inflate(R.layout.frg_media_picker, container, false)
	}

	/**
	 * Fragment is started.
	 */
	@Suppress("deprecation")
	override fun onStart()
	{
		// Super
		super.onStart()
		println("Media nav fragment onStart()")

		//// Add a listener for when a fragment is attached
		//childFragmentManager.addFragmentOnAttachListener(onFragmentAttachListener)

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
	 * Fragment stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()
		println("Media nav fragment onStop()")

		// Remove the back press callback
		onBackPressedCallback.remove()

		//// Remove the listener for when a fragment is attached
		//childFragmentManager.removeFragmentOnAttachListener(onFragmentAttachListener)

		// Remove the tab selected listener
		tabLayout.removeOnTabSelectedListener(onTabSelectedListener)
	}

	/**
	 * View is created.
	 */
	@OptIn(UnstableApi::class)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)
		println("Media nav fragment onViewCreated()")

		// Get the arguments
		val arguments = requireArguments()

		// Set the fragment item
		setFragmentItem()

		// Set the member variables
		mediaPath = item?.mediaPath ?: arguments.getMediaPath()
		mediaArtist = item?.mediaArtist ?: arguments.getMediaArtist()
		mediaTitle = item?.mediaTitle ?: arguments.getMediaTitle()
		mediaType = item?.mediaType ?: arguments.getMediaType()
		shuffleMedia = item?.shouldShuffleMedia ?: arguments.getShuffleMedia()
		recursivelyPlayMedia = item?.shouldRecursivelyPlayMedia ?: arguments.getRecursivelyPlayMedia()
		// TODO: Change this view id
		viewPager = view.findViewById(R.id.view_pager)
		tabLayout = view.findViewById(R.id.tab_layout)
		println("Creating pager adapter")
		pagerAdapter = NacPagerAdapter(childFragmentManager, lifecycle)

		// Setup the tab titles
		titles[0] = getString(R.string.action_browse)
		titles[1] = resources.getStringArray(R.array.audio_sources)[4]

		// Set the initial position based on the media path
		// None
		position = if (mediaType.isMediaNone())
		{
			1
		}
		// File or directory
		else if (mediaType.isMediaFile() || mediaType.isMediaDirectory())
		{
			0
		}
		// Ringtone
		else if (mediaType.isMediaRingtone())
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
		println("Setting pager adapter")
		viewPager.adapter = pagerAdapter

		// Set the current page
		viewPager.setCurrentItem(position, false)

		// Set the tab layout mediator
		println("Setting tab layout mediator")
		TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
			println("New position : $position")
			tab.text = titles[position]
		}.attach()

		// Setup the colors
		println("Setting tab colors")
		setupTabColors()

		// Set the listener when the back button is pressed
		println("On back pressed callback")
		requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

		// Change the visibility based on the current destination
		//navController.addOnDestinationChangedListener { _, destination, _ ->
		//	println("DEstationation changed! $destination")
		//	when (destination.id)
		//	{

		//		// Music
		//		R.id.nacMusicPickerFragment ->
		//		{
		//			println("SHOWING MUSIC PICKER")
		//			// Check if permission should be requested
		//			if (shouldRequestReadMediaAudioPermission())
		//			{
		//				// Request permission to read audio files
		//				NacReadMediaAudioPermission.requestPermission(requireActivity(), NacMusicPickerFragment.READ_REQUEST_CODE)
		//			}
		//			else
		//			{
		//				// Select the tab
		//				selectTabByIndex(position)
		//			}
		//		}

		//		// Ringtone
		//		R.id.nacRingtonePickerFragment ->
		//		{
		//			println("SHOWING RINGTONE")
		//		}

		//		// Unknown
		//		else -> {}

		//	}
		//}
	}

	/**
	 * Permission request receives a result.
	 */
	@OptIn(UnstableApi::class)
	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>,
		grantResults: IntArray)
	{
		// Super
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		// Request code does not match the one that this activity sent
		if (requestCode != NacMusicPickerFragment.READ_REQUEST_CODE)
		{
			return
		}

		//// Get the music fragment
		//val musicFragment = allFragments[0]

		// Permission was granted
		//if ((musicFragment != null) && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
		{
			// Create a new pager adapter in order to refresh the fragments
			// in the view pager. Not sure if this is the way to do it, but
			// this is the only way that I found works
			pagerAdapter = NacPagerAdapter(childFragmentManager, lifecycle)

			// Set pager adapter on the view pager
			viewPager.adapter = pagerAdapter
		}
		// Permission was denied
		else
		{
			viewPager.setCurrentItem(1, true)
			//selectTabByIndex(1)
		}
	}

	///**
	// * Select the tab at the given index.
	// *
	// * @param  index  The index of the tab.
	// */
	//private fun selectTabByIndex(index: Int)
	//{
	//	// Get the tab at the index
	//	val tab = tabLayout.getTabAt(index) ?: return

	//	// Select the tab
	//	tab.select()
	//}

	/**
	 * Set the fragment item.
	 */
	abstract fun setFragmentItem()

	/**
	 * Setup the tab colors.
	 */
	private fun setupTabColors()
	{
		// Get the shared preferences and default color
		val context = requireContext()
		val sharedPreferences = NacSharedPreferences(context)

		// Setup
		tabLayout.setupThemeColor(sharedPreferences)
	}

	/**
	 * Check if should request read media audio permission.
	 */
	private fun shouldRequestReadMediaAudioPermission(): Boolean
	{
		// Current tab is on the Browse tab, but the app does not have
		// permission to read media so that the user can actually browse
		return (position == 0) && !NacReadMediaAudioPermission.hasPermission(requireContext())
	}

}