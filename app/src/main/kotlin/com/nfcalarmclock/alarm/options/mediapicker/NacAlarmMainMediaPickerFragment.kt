package com.nfcalarmclock.alarm.options.mediapicker

import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.mediapicker.music.NacAlarmMusicPickerFragment
import com.nfcalarmclock.alarm.options.mediapicker.ringtone.NacAlarmRingtonePickerFragment
import com.nfcalarmclock.mediapicker.NacBaseMainMediaPickerFragment
import com.nfcalarmclock.system.getAlarm

/**
 * Main media picker fragment that will contain the child fragments.
 */
class NacAlarmMainMediaPickerFragment
	: NacBaseMainMediaPickerFragment<NacAlarm>()
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
			NacAlarmMusicPickerFragment.newInstance(item!!)
		}
		// Use the media path
		else
		{
			NacAlarmMusicPickerFragment.newInstance(mediaPath, mediaArtist, mediaTitle,
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
			NacAlarmRingtonePickerFragment.newInstance(item!!)
		}
		// Use the media path
		else
		{
			NacAlarmRingtonePickerFragment.newInstance(mediaPath, mediaArtist, mediaTitle,
				mediaType, shuffleMedia, recursivelyPlayMedia)
		}
	}

	/**
	 * Set the fragment item.
	 */
	override fun setFragmentItem()
	{
		item = arguments?.getAlarm()
	}

}