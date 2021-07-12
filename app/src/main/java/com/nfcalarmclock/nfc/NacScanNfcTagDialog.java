package com.nfcalarmclock.nfc;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.nfcalarmclock.util.dialog.NacDialog;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.R;

/**
 * Prompt user to scan an NFC tag that will be used to dismiss the given alarm
 * when it goes off.
 */
public class NacScanNfcTagDialog
	extends NacDialog
	implements NacDialog.OnShowListener
{

	/**
	 */
	public NacScanNfcTagDialog()
	{
		super(R.layout.dlg_scan_nfc_tag);
		addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getTitleScanNfcTag());
		setPositiveButton(cons.getActionUseAny());
		setNegativeButton(cons.getActionCancel());
	}

	/**
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		scale(0.85, 0.8, false, true);
	}

}
