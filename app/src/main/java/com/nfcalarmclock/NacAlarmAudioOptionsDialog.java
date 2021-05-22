package com.nfcalarmclock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView;

/**
 */
public class NacAlarmAudioOptionsDialog
	extends DialogFragment
	implements AdapterView.OnItemClickListener
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAlarmAudioOptionsDialog";

	/**
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		return new AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.title_audio_option))
			//.setItems(R.array.audio_options, null)
			.setPositiveButton(getString(R.string.action_ok), (dialog, which) -> {})
			.setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> {})
			.setView(R.layout.dlg_alarm_audio_options)
			.create();
	}

	/**
	 */
	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);

		NacUtility.printf("onDismiss! Main");
	}

	/**
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		NacUtility.printf("On item selected! %d %d", position, id);
		switch (position)
		{
			case 0:
				AlertDialog dialog = (AlertDialog) getDialog();
				ListView listview = dialog.getListView();
				View newView = getLayoutInflater().inflate(R.layout.dlg_alarm_name, listview, true);
				dialog.setView(newView);
				//dialog.setContentView(R.layout.dlg_alarm_name);
				//new NacAlarmAudioSourceDialog().show(getChildFragmentManager(),
				//	NacAlarmAudioSourceDialog.TAG);
				break;
			case 1:
				break;
			default:
				break;
		}
	}

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		AlertDialog dialog = (AlertDialog) getDialog();
		ListView listview = dialog.getListView();

		listview.setOnItemClickListener(this);
	}

}

