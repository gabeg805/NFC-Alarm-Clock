package com.nfcalarmclock.system.permission

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import java.util.Locale

/**
 * A preference that is used to display optional permissions.
 */
class NacRequiredPermissionPreference @JvmOverloads constructor(

	/**
	 * Context.
	 */
	context: Context,

	/**
	 * Attribute set.
	 */
	attrs: AttributeSet? = null,

	/**
	 * Default style.
	 */
	style: Int = 0

	// Constructor
): Preference(context, attrs, style)
{

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

		// Get the text view
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
		val message = context.resources.getString(R.string.message_required)

		// Set the text
		textView.text = message.lowercase(locale)
	}

	/**
	 * Set the color of the permission text.
	 */
	private fun setPermissionTextColor(textView: TextView)
	{
		// Get the color based on the API
		val color = context.getColor(R.color.red)

		// Set the color
		textView.setTextColor(color)
	}

}