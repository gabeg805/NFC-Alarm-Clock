package com.nfcalarmclock.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.nfcalarmclock.databinding.NacClockWidgetConfigureBinding
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupProgressAndThumbColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * The configuration screen for the [NacClockWidgetProvider] AppWidget.
 */
class NacClockWidgetConfigureActivity : AppCompatActivity()
{

	/**
	 * ID of the widget.
	 */
	private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

	/**
	 * Bind the activity with the XML views.
	 */
	private lateinit var binding: NacClockWidgetConfigureBinding

	//private var onClickListener = View.OnClickListener {
	//	val context = this@NacClockWidgetConfigureActivity

	//	// When the button is clicked, store the string locally
	//	val widgetText = appWidgetText.text.toString()
	//	saveTitlePref(context, appWidgetId, widgetText)

	//	// It is the responsibility of the configuration activity to update the app widget
	//	val appWidgetManager = AppWidgetManager.getInstance(context)
	//	updateAppWidget(context, appWidgetManager, appWidgetId)

	//	// Make sure we pass back the original appWidgetId
	//	val resultValue = Intent()
	//	resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
	//	setResult(RESULT_OK, resultValue)
	//	finish()
	//}

	//appWidgetText = binding.appwidgetText as EditText
	//binding.addButton.setOnClickListener(onClickListener)
	//appWidgetText.setText(loadTitlePref(this@NacClockWidgetConfigureActivity, appWidgetId))

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

		// Get the shared preferences
		val sharedPreferences = NacSharedPreferences(this)

		// Setup the activity
		setupInitialValues(sharedPreferences)
		setupColors(sharedPreferences)
		setupListeners(sharedPreferences)

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
	 * Setup colors.
	 */
	private fun setupColors(sharedPreferences: NacSharedPreferences)
	{
		// Titles
		binding.backgroundTransparencyTitle.setTextColor(sharedPreferences.themeColor)
		binding.timeTitle.setTextColor(sharedPreferences.themeColor)
		binding.dateTitle.setTextColor(sharedPreferences.themeColor)
		binding.alarmTitle.setTextColor(sharedPreferences.themeColor)

		// Switches
		binding.showDateSwitch.setupSwitchColor(sharedPreferences)
		binding.showTimeSwitch.setupSwitchColor(sharedPreferences)
		binding.showAlarmTimeSwitch.setupSwitchColor(sharedPreferences)
		binding.showAlarmIconSwitch.setupSwitchColor(sharedPreferences)
		binding.boldDateSwitch.setupSwitchColor(sharedPreferences)
		binding.boldHourSwitch.setupSwitchColor(sharedPreferences)
		binding.boldMinuteSwitch.setupSwitchColor(sharedPreferences)
		binding.boldAmPmSwitch.setupSwitchColor(sharedPreferences)
		binding.boldAlarmTimeSwitch.setupSwitchColor(sharedPreferences)

		// Sliders
		binding.backgroundTransparencySlider.setupProgressAndThumbColor(sharedPreferences)
		binding.dateSizeSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.timeSizeSlider.setupProgressAndThumbColor(sharedPreferences)
		binding.alarmSizeSlider.setupProgressAndThumbColor(sharedPreferences)

		// Done button
		binding.doneButton.setupBackgroundColor(sharedPreferences)
	}

	/**
	 * Setup initial values.
	 */
	private fun setupInitialValues(sharedPreferences: NacSharedPreferences)
	{
		// Switches
		binding.showDateSwitch.isChecked = sharedPreferences.shouldClockWidgetShowDate
		binding.showTimeSwitch.isChecked = sharedPreferences.shouldClockWidgetShowTime
		binding.showAlarmTimeSwitch.isChecked = sharedPreferences.shouldClockWidgetShowAlarmTime
		binding.showAlarmIconSwitch.isChecked = sharedPreferences.shouldClockWidgetShowAlarmIcon
		binding.boldDateSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldDate
		binding.boldHourSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldHour
		binding.boldMinuteSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldMinute
		binding.boldAmPmSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldAmPm
		binding.boldAlarmTimeSwitch.isChecked = sharedPreferences.shouldClockWidgetBoldAlarmTime

		// Sliders
		binding.backgroundTransparencySlider.value = sharedPreferences.clockWidgetBackgroundTransparency.toFloat()
		binding.dateSizeSlider.value = sharedPreferences.clockWidgetDateTextSize
		binding.timeSizeSlider.value = sharedPreferences.clockWidgetTimeTextSize
		binding.alarmSizeSlider.value = sharedPreferences.clockWidgetAlarmTimeTextSize
	}

