package com.nfcalarmclock.alarm.options.mediapicker

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.util.media.NacMedia

/**
 * Preference that displays the sound prompt dialog.
 */
class NacMediaPickerPreference @JvmOverloads constructor(

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
) : Preference(context, attrs, style)
{

	/**
	 * Path of the sound.
	 */
	var mediaPath: String = ""

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference
	}

	/**
	 * Get the summary text.
	 *
	 * @return The summary text.
	 */
	override fun getSummary(): CharSequence
	{
		// Get the name of the media
		val title = NacMedia.getTitle(context, mediaPath)

		// Get the name "None" if unable to determine the name
		val none = context.getString(R.string.none)

		return title.ifEmpty { none }
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any?
	{
		return a.getString(index)
	}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			mediaPath = getPersistedString(mediaPath)
		}
		// Convert the default value
		else
		{
			mediaPath = defaultValue as String

			persistString(mediaPath)
		}
	}

	/**
	 * Set the persist the media path.
	 */
	fun setAndPersistMediaPath(mediaPath: String)
	{
		// Set the media path
		this.mediaPath = mediaPath

		// Persist the value
		persistString(this.mediaPath)

		// Notify of change
		notifyChanged()
	}

}