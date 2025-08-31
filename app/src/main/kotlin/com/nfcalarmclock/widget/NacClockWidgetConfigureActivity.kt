package com.nfcalarmclock.widget

import android.animation.LayoutTransition
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.nfcalarmclock.R
import com.nfcalarmclock.databinding.NacClockWidgetConfigureBinding
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.view.colorpicker.NacColorPickerDialog
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupForegroundColor
import com.nfcalarmclock.view.setupProgressAndThumbColor
import com.nfcalarmclock.view.setupSwitchColor
import com.nfcalarmclock.view.setupThemeColor
import java.util.Calendar
import androidx.core.view.isVisible

/**
 * The configuration screen for the [NacClockWidgetProvider] AppWidget.
 */
class NacClockWidgetConfigureActivity : AppCompatActivity()
{

	/**
	 * Bind the activity with the XML views.
	 */
	private lateinit var binding: NacClockWidgetConfigureBinding

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Data helper for the widget preview.
	 */
	private lateinit var helper: NacClockWidgetDataHelper

	/**
	 * ID of the widget.
	 */
	private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

	/**
	 * An example alarm calendar.
	 */
	private val exampleAlarmCalendar: Calendar
		get()
		{
			// Setup the example calendar
			val calendar = Calendar.getInstance()
			calendar[Calendar.HOUR_OF_DAY] = 8
			calendar[Calendar.MINUTE] = 30

			return calendar
		}

	/**
	 * Whether to show the alarm time or not.
	 */
	private val shouldShowAlarmTime: Boolean
		get()
		{
			return sharedPreferences.shouldClockWidgetShowAlarm && !sharedPreferences.shouldClockWidgetBoldAlarmTime
		}

	/**
	 * Whether to show the bold alarm time or not.
	 */
	private val shouldShowAlarmTimeBold: Boolean
		get()
		{
			return sharedPreferences.shouldClockWidgetShowAlarm && sharedPreferences.shouldClockWidgetBoldAlarmTime
		}

	/**
	 * Change the visibility, drawable, and alpha when an expand/collapse event occurs.
	 */
	private fun changeOnExpandCollapse(linearLayout: LinearLayout, textView: TextView)
	{
		// Get which visibility/drawable/alpha to change to given the current visibility
		val (vis, drawable, alpha) = getExpandCollapseChangeInfo(linearLayout)

		// Toggle the visibility
		linearLayout.visibility = vis

		// Set the new drawable
		textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
		textView.alpha = alpha
	}

	/**
	 * Determine what expand/collapse visibility, drawable, and alpha to change to.
	 */
	private fun getExpandCollapseChangeInfo(linearLayout: LinearLayout): Triple<Int, Int, Float>
	{
		// Check which visibility/drawable/alpha to change to given the current visibility
		return if (linearLayout.isVisible)
		{
			Triple(View.GONE, R.drawable.expand, 1f)
		}
		else
		{
			Triple(View.VISIBLE, R.drawable.collapse, 0.4f)
		}
	}

