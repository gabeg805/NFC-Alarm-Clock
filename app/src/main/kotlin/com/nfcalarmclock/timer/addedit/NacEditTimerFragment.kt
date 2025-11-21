package com.nfcalarmclock.timer.addedit

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.nfcalarmclock.R
import com.nfcalarmclock.system.getTimer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Edit a timer.
 */
@AndroidEntryPoint
class NacEditTimerFragment
	: NacBaseAddEditTimer()
{

	/**
	 * Initialize the timer that will be used in the fragment.
	 */
	override fun initTimer()
	{
		timer = requireArguments().getTimer()!!
	}

	/**
	 * Navigate to the media picker.
	 */
	override fun navigateToMediaPicker(bundle: Bundle)
	{
		// Navigate to the media picker
		findNavController().navigate(R.id.action_nacEditTimerFragment_to_nacTimerMainMediaPickerFragment, bundle)
	}

}