package com.nfcalarmclock.widget

import android.animation.LayoutTransition
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
		return if (linearLayout.visibility == View.VISIBLE)
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

		// Sliders
		binding.backgroundTransparencySlider.setupProgressAndThumbColor(sharedPreferences)
		binding.dateLayoutTextSizeSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.timeLayoutTextSizeTimeSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.timeLayoutTextSizeAmPmSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.alarmLayoutTextSizeSlider.setupProgressAndThumbColor(sharedPreferences)

		// Separators for expandable rows
		binding.timeLayoutBoldSeparator.setupBackgroundColor(sharedPreferences)
		binding.timeLayoutColorSeparator.setupBackgroundColor(sharedPreferences)
		binding.alarmLayoutColorSeparator.setupBackgroundColor(sharedPreferences)

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

		// Colors
		binding.backgroundColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetBackgroundColor)
		binding.hourColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetHourColor)
		binding.minuteColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetMinuteColor)
		binding.amPmColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetAmPmColor)
		binding.dateLayoutColorSwatch.setupForegroundColor(sharedPreferences.clockWidgetDateColor)
		binding.alarmLayoutColorTimeSwatch.setupForegroundColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.alarmLayoutColorIconSwatch.setupForegroundColor(sharedPreferences.clockWidgetAlarmIconColor)

		// Sliders
		binding.backgroundTransparencySlider.value = sharedPreferences.clockWidgetBackgroundTransparency.toFloat()
		binding.dateLayoutTextSizeSlider.value = sharedPreferences.clockWidgetDateTextSize
		binding.timeLayoutTextSizeTimeSlider.value = sharedPreferences.clockWidgetTimeTextSize
		binding.timeLayoutTextSizeAmPmSlider.value = sharedPreferences.clockWidgetAmPmTextSize
		binding.alarmLayoutTextSizeSlider.value = sharedPreferences.clockWidgetAlarmTimeTextSize
	}

	/**
	 * Setup listeners.
	 */
	private fun setupListeners()
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

		// Alarm layout color section
		binding.alarmLayoutColorContainer.setOnClickListener {
			changeOnExpandCollapse(binding.alarmLayoutColorOptionsContainer, binding.alarmLayoutColorDescription)
		}

		// Show date
		binding.dateLayoutShowContainer.setOnClickListener {

			// Toggle the state
			binding.dateLayoutShowSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowDate = binding.dateLayoutShowSwitch.isChecked

			// Update the preview
			binding.widgetDate.visibility = helper.dateVis
			binding.widgetDateBold.visibility = helper.dateBoldVis

		}

		// Show time
		binding.timeLayoutShowContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutShowSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowTime = binding.timeLayoutShowSwitch.isChecked

			// Update the preview
			binding.widgetTime.visibility = helper.timeVis

		}

		// Show alarm
		binding.alarmLayoutShowContainer.setOnClickListener {

			// Toggle the state
			binding.alarmLayoutShowSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowAlarm = binding.alarmLayoutShowSwitch.isChecked

			// Update the preview
			binding.widgetAlarmIcon.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
			binding.widgetAlarmTime.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.widgetAlarmTimeBold.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE

		}

		// Bold date
		binding.dateLayoutBoldContainer.setOnClickListener {

			// Toggle the state
			binding.dateLayoutBoldSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldDate = binding.dateLayoutBoldSwitch.isChecked

			// Update the preview
			binding.widgetDate.visibility = helper.dateVis
			binding.widgetDateBold.visibility = helper.dateBoldVis

		}

		// Bold hour
		binding.timeLayoutBoldHourContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutBoldHourSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldHour = binding.timeLayoutBoldHourSwitch.isChecked

			// Update the preview
			binding.widgetHour.visibility = helper.hourVis
			binding.widgetHourBold.visibility = helper.hourBoldVis

		}

		// Bold minute
		binding.timeLayoutBoldMinuteContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutBoldMinuteSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldMinute = binding.timeLayoutBoldMinuteSwitch.isChecked

			// Update the preview
			binding.widgetMinute.visibility = helper.minuteVis
			binding.widgetMinuteBold.visibility = helper.minuteBoldVis

		}

		// Bold am/pm
		binding.timeLayoutBoldAmPmContainer.setOnClickListener {

			// Toggle the state
			binding.timeLayoutBoldAmPmSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldAmPm = binding.timeLayoutBoldAmPmSwitch.isChecked

			// Update the preview
			binding.widgetAmPm.visibility = helper.meridianVis
			binding.widgetAmPmBold.visibility = helper.meridianBoldVis

		}

		// Bold alarm time
		binding.alarmLayoutBoldContainer.setOnClickListener {

			// Toggle the state
			binding.alarmLayoutBoldSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldAlarmTime = binding.alarmLayoutBoldSwitch.isChecked

			// Update the preview
			binding.widgetAlarmTime.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
			binding.widgetAlarmTimeBold.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE

		}

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
				binding.widgetParent.setBackgroundColor(helper.bgColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_background)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

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
				binding.widgetHour.setTextColor(sharedPreferences.clockWidgetHourColor)
				binding.widgetHourBold.setTextColor(sharedPreferences.clockWidgetHourColor)

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
				binding.widgetColon.setTextColor(sharedPreferences.clockWidgetMinuteColor)
				binding.widgetMinute.setTextColor(sharedPreferences.clockWidgetMinuteColor)
				binding.widgetMinuteBold.setTextColor(sharedPreferences.clockWidgetMinuteColor)

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
				binding.widgetAmPm.setTextColor(sharedPreferences.clockWidgetAmPmColor)
				binding.widgetAmPmBold.setTextColor(sharedPreferences.clockWidgetAmPmColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_am_pm)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

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
				binding.widgetDate.setTextColor(sharedPreferences.clockWidgetDateColor)
				binding.widgetDateBold.setTextColor(sharedPreferences.clockWidgetDateColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_date)
			}

			// Show the dialog
			dialog.show(supportFragmentManager, NacColorPickerDialog.TAG)

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
				binding.widgetAlarmTime.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
				binding.widgetAlarmTimeBold.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)

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
				binding.widgetAlarmIcon.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)

			}

			// Setup the default color selected listener
			dialog.onDefaultColorSelectedListener = NacColorPickerDialog.OnDefaultColorSelectedListener { d ->
				d.color = resources.getInteger(R.integer.default_clock_widget_color_alarm_icon)
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
				binding.widgetParent.setBackgroundColor(helper.bgColor)
			}

		})

		binding.backgroundTransparencySlider.addOnChangeListener { _, value, _ ->

			// Calculate the new background color
			val newColor = NacClockWidgetDataHelper.calcBackgroundColor(
				sharedPreferences.clockWidgetBackgroundColor, value.toInt())

			// Update the preview
			binding.widgetParent.setBackgroundColor(newColor)

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
				binding.widgetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
				binding.widgetDateBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
			}

		})

		binding.dateLayoutTextSizeSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.widgetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.widgetDateBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

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
				binding.widgetHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.widgetHourBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.widgetColon.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.widgetMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
				binding.widgetMinuteBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
			}

		})

		binding.timeLayoutTextSizeTimeSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.widgetHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.widgetHourBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.widgetColon.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.widgetMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.widgetMinuteBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

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
				binding.widgetAmPm.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
				binding.widgetAmPmBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
			}

		})

		binding.timeLayoutTextSizeAmPmSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.widgetAmPm.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.widgetAmPmBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

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
				binding.widgetAlarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
				binding.widgetAlarmTimeBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
			}

		})

		binding.alarmLayoutTextSizeSlider.addOnChangeListener { _, value, _ ->

			// Update the preview
			binding.widgetAlarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
			binding.widgetAlarmTimeBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

		}

		// Done button
		binding.doneButton.setOnClickListener {
			updateAndFinish()
		}
	}

	/**
	 * Setup the widget preview.
	 */
	private fun setupWidgetPreview()
	{
		// Set the background color of the preview separator
		binding.widgetPreviewSeparator.setBackgroundColor(sharedPreferences.themeColor)

		// Set view visibility
		binding.widgetTime.visibility = helper.timeVis
		binding.widgetHour.visibility = helper.hourVis
		binding.widgetHourBold.visibility = helper.hourBoldVis
		binding.widgetMinute.visibility = helper.minuteVis
		binding.widgetMinuteBold.visibility = helper.minuteBoldVis
		binding.widgetAmPm.visibility = helper.meridianVis
		binding.widgetAmPmBold.visibility = helper.meridianBoldVis
		binding.widgetDate.visibility = helper.dateVis
		binding.widgetDateBold.visibility = helper.dateBoldVis
		binding.widgetAlarmIcon.visibility = if (sharedPreferences.shouldClockWidgetShowAlarm) View.VISIBLE else View.GONE
		binding.widgetAlarmTime.visibility = if (shouldShowAlarmTime) View.VISIBLE else View.GONE
		binding.widgetAlarmTimeBold.visibility = if (shouldShowAlarmTimeBold) View.VISIBLE else View.GONE

		// Set the example alarm time
		val time = NacCalendar.getFullTime(this, exampleAlarmCalendar)
		binding.widgetAlarmTime.text = time
		binding.widgetAlarmTimeBold.text = time

		// Set the background color and transparency
		binding.widgetParent.setBackgroundColor(helper.bgColor)

		// Set text and icon colors
		binding.widgetHour.setTextColor(sharedPreferences.clockWidgetHourColor)
		binding.widgetHourBold.setTextColor(sharedPreferences.clockWidgetHourColor)
		binding.widgetColon.setTextColor(sharedPreferences.clockWidgetMinuteColor)
		binding.widgetMinute.setTextColor(sharedPreferences.clockWidgetMinuteColor)
		binding.widgetMinuteBold.setTextColor(sharedPreferences.clockWidgetMinuteColor)
		binding.widgetAmPm.setTextColor(sharedPreferences.clockWidgetAmPmColor)
		binding.widgetAmPmBold.setTextColor(sharedPreferences.clockWidgetAmPmColor)
		binding.widgetDate.setTextColor(sharedPreferences.clockWidgetDateColor)
		binding.widgetDateBold.setTextColor(sharedPreferences.clockWidgetDateColor)
		binding.widgetAlarmTime.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.widgetAlarmTimeBold.setTextColor(sharedPreferences.clockWidgetAlarmTimeColor)
		binding.widgetAlarmIcon.setColorFilter(sharedPreferences.clockWidgetAlarmIconColor)

		// Set text size
		binding.widgetHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.widgetHourBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.widgetColon.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.widgetMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.widgetMinuteBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetTimeTextSize)
		binding.widgetAmPm.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
		binding.widgetAmPmBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAmPmTextSize)
		binding.widgetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
		binding.widgetDateBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetDateTextSize)
		binding.widgetAlarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
		binding.widgetAlarmTimeBold.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.clockWidgetAlarmTimeTextSize)
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

}
