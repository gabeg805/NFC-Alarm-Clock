package com.nfcalarmclock.alarm.options.volume

import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.media3.common.util.UnstableApi
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Volume options for an alarm.
 */
@UnstableApi
open class NacVolumeOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_volume_options

	/**
	 * Preview button.
	 */
	private lateinit var previewButton: MaterialButton

	/**
	 * Gradually increase volume switch.
	 */
	private lateinit var graduallyIncreaseVolumeSwitch: SwitchCompat

	/**
	 * Input layout to select the gradually increase volume wait time.
	 */
	private lateinit var graduallyIncreaseVolumeInputLayout: TextInputLayout

	/**
	 * Restrict volume switch.
	 */
	private lateinit var restrictVolumeSwitch: SwitchCompat

	/**
	 * Audio attributes.
	 */
	private var audioAttributes: NacAudioAttributes? = null

	/**
	 * Volume manager (gradually increase, restrict, snooze/dismiss with volume buttons).
	 */
	private var volumeManager: NacVolumeManager? = null

	/**
	 * Media player.
	 */
	private var mediaPlayer: NacMediaPlayer? = null

	/**
	 * Selected gradually increase volume wait time.
	 */
	private var selectedWaitTime: Int = 0

	/**
	 * Cleanup resources.
	 */
	private fun cleanup()
	{
		// Cleanup volume resources
		volumeManager?.cleanup()

		// Cleanup the media player
		mediaPlayer?.release()
	}

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm
	{
		return arguments?.getAlarm() ?: NacAlarm.build(sharedPreferences)
	}

	/**
	 * Cancel button is clicked.
	 */
	override fun onCancelClicked(alarm: NacAlarm)
	{
		// Cleanup resources
		cleanup()
	}

	/**
	 * Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm)
	{
		// Update the alarm
		updateAlarm(alarm)

		// Cleanup resources
		cleanup()
	}

	/**
	 * Setup whether the gradually increase volume wait time container can be
	 * used or not.
	 */
	private fun setGraduallyIncreaseVolumeUsability()
	{
		// Get the state and alpha
		val state = graduallyIncreaseVolumeSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		graduallyIncreaseVolumeInputLayout.alpha = alpha
		graduallyIncreaseVolumeInputLayout.isEnabled = state
	}

	/**
	 * Set the preview button text.
	 */
	private fun setPreviewText(state: Boolean)
	{
		// Check if preview is running
		if (state)
		{
			// Change the text of the button back
			previewButton.text = resources.getString(R.string.action_preview)
		}
		// Preview not running
		else
		{
			// Change the text of the button to indicate that a preview is running
			previewButton.text = resources.getString(R.string.action_stop_preview)
		}
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm)
	{
		// Media can be previewed
		if (alarm.mediaPath.isNotEmpty())
		{
			val context = requireContext()
			val deviceContext = getDeviceProtectedStorageContext(context)

			// Set member variables for preview to work
			audioAttributes = NacAudioAttributes(context, alarm)
			mediaPlayer = NacMediaPlayer(deviceContext, null)
			volumeManager = NacVolumeManager(context, alarm, audioAttributes!!)

			// Setup the media player
			mediaPlayer!!.onAudioFocusChangeListener = object : NacMediaPlayer.OnAudioFocusChangeListener
			{
				// Empty override functions so that nothing happens when audio
				// focus is lost. This means that audio should keep playing even if
				// audio focus is lost
				override fun onAudioFocusLoss(mediaPlayer: NacMediaPlayer)
				{
				}

				override fun onAudioFocusLossTransient(mediaPlayer: NacMediaPlayer)
				{
				}
			}
		}

		// Set the default selected values
		selectedWaitTime = alarm.graduallyIncreaseVolumeWaitTime

		// Setup the views
		setupGraduallyIncreaseVolume(alarm.shouldGraduallyIncreaseVolume, alarm.graduallyIncreaseVolumeWaitTime)
		setGraduallyIncreaseVolumeUsability()
		setupRestrictVolume(alarm.shouldRestrictVolume)
	}

	/**
	 * Setup any extra buttons.
	 */
	override fun setupExtraButtons(alarm: NacAlarm)
	{
		// Get the button
		previewButton = dialog!!.findViewById(R.id.preview_button)

		// Setup the button
		setupSecondaryButton(previewButton, listener = {

			// Unable to preview because no media to play
			if (alarm.mediaPath.isEmpty())
			{
				quickToast(requireContext(), R.string.error_message_unable_to_preview_volume_options)
				return@setupSecondaryButton
			}

			// Set the button text
			setPreviewText(mediaPlayer!!.wasPlaying)

			// Stop preview
			if (mediaPlayer!!.wasPlaying)
			{
				volumeManager!!.cleanup()
				mediaPlayer!!.stop()
			}
			// Start preview
			else
			{
				// Update the alarm for volume manager
				updateAlarm(alarm)

				// Setup the volume and media player
				volumeManager!!.setup(alarm)
				mediaPlayer!!.playAlarm(alarm)
			}

		})
	}

	/**
	 * Setup the gradually increase volume views.
	 */
	private fun setupGraduallyIncreaseVolume(defaultState: Boolean, defaultTime: Int)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.gradually_increase_volume_container)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.gradually_increase_volume_dropdown_menu)
		graduallyIncreaseVolumeSwitch = dialog!!.findViewById(R.id.gradually_increase_volume_switch)
		graduallyIncreaseVolumeInputLayout = dialog!!.findViewById(R.id.gradually_increase_volume_input_layout)

		// Get the list of seconds, starting at the first index until the end
		// This will omit 0 seconds
		val seconds = resources.getStringArray(R.array.general_seconds_summaries).drop(1).toTypedArray()

		// Get the index of the default selected item in the textview
		val index = NacAlarm.calcGraduallyIncreaseVolumeIndex(defaultTime)

		// Setup the checkbox
		graduallyIncreaseVolumeSwitch.isChecked = defaultState
		graduallyIncreaseVolumeSwitch.setupSwitchColor(sharedPreferences)

		// Setup the input layout and textview
		graduallyIncreaseVolumeInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		autoCompleteTextView.setSimpleItems(seconds)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox and set the usability of the dropdown
			graduallyIncreaseVolumeSwitch.toggle()
			setGraduallyIncreaseVolumeUsability()

		}

		// Set the textview listener
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedWaitTime = NacAlarm.calcGraduallyIncreaseVolumeWaitTime(position)
		}
	}

	/**
	 * Setup whether to restrict volume or not.
	 */
	private fun setupRestrictVolume(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.restrict_volume_container)
		restrictVolumeSwitch = dialog!!.findViewById(R.id.restrict_volume_switch)

		// Setup the switch
		restrictVolumeSwitch.isChecked = default
		restrictVolumeSwitch.setupSwitchColor(sharedPreferences)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			restrictVolumeSwitch.isChecked = !restrictVolumeSwitch.isChecked

		}
	}

	/**
	 * Update the alarm.
	 */
	private fun updateAlarm(alarm: NacAlarm)
	{
		alarm.shouldGraduallyIncreaseVolume = graduallyIncreaseVolumeSwitch.isChecked
		alarm.graduallyIncreaseVolumeWaitTime = selectedWaitTime
		alarm.shouldRestrictVolume = restrictVolumeSwitch.isChecked
	}

}