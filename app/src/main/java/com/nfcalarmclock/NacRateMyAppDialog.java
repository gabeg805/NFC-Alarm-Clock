package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * The dialog class to handle prompting the user for permissions, or simply
 * showing a list of permissions.
 */
public class NacRateMyAppDialog
	extends NacDialog
	implements NacDialog.OnDismissListener,
		NacDialog.OnCancelListener,
		NacDialog.OnNeutralActionListener
		
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mShared;

	/**
	 */
	public NacRateMyAppDialog(Context context)
	{
		super();

		this.mContext = context;
		this.mShared = new NacSharedPreferences(context);

		addOnDismissListener(this);
		addOnCancelListener(this);
		addOnNeutralActionListener(this);
	}

	/**
	 * Build the dialog.
	 */
	public void build()
	{
		Context context = this.getContext();

		this.build(context, R.layout.dlg_rate_my_app);
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
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
		String title = "Rate my app";

		builder.setTitle(title);
		setPositiveButton("Rate");
		setNegativeButton("Later");
		setNeutralButton("Never");

	}

	/**
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		shared.editRateMyAppCounter(0);

		return true;
	}

	/**
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		Context context = this.getContext();
		Uri uri = Uri.parse("market://details?id=com.nfcalarmclock");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);

		shared.editRateMyAppCounter(-1);
		context.startActivity(intent);

		return true;
	}

	/**
	 */
	@Override
	public boolean onNeutralActionDialog(NacDialog dialog)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		shared.editRateMyAppCounter(-1);

		return true;
	}

}
