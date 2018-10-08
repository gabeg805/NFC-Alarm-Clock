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
 * @brief The dialog class that will handle saving the name of the alarm.
 */
public class NacCardNameDialog
	extends NacDialog
	implements NacDialog.OnDismissedListener,TextView.OnEditorActionListener
{

	/**
	 * Alarm.
	 */
	private Alarm mAlarm;

	/**
	 * EditText in the dialog.
	 */
	private EditText mEditText;

	/**
	 */
	public NacCardNameDialog(Alarm a)
	{
		super();
		this.mAlarm = a;
		this.addDismissListener(this);
	}

	/**
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
		String name = this.mAlarm.getName();
		this.mEditText = (EditText) root.findViewById(R.id.alarm_name);

		this.mEditText.setText(name);
		this.mEditText.selectAll();
		this.mEditText.setOnEditorActionListener(this);
		this.mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
	}

	/**
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		String text = this.mEditText.getText().toString();

		NacUtility.printf("Name : %s", text);
		this.mAlarm.setName(text);
		this.mAlarm.changed();
	}

	/**
	 * @brief Close the keyboard when the user hits enter.
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
	 * @brief Close the keyboard.
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
