package com.nfcalarmclock.permission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.view.dialog.NacDialogFragment;
import java.util.Locale;

/**
 * Generic dialog for requesting permissions.
 */
public abstract class NacPermissionRequestDialog
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
	 * Position of this dialog in the permission request manager.
	 */
	private int mPosition;

	/**
	 * Total number of pages in the permission request manager.
	 */
	private int mTotalNumberOfPages;

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
		String permission = this.getPermission();

		// Call the listener
		if (listener != null)
		{
			listener.onPermissionRequestAccepted(permission);
		}
	}

	/**
	 * Call the *Cancel() method for the OnPermissionRequestListener object, if it
	 * has been set.
	 */
	public void callOnPermissionRequestCanceledListener()
	{
		OnPermissionRequestListener listener = this.getOnPermissionRequestListener();
		String permission = this.getPermission();

		// Call the listener
		if (listener != null)
		{
			listener.onPermissionRequestCanceled(permission);
		}
	}

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	protected void doPermissionRequestAccepted()
	{
		// Call the accepeted listeners
		this.callOnPermissionRequestAcceptedListener();
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	protected void doPermissionRequestCanceled()
	{
		// Call the canceled listeners
		this.callOnPermissionRequestCanceledListener();
	}

	/**
	 * Get the ID of the layout.
	 *
	 * @return The ID of the layout.
	 */
	public abstract int getLayoutId();

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
	 * Get the name of the permission.
	 *
	 * @return The name of the permission.
	 */
	public String getPermission()
	{
		return "";
	}

	/**
	 * Get the position of the dialog in the permission request manager.
	 *
	 * @return The position of the dialog in the permission request manager.
	 */
	public int getPosition()
	{
		return this.mPosition;
	}

	/**
	 * Get the ID of the title string.
	 *
	 * @return The ID of the title string.
	 */
	public abstract int getTitleId();

	/**
	 * Get the total number of pages in the permission request manager.
	 *
	 * @return The total number of pages in the permission request manager.
	 */
	public int getTotalNumberOfPages()
	{
		return this.mTotalNumberOfPages;
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
		// Setup the shared preferences
		this.setupSharedPreferences();

		NacSharedConstants cons = this.getSharedConstants();
		int titleId = this.getTitleId();
		int layoutId = this.getLayoutId();

		// Build the dialog
		return new AlertDialog.Builder(requireContext())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.doPermissionRequestAccepted())
			.setNegativeButton(cons.getActionCancel(), (dialog, which) ->
				this.doPermissionRequestCanceled())
			.setTitle(titleId)
			.setView(layoutId)
			.create();
	}

	/**
	 * Called when the view is created.
	 * <p>
	 * This is called right after onCreateDialog().
	 */
	@Override
	public void onStart()
	{
		super.onStart();

		// Setup the page information
		this.setupPageInfo();
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

	/**
	 * Set the position of the dialog in the permission request manager.
	 *
	 * @param position Position of the dialog.
	 */
	public void setPosition(int position)
	{
		this.mPosition = position;
	}

	/**
	 * Set the total number of pages in the permission request manager.
	 *
	 * @param numberOfPages Total number of pages.
	 */
	public void setTotalNumberOfPages(int numberOfPages)
	{
		this.mTotalNumberOfPages = numberOfPages;
	}

	/**
	 * Set the page information in the dialog.
	 */
	public void setupPageInfo()
	{
		Dialog view = getDialog();
		View separator = view.findViewById(R.id.request_separator);
		View pages = view.findViewById(R.id.request_pages);
		int position = this.getPosition();
		int totalNumOfPages = this.getTotalNumberOfPages();

		// Show page information
		if (totalNumOfPages > 1)
		{
			// Make the separate and pages visible
			separator.setVisibility(View.VISIBLE);
			pages.setVisibility(View.VISIBLE);

			// Get the textviews that need to be modified
			Locale locale = Locale.getDefault();
			TextView positionTextView = view.findViewById(R.id.request_current_page);
			TextView totalNumTextView = view.findViewById(R.id.request_total_num_pages);

			// Set the position and total number of pages
			positionTextView.setText(String.format(locale, "%d ", position));
			totalNumTextView.setText(String.format(locale, " %d", totalNumOfPages));
		}
		// Hide page information
		else
		{
			// Make the separate and pages disappear
			separator.setVisibility(View.GONE);
			pages.setVisibility(View.GONE);
		}
	}

}
