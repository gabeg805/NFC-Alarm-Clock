package com.nfcalarmclock.audio.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import com.nfcalarmclock.R;

/**
 */
public class NacAlarmAudioOptionsDialog
	extends DialogFragment
{

	/**
	 * Listener for when an audio option is clicked.
	 */
	@SuppressWarnings("UnnecessaryInterfaceModifier")
	public interface OnAudioOptionClickedListener
	{
		public void onAudioOptionClicked(long alarmId, int which);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAlarmAudioOptionsDialog";

	/**
	 * Alarm ID.
	 */
	private long mAlarmId;

	/**
	 * Listener for when an audio option is clicked.
	 */
	private OnAudioOptionClickedListener mOnAudioOptionClickedListener;

	/**
	 * Call the OnAudioOptionClickedListener object, if it has been set.
	 *
	 * @param  which  Which item was clicked.
	 */
	public void callOnAudioOptionClickedListener(int which)
	{
		OnAudioOptionClickedListener listener =
			this.getOnAudioOptionClickedListener();
		long alarmId = this.getAlarmId();

		if (listener != null)
		{
			listener.onAudioOptionClicked(alarmId, which);
		}
	}

	/**
	 * Get the alarm ID.
	 *
	 * @return The alarm ID.
	 */
	public long getAlarmId()
	{
		return this.mAlarmId;
	}

	/**
	 * Get the OnAudioOptionClickedListener object.
	 *
	 * @return The OnAudioOptionClickedListener object.
	 */
	public OnAudioOptionClickedListener getOnAudioOptionClickedListener()
	{
		return this.mOnAudioOptionClickedListener;
	}

	/**
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		return new AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.title_audio_option))
			.setItems(R.array.audio_options, (dialog, which) ->
				this.callOnAudioOptionClickedListener(which))
			.create();
	}

	/**
	 * Set the alarm ID.
	 *
	 * @param  id  Alarm ID.
	 */
	public void setAlarmId(long id)
	{
		this.mAlarmId = id;
	}

	/**
	 * Set the OnAudioOptionClickedListener object.
	 *
	 * @param  listener  The OnAudioOptionClickedListener object.
	 */
	public void setOnAudioOptionClickedListener(
		OnAudioOptionClickedListener listener)
	{
		this.mOnAudioOptionClickedListener = listener;
	}

}

