package com.nfcalarmclock.timer.options.volume

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.volume.NacVolumeOptionsDialog
import com.nfcalarmclock.system.getTimer

/**
 * Volume options for a timer.
 */
class NacVolumeOptionsDialog
	: NacVolumeOptionsDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

	/**
	 * View has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Change the description for the timer
		val graduallyIncreaseVolumeDescription: TextView = view.findViewById(R.id.gradually_increase_volume_description)
		val restrictVolumeDescription: TextView = view.findViewById(R.id.restrict_volume_description)

		graduallyIncreaseVolumeDescription.setText(R.string.description_volume_gradually_increase_volume_timer)
		restrictVolumeDescription.setText(R.string.description_volume_restrict_volume_timer)
	}

}