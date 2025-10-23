package com.nfcalarmclock.alarm.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.navigate
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor

/**
 * Options for an alarm.
 */
open class NacAlarmOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Find the correct navigation ID to use from a button ID.
	 */
	open fun findNavIdFromButtonId(id: Int): Int
	{
		return when (id)
		{
			R.id.option_flashlight -> R.id.nacFlashlightOptionsDialog
			R.id.option_nfc -> R.id.nacScanNfcTagDialog
			R.id.option_repeat -> R.id.nacRepeatOptionsDialog
			R.id.option_vibrate -> R.id.nacVibrateOptionsDialog
			R.id.option_audio_source -> R.id.nacAudioSourceDialog
			R.id.option_text_to_speech -> R.id.nacTextToSpeechDialog
			R.id.option_upcoming_reminder -> R.id.nacUpcomingReminderDialog
			R.id.option_volume -> R.id.nacVolumeOptionsDialog
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
		val container: LinearLayout = view.findViewById(R.id.all_options)

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
			else if (v.id == R.id.option_flashlight)
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
			val bundle = alarm.toBundle()

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

		/**
		 * Quickly navigate directly to one of the main alarm option dialogs:
		 *
		 * Repeat, Vibrate, NFC, or Flashlight.
		 */
		fun quickNavigate(
			navController: NavController,
			destinationId: Int,
			alarm: NacAlarm
		): MutableLiveData<NacAlarm>?
		{
			// Create bundle with the alarm
			val bundle = alarm.toBundle()

			// Inflate the graph for the nav controller
			val navGraph = navController.navInflater.inflate(R.navigation.nav_quick_alarm_options)

			// Set the start destination
			navGraph.setStartDestination(destinationId)

			// Set the graph of the nav controller
			navController.setGraph(navGraph, bundle)

			// Check if the nav controller did not navigate to the destination
			if (navController.currentDestination == null)
			{
				// Navigate to the destination manually
				navController.navigate(destinationId, bundle)
			}

			// Setup an observe to watch for any changes to the alarm
			return navController.currentBackStackEntry
				?.savedStateHandle
				?.getLiveData("YOYOYO")
		}

	}

}