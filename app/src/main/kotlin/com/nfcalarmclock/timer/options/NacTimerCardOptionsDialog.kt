package com.nfcalarmclock.timer.options

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.alarm.options.name.NacNameDialog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setupProgressAndThumbColor
import com.nfcalarmclock.view.setupRippleColor

/**
 * Options for a timer.
 */
class NacTimerCardOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Timer.
	 */
	private lateinit var timer: NacTimer

	/**
	 * Navigate to a destination in the navigation graph, and save the alarm.
	 */
	private fun navigateTo(destinationId: Int)
	{
		// Get the navigation controller
		val navController = findNavController()

		// Navigate to the destination
		NacTimerOptionsDialog.navigateTo(navController, destinationId, timer)
			?.observe(viewLifecycleOwner) { t ->
				timer = t
			}
	}

	/**
	 * Called when the dialog is canceled.
	 */
	override fun onCancel(dialog: DialogInterface)
	{
		// Super
		super.onCancel(dialog)

		// Get the nav controller
		val navController = findNavController()

		// Save to the current dialog
		navController.currentBackStackEntry?.savedStateHandle?.set("YOYOYO", timer)
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
		return inflater.inflate(R.layout.dlg_timer_card_options, container, false)
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the timer
		timer = arguments?.getTimer()!!

		// Get the context and shared preferences
		val context = requireContext()
		val sharedPreferences = NacSharedPreferences(context)

		// Get the views
		val repeatButton: MaterialButton = view.findViewById(R.id.timer_repeat)
		val vibrateButton: MaterialButton = view.findViewById(R.id.timer_vibrate)
		val nfcButton: MaterialButton = view.findViewById(R.id.timer_nfc)
		val flashlightButton: MaterialButton = view.findViewById(R.id.timer_flashlight)
		val mediaButton: MaterialButton = view.findViewById(R.id.timer_media)
		val volumeImageView: ImageView = view.findViewById(R.id.timer_volume_icon)
		val volumeSeekBar: SeekBar = view.findViewById(R.id.timer_volume_slider)
		val nameButton: MaterialButton = view.findViewById(R.id.timer_name)
		val stopOptionsButton: MaterialButton = view.findViewById(R.id.timer_stop_options)
		val settingsOptionsButton: MaterialButton = view.findViewById(R.id.timer_settings_options)
		val optionsDivider1: View = view.findViewById(R.id.timer_options_divider1)
		val optionsDivider2: View = view.findViewById(R.id.timer_options_divider2)

		// Setup color
		val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)
		optionsDivider1.backgroundTintList = themeColor
		optionsDivider2.backgroundTintList = themeColor

		repeatButton.setupRippleColor(sharedPreferences)
		vibrateButton.setupRippleColor(sharedPreferences)
		nfcButton.setupRippleColor(sharedPreferences)
		flashlightButton.setupRippleColor(sharedPreferences)
		mediaButton.setupRippleColor(sharedPreferences)
		volumeSeekBar.setupProgressAndThumbColor(sharedPreferences)
		nameButton.setupRippleColor(sharedPreferences)
		stopOptionsButton.setupRippleColor(sharedPreferences)
		settingsOptionsButton.setupRippleColor(sharedPreferences)

		// Setup views
		setupRepeatButton(repeatButton)
		setupVibrateButton(vibrateButton)
		setupNfcButton(nfcButton)
		setupFlashlightButton(flashlightButton)
		setupMediaButton(mediaButton)
		setupVolume(volumeSeekBar, volumeImageView)
		setupName(nameButton)
		setupStopOptionsButton(stopOptionsButton)
		setupSettingsOptionsButton(settingsOptionsButton)
	}

	/**
	 * Set the message and alpha of the name view.
	 */
	private fun setNameMessageAndAlpha(button: MaterialButton)
	{
		// Get the name message
		val message = timer.nameNormalized.ifEmpty {
			resources.getString(R.string.title_alarm_name)
		}

		// Get the alpha that the view should be
		val alpha = if (timer.nameNormalized.isNotEmpty()) 1.0f else 0.3f

		// Set the name as text for the button, and the alpha
		button.text = message
		button.alpha = alpha
	}

	/**
	 * Set the volume image view.
	 */
	private fun setVolumeImageView(imageView: ImageView)
	{
		// Set the resource ID depending on the volume level
		val resId: Int = when (timer.volume)
		{

			0 ->
			{
				R.drawable.volume_off
			}

			in 1..33 ->
			{
				R.drawable.volume_low
			}

			in 34..66 ->
			{
				R.drawable.volume_med
			}

			else ->
			{
				R.drawable.volume_high
			}

		}

		// Set the new resource ID and tag
		if ((imageView.tag == null) || (imageView.tag as Int != resId))
		{
			imageView.setImageResource(resId)
			imageView.tag = resId
		}
	}

	/**
	 * Setup the flashlight button.
	 */
	private fun setupFlashlightButton(button: MaterialButton)
	{
		// Set the state
		button.isChecked = timer.shouldUseFlashlight

		// Toggle the repeat button on click
		button.setOnClickListener {
			timer.toggleUseFlashlight()
		}

		// Show the quick navigate dialog on long click
		button.setOnLongClickListener {
			navigateTo(R.id.nacFlashlightOptionsDialog3)
			true
		}
	}

	/**
	 * Setup the media button.
	 */
	private fun setupMediaButton(button: MaterialButton)
	{
		// Default message and alpha
		val mediaTitle = timer.mediaTitle
		var message = mediaTitle
		var alpha = 1f

		// No media selected
		if (mediaTitle.isEmpty())
		{
			message = resources.getString(R.string.description_media)
			alpha = 0.3f
		}

		// Set the text and alpha
		button.text = message
		button.alpha = alpha

		// Show the media activity
		button.setOnClickListener {
			// TODO: How will this work with the fragment? Maybe need to do something in the navigation graph?
		}
	}

	/**
	 * Setup the name.
	 */
	private fun setupName(button: MaterialButton)
	{
		// Set the message and alpha
		setNameMessageAndAlpha(button)

		// Show a dialog to set a new name
		button.setOnClickListener { view ->

			// Show the dialog
			NacNameDialog.create(
				timer.name,
				onNameEnteredListener = { name ->
					timer.name = name
					setNameMessageAndAlpha(button)
				})
				.show(parentFragmentManager, NacNameDialog.TAG)

		}
	}

	/**
	 * Setup the NFC button.
	 */
	private fun setupNfcButton(button: MaterialButton)
	{
		// Set the state
		button.isChecked = timer.shouldUseNfc

		// Toggle the repeat button on click
		button.setOnClickListener {
			timer.toggleUseNfc()
		}

		// Show the quick navigate dialog on long click
		button.setOnLongClickListener {
			navigateTo(R.id.nacScanNfcTagDialog3)
			true
		}
	}

	/**
	 * Setup the repeat button.
	 */
	private fun setupRepeatButton(button: MaterialButton)
	{
		// Set the state
		button.isChecked = timer.shouldRepeat

		// Toggle the repeat button on click
		button.setOnClickListener {
			timer.toggleRepeat()
		}
	}

	/**
	 * Setup the timer options button.
	 */
	private fun setupSettingsOptionsButton(button: MaterialButton)
	{
		// Show the dialog on click
		button.setOnClickListener {
			navigateTo(R.id.nacTimerOptionsDialog)
		}
	}

	/**
	 * Setup the stop options button.
	 */
	private fun setupStopOptionsButton(button: MaterialButton)
	{
		// Show the dialog on click
		button.setOnClickListener {

			NacDismissOptionsDialog.create(
				timer,
				onSaveAlarmListener = {
					// TODO: Add this logic
					//updateAlarm(it)
				})
				.show(parentFragmentManager, NacDismissOptionsDialog.TAG)

		}
	}

	/**
	 * Setup the vibrate button.
	 */
	private fun setupVibrateButton(button: MaterialButton)
	{
		// Set the state
		button.isChecked = timer.shouldVibrate

		// Toggle the repeat button on click
		button.setOnClickListener {
			timer.toggleVibrate()
		}

		// Show the quick navigate dialog on long click
		button.setOnLongClickListener {
			navigateTo(R.id.nacVibrateOptionsDialog3)
			true
		}
	}

	/**
	 * Setup the volume.
	 */
	private fun setupVolume(seekBar: SeekBar, imageView: ImageView)
	{
		// Set the volume level
		seekBar.progress = timer.volume

		// Set the resource ID depending on the volume level
		setVolumeImageView(imageView)

		// Set the listener
		seekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {

			/**
			 * Called when the progress is changed.
			 */
			override fun onProgressChanged(
				seekBar: SeekBar,
				progress: Int,
				fromUser: Boolean)
			{
				// Volume did not change
				if (timer.volume == progress)
				{
					return
				}

				// Set the new volume
				timer.volume = progress

				// Change the volume icon, if needed
				setVolumeImageView(imageView)
			}

			/**
			 * Called when the seekbar is touched.
			 */
			override fun onStartTrackingTouch(seekBar: SeekBar) {}

			/**
			 * Called when the seekbar is no longer touched.
			 */
			override fun onStopTrackingTouch(seekBar: SeekBar) {}

		})
	}

	companion object
	{

		/**
		 * Start the navigation to the alarm options dialog.
		 */
		fun navigate(
			navController: NavController,
			timer: NacTimer
		): MutableLiveData<NacTimer>?
		{
			// Create bundle with the timer
			val bundle = timer.toBundle()

			// Set the graph of the nav controller
			navController.setGraph(R.navigation.nav_timer_options, bundle)

			// Check if the nav controller did not navigate to the destination
			if (navController.currentDestination == null)
			{
				// Navigate to the destination manually
				navController.navigate(R.id.nacTimerCardOptionsDialog, bundle)
			}

			// Setup an observe to watch for any changes to the alarm
			return navController.currentBackStackEntry
				?.savedStateHandle
				?.getLiveData("YOYOYO")
		}

	}

}