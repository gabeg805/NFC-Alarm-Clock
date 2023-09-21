package com.nfcalarmclock.autodismiss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.R;

import java.util.List;

/**
 * Select an amount of time to auto dismiss an alarm.
 */
public class NacAutoDismissDialog
	extends NacScrollablePickerDialogFragment
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAutoDismissDialog";

	/**
	 * Get the list of values for the scrollable picker.
	 *
	 * @return The list of values for the scrollable picker for the scrollable
	 *     picker.
	 */
	public List<String> getScrollablePickerValues()
	{
		NacSharedConstants cons = this.getSharedConstants();
		return cons.getAutoDismissSummaries();
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
			.setTitle(cons.getAutoDismiss())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.callOnScrollablePickerOptionSelectedListener())
			.setNegativeButton(cons.getActionCancel(), (dialog, which) -> {})
			.setView(R.layout.dlg_scrollable_picker)
			.create();
	}

}
