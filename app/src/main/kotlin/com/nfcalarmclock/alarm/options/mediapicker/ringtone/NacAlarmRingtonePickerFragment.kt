package com.nfcalarmclock.alarm.options.mediapicker.ringtone

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.mediapicker.ringtone.NacRingtonePickerFragment
import com.nfcalarmclock.system.addMediaInfo
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.toBundle
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pick a ringtone.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class NacAlarmRingtonePickerFragment
	: NacRingtonePickerFragment<NacAlarm>()
{

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

	/**
	 * Save the fragment item.
	 */
	override fun saveFragmentItem()
	{
		// Update the alarm for the activity
		alarmViewModel.update(item!!)

		// Reschedule the alarm
		NacScheduler.update(requireContext(), item!!)
	}

	/**
	 * Set the fragment item.
	 */
	override fun setFragmentItem(bundle: Bundle)
	{
		item = bundle.getAlarm()
	}

	companion object
	{

		/**
		 * Create a new instance of this fragment.
		 */
		fun newInstance(item: NacAlarm): Fragment
		{
			// Create the fragment
			val fragment: Fragment = NacAlarmRingtonePickerFragment()

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
			val fragment: Fragment = NacAlarmRingtonePickerFragment()

			// Add the bundle to the fragment
			fragment.arguments = Bundle()
				.addMediaInfo(mediaPath, mediaArtist, mediaTitle, mediaType,
					shuffleMedia, recursivelyPlayMedia)

			return fragment
		}

	}

}