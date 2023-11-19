package com.nfcalarmclock.volume

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Preference to choose the default volume and audio source.
 */
class NacVolumePreference @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	style: Int = 0
) : Preference(context, attrs, style),
	OnSeekBarChangeListener
{

	/**
	 * Listener for when the audio options button is clicked.
	 */
	fun interface OnAudioOptionsClickedListener
	{
		fun onAudioOptionsClicked()
	}

	/**
	 * Volume level value.
	 */
	private var volumeValue = 0

	/**
	 * Seekbar.
	 */
	private var volumeSeekBar: SeekBar? = null

	/**
	 * Shared preferences.
	 */
	private var nacSharedPreferences: NacSharedPreferences? = null

	/**
	 * Audio options button clicked listener.
	 */
	var onAudioOptionsClickedListener: OnAudioOptionsClickedListener? = null

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference_volume
	}

	/**
	 * Call the listener to when the audio options button is clicked.
	 */
	private fun callOnAudioOptionsClickedListener()
	{
		onAudioOptionsClickedListener?.onAudioOptionsClicked()
	}

	/**
	 * Called when the preference is attached.
	 */
	override fun onAttached()
	{
		// Super
		super.onAttached()

		// Set the shared preferences
		nacSharedPreferences = NacSharedPreferences(context)
	}

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Define views
		volumeSeekBar = holder.findViewById(R.id.volume_slider) as SeekBar
		val audioOptions = holder.findViewById(R.id.widget) as MaterialButton

		// Setup the audio options
		audioOptions.setOnClickListener {
			callOnAudioOptionsClickedListener()
		}

		// Setup the seekbar
		volumeSeekBar!!.progress = volumeValue

		volumeSeekBar!!.setOnSeekBarChangeListener(this)
		setSeekBarColor()
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		val defaultValue = context.resources.getInteger(R.integer.default_volume)

		return a.getInteger(index, defaultValue)
	}

	/**
	 * Called when the progress bar is changed.
	 */
	override fun onProgressChanged(seekBar: SeekBar, progress: Int,
		fromUser: Boolean)
	{
		volumeValue = progress
	}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			volumeValue = getPersistedInt(volumeValue)
		}
		// Convert the default value
		else
		{
			volumeValue = defaultValue as Int

			persistInt(volumeValue)
		}
	}

	/**
	 * Called when the progress bar is starting to be touched.
	 */
	override fun onStartTrackingTouch(seekBar: SeekBar)
	{
	}

	/**
	 * Called when the progress bar is stopped being touched.
	 */
	override fun onStopTrackingTouch(seekBar: SeekBar)
	{
		persistInt(volumeValue)
	}

	/**
	 * Set the volume seekbar color.
	 */
	@Suppress("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	@SuppressLint("NewApi")
	fun setSeekBarColor()
	{
		val themeColor = nacSharedPreferences!!.themeColor
		val progressDrawable = volumeSeekBar!!.progressDrawable
		val thumbDrawable = volumeSeekBar!!.thumb

		// Check if API >= 29
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			val blendFilter = BlendModeColorFilter(themeColor, BlendMode.SRC_IN)

			progressDrawable.colorFilter = blendFilter
			thumbDrawable.colorFilter = blendFilter
		}
		// Lower API value
		else
		{
			progressDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN)
			thumbDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN)
		}
	}

}
