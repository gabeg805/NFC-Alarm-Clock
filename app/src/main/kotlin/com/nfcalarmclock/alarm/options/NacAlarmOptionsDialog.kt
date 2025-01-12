package com.nfcalarmclock.alarm.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor

/**
 * Show the options for an alarm.
 */
class NacAlarmOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Create back stack entry lifecycle observer.
	 */
	private fun createBackStackEntryLifecycleObserver(
		navController: NavController
	): LifecycleEventObserver
	{
		return LifecycleEventObserver { _, event ->

			// Check if the destination was started
			if (event == Lifecycle.Event.ON_START)
			{
				// Hide the dialog
				this@NacAlarmOptionsDialog.dialog?.hide()
			}
			// Check if the destination was stopped
			else if (event == Lifecycle.Event.ON_STOP)
			{
				// Check if the current back stack entry contains the updated alarm
				if (navController.currentBackStackEntry?.savedStateHandle?.contains("YOYOYO") == true)
				{
					// Dismiss the dialog
					dismiss()
				}
				// The ok button was not clicked, so proceed as normal
				else
				{
					// Show the dialog
					this@NacAlarmOptionsDialog.dialog?.show()

				}
			}

		}
	}

	/**
	 * Find the correct navigation ID to use from a button ID.
	 */
	private fun findNavIdFromButtonId(id: Int): Int
	{
		return when (id)
		{
			R.id.alarm_option_audio_source -> R.id.nacAudioSourceDialog
			R.id.alarm_option_text_to_speech -> R.id.nacTextToSpeechDialog
			R.id.alarm_option_volume -> R.id.nacVolumeOptionsDialog
			R.id.alarm_option_flashlight -> R.id.nacFlashlightOptionsDialog
			//R.id.alarm_option_nfc -> R.id.nacFlashlightOptionsDialog
			R.id.alarm_option_vibrate -> R.id.nacVibrateOptionsDialog
			R.id.alarm_option_upcoming_reminder -> R.id.nacUpcomingReminderDialog
			else -> -1
		}
	}

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_alarm_options, container, false)
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the navigation controller
		val navController = findNavController()

		// Get the container of the dialog
		val container = view.findViewById<LinearLayout>(R.id.all_alarm_options)

		// Iterate over each child in the container
		for (v in container.children)
		{
			// Check if the view does not have an ID
			if (v.id == View.NO_ID)
			{
				v.setupBackgroundColor(sharedPreferences)
				continue
			}
			// Check if this is the flashlight option
			else if (v.id == R.id.alarm_option_flashlight)
			{
				// Check if should hide this option
				if (!sharedPreferences.shouldShowFlashlightButton)
				{
					// Hide the flashlight option
					v.visibility = View.GONE
				}
			}

			// Setup the button
			(v as MaterialButton).setupRippleColor(sharedPreferences)

			// Set the listener
			v.setOnClickListener {

				// Get the ID to navigate to
				val navId = findNavIdFromButtonId(it.id)

				try
				{
					// Navigate to the fragment who's button was clicked
					navController.navigate(navId, arguments)
				}
				catch (_: IllegalStateException)
				{
				}

				// Get the back stack entry
				val navBackStackEntry = navController.currentBackStackEntry ?: return@setOnClickListener

				// Create an observer for any changes to the lifecycle of the back stack entry
				val observer = createBackStackEntryLifecycleObserver(navController)

				// Observe any changes to the lifecycle of the back stack entry
				navBackStackEntry.lifecycle.addObserver(observer)

				// Observe the lifecycle of the current fragment
				waitToCleanupBackStackEntryLifecycleObserver(navBackStackEntry, observer)

			}
		}
	}

	/**
	 * Wait to cleanup the back stack entry lifecycle observer.
	 */
	private fun waitToCleanupBackStackEntryLifecycleObserver(
		navBackStackEntry: NavBackStackEntry,
		lifecycleEventObserver: LifecycleEventObserver)
	{
		viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->

			// Check if the fragment is going to be destroyed
			if (event == Lifecycle.Event.ON_DESTROY)
			{
				// Remove the observer to the back stack entry
				navBackStackEntry.lifecycle.removeObserver(lifecycleEventObserver)
			}

		})
	}

}