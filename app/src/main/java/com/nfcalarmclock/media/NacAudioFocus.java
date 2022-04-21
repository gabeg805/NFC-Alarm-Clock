package com.nfcalarmclock.media;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.google.android.exoplayer2.audio.AudioAttributes;

/**
 * Audio focus.
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "RedundantSuppression", "UnusedReturnValue"})
public class NacAudioFocus
{

	/**
	 * Abandon audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static int abandon(Context context,
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
	public static boolean request(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs, int focusGainType)
	{
		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);
		int result;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioAttributes audioAttributes = attrs.getAudioAttributes();
			AudioFocusRequest request = new AudioFocusRequest.Builder(focusGainType)
				.setAudioAttributes(audioAttributes.getAudioAttributesV21())
				.setOnAudioFocusChangeListener(listener)
				.build();
			result = am.requestAudioFocus(request);
		}
		else
		{
			int stream = attrs.getStream();
			result = am.requestAudioFocus(listener, stream, focusGainType);
		}

		return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
	}

	/**
	 * Request to gain audio focus.
	 */
	public static boolean requestGain(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		return NacAudioFocus.request(context, listener, attrs,
			AudioManager.AUDIOFOCUS_GAIN);
	}

	/**
	 * Request to gain transient audio focus.
	 */
	public static boolean requestGainTransient(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		return NacAudioFocus.request(context, listener, attrs,
			AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	}

}
