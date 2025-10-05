package com.nfcalarmclock.timer.options.audiosource

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.audiosource.NacAudioSourceDialog
import com.nfcalarmclock.system.getTimer

/**
 * Select the audio source that the media should be played from.
 */
class NacAudioSourceDialog
	: NacAudioSourceDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Change the description for the timer
		val audioSourceDescription: TextView = view.findViewById(R.id.audio_source_description)

		audioSourceDescription.setText(R.string.description_audio_sources_timer)
	}

}