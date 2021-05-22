package com.nfcalarmclock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

/**
 */
public class NacAlarmAudioSourceDialog
	extends DialogFragment
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAlarmAudioSourceDialog";

	/**
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		return new AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.title_audio_source))
			.setSingleChoiceItems(R.array.audio_sources, -1, (dialog, which) -> {})
			.setPositiveButton(getString(R.string.action_ok), (dialog, which) -> {})
			.setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> {})
			//.setView(R.layout.dlg_alarm_audio_options)
			.create();
	}

	/**
	 */
	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);

		NacUtility.printf("onDismiss! Sources");
	}

}

