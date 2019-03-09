package com.nfcalarmclock;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;

/**
 * Dialog to dismiss by tapping NFC tag.
 */
public class NacAlarmDialog
	extends NacDialog
	implements NacDialog.OnShowListener
{

	/**
	 */
	public NacAlarmDialog()
	{
		super();

		this.addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = "Dismiss Alarm";

		builder.setTitle(title);
		builder.setIcon(R.mipmap.ic_launcher);
		builder.setNegativeButton("Snooze", this);
		builder.setCancelable(false);
		//this.mDialog.setCanceledOnTouchOutside(false);
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		ImageView icon = (ImageView) root.findViewById(R.id.nac_nfc_icon);
		AlphaAnimation animation = new AlphaAnimation(0.1f, 1f);
		ViewGroup.LayoutParams params = icon.getLayoutParams();
		int duration = 2000;

		icon.setLayoutParams(params);
		animation.setDuration(duration);
		animation.setRepeatMode(ValueAnimator.REVERSE);
		animation.setRepeatCount(ValueAnimator.INFINITE);
		icon.startAnimation(animation);
	}

}
