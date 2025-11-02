package com.nfcalarmclock.timer.addedit

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
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

	/**
	 * Called when the fragment is created.
	 */
	@OptIn(UnstableApi::class)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Setup the done button
		setupSaveButton()
	}

}