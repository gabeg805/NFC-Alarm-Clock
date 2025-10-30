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

		// Get the views
		val ttsDescription: TextView = view.findViewById(R.id.tts_what_to_say_description)
		val whatToSayNameDescription: TextView = view.findViewById(R.id.tts_say_alarm_name)

		// Change the description for the timer
		ttsDescription.setText(R.string.description_text_to_speech_what_to_say_timer)
		whatToSayNameDescription.setText(R.string.message_tts_timer_name)
	}

}