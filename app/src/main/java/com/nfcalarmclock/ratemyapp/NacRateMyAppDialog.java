package com.nfcalarmclock.ratemyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.nfcalarmclock.util.dialog.NacDialog;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * The dialog class to handle prompting the user for permissions, or simply
 * showing a list of permissions.
 */
public class NacRateMyAppDialog
	extends NacDialog
	implements NacDialog.OnShowListener,
		NacDialog.OnDismissListener,
		NacDialog.OnCancelListener,
		NacDialog.OnNeutralActionListener
		
{

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mShared;

	/**
	 */
	public NacRateMyAppDialog()
	{
		super(R.layout.dlg_rate_my_app);
		addOnShowListener(this);
		addOnDismissListener(this);
		addOnCancelListener(this);
		addOnNeutralActionListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public AlertDialog.Builder build(Context context)
	{
		this.mShared = new NacSharedPreferences(context);
		return super.build(context);
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mShared;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getTitleRateMyApp());
		setPositiveButton(cons.getActionRateNow());
		setNegativeButton(cons.getActionRateLater());
		setNeutralButton(cons.getActionRateNever());
	}

	/**
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		shared.resetRateMyApp();
		return true;
	}

	/**
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		Context context = getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		Uri uri = Uri.parse("market://details?id=com.nfcalarmclock");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);

		shared.ratedRateMyApp();
		context.startActivity(intent);
		return true;
	}

	/**
	 */
	@Override
	public boolean onNeutralActionDialog(NacDialog dialog)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		shared.postponeRateMyApp();
		dialog.neutralDismiss();
		return true;
	}

	/**
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		scale(0.9, 0.7, false, true);
	}

}
