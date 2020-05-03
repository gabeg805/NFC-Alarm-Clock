package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The dialog class that will handle saving the name of the alarm.
 */
public class NacNameDialog
	extends NacDialog
	implements TextView.OnEditorActionListener,
		NacDialog.OnDismissListener,
		NacDialog.OnShowListener
{

	/**
	 * EditText in the dialog.
	 */
	private EditText mEditText;

	/**
	 */
	public NacNameDialog()
	{
		super(R.layout.dlg_alarm_name);

		this.mEditText = null;

		addOnDismissListener(this);
		addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getTitleName());
		setPositiveButton(cons.getActionOk());
		setNegativeButton(cons.getActionCancel());
	}

	/**
	 * Save the alarm name as the dialog data.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		String name = this.mEditText.getText().toString();
		dialog.saveData(name);
		return true;
	}

	/**
	 * Close the keyboard when the user hits enter.
	 */
	@Override
	public boolean onEditorAction(TextView tv, int action, KeyEvent event)
	{
		if ((event == null) && (action == EditorInfo.IME_ACTION_DONE))
		{
			dismiss();
			return true;
		}
		return false;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = root.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		String name = this.getDataString();
		this.mEditText = (EditText) root.findViewById(R.id.alarm_name);

		this.mEditText.setText(name);
		this.mEditText.selectAll();
		this.mEditText.setOnEditorActionListener(this);
		this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		this.mEditText.setBackgroundTintList(ColorStateList.valueOf(shared.getThemeColor()));
		showKeyboard();
		scale(0.9, 0.5, false, true);
	}

}
