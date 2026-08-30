package com.nfcalarmclock.settings.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R

/**
 * Custom preference that holds a compound button, typically a checkbox or a switch.
 *
 * @param context Context.
 * @param attrs Attribute set.
 * @param layoutResourceId The resource ID of the layout to inflate for this preference.
 */
@SuppressLint("ResourceType")
abstract class NacCompoundButtonPreference(
	context: Context,
	attrs: AttributeSet?,
	layoutResourceId: Int
) : Preference(context, attrs),
	CompoundButton.OnCheckedChangeListener
{

	/**
	 * The parent layout.
	 */
	protected lateinit var parent: ViewGroup

	/**
	 * Button.
	 */
	protected lateinit var compoundButton: CompoundButton

	/**
	 * Summary text when checkbox is enabled.
	 */
	private var enabledSummary: String? = null

	/**
	 * Summary text when checkbox is disabled.
	 */
	private var disabledSummary: String? = null

	/**
	 * The enabled status that was buffered until the view is bound.
	 *
	 * Note: If it is null, the enabled status is not buffered.
	 */
	private var bufferedEnableStatus: Boolean? = null

	/**
	 * The default value that will be returned in onGetDefaultValue().
	 */
	open val defaultValue: Boolean = true

	/**
	 * Check value.
	 */
	var isChecked = false

	/**
	 * Constructor.
	 */
	init
	{
		// Set the layout
		layoutResource = layoutResourceId

		// Perform a button click when the preference is clicked
		onPreferenceClickListener = OnPreferenceClickListener {
			compoundButton.performClick()
			true
		}

		// Find the summary from the attributes set
		val array = intArrayOf(android.R.attr.summaryOn, android.R.attr.summaryOff)
		val ta = context.obtainStyledAttributes(attrs, array)

		// Get the attributes from the typed array
		try
		{
			// Summary
			enabledSummary = ta.getString(0)
			disabledSummary = ta.getString(1)
		}
		// Recycle the typed array
		finally
		{
			ta.recycle()
		}
	}

	/**
	 * Get the summary text.
	 *
	 * @return The summary text.
	 */
	override fun getSummary(): CharSequence?
	{
		// Enabled summary
		return if (isChecked)
		{
			enabledSummary
		}
		// Disabled summary
		else
		{
			disabledSummary
		}
	}

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Setup the views
		parent = holder.itemView as ViewGroup
		compoundButton = holder.findViewById(R.id.widget) as CompoundButton
		val imageFrame = holder.findViewById(R.id.icon_frame) as LinearLayout
		val imageView = holder.findViewById(R.id.icon) as ImageView

		// Setup the icon
		if (icon != null)
		{
			imageView.setImageDrawable(icon)
			imageFrame.visibility = View.VISIBLE
		}
		// Hide the icon frame
		else
		{
			imageFrame.visibility = View.GONE
		}

		// Set the checked status and sandwich it by unsetting and resetting
		// the listener so that it does not go off when the status is set
		compoundButton.setOnCheckedChangeListener(null)
		compoundButton.isChecked = isChecked
		compoundButton.setOnCheckedChangeListener(this)

		// Check if the enabled status was buffered
		if (bufferedEnableStatus != null)
		{
			this.isEnabled = bufferedEnableStatus!!
			bufferedEnableStatus = null
		}
	}

	/**
	 * Handle checkbox changes.
	 */
	override fun onCheckedChanged(button: CompoundButton, state: Boolean)
	{
		// Call the listener and check if the call was canceled
		if (!callChangeListener(state))
		{
			return
		}

		// Set the checked status
		isChecked = state
		button.isChecked = state

		// Set the summary
		summary = this.summary

		// Persist the state
		persistBoolean(state)

		// Don't know what this does
		notifyDependencyChange(!state)
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		return a.getBoolean(index, defaultValue)
	}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			isChecked = getPersistedBoolean(isChecked)
		}
		// Convert the default value
		else
		{
			isChecked = defaultValue as Boolean

			persistBoolean(isChecked)
		}
	}

	/**
	 * Set whether the preference is enabled or not.
	 */
	override fun setEnabled(enabled: Boolean)
	{
		// Super
		super.setEnabled(enabled)

		// Check if the view has been initialized yet or not
		if (!this::parent.isInitialized)
		{
			bufferedEnableStatus = enabled
			return
		}

		// Set the alpha of the parent
		parent.alpha = if (enabled) 1.0f else 0.25f
	}

}