package com.nfcalarmclock.audiosource

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * A way for users to select the audio source that the alarm media should
 * originate from.
 */
class NacAudioSourceDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when an audio source is selected.
	 */
	fun interface OnAudioSourceSelectedListener
	{
		fun onAudioSourceSelected(audioSource: String)
	}

	/**
	 * Default audio source.
	 */
	var defaultAudioSource: String = ""

	/**
	 * The currently selected audio source.
	 */
	private val audioSource: String
		get()
		{
			// Get the view ID of the currently selected radio button
			val viewId = radioGroup!!.checkedRadioButtonId

			// Get the radio button
			val radioButton = radioGroup!!.findViewById<RadioButton>(viewId)

			// Get the text of the radio button
			return radioButton.text.toString()
		}

	/**
	 * Radio button group for each alarm source.
	 */
	private var radioGroup: RadioGroup? = null

	/**
	 * Listener for when an audio source is selected.
	 */
	var onAudioSourceSelectedListener: OnAudioSourceSelectedListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_audio_source)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				onAudioSourceSelectedListener?.onAudioSourceSelected(audioSource)

			}
			.setNegativeButton(R.string.action_cancel) { _, _ ->
			}
			.setView(R.layout.dlg_alarm_audio_source)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Set the radio group view
		radioGroup = dialog!!.findViewById(R.id.audio_sources)

		// Setup the views
		setupAudioSources()
		setupAudioSourceColor()
	}

	/**
	 * Setup the audio source.
	 */
	private fun setupAudioSources()
	{
		// No audio sources to setup
		if (radioGroup!!.childCount > 0)
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
			if (defaultAudioSource.isNotEmpty() && (src == defaultAudioSource))
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
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Generate the color state list of the radio button
		val colorStateList = ColorStateList(states, colors)

		// Iterate over each radio button
		for (i in 0 until radioGroup!!.childCount)
		{
			// Get the button
			val button = radioGroup!!.getChildAt(i) as RadioButton

			// Set the color state list
			button.buttonTintList = colorStateList
		}
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAudioSourceDialog"

	}

}
