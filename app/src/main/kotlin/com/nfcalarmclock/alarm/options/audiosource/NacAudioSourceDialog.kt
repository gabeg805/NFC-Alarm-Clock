package com.nfcalarmclock.alarm.options.audiosource

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.view.getCheckedText
import androidx.core.view.isNotEmpty

/**
 * A way for users to select the audio source that the alarm media should
 * originate from.
 */
class NacAudioSourceDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_audio_source

	/**
	 * Radio group of all the radio buttons.
	 */
	private lateinit var radioGroup: RadioGroup

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

		// Set the radio group
		radioGroup = dialog!!.findViewById(R.id.audio_sources)

		// Setup the views
		setupAudioSources(a.audioSource)
		setupAudioSourceColor()
	}

	/**
	 * Setup the audio source.
	 */
	private fun setupAudioSources(default: String)
	{
		// No audio sources to setup
		if (radioGroup.isNotEmpty())
		{
			return
		}

		// Get all audio sources
		val allAudioSources = resources.getStringArray(R.array.audio_sources)

		// Iterate over each audio source
		for (src in allAudioSources)
		{
			// Inflate the radio button
			val view = layoutInflater.inflate(R.layout.radio_button, radioGroup, true)
			val button = view.findViewById<RadioButton>(R.id.radio_button)

			// Generate a view ID
			val id = View.generateViewId()

			// Set the view ID and text for the radio button
			button.id = id
			button.text = src

			// Ensure the default audio source is checked. If none is set, then nothing
			// will be checked
			if (default.isNotEmpty() && (src == default))
			{
				button.isChecked = true
			}
		}

	}

	/**
	 * Setup the color of the audio source items.
	 */
	private fun setupAudioSourceColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Generate the color state list of the radio button
		val colorStateList = ColorStateList(states, colors)

		// Iterate over each radio button
		for (i in 0 until radioGroup.childCount)
		{
			// Get the button
			val button = radioGroup.getChildAt(i) as RadioButton

			// Set the color state list
			button.buttonTintList = colorStateList
		}
	}

}
