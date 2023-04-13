package com.nfcalarmclock.permission.scheduleexactalarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.dialog.NacDialogFragment;

/**
 */
@RequiresApi(api=Build.VERSION_CODES.S)
public class NacScheduleExactAlarmPermissionDialog
	extends NacDialogFragment
{

	/**
	 * Listener for when what's new dialog has been read.
	 */
	@SuppressWarnings("UnnecessaryInterfaceModifier")
	public interface OnPermissionRequestListener
	{
		public void onPermissionRequestAccepted(String permission);
		public void onPermissionRequestCanceled(String permission);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacScheduleExactAlarmPermissionDialog";

	/**
	 * Listener for when the permission request is done.
	 */
	private OnPermissionRequestListener mOnPermissionRequestListener;

	/**
	 * Call the *Done() method for the OnPermissionRequestListener object, if it
	 * has been set.
	 */
	public void callOnPermissionRequestAcceptedListener()
	{
		OnPermissionRequestListener listener = this.getOnPermissionRequestListener();

		if (listener != null)
		{
			listener.onPermissionRequestAccepted(
				Manifest.permission.SCHEDULE_EXACT_ALARM);
		}
	}

	/**
	 * Call the *Cancel() method for the OnPermissionRequestListener object, if it
	 * has been set.
	 */
	public void callOnPermissionRequestCanceledListener()
	{
		OnPermissionRequestListener listener = this.getOnPermissionRequestListener();

		if (listener != null)
		{
			listener.onPermissionRequestCanceled(
				Manifest.permission.SCHEDULE_EXACT_ALARM);
		}
	}

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	private void doPermissionRequestAccepted()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		// Set the flag that the permission was requested
		shared.editWasScheduleExactAlarmPermissionRequested(true);

		// Call the accepeted listeners
		this.callOnPermissionRequestAcceptedListener();
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	private void doPermissionRequestCanceled()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		// Set the flag that the permission was requested
		shared.editWasScheduleExactAlarmPermissionRequested(true);

		// Call the accepeted listeners
		this.callOnPermissionRequestCanceledListener();
	}

	/**
	 * Get the OnPermissionRequestListener object.
	 *
	 * @return The OnPermissionRequestListener object.
	 */
	public OnPermissionRequestListener getOnPermissionRequestListener()
	{
		return this.mOnPermissionRequestListener;
	}

	/**
	 * Called when the dialog is canceled.
	 */
	@Override
	public void onCancel(@NonNull DialogInterface dialog)
	{
		this.doPermissionRequestCanceled();
	}

	/**
	 * Called when the dialog is created.
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		this.setupSharedPreferences();

		NacSharedConstants cons = this.getSharedConstants();

		return new AlertDialog.Builder(requireContext())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.doPermissionRequestAccepted())
			.setNegativeButton(cons.getActionCancel(), (dialog, which) ->
				this.doPermissionRequestCanceled())
			.setView(R.layout.dlg_request_schedule_exact_alarm_permission)
			.create();
	}

	/**
	 * Set the OnPermissionRequestListener object.
	 *
	 * @param  listener  The OnPermissionRequestListener object.
	 */
	public void setOnPermissionRequestListener(OnPermissionRequestListener listener)
	{
		this.mOnPermissionRequestListener = listener;
	}

}
