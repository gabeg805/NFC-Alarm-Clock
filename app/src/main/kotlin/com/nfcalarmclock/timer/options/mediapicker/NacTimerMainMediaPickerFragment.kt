package com.nfcalarmclock.timer.options.mediapicker

import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.mediapicker.NacBaseMainMediaPickerFragment
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.timer.options.mediapicker.music.NacTimerMusicPickerFragment
import com.nfcalarmclock.timer.options.mediapicker.ringtone.NacTimerRingtonePickerFragment

/**
 * Main media picker fragment that will contain the child fragments.
 */
class NacTimerMainMediaPickerFragment
	: NacBaseMainMediaPickerFragment<NacTimer>()
{

	/**
	 * Create a music fragment.
	 */
	@OptIn(UnstableApi::class)
	override fun createMusicFragment(): Fragment
	{
		// Item is not null
		return if (item != null)
		{
			NacTimerMusicPickerFragment.newInstance(item!!)
		}
		// Use the media path
		else
		{
			NacTimerMusicPickerFragment.newInstance(mediaPath, mediaArtist, mediaTitle,
				mediaType, shuffleMedia, recursivelyPlayMedia)
		}
	}

	/**
	 * Create a ringtone fragment.
	 */
	@OptIn(UnstableApi::class)
	override fun createRingtoneFragment(): Fragment
	{
		// Item is not null
		return if (item != null)
		{
			NacTimerRingtonePickerFragment.newInstance(item!!)
		}
		// Use the media path
		else
		{
			NacTimerRingtonePickerFragment.newInstance(mediaPath, mediaArtist, mediaTitle,
				mediaType, shuffleMedia, recursivelyPlayMedia)
		}
	}

	/**
	 * Set the fragment item.
	 */
	override fun setFragmentItem()
	{
		item = arguments?.getTimer()
	}

}