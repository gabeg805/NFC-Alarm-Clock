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
	context: Context? = null,
	alarm: NacAlarm? = null,
	var contentType: Int = AudioAttributes.CONTENT_TYPE_SONIFICATION,
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
	 * Audio focus request object that is used when requesting audio focus. If null, it will be
	 * set by NacAudioManager.requestFocus().
	 */
	var audioFocusRequest: AudioFocusRequest? = null

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
	 * Whether audio was ducking or not.
	 */
	var wasDucking = false

	/**
	 * Constructor.
	 */
	init
	{
		// Set alarm based attributes
		if ((context != null) && (alarm != null))
		{
			// Set audio usage
			audioUsage = NacAudioManager.sourceToUsage(context, alarm.audioSource)

			// Set the text-to-speech rate and voice
			speechRate = alarm.ttsSpeechRate
			voice = alarm.ttsVoice
		}
	}

	/**
	 * Copy the NacAudioAttributes object to a new object.
	 */
	fun copy(): NacAudioAttributes
	{
		// Create a new object
		val attrs = NacAudioAttributes()

		// Copy all the attributes
		attrs.audioUsage = audioUsage
		attrs.contentType = contentType
		attrs.speechRate = speechRate
		attrs.voice = voice
		attrs.audioFocusRequest = null

		return attrs
	}

}