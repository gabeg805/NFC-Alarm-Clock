package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The dialog class that will handle saving the name of the alarm.
 */
public class NacNameDialog
	extends NacDialog
	implements NacDialog.OnDismissListener,TextView.OnEditorActionListener
{

	/**
	 * EditText in the dialog.
	 */
	private EditText mEditText;

	/**
	 */
	public NacNameDialog()
	{
		super();

		this.mEditText = null;

		this.addDismissListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = "Set Alarm Name";

		builder.setTitle(title);
		this.setPositiveButton("OK");
		this.setNegativeButton("Cancel");
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(Context context, View root)
	{
		this.mEditText = (EditText) root.findViewById(R.id.alarm_name);
		Object data = this.getData();
		String name = (data != null) ? (String) data : "";

		this.mEditText.setText(name);
		this.mEditText.selectAll();
		this.mEditText.setOnEditorActionListener(this);
		this.mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		this.scale(0.8, 0.5, false, true);
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
			closeKeyboard(tv);
			this.dismiss();

			return true;
		}

		return false;
	}

	/**
	 * Close the keyboard.
	 */
	private void closeKeyboard(TextView tv)
	{
		this.mEditText.clearFocus();

		Context context = tv.getContext();
		String name = Context.INPUT_METHOD_SERVICE;
		int flags = InputMethodManager.RESULT_UNCHANGED_SHOWN;
		Object service = context.getSystemService(name);
		InputMethodManager manager = (InputMethodManager) service;

		manager.hideSoftInputFromWindow(tv.getWindowToken(), flags);
	}

}
