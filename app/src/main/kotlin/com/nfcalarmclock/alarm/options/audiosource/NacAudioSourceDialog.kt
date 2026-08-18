package com.nfcalarmclock.alarm.options.audiosource

import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacRadioButtonPromptDialog
import com.nfcalarmclock.view.getCheckedText

/**
 * Select the audio source that the media should be played from.
 */
open class NacAudioSourceDialog
	: NacRadioButtonPromptDialog()
{

	/**
	 * Title string resource ID.
	 */
	override val titleId: Int = R.string.action_alarm_option_audio_source

	/**
	 * Description string resource ID.
	 */
	override val descriptionId: Int = R.string.description_audio_sources_alarm

	/**
	 * String array containing the text of each radio button.
	 */
	override val array: Array<String> by lazy { resources.getStringArray(R.array.audio_sources) }

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.audioSource = radioGroup.getCheckedText()
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build(sharedPreferences)

		// Set the default index
		defaultSelectedIndex = array.indexOf(a.audioSource)

		// Super
		super.setupAlarmOptions(alarm)
	}

}
