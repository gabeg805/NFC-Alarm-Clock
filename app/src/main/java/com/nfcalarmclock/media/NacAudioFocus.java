package com.nfcalarmclock.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

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
	 * @return The audio focus request.
	 */
	public static AudioFocusRequest getRequest(
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioAttributes audioAttributes = attrs.getAudioAttributes();
			int focus = attrs.getFocus();

			return new AudioFocusRequest.Builder(focus)
				.setAudioAttributes(audioAttributes)
				.setOnAudioFocusChangeListener(listener)
				.build();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Request audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static boolean request(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);
		int result;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioFocusRequest request = NacAudioFocus.getRequest(listener, attrs);
			result = am.requestAudioFocus(request);
		}
		else
		{
			int stream = attrs.getStream();
			int focus = attrs.getFocus();
			result = am.requestAudioFocus(listener, stream, focus);
		}

		return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
	}

	/**
	 * @see NacAudioFocus#request(Context,
	 *  AudioManager.OnAudioFocusChangeListener, NacAudioAttributes)
	 */
	public static boolean requestGain(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		attrs.setFocus(AudioManager.AUDIOFOCUS_GAIN);

		return NacAudioFocus.request(context, listener, attrs);
	}

	/**
	 * @see NacAudioFocus#request(Context,
	 *  AudioManager.OnAudioFocusChangeListener, NacAudioAttributes)
	 */
	public static boolean requestTransient(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudioAttributes attrs)
	{
		attrs.setFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

		return NacAudioFocus.request(context, listener, attrs);
	}

}
