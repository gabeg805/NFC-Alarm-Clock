package com.nfcalarmclock.alarm.options.audiosource

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.getCheckedText

/**
 * A way for users to select the audio source that the alarm media should
 * originate from.
 */
class NacAudioSourceDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_audio_source, container, false)
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the bundle
		val alarm = NacBundle.getAlarm(arguments)

		// Get the views
		val radioGroup: RadioGroup = dialog!!.findViewById(R.id.audio_sources)
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Get the default values
		val default = alarm?.audioSource ?: ""

		// Setup the views
		setupAudioSources(radioGroup, default)
		setupAudioSourceColor(radioGroup)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attribute
			alarm?.audioSource = radioGroup.getCheckedText()

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Setup the audio source.
	 */
	private fun setupAudioSources(radioGroup: RadioGroup, default: String)
	{
		// No audio sources to setup
		if (radioGroup.childCount > 0)
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
	private fun setupAudioSourceColor(radioGroup: RadioGroup)
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

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAudioSourceDialog"

	}

}
