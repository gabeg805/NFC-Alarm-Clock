package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The dialog class to handle prompting the user for permissions, or simply
 * showing a list of permissions.
 */
public class NacPermissionsDrawOverlayExplanationDialog
	extends NacDialog
{

	/**
	 */
	public NacPermissionsDrawOverlayExplanationDialog()
	{
		super();
	}

	/**
	 * Build the dialog.
	 */
	public void build(Context context)
	{
		this.build(context, R.layout.dlg_permission_draw_overlay_explanation);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		setPositiveButton("Close");

	}

}
