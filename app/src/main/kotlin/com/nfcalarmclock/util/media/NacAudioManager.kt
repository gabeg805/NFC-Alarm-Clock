package com.nfcalarmclock.util.media

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import androidx.media3.common.C
import com.nfcalarmclock.R

/**
 * Audio manager.
 */
object NacAudioManager
{

	/**
	 * Abandon audio focus.
	 */
	@Suppress("deprecation")
	fun abandonFocus(
		context: Context,
		attrs: NacAudioAttributes
	): Int
	{

		val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

		// Need to use the AudioFocusRequest object that was used when requesting audio
		// focus in order to abandon focus.
		return if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && (attrs.audioFocusRequest != null))
		{
			audioManager.abandonAudioFocusRequest(attrs.audioFocusRequest!!)
		}
		// Simpler way to abandon audio focus in older API
		else
		{
			audioManager.abandonAudioFocus(null)
		}
	}

	/**
	 * Request to generally gain audio focus.
	 */
	@Suppress("deprecation")
	private fun requestFocus(
		context: Context,
		listener: OnAudioFocusChangeListener?,
		attrs: NacAudioAttributes, focusGainType: Int
	): Boolean
	{
		// Get the audio manager object
		val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

		// Assume a result of FAILED
		var result: Int

        // Build the audio request
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			var builder = AudioFocusRequest.Builder(focusGainType)
				.setAudioAttributes(attrs.audioAttributesV21)

			// Set the listener only if it is not null
			if (listener != null)
			{
				builder = builder.setOnAudioFocusChangeListener(listener)
			}

			// Build the audio request and set it in the audio attributes object
			val request = builder.build()
			attrs.audioFocusRequest = request

			// Request audio focus and get the result
			result = audioManager.requestAudioFocus(request)
		}
		else
		{
			// Get the stream the request is for
			val stream = if (attrs.stream == AudioManager.USE_DEFAULT_STREAM_TYPE)
			{
				// Stream has not been set. Must be set before requesting focus. Use music
				// stream by default
				AudioManager.STREAM_MUSIC
			}
			else
			{
				attrs.stream
			}

			// Request focus
			result = audioManager.requestAudioFocus(listener, stream, focusGainType)
		}

		// Check the result
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
	}

	/**
	 * Request to gain audio focus.
	 */
	fun requestFocusGain(
		context: Context,
		listener: OnAudioFocusChangeListener?,
		attrs: NacAudioAttributes
	): Boolean
	{
		return requestFocus(context, listener, attrs, AudioManager.AUDIOFOCUS_GAIN)
	}

	/**
	 * Request to gain transient audio focus.
	 */
	fun requestFocusGainTransient(
		context: Context,
		listener: OnAudioFocusChangeListener?,
		attrs: NacAudioAttributes
	): Boolean
	{
		return requestFocus(context, listener, attrs, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
	}

	/**
	 * Convert a source to a usage.
	 */
	fun sourceToUsage(context: Context, source: String?): Int
	{
		// Check if the source is not set
		if (source.isNullOrEmpty())
		{
			return C.USAGE_UNKNOWN
		}

		// Get all the audio sources
		val audioSources = context.resources.getStringArray(R.array.audio_sources)

		// Alarm
		return when(source)
		{
			audioSources[0] -> C.USAGE_ALARM
			audioSources[1] -> C.USAGE_VOICE_COMMUNICATION
			audioSources[2] -> C.USAGE_MEDIA
			audioSources[3] -> C.USAGE_NOTIFICATION
			audioSources[4] -> C.USAGE_NOTIFICATION_RINGTONE
			else            -> C.USAGE_MEDIA
		}
	}

	/**
	 * Get the stream from a usage type.
	 *
	 * @return The stream from a usage type.
	 */
	fun usageToStream(usage: Int): Int
	{
		// Alarm
		return when (usage)
		{
			C.USAGE_ALARM                 -> AudioManager.STREAM_ALARM
			C.USAGE_VOICE_COMMUNICATION   -> AudioManager.STREAM_VOICE_CALL
			C.USAGE_MEDIA                 -> AudioManager.STREAM_MUSIC
			C.USAGE_NOTIFICATION          -> AudioManager.STREAM_NOTIFICATION
			C.USAGE_NOTIFICATION_RINGTONE -> AudioManager.STREAM_RING
			else                          -> AudioManager.USE_DEFAULT_STREAM_TYPE
		}
	}

}