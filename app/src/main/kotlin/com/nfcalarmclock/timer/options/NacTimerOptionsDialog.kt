package com.nfcalarmclock.timer.options

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
import com.nfcalarmclock.alarm.options.NacAlarmOptionsDialog
import com.nfcalarmclock.system.navigate
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor

/**
 * Options for a timer.
 */
class NacTimerOptionsDialog
	: NacAlarmOptionsDialog()
{

	/**
	 * Find the correct navigation ID to use from a button ID.
	 */
	override fun findNavIdFromButtonId(id: Int): Int
	{
		return when (id)
		{
			R.id.option_flashlight -> R.id.nacFlashlightOptionsDialog3
			R.id.option_nfc -> R.id.nacScanNfcTagDialog3
			R.id.option_vibrate -> R.id.nacVibrateOptionsDialog3
			R.id.option_audio_source -> R.id.nacAudioSourceDialog2
			R.id.option_text_to_speech -> R.id.nacTextToSpeechDialog2
			R.id.option_volume -> R.id.nacVolumeOptionsDialog2
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
		return inflater.inflate(R.layout.dlg_timer_options, container, false)
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
				// TODO: The main timer options dialog does not close. Not a deal breaker but should figure out why this is
				navController.navigate(navId, arguments, this)

			}
		}
	}

	companion object
	{

		/**
		 * Navigate directly to one of the destination in the navigation graph.
		 */
		fun navigateTo(
			navController: NavController,
			destinationId: Int,
			timer: NacTimer
		): MutableLiveData<NacTimer>?
		{
			// Create bundle with the timer
			val bundle = timer.toBundle()

			// Set the graph of the nav controller
			navController.setGraph(R.navigation.nav_timer_options, bundle)

			// Navigate to the destination
			navController.navigate(destinationId, bundle)

			// Setup an observe to watch for any changes to the timer
			return navController.currentBackStackEntry
				?.savedStateHandle
				?.getLiveData("YOYOYO")
		}

	}

}