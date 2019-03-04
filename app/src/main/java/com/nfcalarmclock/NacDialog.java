package com.nfcalarmclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief A generic dialog object.
 */
public class NacDialog
	implements DialogInterface.OnClickListener,DialogInterface.OnCancelListener,View.OnLayoutChangeListener
	//implements DialogInterface.OnClickListener,View.OnClickListener,DialogInterface.OnCancelListener,View.OnLayoutChangeListener
{

	/**
	 * Build listener.
	 */
	public interface OnBuildListener
	{
		public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder);
	}

	/**
	 * Cancel listener.
	 */
	public interface OnCancelListener
	{
		public boolean onCancelDialog(NacDialog dialog);
	}

	/**
	 * Dismiss listener.
	 */
	public interface OnDismissListener
	{
		public boolean onDismissDialog(NacDialog dialog);
	}

	/**
	 * Hide listener.
	 */
	public interface OnHideListener
	{
		public boolean onHideDialog(NacDialog dialog);
	}

	/**
	 * Neutral action listener.
	 */
	public interface OnNeutralActionListener
	{
		public boolean onNeutralActionDialog(NacDialog dialog);
	}

	/**
	 * Show listener.
	 */
	public interface OnShowListener
	{
		public void onShowDialog(NacDialog dialog, View root);
	}

	/**
	 * Scaling structure.
	 */
	public class Scaler
	{

		/**
		 * Scale the screen height by some fraction (0 <= x <= 1.0).
		 */
		private double mHeightScale;

		/**
		 * Scale the screen width by some fraction (0 <= x <= 1.0).
		 */
		private double mWidthScale;

		/**
		 * Wrap height content.
		 */
		private boolean mWrapHeight;

		/**
		 * Wrap width content.
		 */
		private boolean mWrapWidth;

		/**
		 */
		public Scaler()
		{
			this.mHeightScale = 1.0;
			this.mWidthScale = 1.0;
			this.mWrapHeight = true;
			this.mWrapWidth = true;
		}

		/**
		 * @return The fraction to scale the height by.
		 */
		public double getHeightScale()
		{
			return this.mHeightScale;
		}

		/**
		 * @return The fraction to scale the width by.
		 */
		public double getWidthScale()
		{
			return this.mWidthScale;
		}

		/**
		 * Set height scale.
		 */
		public void setHeightScale(double scale, boolean wrap)
		{
			this.mHeightScale = scale;
			this.mWrapHeight = wrap;
		}

		/**
		 * Set width scale.
		 */
		public void setWidthScale(double scale, boolean wrap)
		{
			this.mWidthScale = scale;
			this.mWrapWidth = wrap;
		}

		/**
		 * @return True if the height should wrap to content, and False
		 * otherwise.
		 */
		public boolean wrapHeight()
		{
			return this.mWrapHeight;
		}

		/**
		 * @return True if the width should wrap to content, and False
		 * otherwise.
		 */
		public boolean wrapWidth()
		{
			return this.mWrapWidth;
		}

	}

	/**
	 * Alert dialog builder.
	 */
	private AlertDialog.Builder mBuilder;

	/**
	 * Saved data.
	 */
	private Object mData;

	/**
	 * Alert dialog.
	 */
	private AlertDialog mDialog;

	/**
	 * Root view.
	 */
	private View mRoot;

	/**
	 * The dialog listeners.
	 */
	private OnBuildListener mBuildListener;
	private List<OnCancelListener> mCanceledListener;
	private List<OnDismissListener> mDismissedListener;
	private List<OnHideListener> mHiddenListener;
	private List<OnNeutralActionListener> mNeutralActionListener;
	private OnShowListener mShowListener;

	/**
	 * Dialog scaler.
	 */
	private Scaler mScaler;

	/**
	 */
	public NacDialog()
	{
		this.mBuilder = null;
		this.mData = null;
		this.mDialog = null;
		this.mRoot = null;
		this.mBuildListener = null;
		this.mCanceledListener = new ArrayList<>();
		this.mDismissedListener = new ArrayList<>();
		this.mHiddenListener = new ArrayList<>();
		this.mNeutralActionListener = new ArrayList<>();
		this.mShowListener = null;
		this.mScaler = null;
	}

	/**
	 * Add a cancel listener. More than one can be added, and they will be
	 * run in the order that they are added.
	 */
	public void addCancelListener(OnCancelListener listener)
	{
		this.mCanceledListener.add(listener);
	}

	/**
	 * Add a dismiss listener. More than one can be added, and they will be
	 * run in the order that they are added.
	 */
	public void addDismissListener(OnDismissListener listener)
	{
		this.mDismissedListener.add(listener);
	}

	/**
	 * Add a hidden listener. More than one can be added, and they will be
	 * run in the order that they are added.
	 */
	public void addHiddenListener(OnHideListener listener)
	{
		this.mHiddenListener.add(listener);
	}

	/**
	 * Add a neutral action listener. More than one can be added, and they will
	 * be run in the order that they are added.
	 */
	public void addNeutralActionListener(OnNeutralActionListener listener)
	{
		this.mNeutralActionListener.add(listener);
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

		root.addOnLayoutChangeListener(this);

		this.removeParent(root);
		this.mBuilder.setView(root);
		this.mBuilder.setOnCancelListener(this);
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
	 * Cancel the dialog and call the onCancelDialog listener.
	 */
	public void cancel()
	{
		if (this.mDialog == null)
		{
			return;
		}

		for (OnCancelListener listener : this.mCanceledListener)
		{
			if (!listener.onCancelDialog(this))
			{
				return;
			}
		}

		this.mDialog.cancel();
	}

	/**
	 * Dismiss the dialog and call the onDismissDialog listener.
	 */
	public void dismiss()
	{
		if (this.mDialog == null)
		{
			return;
		}

		for (OnDismissListener listener : this.mDismissedListener)
		{
			if (!listener.onDismissDialog(this))
			{
				return;
			}
		}

		this.mDialog.dismiss();
	}

	/**
	 * @return The saved data.
	 */
	public Object getData()
	{
		return this.mData;
	}

	/**
	 * @return The root view.
	 */
	public View getRootView()
	{
		return this.mRoot;
	}

	/**
	 * Hide the dialog and call the onHideDialog listener.
	 */
	public void hide()
	{
		if (this.mDialog == null)
		{
			return;
		}

		for (OnHideListener listener : this.mHiddenListener)
		{
			if (!listener.onHideDialog(this))
			{
				return;
			}
		}

		this.mDialog.hide();
	}

	/**
	 * Neutral action on the dialog and call the onActionDialog listener.
	 */
	public void neutral()
	{
		if (this.mDialog == null)
		{
			return;
		}

		for (OnNeutralActionListener listener : this.mNeutralActionListener)
		{
			if (!listener.onNeutralActionDialog(this))
			{
				return;
			}
		}
	}

	/**
	 * Called when the dialog is being built.
	 *
	 * This will typically be overriden by the user.
	 */
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		if (this.mBuildListener != null)
		{
			this.mBuildListener.onBuildDialog(this, builder);
		}
	}

	@Override
	public void onCancel(DialogInterface dialog)
	{
		this.cancel();
	}

	/**
	 * Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	//@Override
	//public void onClick(View view)
	//{
	//	int tag = (int) view.getTag(R.id.nac_dialog_button_tag_key);

	//	switch (tag)
	//	{
	//	case AlertDialog.BUTTON_POSITIVE:
	//		NacUtility.printf("new onClick! Dismiss");
	//		this.dismiss();
	//		break;
	//	case AlertDialog.BUTTON_NEUTRAL:
	//		NacUtility.printf("new onClick! Neutral");
	//		this.neutral();
	//		break;
	//	case AlertDialog.BUTTON_NEGATIVE:
	//	default:
	//		NacUtility.printf("new onClick! Cancel");
	//		this.cancel();
	//		break;
	//	}
	//}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		switch (which)
		{
		case DialogInterface.BUTTON_POSITIVE:
			NacUtility.printf("onClick! Dismiss");
			this.dismiss();
			break;
		case DialogInterface.BUTTON_NEUTRAL:
			NacUtility.printf("onClick! Neutral");
			this.neutral();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
		default:
			NacUtility.printf("onClick! Cancel");
			this.cancel();
			break;
		}
	}

	/**
	 * Scale the dialog's width and height.
	 */
	@Override
	public void onLayoutChange(View v, int left, int top, int right,
		int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
	{
		if (this.mScaler == null)
		{
			return;
		}

		Context context = this.mDialog.getContext();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int dialogWidth = (int)(dm.widthPixels * this.mScaler.getWidthScale());
		int dialogHeight = (int)(dm.heightPixels * this.mScaler.getHeightScale());

		if (this.mScaler.wrapWidth() || this.mScaler.wrapHeight())
		{
			this.mRoot.invalidate();
			this.mRoot.requestLayout();
			this.mRoot.measure(MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED));

			if (this.mScaler.wrapWidth() && (this.mRoot.getWidth() <= dialogWidth))
			{
				dialogWidth = LayoutParams.WRAP_CONTENT;
			}

			if (this.mScaler.wrapHeight() && (this.mRoot.getHeight() <= dialogHeight))
			{
				dialogHeight = LayoutParams.WRAP_CONTENT;
			}
		}

		this.mDialog.getWindow().setLayout(dialogWidth, dialogHeight);
	}

	/**
	 * Called when the dialog is being shown.
	 *
	 * This will typically be overriden by the user.
	 */
	public void onShowDialog(Context context, View root)
	{
		if (this.mShowListener != null)
		{
			this.mShowListener.onShowDialog(this, root);
		}
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
	 * Save data.
	 *
	 * @param  data  Data to save.
	 */
	public void saveData(Object data)
	{
		this.mData = data;
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

		this.mScaler = new Scaler();

		this.mScaler.setWidthScale(width, wrapWidth);
		this.mScaler.setHeightScale(height, wrapHeight);
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
	 * Setup dialog colors and listeners.
	 */
	public void setupDialog()
	{
		Context context = this.mRoot.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int[] buttonTypes = new int[] { AlertDialog.BUTTON_NEGATIVE,
			AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEUTRAL };
		Button button;

		for (int i=0; i < 3; i++)
		{
			button = this.mDialog.getButton(buttonTypes[i]);

			if (button == null)
			{
				continue;
			}

			button.setTextColor(shared.themeColor);

			//button.setTag(R.id.nac_dialog_button_tag_key, buttonTypes[i]);
			if (buttonTypes[i] == AlertDialog.BUTTON_NEUTRAL)
			{
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						neutral();
					}
				});
			}
		}

		this.mDialog.getWindow().setBackgroundDrawableResource(R.color.gray);
	}

	/**
	 * Set the negative button which will call onCancelDialog when clicked.
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
	 * Set the neutral button which will call onActionDialog when clicked.
	 */
	public void setNeutralButton(String title)
	{
		if (this.mBuilder == null)
		{
			return;
		}

		this.mBuilder.setNeutralButton(title, this);
	}

	/**
	 * Set the onBuild listener.
	 */
	public void setOnBuildListener(OnBuildListener listener)
	{
		this.mBuildListener = listener;
	}

	/**
	 * Set the onShow listener.
	 */
	public void setOnShowListener(OnShowListener listener)
	{
		this.mShowListener = listener;
	}

	/**
	 * Set the positive button which will call onDismissDialog when clicked.
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
		this.setupDialog();

		//Button neutralButton = this.mDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

		//if (neutralButton != null)
		//{
		//	neutralButton.setOnClickListener(this);
		//}

		return this.mDialog;
	}

}
