package com.nfcalarmclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
	public interface OnCanceledListener
	{
		public void onDialogCanceled(NacDialog dialog);
	}

	public interface OnDismissedListener
	{
		public void onDialogDismissed(NacDialog dialog);
	}

	public interface OnHiddenListener
	{
		public void onDialogHidden(NacDialog dialog);
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
	private List<OnCanceledListener> mCanceledListener;
	private List<OnDismissedListener> mDismissedListener;
	private List<OnHiddenListener> mHiddenListener;

	/**
	 */
	public NacDialog()
	{
		this.mBuilder = null;
		this.mDialog = null;
		this.mRoot = null;
		this.mCanceledListener = new ArrayList<>();
		this.mDismissedListener = new ArrayList<>();
		this.mHiddenListener = new ArrayList<>();
	}

	/**
	 * Add a cancel listener. More than one can be added, and they will be
	 * run in the order that they are added.
	 */
	public void addCancelListener(OnCanceledListener listener)
	{
		this.mCanceledListener.add(listener);
	}

	/**
	 * Add a dismiss listener. More than one can be added, and they will be
	 * run in the order that they are added.
	 */
	public void addDismissListener(OnDismissedListener listener)
	{
		this.mDismissedListener.add(listener);
	}

	/**
	 * Add a hidden listener. More than one can be added, and they will be
	 * run in the order that they are added.
	 */
	public void addHiddenListener(OnHiddenListener listener)
	{
		this.mHiddenListener.add(listener);
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
		this.onBuildDialog(context, this.mBuilder);

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
	 * Cancel the dialog and call the onDialogCanceled listener.
	 */
	public void cancel()
	{
		if (this.mDialog == null)
		{
			return;
		}

		for (OnCanceledListener listener : this.mCanceledListener)
		{
			listener.onDialogCanceled(this);
		}

		this.mDialog.cancel();
	}

	/**
	 * Dismiss the dialog and call the onDialogDismissed listener.
	 */
	public void dismiss()
	{
		if (this.mDialog == null)
		{
			return;
		}

		for (OnDismissedListener listener : this.mDismissedListener)
		{
			listener.onDialogDismissed(this);
		}

		this.mDialog.dismiss();
	}

	/**
	 * @return The root view.
	 */
	public View getRoot()
	{
		return this.mRoot;
	}

	/**
	 * Hide the dialog and call the onDialogHidden listener.
	 */
	public void hide()
	{
		if (this.mDialog == null)
		{
			return;
		}

		for (OnHiddenListener listener : this.mHiddenListener)
		{
			listener.onDialogHidden(this);
		}

		this.mDialog.hide();
	}

	/**
	 * Called when the dialog is being built.
	 *
	 * This will typically be overriden by the user.
	 */
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
	}

	/**
	 * Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		switch (which)
		{
		case DialogInterface.BUTTON_POSITIVE:
			this.dismiss();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
		default:
			this.cancel();
			break;
		}
	}

	/**
	 * Called when the dialog is being shown.
	 *
	 * This will typically be overriden by the user.
	 */
	public void onShowDialog(Context context, View root)
	{
	}

	/**
	 * Remove the parent from the view that will be attached to the dialog.
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
	 * Scale the dialog size.
	 *
	 * @param  width  Width. Value must be 0 <= w <= 1.
	 * @param  height  Height. Value must be 0 <= h <= 1.
	 * @param  wrapWidth  Wrap the width in the event that the dialog is
	 *                    scaled too big.
	 * @param  wrapHeight  Wrap the height in the event that the dialog is
	 *                     scaled too big.
	 */
	public void scale(double width, double height, boolean wrapWidth, boolean wrapHeight)
	{
		if ((this.mBuilder == null) || (this.mDialog == null)
			|| (width < 0) || (width > 1) || (height < 0) || (height > 1))
		{
			return;
		}

		Context context = this.mDialog.getContext();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int dialogwidth = (int)(dm.widthPixels * width);
		int dialogheight = (int)(dm.heightPixels * height);

		if (wrapWidth || wrapHeight)
		{
			View view = this.mRoot.getRootView();
			view.requestLayout();
			view.invalidate();
			view.measure(MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED));

			int viewwidth = view.getMeasuredWidth();
			int viewheight = view.getMeasuredHeight();

			//NacUtility.printf("View Size : %d x %d",
			//	viewwidth, viewheight);

			if (wrapWidth && (viewwidth < dialogwidth))
			{
				dialogwidth = viewwidth;
			}

			if (wrapHeight && (viewheight < dialogheight))
			{
				dialogheight = viewheight;
			}
		}

		//NacUtility.printf("Final Dialog Size : %d x %d",
		//	dialogwidth, dialogheight);

		this.mDialog.getWindow().setLayout(dialogwidth, dialogheight);
	}

	/**
	 * Scale the dialog size.
	 *
	 * @param  w  Width. Value must be 0 <= w <= 1.
	 * @param  h  Height. Value must be 0 <= h <= 1.
	 */
	public void scale(double w, double h)
	{
		this.scale(w, h, false, false);
	}

	/**
	 * Set the negative button which will call onDialogCanceled when clicked.
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
	 * Set the positive button which will call onDialogDismissed when clicked.
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
	 * Show the dialog.
	 */
	public AlertDialog show()
	{
		Context context = this.mRoot.getContext();

		if (this.mDialog != null)
		{
			this.mDialog.show();
		}
		else
		{
			this.mDialog = this.mBuilder.show();
		}

		this.onShowDialog(context, this.mRoot);

		return this.mDialog;
	}

}
