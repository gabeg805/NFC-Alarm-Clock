package com.nfcalarmclock.timer.addedit

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nfcalarmclock.R
import com.nfcalarmclock.timer.db.NacTimer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Add a timer.
 */
@AndroidEntryPoint
class NacAddTimerFragment
	: NacBaseAddEditTimer()
{

	/**
	 * Callback when back is pressed.
	 */
	private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
		override fun handleOnBackPressed()
		{
			lifecycleScope.launch {

				// No timers so go back to show alarms fragment
				if (timerViewModel.count() == 0)
				{
					findNavController().navigate(R.id.action_global_nacShowAlarmsFragment)
				}
				// Normal pop back stack
				else
				{
					findNavController().popBackStack()
				}

			}
		}
	}


	/**
	 * Initialize the timer that will be used in the fragment.
	 */
	override fun initTimer()
	{
		timer = NacTimer.build(sharedPreferences)
	}

	/**
	 * Navigate to the media picker.
	 */
	override fun navigateToMediaPicker(bundle: Bundle)
	{
		// Navigate to the media picker
		findNavController().navigate(R.id.action_nacAddTimerFragment_to_nacTimerMainMediaPickerFragment, bundle)
	}

	/**
	 * Fragment stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Remove the back press callback
		onBackPressedCallback.remove()
	}

	/**
	 * Called when the fragment is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Setup back press
		requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
	}

}