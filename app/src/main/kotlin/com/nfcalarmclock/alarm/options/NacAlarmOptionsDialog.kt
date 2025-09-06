package com.nfcalarmclock.alarm.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.addAlarm
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor

/**
 * Observe a back stack entry until it is cleaned up.
 */
fun NavController.navigate(
	navId: Int,
	args: Bundle?,
	fragment: NacBottomSheetDialogFragment,
	onBackStackPopulated: () -> Unit = {

		// Dismiss the dialog
		fragment.dismiss()

	}
)
{
	try
	{
		// Navigate to the fragment
		navigate(navId, args)

		// Observe the back stack entry
		observeBackStackEntry(this, fragment, onBackStackPopulated = onBackStackPopulated)
	}
	catch (_: IllegalStateException)
	{
	}
}

/**
 * Create back stack entry lifecycle observer.
 */
fun createBackStackEntryLifecycleObserver(
	navController: NavController,
	fragment: NacBottomSheetDialogFragment,
	onBackStackPopulated: () -> Unit = {

		// Dismiss the dialog
		fragment.dismiss()

	}
): LifecycleEventObserver
{
	return LifecycleEventObserver { _, event ->

		// Check if the destination was started
		if (event == Lifecycle.Event.ON_START)
		{
			// Hide the dialog
			fragment.dialog?.hide()
		}
		// Check if the destination was stopped
		else if (event == Lifecycle.Event.ON_STOP)
		{
			// Check if the current back stack entry contains the updated alarm
			if (navController.currentBackStackEntry?.savedStateHandle?.contains("YOYOYO") == true)
			{
				// Call the listener
				onBackStackPopulated()
			}
			// The ok button was not clicked, so proceed as normal
			else
			{
				// Show the dialog
				fragment.dialog?.show()

			}
		}

	}
}

/**
 * Observe a back stack entry until it is cleaned up.
 */
fun observeBackStackEntry(
	navController: NavController,
	fragment: NacBottomSheetDialogFragment,
	onBackStackPopulated: () -> Unit = {

		// Dismiss the dialog
		fragment.dismiss()

	}
)
{
	// Get the back stack entry
	val navBackStackEntry = navController.currentBackStackEntry ?: return

	// Create an observer for any changes to the lifecycle of the back stack entry
	val observer = createBackStackEntryLifecycleObserver(
		navController, fragment,
		onBackStackPopulated = onBackStackPopulated)

	// Observe any changes to the lifecycle of the back stack entry
	navBackStackEntry.lifecycle.addObserver(observer)

	// Observe the lifecycle of the current fragment
	waitToCleanupBackStackEntryLifecycleObserver(
		fragment.viewLifecycleOwner, navBackStackEntry, observer)
}

/**
 * Wait to cleanup the back stack entry lifecycle observer.
 */
fun waitToCleanupBackStackEntryLifecycleObserver(
	lifecycleOwner: LifecycleOwner,
	navBackStackEntry: NavBackStackEntry,
	lifecycleEventObserver: LifecycleEventObserver)
{
	lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->

		// Check if the fragment is going to be destroyed
		if (event == Lifecycle.Event.ON_DESTROY)
		{
			// Remove the observer to the back stack entry
			navBackStackEntry.lifecycle.removeObserver(lifecycleEventObserver)
		}

	})
}

/**
 * Show the options for an alarm.
 */
class NacAlarmOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Find the correct navigation ID to use from a button ID.
	 */
	private fun findNavIdFromButtonId(id: Int): Int
	{
		return when (id)
		{
			R.id.alarm_option_flashlight -> R.id.nacFlashlightOptionsDialog
			R.id.alarm_option_nfc -> R.id.nacScanNfcTagDialog
			R.id.alarm_option_repeat -> R.id.nacRepeatOptionsDialog
			R.id.alarm_option_vibrate -> R.id.nacVibrateOptionsDialog
			R.id.alarm_option_audio_source -> R.id.nacAudioSourceDialog
			R.id.alarm_option_text_to_speech -> R.id.nacTextToSpeechDialog
			R.id.alarm_option_upcoming_reminder -> R.id.nacUpcomingReminderDialog
			R.id.alarm_option_volume -> R.id.nacVolumeOptionsDialog
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

				// Navigate to the fragment who's button was clicked
				navController.navigate(navId, arguments, this)

			}
		}
	}

	companion object
	{

		/**
		 * Start the navigation to the alarm options dialog.
		 */
		fun navigate(
			navController: NavController,
			alarm: NacAlarm
		): MutableLiveData<NacAlarm>?
		{
			// Create bundle with the alarm
			val bundle = Bundle().addAlarm(alarm)

			// Set the graph of the nav controller
			navController.setGraph(R.navigation.nav_alarm_options, bundle)

			// Check if the nav controller did not navigate to the destination
			if (navController.currentDestination == null)
			{
				// Navigate to the destination manually
				navController.navigate(R.id.nacAlarmOptionsDialog, bundle)
			}

			// Setup an observe to watch for any changes to the alarm
			return navController.currentBackStackEntry
				?.savedStateHandle
				?.getLiveData("YOYOYO")
		}

	}

}