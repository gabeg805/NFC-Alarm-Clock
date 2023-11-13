package com.nfcalarmclock.view.dayofweek

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * A button that consists of an image to the left, and text to the right
 * of it.
 */
class NacDayButton
	: LinearLayout,
	View.OnClickListener,
	OnButtonCheckedListener
{

	/**
	 * Listener for day change events.
	 */
	interface OnDayChangedListener
	{
		fun onDayChanged(button: NacDayButton)
	}

	/**
	 * Attributes for button and text.
	 */
	class Attributes(context: Context, attrs: AttributeSet?)
	{

		/**
		 * Width.
		 */
		var width = 0

		/**
		 * Height.
		 */
		var height = 0

		/**
		 * Text.
		 */
		var text: String? = null

		/**
		 * Initialize the attributes.
		 */
		init
		{
			// Create a typed array to search for custom attributes
			val ta = context.theme.obtainStyledAttributes(attrs,
				R.styleable.NacDayButton, 0, R.style.NacDayButton)

			// Create a typed array searching for default android attributes
			val androidTa = context.theme.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.text), 0, 0)

			// Get attributes from the typed array
			try
			{
				width = ta.getDimension(R.styleable.NacDayButton_nacWidth,
					ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()).toInt()
				height = ta.getDimension(R.styleable.NacDayButton_nacHeight,
					ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()).toInt()
				text = androidTa.getString(0)
			}
			// Recycle the typed array
			finally
			{
				ta.recycle()
			}
		}

	}

	/**
	 * Day button parent.
	 */
	private var buttonToggleGroup: MaterialButtonToggleGroup? = null

	/**
	 * Day button.
	 */
	var button: MaterialButton? = null
		private set

	/**
	 * View attributes.
	 */
	private var dayAttributes: Attributes? = null

	/**
	 * Day changed listener.
	 */
	var onDayChangedListener: OnDayChangedListener? = null

	/**
	 * Constructor.
	 */
	constructor(context: Context) : super(context, null)
	{
		init(null)
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)
	{
		init(attrs)
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context, attrs: AttributeSet?,
		defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		init(attrs)
	}

	/**
	 * Disable and animate the view.
	 */
	fun disable()
	{
		// Toggle the button if it is enabled
		if (button!!.isChecked)
		{
			toggle()
		}
	}

	/**
	 * Enable and animate the view.
	 */
	fun enable()
	{
		// Toggle the button if it is disabled
		if (!button!!.isChecked)
		{
			toggle()
		}
	}

	/**
	 * Initialize the view.
	 */
	fun init(attrs: AttributeSet?)
	{
		// Check if attributes are null
		if (attrs == null)
		{
			return
		}

		// Set the orientation
		orientation = HORIZONTAL

		// Setup style
		setupStyle()

		// Set other attributes
		dayAttributes = Attributes(context, attrs)
		onDayChangedListener = null
		buttonToggleGroup!!.addOnButtonCheckedListener(this)

		// Set an on click listener
		setOnClickListener(this)
	}

	/**
	 * Called when the button is checked.
	 */
	override fun onButtonChecked(group: MaterialButtonToggleGroup, checkedId: Int,
		isChecked: Boolean)
	{
		// Return if there are no listeners set
		val listener = onDayChangedListener ?: return

		// Remove the toggle group listener that was set when this view is
		// initialized so that this onButtonChecked() method does not potentially get
		// called recursively
		buttonToggleGroup!!.removeOnButtonCheckedListener(this)

		// Call the listener
		listener.onDayChanged(this)

		// Add the toggle group listener that was set when this view is
		// initialized back
		buttonToggleGroup!!.addOnButtonCheckedListener(this)
	}

	/**
	 * Called when the view is clicked
	 */
	override fun onClick(view: View)
	{
		// Get the current state
		val checked = button!!.isChecked

		// Toggle the state
		button!!.isChecked = !checked
	}

	/**
	 * Finish setting up the View.
	 */
	override fun onFinishInflate()
	{
		super.onFinishInflate()

		setViewAttributes()
	}

	/**
	 * Set the width and height of the button.
	 */
	fun setSize(width: Int, height: Int)
	{
		// Get the button's current layout parameters
		val params = button!!.layoutParams as LayoutParams

		// Change the parameters
		params.width = width
		params.height = height

		// Apply the parameters to the button
		button!!.layoutParams = params
	}

	/**
	 * Set the day button style.
	 */
	fun setStyle(style: Int)
	{
		// Get the layout inflater
		val inflater = LayoutInflater.from(context)

		// Determine which layout to use
		val layout = if (style == 1)
		{
			R.layout.nac_day_button_filled
		}
		else
		{
			R.layout.nac_day_button_outlined
		}

		// Remove all views
		removeAllViews()

		// Inflate the layout
		inflater.inflate(layout, this, true)

		// Set the member variables
		buttonToggleGroup = findViewById(R.id.nac_day_button_group)
		button = findViewById(R.id.nac_day_button)
	}

	/**
	 * Set the text in the button.
	 *
	 * @param  text  The text to enter in the button.
	 */
	fun setText(text: String?)
	{
		button!!.text = text
	}

	/**
	 * Setup the style of the day button.
	 */
	private fun setupStyle()
	{
		// Get the style
		val shared = NacSharedPreferences(context)
		val style = shared.dayButtonStyle

		// Set the style
		setStyle(style)
	}

	/**
	 * Set view attributes.
	 */
	private fun setViewAttributes()
	{
		// Set size
		setSize(dayAttributes!!.width, dayAttributes!!.height)

		// Set text
		setText(dayAttributes!!.text)
	}

	/**
	 * Toggle the day button.
	 */
	fun toggle()
	{
		// Get the current status
		val status = button!!.isChecked

		// Toggle the status
		button!!.isChecked = !status
	}

}