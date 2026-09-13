package com.nfcalarmclock.system.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import androidx.media3.common.C
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Audio attributes.
 *
 * @param context Context.
 * @param source Audio source.
 */
class NacAudioAttributes(
	context: Context,
	source: String = ""
)
{

	/**
	 * Audio attributes.
	 */
	val audioAttributes: AudioAttributes
		get() = AudioAttributes.Builder()
			.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
			.setUsage(audioUsage)
			.build()

	/**
	 * Audio attributes.
	 */
	val audioAttributesMedia3: androidx.media3.common.AudioAttributes
		get() = androidx.media3.common.AudioAttributes.Builder()
			.setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
			.setUsage(NacAudioManager.usageToUsageMedia3(audioAttributes.usage))
			.build()

	/**
	 * Audio usage.
	 */
	var audioUsage = 0

	/**
	 * Whether audio was ducking or not.
	 */
	var wasDucking = false

	/**
	 * Audio stream.
	 */
	val stream: Int
		get() = NacAudioManager.usageToStream(audioUsage)

	/**
	 * Speech rate for text-to-speech.
	 */
	var speechRate: Float = 0f

	/**
	 * Voice name for text-to-speech.
	 */
	var voice: String = ""

	/**
	 * Audio focus request object that is used when initially requesting audio focus.
	 * This is set by the NacAudioManager.
	 */
	var audioFocusRequest: AudioFocusRequest? = null

	/**
	 * Constructor.
	 */
	constructor(context: Context, alarm: NacAlarm) : this(context, "")
	{
		merge(context, alarm)
	}

	/**
	 * Constructor.
	 */
	init
	{
		// Set usage from audio source
		setUsageFromSource(context, source)
	}

	/**
	 * Merge the current audio attributes with that of the alarm.
	 */
	fun merge(context: Context, alarm: NacAlarm): NacAudioAttributes
	{
		// Set audio usage from audio source
		setUsageFromSource(context, alarm.audioSource)

		// Set the text-to-speech rate and voice
		speechRate = alarm.ttsSpeechRate
		voice = alarm.ttsVoice

		return this
	}

		/**
	 * Set the audio usage from the source name.
	 */
	private fun setUsageFromSource(context: Context, source: String)
	{
		audioUsage = NacAudioManager.sourceToUsage(context, source)
	}

}