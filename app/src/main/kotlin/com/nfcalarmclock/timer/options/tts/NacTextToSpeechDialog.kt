package com.nfcalarmclock.timer.options.tts

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.tts.NacTextToSpeechDialog
import com.nfcalarmclock.system.getTimer

/**
 * Text to speech options for a timer.
 */
class NacTextToSpeechDialog
	: NacTextToSpeechDialog()
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
		val scanNfcTagDescription: TextView = view.findViewById(R.id.tts_what_to_say_description)

		scanNfcTagDescription.setText(R.string.description_text_to_speech_what_to_say_timer)
	}

}