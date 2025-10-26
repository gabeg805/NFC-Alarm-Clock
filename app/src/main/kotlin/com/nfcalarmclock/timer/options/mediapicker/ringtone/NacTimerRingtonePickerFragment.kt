package com.nfcalarmclock.timer.options.mediapicker.ringtone

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.mediapicker.ringtone.NacRingtonePickerFragment
import com.nfcalarmclock.system.addMediaInfo
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.timer.db.NacTimer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pick a ringtone.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class NacTimerRingtonePickerFragment
	: NacRingtonePickerFragment<NacTimer>()
{

	/**
	 * Timer view model.
	 */
	private val timerViewModel: NacTimerViewModel by viewModels()

	/**
	 * Save the fragment item.
	 */
	override fun saveFragmentItem()
	{
		// Update the timer
		timerViewModel.update(item!!)
	}

	/**
	 * Set the fragment item.
	 */
	override fun setFragmentItem(bundle: Bundle)
	{
		item = bundle.getTimer()
	}

	companion object
	{

		/**
		 * Create a new instance of this fragment.
		 */
		fun newInstance(item: NacTimer): Fragment
		{
			// Create the fragment
			val fragment: Fragment = NacTimerRingtonePickerFragment()

			// Add the bundle to the fragment
			fragment.arguments = item.toBundle()

			return fragment
		}

		/**
		 * Create a new instance of this fragment.
		 */
		fun newInstance(
			mediaPath: String,
			mediaArtist: String,
			mediaTitle: String,
			mediaType: Int,
			shuffleMedia: Boolean,
			recursivelyPlayMedia: Boolean
		): Fragment
		{
			// Create the fragment
			val fragment: Fragment = NacTimerRingtonePickerFragment()

			// Add the bundle to the fragment
			fragment.arguments = Bundle()
				.addMediaInfo(mediaPath, mediaArtist, mediaTitle, mediaType,
					shuffleMedia, recursivelyPlayMedia)

			return fragment
		}

	}

}