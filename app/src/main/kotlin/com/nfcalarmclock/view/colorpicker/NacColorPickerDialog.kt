package com.nfcalarmclock.view.colorpicker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Handle displaying the color picker dialog.
 */
class NacColorPickerDialog
	: NacDialogFragment(),
	TextView.OnEditorActionListener,
	TextWatcher,
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
	private var colorPicker: NacColorPicker? = null

	/**
	 * Example of the color.
	 */
	private var exampleColor: ImageView? = null

	/**
	 * User can enter a hex color value.
	 */
	private var editText: EditText? = null

	/**
	 * The color of the edit text box.
	 */
	var color: Int
		get() = colorPicker!!.color
		set(value)
		{
			// Select the color of the color picker. This will set the HSV,
			// move the color selector, and redraw the shader
			colorPicker!!.selectColor(value)

			// Set the example color
			exampleColor!!.setColorFilter(value, PorterDuff.Mode.SRC)

			// Set the hex color in the edit text
			editText!!.setText(colorPicker!!.hexColor)
		}

	/**
	 * The EditText color.
	 */
	private val editTextColor: String
		get() = editText!!.text.toString()

	/**
	 * Listener for when the color is selected.
	 */
	var onColorSelectedListener: OnColorSelectedListener? = null

	/**
	 * Listener for when the default color is selected.
	 */
	var onDefaultColorSelectedListener: OnDefaultColorSelectedListener? = null

	/**
	 * Note: This is required to implement TextWatcher.
	 */
	override fun afterTextChanged(s: Editable)
	{
	}

	/**
	 * Note: This is required to implement TextWatcher.
	 */
	override fun beforeTextChanged(seq: CharSequence, start: Int, count: Int, after: Int)
	{
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
	 * Close the keyboard when the user hits enter.
	 */
	override fun onEditorAction(tv: TextView?, action: Int, event: KeyEvent?): Boolean
	{
		//if ((event == null) && (action == EditorInfo.IME_ACTION_DONE))
		if ((event != null) || (action != EditorInfo.IME_ACTION_DONE))
		{
			return false
		}

		// Check if the color is not hex or it cannot be parsed
		if (!NacColorPicker.isHexString(editTextColor) || !NacColorPicker.canParseColor(editTextColor))
		{
			// Toast an error message
			quickToast(requireContext(), R.string.error_message_select_color)
			return false
		}

		// Set the new color (this affects the color picker, example color, and edit text
		color = Color.parseColor("#$editTextColor")

		// Close the keyboard
		closeKeyboard()
		return true
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
		editText = dialog!!.findViewById(R.id.color_value)

		// Set the initial color
		color = initialColor

		// Setup the views
		colorPicker!!.setOnTouchListener(this)
		setupEditText()
		setupNeutralButtonOnClickListener()
	}

	/**
	 * Note: This is required to implement TextWatcher.
	 */
	override fun onTextChanged(seq: CharSequence, start: Int, before: Int, count: Int)
	{
	}

	/**
	 * Capture touch events on the color picker.
	 */
	override fun onTouch(view: View, event: MotionEvent): Boolean
	{
		// Call the touch event
		view.onTouchEvent(event)

		// Set the example color and hex color via the setter backing field
		color = color

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
		val shared = NacSharedPreferences(requireContext())

		// Set the listeners
		editText!!.addTextChangedListener(this)
		editText!!.setOnEditorActionListener(this)

		// Set the options and types
		editText!!.setRawInputType(InputType.TYPE_CLASS_TEXT)
		editText!!.imeOptions = EditorInfo.IME_ACTION_DONE

		// Set the background color
		editText!!.backgroundTintList = ColorStateList.valueOf(shared.themeColor)
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

		}
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacColorPickerDialog"

	}

}