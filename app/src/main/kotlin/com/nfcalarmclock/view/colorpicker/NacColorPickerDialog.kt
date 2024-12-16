package com.nfcalarmclock.view.colorpicker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.dialog.NacDialogFragment
import com.nfcalarmclock.view.setupInputLayoutColor

/**
 * Handle displaying the color picker dialog.
 */
class NacColorPickerDialog
	: NacDialogFragment(),
	View.OnTouchListener
{

	/**
	 * Listener for when a color is selected.
	 */
	fun interface OnColorSelectedListener
	{
		fun onColorSelected(color: Int)
	}

	/**
	 * Listener for when the default color is selected.
	 */
	fun interface OnDefaultColorSelectedListener
	{
		fun onDefaultColorSelected(dialog: NacColorPickerDialog)
	}

	/**
	 * Initial color to show.
	 */
	var initialColor: Int = 0

	/**
	 * Color picker.
	 */
	private lateinit var colorPicker: NacColorPicker

	/**
	 * Example of the color.
	 */
	private lateinit var exampleColor: ImageView

	/**
	 * Input layout for the hex color.
	 */
	private lateinit var inputLayout: TextInputLayout

	/**
	 * Edit text for the hex color.
	 */
	private lateinit var editText: TextInputEditText

	/**
	 * The color of the edit text box.
	 */
	var color: Int
		get() = colorPicker.color
		set(value)
		{
			// Select the color of the color picker. This will set the HSV,
			// move the color selector, and redraw the shader
			colorPicker.selectColor(value)
		}

	/**
	 * The EditText color.
	 */
	private val editTextColor: String
		get() = editText.text.toString()

	/**
	 * Listener for when the color is selected.
	 */
	var onColorSelectedListener: OnColorSelectedListener? = null

	/**
	 * Listener for when the default color is selected.
	 */
	var onDefaultColorSelectedListener: OnDefaultColorSelectedListener? = null

	/**
	 * Check if can parse the color.
	 *
	 * @return True if can parse the color, and False otherwise.
	 */
	private fun canParseColor(name: String): Boolean
	{
		// Attempt to parse the color
		return try
		{
			Color.parseColor("#$name")
			true
		}
		// Unable to parse the color
		catch (e: IllegalArgumentException)
		{
			false
		}
	}

	/**
	 * Close the keyboard.
	 */
	@Suppress("deprecation")
	private fun closeKeyboard()
	{
		val context = requireContext()

		// Check the Android version. It needs to be done differently depending on
		// the version due to deprecation
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			val inputManager = context.getSystemService(InputMethodManager::class.java)
			val token = dialog!!.currentFocus?.windowToken

			// Hide the keyboard
			inputManager.hideSoftInputFromWindow(token, 0)
		}
		// API < 31
		else
		{
			val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			//val flags = InputMethodManager.HIDE_IMPLICIT_ONLY

			// Hide the keyboard
			//inputManager.toggleSoftInput(flags, 0)
			inputManager.toggleSoftInput(0, 0)
		}
	}

	/**
	 * Check if valid hex string was input into the EditText.
	 *
	 * @return True if it is a valid hex string, and False otherwise.
	 */
	private fun isHexString(name: String): Boolean
	{
		// Iterate over each character in the hex string
		name.forEach { char ->

			// Convert the character to base 16
			val digit = char.digitToIntOrNull(16) ?: -1

			// Unable to convert the character so this is not a hex string
			if (digit == -1)
			{
				return false
			}

		}

		// Success. This is a hex string
		return true
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_select_color)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				onColorSelectedListener?.onColorSelected(color)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setNeutralButton(R.string.action_default, null)
			.setView(R.layout.dlg_color_picker)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Setup the member variables
		colorPicker = dialog!!.findViewById(R.id.color_picker)
		exampleColor = dialog!!.findViewById(R.id.color_example)
		editText = dialog!!.findViewById(R.id.color_edittext)
		inputLayout = dialog!!.findViewById(R.id.color_input_layout)

		// Set the initial color
		color = initialColor
		updateExampleAndHexColors(initialColor)

		// Setup the views
		colorPicker.setOnTouchListener(this)
		setupEditText()
		setupNeutralButtonOnClickListener()
	}

	/**
	 * Capture touch events on the color picker.
	 */
	override fun onTouch(view: View, event: MotionEvent): Boolean
	{
		// Call the touch event
		view.onTouchEvent(event)

		// Update the example color and hex color
		updateExampleAndHexColors(color)

		// Perform click
		if (event.action == MotionEvent.ACTION_UP)
		{
			view.performClick()
		}

		return true
	}

	/**
	 * Setup the edit text color.
	 */
	private fun setupEditText()
	{
		// Get the context
		val context = requireContext()

		// Setup the color
		inputLayout.setupInputLayoutColor(context, sharedPreferences!!)

		// Set the keyboard action listener so that the keyboard closes when the user
		// hits enter
		editText.setOnEditorActionListener { _, action, event ->

			// Check if a keyboard action occurred
			if ((event != null) || (action != EditorInfo.IME_ACTION_GO))
			{
				return@setOnEditorActionListener false
			}

			// Check if the color is not hex or it cannot be parsed
			if (!isHexString(editTextColor) || !canParseColor(editTextColor))
			{
				// Show error hint message
				inputLayout.setError(getString(R.string.error_message_select_color))
				return@setOnEditorActionListener false
			}

			// Set the new color on the color picker, example color, and hex color
			color = Color.parseColor("#$editTextColor")
			updateExampleAndHexColors(color)

			// Close the keyboard
			closeKeyboard()
			return@setOnEditorActionListener true
		}
	}

	/**
	 * Setup neutral button on click listener.
	 */
	private fun setupNeutralButtonOnClickListener()
	{
		// Get the neutral button
		val alertDialog = dialog as AlertDialog
		val neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL)

		// Set the listener
		neutralButton.setOnClickListener {

			// Call the listener
			onDefaultColorSelectedListener?.onDefaultColorSelected(this)

			// Update the example color and hex color
			updateExampleAndHexColors(color)

		}
	}

	/**
	 * Update the example and hex colors.
	 */
	private fun updateExampleAndHexColors(newColor: Int)
	{
		// Set the example color
		exampleColor.setColorFilter(newColor, PorterDuff.Mode.SRC)

		// Set the hex color in the edit text
		val hexColor = String.format("%06X", (0xFFFFFF and newColor))

		editText.setText(hexColor)

		// Clear the error hint message, if it was set
		inputLayout.error = null
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacColorPickerDialog"

	}

}