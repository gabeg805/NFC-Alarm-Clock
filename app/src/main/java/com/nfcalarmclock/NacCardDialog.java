package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.DisplayMetrics;

public class NacCardDialog
	implements DialogInterface.OnClickListener
{

	/**
	 * @brief Context.
	 */
	private Context mContext = null;

	/**
	 * @brief Alert dialog.
	 */
	private AlertDialog mDialog = null;

	/**
	 * @brief Indicate whether the dialog was canceled or not.
	 */
	private boolean mWasCanceled = false;

	/**
	 */
	public NacCardDialog(Context c)
	{
		this.mContext = c;
	}

	/**
	 * @brief Inflate the layout.
	 */
	public View inflate(int id)
	{
		return this.inflate(id, null);
	}

	/**
	 * @see infalte().
	 */
	public View inflate(int id, ViewGroup root)
	{
		LayoutInflater inflater = LayoutInflater.from(this.mContext);
		return inflater.inflate(id, root);
	}

	/**
	 * @brief Build the dialog.
	 *
	 * @param  v  View to attach to the dialog.
	 * @param  title  The title of the dialog.
	 * @param  positive Positive button onClick listener.
	 * @param  negative  Negative button onClick listener.
	 *
	 * @return The alert dialog.
	 */
	public AlertDialog build(View v, String title,
		DialogInterface.OnClickListener positive,
		DialogInterface.OnClickListener negative)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		this.mWasCanceled = false;

		this.removeParent(v);
		builder.setView(v);
		builder.setTitle(title);
		builder.setCancelable(true);

		if (positive != null)
		{
			builder.setPositiveButton("OK", positive);
		}

		if (negative != null)
		{
			builder.setNegativeButton("Cancel", negative);
		}

		return (this.mDialog = builder.show());
	}

	/**
	 * @see AlertDialog()
	 */
	public AlertDialog build(View v, String title, boolean setpositive,
		boolean setnegative)
	{
		DialogInterface.OnClickListener positive = null;
		DialogInterface.OnClickListener negative = null;

		if (setpositive)
		{
			positive = this;
		}

		if (setnegative)
		{
			negative = this;
		}

		return this.build(v, title, positive, negative);
	}

	/**
	 * @brief Scale the dialog size.
	 *
	 * @param  w  Width. Value must be 0 <= w <= 1.
	 * @param  h  Height. Value must be 0 <= h <= 1.
	 */
	public void scale(double w, double h)
	{
		if ((this.mContext == null) || (this.mDialog == null) || (w < 0)
			|| (w > 1) || (h < 0) || (h > 1))
		{
			return;
		}

		AppCompatActivity a = (AppCompatActivity) mContext;
		DisplayMetrics dm = a.getResources().getDisplayMetrics();
		int width = (int)(dm.widthPixels * w);
		int height = (int)(dm.heightPixels * h);

		this.mDialog.getWindow().setLayout(width, height);
	}

	/**
	 * @brief Dismiss the dialog.
	 */
	public void dismiss()
	{
		if (this.mDialog == null)
		{
			return;
		}

		this.mDialog.dismiss();
	}

	/**
	 * @brief Set the onDissmiss listener.
	 */
	public void setOnDismissListener(DialogInterface.OnDismissListener listener)
	{
		if (this.mDialog == null)
		{
			return;
		}

		this.mDialog.setOnDismissListener(listener);
	}

	/**
	 * @brief Set the onCancel listener.
	 */
	public void setOnCancelListener(DialogInterface.OnCancelListener listener)
	{
		if (this.mDialog == null)
		{
			return;
		}

		this.mDialog.setOnCancelListener(listener);
	}

	/**
	 * @brief Check if dialog was canceled.
	 */
	public boolean wasCanceled()
	{
		return this.mWasCanceled;
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
		switch (which)
		{
		case DialogInterface.BUTTON_POSITIVE:
			mWasCanceled = false;
			dialog.dismiss();
			return;
		case DialogInterface.BUTTON_NEGATIVE:
		default:
			mWasCanceled = true;
			dialog.cancel();
			return;
		}
	}

}
