package com.nfcalarmclock.alarm.options.name

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment
import com.nfcalarmclock.view.setupInputLayoutColor

/**
 * The dialog class that will handle saving the name of the alarm.
 */
class NacNameDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when a name is entered.
	 */
	fun interface OnNameEnteredListener
	{
		fun onNameEntered(name: String)
	}

	/**
	 * Default alarm name to show.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultName : String = ""

	/**
	 * Listener for when the name is entered.
	 */
	var onNameEnteredListener: OnNameEnteredListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Get the name that is in the edit text
				val editText = dialog!!.findViewById<TextInputEditText>(R.id.name_entry)
				val name = editText.text.toString()

				// Call the listener
				onNameEnteredListener?.onNameEntered(name)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_alarm_name)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the context
		val context = requireContext()

		// Get the views
		val inputLayout: TextInputLayout = dialog!!.findViewById(R.id.name_box)
		val editText: TextInputEditText = dialog!!.findViewById(R.id.name_entry)

		// Set the dialog background color
		dialog!!.window?.setBackgroundDrawableResource(R.color.gray)

		// Set text to show
		editText.setText(defaultName)

		// Select all the text so that the user can delete it easily if they
		// choose to
		editText.selectAll()

		// This will show the newline button in the soft keyboard, since the
		// text entry is multi-line
		editText.imeOptions = EditorInfo.IME_ACTION_DONE

		// Setup the color
		inputLayout.setupInputLayoutColor(context, sharedPreferences!!)

		// Show the keyboard. This occurs on a delay so that the window has time to
		// be in focus
		waitToShowKeyboard(editText)
	}

	/**
	 * Show the keyboard.
	 */
	@Suppress("deprecation")
	private fun showKeyboard(editText: TextInputEditText)
	{
		// Define the context
		val context: Context

		try
		{
			// Get the context
			context = requireContext()
		}
		catch (_: IllegalStateException)
		{
			return
		}

		// Request focus
		editText.requestFocus()

		// Check API >= 31
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			val imm = context.getSystemService(InputMethodManager::class.java)

			// Show implicit
			imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
		}
		// API < 31
		else
		{
			val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

			// Show forced
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
		}
	}

	/**
	 * Wait to show the soft keyboard.
	 */
	private fun waitToShowKeyboard(editText: TextInputEditText)
	{
		val looper = requireContext().mainLooper
		val handler = Handler(looper)

		// Wait for 200 ms and then show the keyboard
		handler.postDelayed({
			showKeyboard(editText)
		}, 200)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacNameDialog"

		/**
		 * Create a dialog that can be shown easily.
		 */
		fun create(
			name: String,
			onNameEnteredListener: (String) -> Unit = {}
		): NacNameDialog
		{
			// Create the dialog
			val dialog = NacNameDialog()

			// Set the default name
			dialog.defaultName = name

			// Set the listener
			dialog.onNameEnteredListener = OnNameEnteredListener { n ->
				onNameEnteredListener(n)
			}

			return dialog
		}

	}

}