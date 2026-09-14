package com.nfcalarmclock.system.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Audio attributes.
 *
 * @param context Context.
 * @param alarm Alarm.
 * @param contentType Content type to use for audio attributes.
 */
class NacAudioAttributes(
	context: Context,
	alarm: NacAlarm? = null,
	private val contentType: Int = AudioAttributes.CONTENT_TYPE_SONIFICATION
)
{

	/**
	 * Audio attributes.
	 */
	val audioAttributes: AudioAttributes
		get() = AudioAttributes.Builder()
			.setContentType(contentType)
			.setUsage(audioUsage)
			.build()

	/**
	 * Audio attributes.
	 */
	val audioAttributesMedia3: androidx.media3.common.AudioAttributes
		get() = androidx.media3.common.AudioAttributes.Builder()
			.setContentType(NacAudioManager.contentTypeToContentTypeMedia3(audioAttributes.contentType))
			.setUsage(NacAudioManager.usageToUsageMedia3(audioAttributes.usage))
			.build()

	/**
	 * Audio usage.
	 */
	var audioUsage = AudioAttributes.USAGE_UNKNOWN

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
	init
	{
		// Set alarm based attributes
		if (alarm != null)
		{
			// Set audio usage
			audioUsage = NacAudioManager.sourceToUsage(context, alarm.audioSource)

			// Set the text-to-speech rate and voice
			speechRate = alarm.ttsSpeechRate
			voice = alarm.ttsVoice
		}

		//// Set usage from audio source
		//setUsageFromSource(context, source)
		//    audioUsage = NacAudioManager.sourceToUsage(context, source)
	}

}