package com.nfcalarmclock.alarm.options.audiosource

import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacRadioButtonPromptDialog
import com.nfcalarmclock.view.getCheckedText
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Select the audio source that the media should be played from.
 */
open class NacAudioSourceDialog
	: NacRadioButtonPromptDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId: Int = R.layout.dlg_audio_source

	/**
	 * Title string resource ID.
	 */
	override val titleId: Int = R.string.action_alarm_option_audio_source

	/**
	 * Description string resource ID.
	 */
	override val descriptionId: Int = R.string.description_audio_sources_alarm

	/**
	 * String array containing the text of each radio button.
	 */
	override val array: Array<String> by lazy { resources.getStringArray(R.array.audio_sources) }

	/**
	 * Speakers and bluetooth switch.
	 */
	private lateinit var speakersAndBluetoothSwitch: SwitchCompat

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm)
	{
		// Update the alarm
		alarm.audioSource = radioGroup.getCheckedText()
		alarm.shouldPlayAudioThroughSpeakersAndBluetooth = speakersAndBluetoothSwitch.isChecked
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm)
	{
		// Set the default index
		defaultSelectedIndex = array.indexOf(alarm.audioSource)
			.coerceAtLeast(0)

		// Super
		super.setupAlarmOptions(alarm)

		// Setup
		setupSpeakersAndBluetooth(alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
	}

	/**
	 * Setup the speakers and bluetooth option.
	 */
	private fun setupSpeakersAndBluetooth(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.speakers_and_bluetooth_container)
		speakersAndBluetoothSwitch = dialog!!.findViewById(R.id.speakers_and_bluetooth_switch)

		// Setup the switch
		speakersAndBluetoothSwitch.isChecked = default
		speakersAndBluetoothSwitch.setupSwitchColor(sharedPreferences)

		// Set the listener for the container to toggle the switch
		relativeLayout.setOnClickListener {
			speakersAndBluetoothSwitch.toggle()
		}
	}
}
