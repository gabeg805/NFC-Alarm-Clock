package com.nfcalarmclock.mediapicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
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
			// Current destination is the music picker fragment
			if (viewPager.currentItem == 0)
			{
				// Get the music picker fragment
				val musicPickerFragment = childFragmentManager.fragments
					.first { it is NacMusicPickerFragment<*> } as NacMusicPickerFragment<*>?

				// Get the file browser
				val fileBrowser = musicPickerFragment?.fileBrowser

				// Go to previous directory if not already at root level,
				// otherwise, do a normal back press which will exit the activity
				if (fileBrowser?.isAtRoot == false)
				{
					fileBrowser.previousDirectory()
					return
				}
			}

			// Go back to the previous fragment
			findNavController().popBackStack()
		}
	}

	/**
	 * Page change listener for the ViewPager2.
	 */
	private val onPageChangeCallback: ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback()
	{

		/**
		 * Page selected.
		 */
		@OptIn(UnstableApi::class)
		override fun onPageSelected(position: Int)
		{
			super.onPageSelected(position)

			// Get the activity
			val activity = requireActivity()

			// On the music tab and the app does not have permission to read audio files
			if ((position == 0) && !NacReadMediaAudioPermission.hasPermission(activity))
			{
				// Request permission to read audio files
				permissionRequestResult.launch(NacReadMediaAudioPermission.permissionName)
				return
			}
		}

	}

	/**
	 * Permission request to read media audio.
	 */
	private val permissionRequestResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->

		// Permission was granted
		if (permissionGranted)
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
		}

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
		return inflater.inflate(R.layout.frg_media_picker, container, false)
	}

	/**
	 * Fragment is started.
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Set the listener when the back button is pressed
		requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

		// Register the page change listener
		viewPager.registerOnPageChangeCallback(onPageChangeCallback)
	}

	/**
	 * Fragment stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Remove the back press callback
		onBackPressedCallback.remove()

		// Remove the page change listener
		viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
	}

	/**
	 * View is created.
	 */
	@OptIn(UnstableApi::class)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

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
		viewPager = view.findViewById(R.id.view_pager)
		tabLayout = view.findViewById(R.id.tab_layout)
		pagerAdapter = NacPagerAdapter(childFragmentManager, lifecycle)

		// Setup
		setupInitialPosition()
		setupViewPager()
		setupTabLayout()
	}

	/**
	 * Set the fragment item.
	 */
	abstract fun setFragmentItem()

	/**
	 * Setup the initial position.
	 */
	private fun setupInitialPosition()
	{
	}

	/**
	 * Setup the TabLayout.
	 */
	private fun setupTabLayout()
	{
		// Get the shared preferences and default color
		val context = requireContext()
		val sharedPreferences = NacSharedPreferences(context)

		// Setup the color
		tabLayout.setupThemeColor(sharedPreferences)

		// Setup the tab titles
		titles[0] = getString(R.string.action_browse)
		titles[1] = resources.getStringArray(R.array.audio_sources)[4]

		// Set the tab layout mediator
		TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
			tab.text = titles[position]
		}.attach()
	}

	/**
	 * Setup the ViewPager2.
	 */
	private fun setupViewPager()
	{
		// Determine the initial page to be on
		val initialPage = if (mediaType.isMediaNone())
		{
			// None
			1
		}
		else if (mediaType.isMediaFile() || mediaType.isMediaDirectory())
		{
			// File or directory
			0
		}
		else if (mediaType.isMediaRingtone())
		{
			// Ringtone
			1
		}
		else
		{
			// Unknown
			1
		}
		// Spotify
		//else if (NacMedia.isSpotify(mediaType))
		//{
		//	2
		//}

		// Set the pager adapter
		viewPager.adapter = pagerAdapter

		// Set the current page
		viewPager.setCurrentItem(initialPage, false)
	}

}