	/**
	 * Called when the activity is created.
	 */
	public override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED)

		// Inflate the view
		binding = NacClockWidgetConfigureBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Find the widget id from the intent.
		if (intent.extras != null)
		{
			appWidgetId = intent.extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID)
		}

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
		{
			finish()
			return
		}

		// Get the shared preferences and data helper for the widget preview
		sharedPreferences = NacSharedPreferences(this)
		helper = NacClockWidgetDataHelper(this, sharedPreferences)

		// Setup the activity
		setupAnimateLayoutChanges()
		setupWidgetPreview()
		setupAmPmVisibility()
		setupInitialValues()
		setupColors()
		setupListeners()

		// Setup on back pressed
		onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true)
		{
			override fun handleOnBackPressed()
			{
				updateAndFinish()
			}
		})
	}

	/**
	 * Setup AM/PM visibility based on whether the device is using 12 or 24 hour time,
	 * since AM/PM items do not need to be shown for a 24 hour clock.
	 */
	private fun setupAmPmVisibility()
	{
		// Determine visibility based on whether using 24 hour time or not
		val vis = if (DateFormat.is24HourFormat(this)) View.GONE else View.VISIBLE

		// Set the visiblity to every AM/PM item
		binding.timeLayoutBoldAmPmContainer.visibility = vis
		binding.timeLayoutColorAmPmContainer.visibility = vis
		binding.timeLayoutTextSizeAmPmDescription.visibility = vis
		binding.timeLayoutTextSizeAmPmSlider.visibility = vis
	}

	/**
	 * Setup animate layout changes.
	 */
	private fun setupAnimateLayoutChanges()
	{
		// Get the parent and child views (the ones that have "animateLayoutChanges"
		// set on them)
		val parentLayout = binding.widgetConfigureParent
		val childLayout = binding.widgetConfigureChild

		// Setup layout change transitions. Most importantly, shorten the time where the
		// view is disappearing because it looks weird if it is too slow
		parentLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
		childLayout.layoutTransition.setDuration(LayoutTransition.APPEARING, 300)
		childLayout.layoutTransition.setDuration(LayoutTransition.DISAPPEARING, 100)
		childLayout.layoutTransition.setDuration(LayoutTransition.CHANGE_APPEARING, 300)
		childLayout.layoutTransition.setDuration(LayoutTransition.CHANGE_DISAPPEARING, 300)
	}

	/**
	 * Setup colors.
	 */
	private fun setupColors()
	{
		// Titles
		binding.backgroundTitle.setTextColor(sharedPreferences.themeColor)
		binding.timeLayoutTitle.setTextColor(sharedPreferences.themeColor)
		binding.dateLayoutTitle.setTextColor(sharedPreferences.themeColor)
		binding.alarmLayoutTitle.setTextColor(sharedPreferences.themeColor)

		// Switches
		binding.timeLayoutShowSwitch.setupSwitchColor(sharedPreferences)
		binding.dateLayoutShowSwitch.setupSwitchColor(sharedPreferences)
		binding.alarmLayoutShowSwitch.setupSwitchColor(sharedPreferences)
		binding.dateLayoutBoldSwitch.setupSwitchColor(sharedPreferences)
		binding.timeLayoutBoldHourSwitch.setupSwitchColor(sharedPreferences)
		binding.timeLayoutBoldMinuteSwitch.setupSwitchColor(sharedPreferences)
		binding.timeLayoutBoldAmPmSwitch.setupSwitchColor(sharedPreferences)
		binding.alarmLayoutBoldSwitch.setupSwitchColor(sharedPreferences)
		binding.alarmLayoutShowAppSpecificSwitch.setupSwitchColor(sharedPreferences)

		// Radio groups
		binding.generalLayoutAlignmentCenterRadioButton.setupBackgroundColor(sharedPreferences)
		binding.generalLayoutAlignmentLeftRadioButton.setupBackgroundColor(sharedPreferences)
		binding.generalLayoutAlignmentRightRadioButton.setupBackgroundColor(sharedPreferences)
		binding.alarmLayoutPositionAboveDateRadioButton.setupBackgroundColor(sharedPreferences)
		binding.alarmLayoutPositionBelowDateRadioButton.setupBackgroundColor(sharedPreferences)
		binding.alarmLayoutPositionSameLineAsDateRadioButton.setupBackgroundColor(sharedPreferences)

		// Sliders
		binding.backgroundTransparencySlider.setupProgressAndThumbColor(sharedPreferences)
		binding.dateLayoutTextSizeSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.timeLayoutTextSizeTimeSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.timeLayoutTextSizeAmPmSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.alarmLayoutTextSizeSlider.setupProgressAndThumbColor(sharedPreferences)

		// Separators for expandable rows
		binding.generalLayoutAlignmentSeparator.setupBackgroundColor(sharedPreferences)
		binding.timeLayoutBoldSeparator.setupBackgroundColor(sharedPreferences)
		binding.timeLayoutColorSeparator.setupBackgroundColor(sharedPreferences)
		binding.alarmLayoutColorSeparator.setupBackgroundColor(sharedPreferences)
		binding.alarmLayoutPositionSeparator.setupBackgroundColor(sharedPreferences)

		// Done button
		binding.doneButton.setupThemeColor(sharedPreferences)
	}

	/**
	 * Setup initial values.
	 */
	private fun setupInitialValues()
	{
		// Switches
		binding.dateLayoutShowSwitch.isChecked = sharedPreferences.shouldClockWidgetShowDate
		binding.timeLayoutShowSwitch.isChecked = sharedPreferences.shouldClockWidgetShowTime
		binding.alarmLayoutShowSwitch.isChecked = sharedPreferences.shouldClockWidgetShowAlarm
		binding.dateLayoutBoldSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldDate
		binding.timeLayoutBoldHourSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldHour
		binding.timeLayoutBoldMinuteSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldMinute
		binding.timeLayoutBoldAmPmSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldAmPm
		binding.alarmLayoutBoldSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldAlarmTime
		binding.alarmLayoutShowAppSpecificSwitch.isChecked = sharedPreferences.shouldClockWidgetShowAppSpecificAlarms
		binding.alarmLayoutShowAppSpecificDescription.text =
			if (sharedPreferences.shouldClockWidgetShowAppSpecificAlarms)
			{
				getString(R.string.description_only_show_app_alarms)
			}
			else
			{
				getString(R.string.description_show_any_alarms)
			}

		// Radio groups
		binding.generalLayoutAlignmentCenterRadioButton.isChecked = (sharedPreferences.clockWidgetGeneralAlignment == Gravity.CENTER_HORIZONTAL)
		binding.generalLayoutAlignmentLeftRadioButton.isChecked = (sharedPreferences.clockWidgetGeneralAlignment == Gravity.START)
		binding.generalLayoutAlignmentRightRadioButton.isChecked = (sharedPreferences.clockWidgetGeneralAlignment == Gravity.END)
		binding.alarmLayoutPositionAboveDateRadioButton.isChecked = sharedPreferences.clockWidgetAlarmTimePositionAboveDate
		binding.alarmLayoutPositionBelowDateRadioButton.isChecked = sharedPreferences.clockWidgetAlarmTimePositionBelowDate
		binding.alarmLayoutPositionSameLineAsDateRadioButton.isChecked = sharedPreferences.clockWidgetAlarmTimePositionSameLineAsDate

		// Colors
		binding.backgroundColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetBackgroundColor)
		binding.hourColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetHourColor)
		binding.minuteColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetMinuteColor)
		binding.amPmColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetAmPmColor)
		binding.dateLayoutColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetDateColor)
		binding.alarmLayoutColorTimeSwatch.setupForegroundColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.alarmLayoutColorIconSwatch.setupForegroundColor(sharedPreferences.clockWidgetAlarmIconColor)

		// Sliders
		binding.backgroundTransparencySlider.setValueSafe(sharedPreferences.clockWidgetBackgroundTransparency.toFloat(), fallback = 100f)
		binding.dateLayoutTextSizeSlider.setValueSafe(sharedPreferences.clockWidgetDateTextSize, fallback = 14f)
		binding.timeLayoutTextSizeTimeSlider.setValueSafe(sharedPreferences.clockWidgetTimeTextSize, fallback = 78f)
		binding.timeLayoutTextSizeAmPmSlider.setValueSafe(sharedPreferences.clockWidgetAmPmTextSize, fallback = 18f)
		binding.alarmLayoutTextSizeSlider.setValueSafe(sharedPreferences.clockWidgetAlarmTimeTextSize, fallback = 14f)

		// Save the shared preference values just in case the above failed
		sharedPreferences.clockWidgetBackgroundTransparency = binding.backgroundTransparencySlider.value.toInt()
		sharedPreferences.clockWidgetDateTextSize = binding.dateLayoutTextSizeSlider.value
		sharedPreferences.clockWidgetTimeTextSize = binding.timeLayoutTextSizeTimeSlider.value
		sharedPreferences.clockWidgetAmPmTextSize = binding.timeLayoutTextSizeAmPmSlider.value
		sharedPreferences.clockWidgetAlarmTimeTextSize = binding.alarmLayoutTextSizeSlider.value
	}

	/**
	 * Setup alarm layout listeners.
	 */
	private fun setupAlarmLayoutListeners()
	{
		// Alarm layout color section
		binding.alarmLayoutColorContainer.setOnClickListener {
			changeOnExpandCollapse(binding.alarmLayoutColorOptionsContainer, binding.alarmLayoutColorDescription)
		}

		// Alarm layout position section
		binding.alarmLayoutPositionContainer.setOnClickListener {
			changeOnExpandCollapse(binding.alarmLayoutPositionOptionsContainer, binding.alarmLayoutPositionDescription)

		}

		// Show alarm
		binding.alarmLayoutShowContainer.setOnClickListener {

			// Toggle the state
			binding.alarmLayoutShowSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowAlarm = binding.alarmLayoutShowSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetAlarmIcon.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmIconAbove.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmIconBelow.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTime.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeAbove.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBelow.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBold.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBoldAbove.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBoldBelow.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE

		}

		// Show app specific alarms
		binding.alarmLayoutShowAppSpecificContainer.setOnClickListener {

			// Toggle the state
			binding.alarmLayoutShowAppSpecificSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowAppSpecificAlarms = binding.alarmLayoutShowAppSpecificSwitch.isChecked
			binding.alarmLayoutShowAppSpecificDescription.text =
				if (sharedPreferences.shouldClockWidgetShowAppSpecificAlarms)
				{
					getString(R.string.description_only_show_app_alarms)
				}
				else
				{
					getString(R.string.description_show_any_alarms)
				}

		}

		// Bold alarm time
		binding.alarmLayoutBoldContainer.setOnClickListener {

			// Toggle the state
			binding.alarmLayoutBoldSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldAlarmTime = binding.alarmLayoutBoldSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetAlarmTime.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeAbove.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBelow.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBold.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBoldAbove.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE
			binding.previewWidget.widgetAlarmTimeBoldBelow.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE

		}

		// Alarm time color
		binding.alarmLayoutColorTimeContainer.setOnClickListener {

			// Create the dialog
			val dialog = NacColorPickerDialog()

			// Setup the initial color
			dialog.initialColor = sharedPreferences.clockWidgetAlarmTimeColor

			// Setup the color selected listener
			dialog.onColorSelectedListener = NacColorPickerDialog.OnColorSelectedListener { color ->

				// Set the color
				sharedPreferences.clockWidgetAlarmTimeColor = color
				binding.alarmLayoutColorTimeSwatch.setupForegroundColor(color)

				// Update the preview
				binding.previewWidget.widgetAlarmTime.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
				binding.previewWidget.widgetAlarmTimeAbove.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
				binding.previewWidget.widgetAlarmTimeBelow.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
				binding.previewWidget.widgetAlarmTimeBold.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
				binding.previewWidget.widgetAlarmTimeBoldAbove.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
				binding.previewWidget.widgetAlarmTimeBoldBelow.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_alarm_time)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

		}

		// Alarm icon color
		binding.alarmLayoutColorIconContainer.setOnClickListener {

			// Create the dialog
			val dialog = NacColorPickerDialog()

			// Setup the initial color
			dialog.initialColor = sharedPreferences.clockWidgetAlarmIconColor

			// Setup the color selected listener
			dialog.onColorSelectedListener = NacColorPickerDialog.OnColorSelectedListener { color ->

				// Set the color
				sharedPreferences.clockWidgetAlarmIconColor = color
				binding.alarmLayoutColorIconSwatch.setupForegroundColor(color)

				// Update the preview
				binding.previewWidget.widgetAlarmIcon.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)
				binding.previewWidget.widgetAlarmIconAbove.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)
				binding.previewWidget.widgetAlarmIconBelow.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_alarm_icon)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

		}

		// Alarm time size
		binding.alarmLayoutTextSizeSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

			/**
			 * Called when the touch is started.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the touch is stopped.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Set the new text size
				sharedPreferences.clockWidgetAlarmTimeTextSize = slider.value

				// Update the preview
				binding.previewWidget.widgetAlarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
				binding.previewWidget.widgetAlarmTimeAbove.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
				binding.previewWidget.widgetAlarmTimeBelow.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
				binding.previewWidget.widgetAlarmTimeBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
				binding.previewWidget.widgetAlarmTimeBoldAbove.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
				binding.previewWidget.widgetAlarmTimeBoldBelow.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
				updateAlarmIconMargins()
			}

		})

		binding.alarmLayoutTextSizeSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.previewWidget.widgetAlarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetAlarmTimeAbove.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetAlarmTimeBelow.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetAlarmTimeBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetAlarmTimeBoldAbove.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetAlarmTimeBoldBelow.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			updateAlarmIconMargins(alarmTextSize = value)

		}

		// Alarm position
		binding.alarmLayoutPositionRadioGroup.setOnCheckedChangeListener { _, id ->

			when (id)
			{
				// Above
				R.id.alarm_layout_position_above_date_radio_button ->
				{
					sharedPreferences.clockWidgetAlarmTimePositionAboveDate = true
					sharedPreferences.clockWidgetAlarmTimePositionBelowDate = false
					sharedPreferences.clockWidgetAlarmTimePositionSameLineAsDate = false
				}

				// Below
				R.id.alarm_layout_position_below_date_radio_button ->
				{
					sharedPreferences.clockWidgetAlarmTimePositionAboveDate = false
					sharedPreferences.clockWidgetAlarmTimePositionBelowDate = true
					sharedPreferences.clockWidgetAlarmTimePositionSameLineAsDate = false
				}

				// Same line
				R.id.alarm_layout_position_same_line_as_date_radio_button ->
				{
					sharedPreferences.clockWidgetAlarmTimePositionAboveDate = false
					sharedPreferences.clockWidgetAlarmTimePositionBelowDate = false
					sharedPreferences.clockWidgetAlarmTimePositionSameLineAsDate = true
				}

				else -> {}
			}

			// Update the preview
			binding.previewWidget.widgetAlarmAboveContainer.visibility = helper.alarmPositionAboveDateVis
			binding.previewWidget.widgetAlarmSameLineAsDateContainer.visibility = helper.alarmPositionSameLineAsDateVis
			binding.previewWidget.widgetAlarmBelowContainer.visibility = helper.alarmPositionBelowDateVis

		}
	}

	/**
	 * Setup background listeners.
	 */
	private fun setupBackgroundListeners()
	{
		// Background color
		binding.backgroundColorContainer.setOnClickListener {

			// Create the dialog
			val dialog = NacColorPickerDialog()

			// Setup the initial color
			dialog.initialColor = sharedPreferences.clockWidgetBackgroundColor

			// Setup the color selected listener
			dialog.onColorSelectedListener = NacColorPickerDialog.OnColorSelectedListener { color ->

				// Set the color
				sharedPreferences.clockWidgetBackgroundColor = color
				binding.backgroundColorSwatch.setupForegroundColor(color)

				// Update the preview
				binding.previewParent.setBackgroundColor(helper.bgColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_background)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

		}

		// Background transparency
		binding.backgroundTransparencySlider.addOnSliderTouchListener(object: OnSliderTouchListener {

			/**
			 * Called when the touch is started.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the touch is stopped.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Set the new transparency
				sharedPreferences.clockWidgetBackgroundTransparency = slider.value.toInt()

				// Update the preview
				binding.previewParent.setBackgroundColor(helper.bgColor)
			}

		})

		binding.backgroundTransparencySlider.addOnChangeListener { _, value, _ ->

			// Calculate the new background color
			val newColor = NacClockWidgetDataHelper.calcBackgroundColor(
				sharedPreferences.clockWidgetBackgroundColor, value.toInt())

			// Update the preview
			binding.previewParent.setBackgroundColor(newColor)

		}
	}

	/**
	 * Setup date layout listeners.
	 */
	private fun setupDateLayoutListeners()
	{
		// Show date
		binding.dateLayoutShowContainer.setOnClickListener {

			// Toggle the state
			binding.dateLayoutShowSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowDate = binding.dateLayoutShowSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetDate.visibility = helper.dateVis
			binding.previewWidget.widgetDateBold.visibility = helper.dateBoldVis

		}

		// Bold date
		binding.dateLayoutBoldContainer.setOnClickListener {

			// Toggle the state
			binding.dateLayoutBoldSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldDate = binding.dateLayoutBoldSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetDate.visibility = helper.dateVis
			binding.previewWidget.widgetDateBold.visibility = helper.dateBoldVis

		}

		// Date color
		binding.dateLayoutColorContainer.setOnClickListener {

			// Create the dialog
			val dialog = NacColorPickerDialog()

			// Setup the initial color
			dialog.initialColor = sharedPreferences.clockWidgetDateColor

			// Setup the color selected listener
			dialog.onColorSelectedListener = NacColorPickerDialog.OnColorSelectedListener { color ->

				// Set the color
				sharedPreferences.clockWidgetDateColor = color
				binding.dateLayoutColorSwatch.setupForegroundColor(color)

				// Update the preview
				binding.previewWidget.widgetDate.setTextColor(sharedPreferences.clockWidgetDateColor)
				binding.previewWidget.widgetDateBold.setTextColor(sharedPreferences.clockWidgetDateColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_date)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

		}

		// Date size
		binding.dateLayoutTextSizeSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

			/**
			 * Called when the touch is started.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the touch is stopped.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Set the new text size
				sharedPreferences.clockWidgetDateTextSize = slider.value

				// Update the preview
				binding.previewWidget.widgetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
				binding.previewWidget.widgetDateBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
				updateAlarmIconMargins()
			}

		})

		binding.dateLayoutTextSizeSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.previewWidget.widgetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetDateBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			updateAlarmIconMargins(dateTextSize = value)

		}
	}

	/**
	 * Setup general listeners.
	 */
	private fun setupGeneralListeners()
	{
		// General layout alignment section
		binding.generalLayoutAlignmentContainer.setOnClickListener {
			changeOnExpandCollapse(binding.generalLayoutAlignmentOptionsContainer, binding.generalLayoutAlignmentDescription)
		}

		// Alignment
		binding.generalLayoutAlignmentRadioGroup.setOnCheckedChangeListener { _, id ->

			when (id)
			{

				// Center
				R.id.general_layout_alignment_center_radio_button -> sharedPreferences.clockWidgetGeneralAlignment = Gravity.CENTER_HORIZONTAL

				// Left
				R.id.general_layout_alignment_left_radio_button -> sharedPreferences.clockWidgetGeneralAlignment = Gravity.START

				// Right
				R.id.general_layout_alignment_right_radio_button -> sharedPreferences.clockWidgetGeneralAlignment = Gravity.END

				// Default to center
				else -> sharedPreferences.clockWidgetGeneralAlignment = Gravity.CENTER_HORIZONTAL
			}

			// Update the preview
			binding.previewWidget.widgetTime.gravity = sharedPreferences.clockWidgetGeneralAlignment
			binding.previewWidget.widgetAlarmAboveContainer.gravity = sharedPreferences.clockWidgetGeneralAlignment
			binding.previewWidget.widgetAlarmDateInlineContainer.gravity = sharedPreferences.clockWidgetGeneralAlignment
			binding.previewWidget.widgetAlarmBelowContainer.gravity = sharedPreferences.clockWidgetGeneralAlignment

		}
	}

	/**
	 * Setup time layout listeners.
	 */
	private fun setupTimeLayoutListeners()
	{
		// Time layout bold section
		binding.timeLayoutBoldContainer.setOnClickListener {
			changeOnExpandCollapse(binding.timeLayoutBoldOptionsContainer, binding.timeLayoutBoldDescription)
		}

		// Time layout color section
		binding.timeLayoutColorContainer.setOnClickListener {
			changeOnExpandCollapse(binding.timeLayoutColorOptionsContainer, binding.timeLayoutColorDescription)

		}

		// Time layout text size section
		binding.timeLayoutTextSizeContainer.setOnClickListener {
			changeOnExpandCollapse(binding.timeLayoutTextSizeOptionsContainer, binding.timeLayoutTextSizeDescription)
		}

		// Show time
		binding.timeLayoutShowContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutShowSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowTime = binding.timeLayoutShowSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetTime.visibility = helper.timeVis

		}

		// Bold hour
		binding.timeLayoutBoldHourContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutBoldHourSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldHour = binding.timeLayoutBoldHourSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetHour.visibility = helper.hourVis
			binding.previewWidget.widgetHourBold.visibility = helper.hourBoldVis

		}

		// Bold minute
		binding.timeLayoutBoldMinuteContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutBoldMinuteSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldMinute = binding.timeLayoutBoldMinuteSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetMinute.visibility = helper.minuteVis
			binding.previewWidget.widgetMinuteBold.visibility = helper.minuteBoldVis

		}

		// Bold am/pm
		binding.timeLayoutBoldAmPmContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutBoldAmPmSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldAmPm = binding.timeLayoutBoldAmPmSwitch.isChecked

			// Update the preview
			binding.previewWidget.widgetAmPm.visibility = helper.meridianVis
			binding.previewWidget.widgetAmPmBold.visibility = helper.meridianBoldVis

		}

		// Hour color
		binding.timeLayoutColorHourContainer.setOnClickListener {

			// Create the dialog
			val dialog = NacColorPickerDialog()

			// Setup the initial color
			dialog.initialColor = sharedPreferences.clockWidgetHourColor

			// Setup the color selected listener
			dialog.onColorSelectedListener = NacColorPickerDialog.OnColorSelectedListener { color ->

				// Set the color
				sharedPreferences.clockWidgetHourColor = color
				binding.hourColorSwatch.setupForegroundColor(color)

				// Update the preview
				binding.previewWidget.widgetHour.setTextColor(sharedPreferences.clockWidgetHourColor)
				binding.previewWidget.widgetHourBold.setTextColor(sharedPreferences.clockWidgetHourColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_hour)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

		}

		// Minute color
		binding.timeLayoutColorMinuteContainer.setOnClickListener {

			// Create the dialog
			val dialog = NacColorPickerDialog()

			// Setup the initial color
			dialog.initialColor = sharedPreferences.clockWidgetMinuteColor

			// Setup the color selected listener
			dialog.onColorSelectedListener = NacColorPickerDialog.OnColorSelectedListener { color ->

				// Set the color
				sharedPreferences.clockWidgetMinuteColor = color
				binding.minuteColorSwatch.setupForegroundColor(color)

				// Update the preview
				binding.previewWidget.widgetColon.setTextColor(sharedPreferences.clockWidgetMinuteColor)
				binding.previewWidget.widgetMinute.setTextColor(sharedPreferences.clockWidgetMinuteColor)
				binding.previewWidget.widgetMinuteBold.setTextColor(sharedPreferences.clockWidgetMinuteColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_minute)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

		}

		// AM/PM color
		binding.timeLayoutColorAmPmContainer.setOnClickListener {

			// Create the dialog
			val dialog = NacColorPickerDialog()

			// Setup the initial color
			dialog.initialColor = sharedPreferences.clockWidgetAmPmColor

			// Setup the color selected listener
			dialog.onColorSelectedListener = NacColorPickerDialog.OnColorSelectedListener { color ->

				// Set the color
				sharedPreferences.clockWidgetAmPmColor = color
				binding.amPmColorSwatch.setupForegroundColor(color)

				// Update the preview
				binding.previewWidget.widgetAmPm.setTextColor(sharedPreferences.clockWidgetAmPmColor)
				binding.previewWidget.widgetAmPmBold.setTextColor(sharedPreferences.clockWidgetAmPmColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_am_pm)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

		}
		// Time size
		binding.timeLayoutTextSizeTimeSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

			/**
			 * Called when the touch is started.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the touch is stopped.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Set the new text size
				sharedPreferences.clockWidgetTimeTextSize = slider.value

				// Update the preview
				binding.previewWidget.widgetHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.previewWidget.widgetHourBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.previewWidget.widgetColon.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.previewWidget.widgetMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.previewWidget.widgetMinuteBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
			}

		})

		binding.timeLayoutTextSizeTimeSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.previewWidget.widgetHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetHourBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetColon.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetMinuteBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

		}

		// AM/PM size
		binding.timeLayoutTextSizeAmPmSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

			/**
			 * Called when the touch is started.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the touch is stopped.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Set the new text size
				sharedPreferences.clockWidgetAmPmTextSize = slider.value

				// Update the preview
				binding.previewWidget.widgetAmPm.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
				binding.previewWidget.widgetAmPmBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
			}

		})

		binding.timeLayoutTextSizeAmPmSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.previewWidget.widgetAmPm.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.previewWidget.widgetAmPmBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

		}
	}

	/**
	 * Setup listeners.
	 */
	private fun setupListeners()
	{
		setupGeneralListeners()
		setupTimeLayoutListeners()
		setupDateLayoutListeners()
		setupAlarmLayoutListeners()
		setupBackgroundListeners()

		// Done button
		binding.doneButton.setOnClickListener {
			updateAndFinish()
		}
	}

	/**
	 * Update the alarm icon margins.
	 */
	private fun updateAlarmIconMargins(
		dateTextSize: Float = sharedPreferences.clockWidgetDateTextSize,
		alarmTextSize: Float = sharedPreferences.clockWidgetAlarmTimeTextSize)
	{
		// Get the average text size, depending on if the alarm is shown inline or
		// different line that the date
		val avgTextSize = if (sharedPreferences.clockWidgetAlarmTimePositionSameLineAsDate)
		{
			(dateTextSize + alarmTextSize) / 2
		}
		else
		{
			alarmTextSize
		}

		// Calculate the new margin in dp and px
		val newMarginDp = NacClockWidgetDataHelper.calcAlarmIconMargin(this, avgTextSize)
		val newMarginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp,
			resources.displayMetrics).toInt()

		// Set the new margin
		binding.previewWidget.widgetAlarmIcon.updateLayoutParams<ViewGroup.MarginLayoutParams> {
			marginStart = newMarginPx
			marginEnd = newMarginPx
		}

		binding.previewWidget.widgetAlarmIconAbove.updateLayoutParams<ViewGroup.MarginLayoutParams> {
			marginStart = 0
			marginEnd = newMarginPx
		}

		binding.previewWidget.widgetAlarmIconBelow.updateLayoutParams<ViewGroup.MarginLayoutParams> {
			marginStart = 0
			marginEnd = newMarginPx
		}
	}

	/**
	 * Setup the widget preview.
	 */
	private fun setupWidgetPreview()
	{
		// Background color of the preview separator
		binding.widgetPreviewSeparator.setBackgroundColor(sharedPreferences.themeColor)

		// View visibility
		binding.previewWidget.widgetTime.visibility = helper.timeVis
		binding.previewWidget.widgetHour.visibility = helper.hourVis
		binding.previewWidget.widgetHourBold.visibility = helper.hourBoldVis
		binding.previewWidget.widgetMinute.visibility = helper.minuteVis
		binding.previewWidget.widgetMinuteBold.visibility = helper.minuteBoldVis
		binding.previewWidget.widgetAmPm.visibility = helper.meridianVis
		binding.previewWidget.widgetAmPmBold.visibility = helper.meridianBoldVis
		binding.previewWidget.widgetDate.visibility = helper.dateVis
		binding.previewWidget.widgetDateBold.visibility = helper.dateBoldVis
		binding.previewWidget.widgetAlarmIcon.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmIconAbove.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmIconBelow.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmTime.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmTimeAbove.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmTimeBelow.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmTimeBold.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmTimeBoldAbove.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmTimeBoldBelow.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE
		binding.previewWidget.widgetAlarmAboveContainer.visibility = helper.alarmPositionAboveDateVis
		binding.previewWidget.widgetAlarmSameLineAsDateContainer.visibility = helper.alarmPositionSameLineAsDateVis
		binding.previewWidget.widgetAlarmBelowContainer.visibility = helper.alarmPositionBelowDateVis

		// Gravity
		binding.previewWidget.widgetTime.gravity = sharedPreferences.clockWidgetGeneralAlignment
		binding.previewWidget.widgetAlarmAboveContainer.gravity = sharedPreferences.clockWidgetGeneralAlignment
		binding.previewWidget.widgetAlarmDateInlineContainer.gravity = sharedPreferences.clockWidgetGeneralAlignment
		binding.previewWidget.widgetAlarmBelowContainer.gravity = sharedPreferences.clockWidgetGeneralAlignment

		// Example alarm time
		val time = NacCalendar.getFullTime(this, exampleAlarmCalendar)
		binding.previewWidget.widgetAlarmTime.text = time
		binding.previewWidget.widgetAlarmTimeAbove.text = time
		binding.previewWidget.widgetAlarmTimeBelow.text = time
		binding.previewWidget.widgetAlarmTimeBold.text = time
		binding.previewWidget.widgetAlarmTimeBoldAbove.text = time
		binding.previewWidget.widgetAlarmTimeBoldBelow.text = time

		// Background color and transparency
		binding.previewParent.setBackgroundColor(helper.bgColor)
		binding.previewWidget.widgetParent.background = null

		// Text and icon colors
		binding.previewWidget.widgetHour.setTextColor(sharedPreferences.clockWidgetHourColor)
		binding.previewWidget.widgetHourBold.setTextColor(sharedPreferences.clockWidgetHourColor)
		binding.previewWidget.widgetColon.setTextColor(sharedPreferences.clockWidgetMinuteColor)
		binding.previewWidget.widgetMinute.setTextColor(sharedPreferences.clockWidgetMinuteColor)
		binding.previewWidget.widgetMinuteBold.setTextColor(sharedPreferences.clockWidgetMinuteColor)
		binding.previewWidget.widgetAmPm.setTextColor(sharedPreferences.clockWidgetAmPmColor)
		binding.previewWidget.widgetAmPmBold.setTextColor(sharedPreferences.clockWidgetAmPmColor)
		binding.previewWidget.widgetDate.setTextColor(sharedPreferences.clockWidgetDateColor)
		binding.previewWidget.widgetDateBold.setTextColor(sharedPreferences.clockWidgetDateColor)
		binding.previewWidget.widgetAlarmTime.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.previewWidget.widgetAlarmTimeAbove.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.previewWidget.widgetAlarmTimeBelow.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.previewWidget.widgetAlarmTimeBold.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.previewWidget.widgetAlarmTimeBoldAbove.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.previewWidget.widgetAlarmTimeBoldBelow.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.previewWidget.widgetAlarmIcon.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)
		binding.previewWidget.widgetAlarmIconAbove.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)
		binding.previewWidget.widgetAlarmIconBelow.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)

		// Text size
		binding.previewWidget.widgetHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.previewWidget.widgetHourBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.previewWidget.widgetColon.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.previewWidget.widgetMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.previewWidget.widgetMinuteBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.previewWidget.widgetAmPm.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
		binding.previewWidget.widgetAmPmBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
		binding.previewWidget.widgetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
		binding.previewWidget.widgetDateBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
		binding.previewWidget.widgetAlarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
		binding.previewWidget.widgetAlarmTimeAbove.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
		binding.previewWidget.widgetAlarmTimeBelow.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
		binding.previewWidget.widgetAlarmTimeBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
		binding.previewWidget.widgetAlarmTimeBoldAbove.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
		binding.previewWidget.widgetAlarmTimeBoldBelow.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)

		// Set the margin
		updateAlarmIconMargins()
	}

	/**
	 * Update the widget and finish the activity.
	 */
	private fun updateAndFinish()
	{
		// It is the responsibility of the configuration activity to update the app widget
		val appWidgetManager = AppWidgetManager.getInstance(this)
		updateAppWidget(this, appWidgetManager, appWidgetId)

		// Make sure we pass back the original appWidgetId
		val resultValue = Intent()
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
		setResult(RESULT_OK, resultValue)
		finish()
	}

	/**
	 * Set a slider value and if it fails, set it to the fallback value.
	 */
	private fun Slider.setValueSafe(value: Float, fallback: Float)
	{
		try
		{
			// Set to the desired value
			this.value = value
		}
		catch (_: IllegalStateException)
		{
			try
			{
				// Set to the fallback value
				this.value = fallback
			}
			catch (_: IllegalStateException)
			{
			}
		}
	}

}