	/**
	 * Setup listeners.
	 */
	private fun setupListeners(sharedPreferences: NacSharedPreferences)
	{
		// Show date
		binding.showDateContainer.setOnClickListener {
			binding.showDateSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowDate = binding.showDateSwitch.isChecked
		}

		// Show time
		binding.showTimeContainer.setOnClickListener {
			binding.showTimeSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowTime = binding.showTimeSwitch.isChecked
		}

		// Show alarm time
		binding.showAlarmTimeContainer.setOnClickListener {
			binding.showAlarmTimeSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowAlarmTime = binding.showAlarmTimeSwitch.isChecked
		}

		// Show alarm icon
		binding.showAlarmIconContainer.setOnClickListener {
			binding.showAlarmIconSwitch.toggle()
			sharedPreferences.shouldClockWidgetShowAlarmIcon = binding.showAlarmIconSwitch.isChecked
		}

		// Bold date
		binding.boldDateContainer.setOnClickListener {
			binding.boldDateSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldDate = binding.boldDateSwitch.isChecked
		}

		// Bold hour
		binding.boldHourContainer.setOnClickListener {
			binding.boldHourSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldHour = binding.boldHourSwitch.isChecked
		}

		// Bold minute
		binding.boldMinuteContainer.setOnClickListener {
			binding.boldMinuteSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldMinute = binding.boldMinuteSwitch.isChecked
		}

		// Bold am/pm
		binding.boldAmPmContainer.setOnClickListener {
			binding.boldAmPmSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldAmPm = binding.boldAmPmSwitch.isChecked
		}

		// Bold alarm time
		binding.boldAlarmTimeContainer.setOnClickListener {
			binding.boldAlarmTimeSwitch.toggle()
			sharedPreferences.shouldClockWidgetBoldAlarmTime = binding.boldAlarmTimeSwitch.isChecked
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
				sharedPreferences.clockWidgetBackgroundTransparency = slider.value.toInt()
			}

		})

		// Date size
		binding.dateSizeSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

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
				sharedPreferences.clockWidgetDateTextSize = slider.value
			}

		})

		// Time size
		binding.timeSizeSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

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
				sharedPreferences.clockWidgetTimeTextSize = slider.value
			}

		})

		// Alarm time size
		binding.alarmSizeSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

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
				sharedPreferences.clockWidgetAlarmTimeTextSize = slider.value
			}

		})

		// Done button
		binding.doneButton.setOnClickListener {
			updateAndFinish()
		}
	}

	/**
	 * Update the widget and finish the activity.
	 */
	private fun updateAndFinish()
	{
		//// When the button is clicked, store the string locally
		//val widgetText = appWidgetText.text.toString()
		//saveTitlePref(context, appWidgetId, widgetText)

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

//// Write the prefix to the SharedPreferences object for this widget
//internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String)
//{
//	val sharedPreferences = NacSharedPreferences(context)
//
//	//val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
//	//prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
//	//prefs.apply()
//}
//
//// Read the prefix from the SharedPreferences object for this widget.
//// If there is no preference saved, get the default from a resource
//internal fun loadTitlePref(context: Context, appWidgetId: Int): String
//{
//	//val prefs = context.getSharedPreferences(PREFS_NAME, 0)
//	//val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
//	//return titleValue ?: context.getString(R.string.appwidget_text)
//	return context.getString(R.string.appwidget_text)
//}
//
//internal fun deleteTitlePref(context: Context, appWidgetId: Int)
//{
//	//val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
//	//prefs.remove(PREF_PREFIX_KEY + appWidgetId)
//	//prefs.apply()
//}
