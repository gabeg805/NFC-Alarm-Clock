package com.nfcalarmclock.timer.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.NacAlarmOptionsDialog
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.db.NacTimer

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
	 * Create the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_timer_options, container, false)
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

			// Nav controller did not navigate to the destination after setting graph
			if (navController.currentDestination == null)
			{
				// Navigate to the destination manually
				navController.navigate(destinationId, bundle)
			}

			// Setup an observe to watch for any changes to the timer
			return navController.currentBackStackEntry
				?.savedStateHandle
				?.getLiveData("YOYOYO")
		}

	}

}