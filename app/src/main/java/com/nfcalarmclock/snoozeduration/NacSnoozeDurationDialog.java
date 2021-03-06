package com.nfcalarmclock.snoozeduration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nfcalarmclock.util.dialog.NacScrollablePickerDialogFragment;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.R;

import java.util.List;

/**
 * Select how long snoozing an alarm should be.
 */
public class NacSnoozeDurationDialog
	extends NacScrollablePickerDialogFragment
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacSnoozeDurationDialog";

	/**
	 * Get the list of values for the scrollable picker.
	 *
	 * @return The list of values for the scrollable picker for the scrollable
	 *     picker.
	 */
	public List<String> getScrollablePickerValues()
	{
		NacSharedConstants cons = this.getSharedConstants();
		return cons.getSnoozeDurationSummaries();
	}

	/**
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		this.setupSharedPreferences();

		NacSharedConstants cons = this.getSharedConstants();

		return new AlertDialog.Builder(requireContext())
			.setTitle(cons.getSnoozeDuration())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.callOnScrollablePickerOptionSelectedListener())
			.setNegativeButton(cons.getActionCancel(), (dialog, which) -> {})
			.setView(R.layout.dlg_scrollable_picker)
			.create();
	}

}
