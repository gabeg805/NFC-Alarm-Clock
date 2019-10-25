package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The dialog class to handle prompting the user for permissions, or simply
 * showing a list of permissions.
 */
public class NacPermissionsDrawOverlayDialog
	extends NacDialog
	implements NacDialog.OnNeutralActionListener,
		NacDialog.OnShowListener
{

	/**
	 */
	public NacPermissionsDrawOverlayDialog()
	{
		super();

		addOnNeutralActionListener(this);
		addOnShowListener(this);
	}

	/**
	 * Add row to permissions list.
	 */
	//@SuppressWarnings("deprecation")
	//@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
	//public void add(Context context, int drawableId, int stringId)
	//{
	//	Resources res = context.getResources();
	//	String string = res.getString(stringId);
	//	Drawable drawable = null;

	//	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
	//	{
	//		Resources.Theme theme = context.getTheme();
	//		drawable = res.getDrawable(drawableId, theme);
	//	}
	//	else
	//	{
	//		drawable = res.getDrawable(drawableId);
	//	}

	//	this.add(context, drawable, string);
	//}

	//public void add(Context context, Drawable drawable, String message)
	//{
	//	LinearLayout parent = this.getParent();

	//	if (parent == null)
	//	{
	//		return;
	//	}

	//	TextView view = new TextView(context);

	//	view.setText(message);
	//	view.setCompoundDrawablePadding(60);
	//	view.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
	//	parent.addView(view);
	//}

	/**
	 * Build the dialog.
	 */
	public void build(Context context)
	{
		this.build(context, R.layout.dlg_permission_draw_overlay);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		setPositiveButton("Yes");
		setNegativeButton("No");
		setNeutralButton("Learn More");

	}

	/**
	 */
	@Override
	public boolean onNeutralActionDialog(NacDialog dialog)
	{
		Context context = dialog.getRoot().getContext();
		NacPermissionsDrawOverlayExplanationDialog learnMoreDialog =
			new NacPermissionsDrawOverlayExplanationDialog();

		learnMoreDialog.build(context);
		learnMoreDialog.show();


		return true;
	}

	/**
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = root.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		ImageView icon = root.findViewById(R.id.overlay_icon);

		icon.setColorFilter(shared.getThemeColor(), PorterDuff.Mode.SRC_IN);
	}

}
