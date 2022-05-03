package com.nfcalarmclock.media;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.google.android.exoplayer2.C;

import com.nfcalarmclock.shared.NacSharedConstants;

import java.util.List;

/**
 * Audio manager.
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "RedundantSuppression", "UnusedReturnValue"})
public class NacAudioManager
{

	/**
	 * Abandon audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static int abandonFocus(Context context,
		AudioManager.OnAudioFocusChangeListener listener)
	{
		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);
		int result;

		// TODO: This is commented out because you need the original
		// AudioFocusRequest object that was used when requesting audio focus.
		// Unfortunately, that is a local variable in the functions below. Could
		// probably save it to NacAudioAttributes when I create it?

		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		//{
		//	AudioFocusRequest request = this.getAudioFocusRequest();
		//	result = am.abandonAudioFocus(request);
		//}
		//else
		//{
			result = am.abandonAudioFocus(listener);
		//}

		return result;
	}

	/**
	 * Request to generally gain audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static boolean requestFocus(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs, int focusGainType)
	{
		// Get the audio manager object
		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);

		// Assume a result of FAILED
		int result = AudioManager.AUDIOFOCUS_REQUEST_FAILED;

		// Build the audio request
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioFocusRequest.Builder builder = new AudioFocusRequest.Builder(focusGainType)
				.setAudioAttributes(attrs.getAudioAttributesV21());

			// Set the listener only if it is not null
			if (listener != null)
			{
				builder = builder.setOnAudioFocusChangeListener(listener);
			}

			// Request audio focus and get the result
			AudioFocusRequest request = builder.build();
			result = am.requestAudioFocus(request);
		}
		else
		{
			// Get the stream the request is for
			int stream = attrs.getStream();

			// Request focus when the stream is NOT the default type
			if (stream != AudioManager.USE_DEFAULT_STREAM_TYPE)
			{
				result = am.requestAudioFocus(listener, stream, focusGainType);
			}
		}

		// Check the result
		return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
	}

	/**
	 * Request to gain audio focus.
	 */
	public static boolean requestFocusGain(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		return NacAudioManager.requestFocus(context, listener, attrs,
			AudioManager.AUDIOFOCUS_GAIN);
	}

	/**
	 * Request to gain transient audio focus.
	 */
	public static boolean requestFocusGainTransient(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		return NacAudioManager.requestFocus(context, listener, attrs,
			AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	}

	/**
	 * Convert a source to a usage.
	 */
	public static int sourceToUsage(Context context, String source)
	{
		if ((source == null) || source.isEmpty())
		{
			return C.USAGE_UNKNOWN;
		}

		NacSharedConstants cons = new NacSharedConstants(context);
		List<String> audioSources = cons.getAudioSources();

		// Alarm
		if (source.equals(audioSources.get(0)))
		{
			return C.USAGE_ALARM;
		}
		// Call
		else if (source.equals(audioSources.get(1)))
		{
			return C.USAGE_VOICE_COMMUNICATION;
		}
		// Media
		else if (source.equals(audioSources.get(2)))
		{
			return C.USAGE_MEDIA;
		}
		// Notification
		else if (source.equals(audioSources.get(3)))
		{
			return C.USAGE_NOTIFICATION;
		}
		// Ringtone
		else if (source.equals(audioSources.get(4)))
		{
			return C.USAGE_NOTIFICATION_RINGTONE;
		}
		// Default to media
		else
		{
			return C.USAGE_MEDIA;
		}
	}

	/**
	 * @return The stream from a usage type.
	 */
	public static int usageToStream(int usage)
	{
		// Alarm
		if (usage == C.USAGE_ALARM)
		{
			return AudioManager.STREAM_ALARM;
		}
		// Call
		else if (usage == C.USAGE_VOICE_COMMUNICATION)
		{
			return AudioManager.STREAM_VOICE_CALL;
		}
		// Media
		else if (usage == C.USAGE_MEDIA)
		{
			return AudioManager.STREAM_MUSIC;
		}
		// Notification
		else if (usage == C.USAGE_NOTIFICATION)
		{
			return AudioManager.STREAM_NOTIFICATION;
		}
		// Ringtone
		else if (usage == C.USAGE_NOTIFICATION_RINGTONE)
		{
			return AudioManager.STREAM_RING;
		}
		// Default
		else
		{
			return AudioManager.USE_DEFAULT_STREAM_TYPE;
		}
	}

}
