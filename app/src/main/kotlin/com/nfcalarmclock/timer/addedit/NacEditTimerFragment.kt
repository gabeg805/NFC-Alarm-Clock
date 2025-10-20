package com.nfcalarmclock.timer.addedit

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
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