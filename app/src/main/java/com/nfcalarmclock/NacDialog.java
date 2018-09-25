package com.nfcalarmclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.DisplayMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief A generic dialog object.
 */
public class NacDialog
	implements DialogInterface.OnClickListener
{

	/**
	 */
	public interface OnDismissedListener
	{
		public void onDialogDismissed();
	}

	public interface OnCanceledListener
	{
		public void onDialogCanceled();
	}

	/**
	 * Alert dialog builder.
	 */
	private AlertDialog.Builder mBuilder;

	/**
	 * Alert dialog.
	 */
	private AlertDialog mDialog;

	/**
	 * Root view.
	 */
	private View mRoot;

	/**
	 * The dialog listener.
	 */
	private List<OnDismissedListener> mDismissListener;
	private List<OnCanceledListener> mCancelListener;

	/**
	 */
	public NacDialog()
	{
		this.mBuilder = null;
		this.mDialog = null;
		this.mRoot = null;
		this.mDismissListener = new ArrayList<>();
		this.mCancelListener = new ArrayList<>();
	}

	/**
	 * Show the dialog.
	 */
	public AlertDialog show()
	{
		this.mDialog = this.mBuilder.show();

		return this.mDialog;
	}

	public void onBuildDialog(AlertDialog.Builder builder, View root)
	{
		NacUtility.printf("NacDialog onInflated!");
	}

	/**
	 * Build the dialog.
	 *
	 * @param  title  The title of the dialog.
	 * @param  root  The view to attach to the dialog.
	 *
	 * @return The dialog builder.
	 */
	public AlertDialog.Builder build(Context context, View root)
	{
		this.mBuilder = new AlertDialog.Builder(context);
		this.mRoot = root;

		this.removeParent(root);
		this.mBuilder.setView(root);
		this.onBuildDialog(this.mBuilder, mRoot);

		return this.mBuilder;
	}

	/**
	 * Build the dialog with a layout id.
	 *
	 * @see build.
	 */
	public AlertDialog.Builder build(Context context, int id)
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		this.mRoot = inflater.inflate(id, (ViewGroup)null);

		return build(context, this.mRoot);
	}

	/**
	 */
	public void setPositiveButton(String title)
	{
		if (this.mBuilder == null)
		{
			return;
		}

		this.mBuilder.setPositiveButton(title, this);
	}

	/**
	 */
	public void setNegativeButton(String title)
	{
		if (this.mBuilder == null)
		{
			return;
		}

		this.mBuilder.setNegativeButton(title, this);
	}

	/**
	 */
	public void addDismissListener(OnDismissedListener listener)
	{
		this.mDismissListener.add(listener);
	}

	/**
	 */
	public void addCancelListener(OnCanceledListener listener)
	{
		this.mCancelListener.add(listener);
	}

	public View getRoot()
	{
		return this.mRoot;
	}

	public void dismiss()
	{
		if (this.mDialog == null)
		{
			return;
		}

		this.onClick(this.mDialog, DialogInterface.BUTTON_POSITIVE);
	}

	/**
	 * @brief Scale the dialog size.
	 *
	 * @param  w  Width. Value must be 0 <= w <= 1.
	 * @param  h  Height. Value must be 0 <= h <= 1.
	 */
	public void scale(double w, double h)
	{
		if ((this.mBuilder == null) || (this.mDialog == null)
			|| (w < 0) || (w > 1) || (h < 0) || (h > 1))
		{
			return;
		}

		Activity a = (Activity) this.mBuilder.getContext();
		DisplayMetrics dm = a.getResources().getDisplayMetrics();
		int width = (int)(dm.widthPixels * w);
		int height = (int)(dm.heightPixels * h);

		this.mDialog.getWindow().setLayout(width, height);
	}

	/**
	 * @brief Remove the parent from the view that will be attached to the
	 *        dialog.
	 */
	private void removeParent(View v)
	{
		ViewGroup parent = (ViewGroup) v.getParent();

		if (parent != null)
		{
			parent.removeView(v);
		}
	}

	/**
	 * @brief Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		NacUtility.printf("Dialog clicked.");

		switch (which)
		{
		case DialogInterface.BUTTON_POSITIVE:
			for (OnDismissedListener listener : this.mDismissListener)
			{
				listener.onDialogDismissed();
			}

			dialog.dismiss();
			return;
		case DialogInterface.BUTTON_NEGATIVE:
		default:
			for (OnCanceledListener listener : this.mCancelListener)
			{
				listener.onDialogCanceled();
			}

			dialog.cancel();
			return;
		}
	}

}
