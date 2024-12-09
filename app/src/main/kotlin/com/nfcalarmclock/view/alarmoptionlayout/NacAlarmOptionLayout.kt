package com.nfcalarmclock.view.alarmoptionlayout

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R

/**
 * Alarm option layout so that the layout can be consistent across each new alarm option.
 */
class NacAlarmOptionLayout @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : LinearLayout(context, attrs)
{

	/**
	 * Constructor.
	 */
	init
	{
		inflate(context, R.layout.nac_alarm_option_layout, this)
	}

	/**
	 * Add child views when finished being inflated.
	 */
	override fun onFinishInflate()
	{
		// Super
		super.onFinishInflate()

		// Check if has more than one child. This should always be the case, but better to
		// be cautious
		if (childCount <= 1)
		{
			return
		}

		// Get the main and inner most layouts
		val mainLayout: LinearLayout = getChildAt(0) as LinearLayout
		val innerLayout: LinearLayout = findViewById(R.id.container)

		// Iterate over each child
		for (i in 1..<childCount)
		{
			// Get the child view
			val child = getChildAt(1)

			// Remove the child view
			removeView(child)

			// Check the type of the child to determine where to add it
			if (child is MaterialButton)
			{
				// Add the child in between the OK and Cancel buttons
				mainLayout.addView(child)
			}
			else
			{
				// Add the child to the inner most layout
				innerLayout.addView(child)
			}
		}
	}

}