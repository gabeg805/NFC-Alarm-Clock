package com.nfcalarmclock.permission

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import java.util.Locale

/**
 * A preference that is used to display optional permissions.
 */
class NacOptionalPermissionPreference
	: Preference
{

	/**
	 * Constructor.
	 */
	constructor(context: Context) : super(context)
	{
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
	{
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context, attrs: AttributeSet?, style: Int) : super(context, attrs, style)
	{
	}

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference_permission
	}

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Get the textview
		val permissionType = holder.findViewById(R.id.permission_type) as TextView

		// Setup the textview
		setPermissionText(permissionType)
		setPermissionTextColor(permissionType)
	}

	/**
	 * Set the permission text.
	 */
	private fun setPermissionText(textView: TextView)
	{
		// Get the message
		val locale = Locale.getDefault()
		val message = context.resources.getString(R.string.message_optional)

		// Set the text
		textView.text = message.lowercase(locale)
	}

	/**
	 * Set the color of the permission text.
	 */
	@Suppress("deprecation")
	private fun setPermissionTextColor(textView: TextView)
	{
		// Get the color based on the API
		val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			context.getColor(R.color.green)
		}
		else
		{
			context.resources.getColor(R.color.green)
		}

		// Set the color
		textView.setTextColor(color)
	}

}