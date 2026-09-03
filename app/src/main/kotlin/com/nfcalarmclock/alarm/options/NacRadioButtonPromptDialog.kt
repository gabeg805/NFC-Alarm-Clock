package com.nfcalarmclock.alarm.options

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isNotEmpty
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Show a dialog prompt that has radio buttons.
 */
abstract class NacRadioButtonPromptDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId: Int = R.layout.dlg_radio_button_prompt

	/**
	 * Title string resource ID.
	 */
	abstract val titleId: Int

	/**
	 * Description string resource ID.
	 */
	abstract val descriptionId: Int

	/**
	 * String array containing the text of each radio button.
	 */
	abstract val array: Array<String>

	/**
	 * Radio group.
	 */
	lateinit var radioGroup: RadioGroup

	/**
	 * Default selected index.
	 *
	 * This will be changed externally.
	 */
	var defaultSelectedIndex: Int = 0

	/**
	 * Currently selected index.
	 */
	protected var currentlySelectedIndex: Int = defaultSelectedIndex

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Find the radio button that is selected
		val radioButtonID: Int = radioGroup.checkedRadioButtonId
		val radioButton: View = radioGroup.findViewById(radioButtonID)

		// Set the currently selected index
		currentlySelectedIndex = radioGroup.indexOfChild(radioButton)
	}

	/**
	 * Setup all alarm options.
	 *
	 * Do nothing by default for this type of dialog.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the views
		val title: TextView = dialog!!.findViewById(R.id.prompt_title)
		val description: TextView = dialog!!.findViewById(R.id.prompt_description)
		radioGroup = dialog!!.findViewById(R.id.prompt_radio_group)

		// Setup the views
		title.setText(titleId)
		description.setText(descriptionId)
		setupRadioGroup(radioGroup, defaultSelectedIndex)
		setupRadioButtonColor(radioGroup)
	}

	/**
	 * Setup the color of the radio buttons.
	 */
	private fun setupRadioButtonColor(radioGroup: RadioGroup)
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

	/**
	 * Setup the radio group.
	 */
	protected fun setupRadioGroup(radioGroup: RadioGroup, defaultIndex: Int)
	{
		// Radio group already setup
		if (radioGroup.isNotEmpty())
		{
			return
		}

		// Iterate over each item
		array.forEachIndexed { index, item ->

			// Inflate the radio button
			val view: View = layoutInflater.inflate(R.layout.radio_button_audio_source, radioGroup, true)
			val button: RadioButton = view.findViewById(R.id.radio_button)

			// Generate a view ID
			val id = View.generateViewId()

			// Set the view ID and text for the radio button
			button.id = id
			button.text = item

			// Set the default selected radio button
			if (index == defaultIndex)
			{
				button.isChecked = true
			}

		}
	}

}
