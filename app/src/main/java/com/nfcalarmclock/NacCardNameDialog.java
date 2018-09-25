package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	private EditText mEditText = null;

	/**
	 */
	public NacCardNameDialog(Alarm a)
	{
		this.mAlarm = a;
		this.addDismissListener(this);
	}

	/**
	 */
	@Override
	public void onBuildDialog(AlertDialog.Builder builder, View root)
	{
		NacUtility.printf("NameDialog onInflated!");
		this.mEditText = (EditText) root.findViewById(R.id.alarm_name);

		builder.setTitle("Set Alarm Name");
		this.setPositiveButton("OK");
		this.setNegativeButton("Cancel");
		this.mEditText.setText(mAlarm.getName());
		this.mEditText.selectAll();
		this.mEditText.setOnEditorActionListener(this);
		this.mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
	}

	/**
	 */
	@Override
	public void onDialogDismissed()
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
	public boolean onEditorAction(TextView tv, int actionId,
								  KeyEvent event)
	{
		if ((event == null) && (action == EditorInfo.IME_ACTION_DONE))
		{
			closeKeyboard(tv);
			this.dismiss();
			return true;
		}

		return false;

		//if (event == null)
		//{
		//	if (actionId == EditorInfo.IME_ACTION_DONE)
		//	{
		//		closeKeyboard(tv);
		//		this.dismiss();
		//	}
		//	else
		//	{
		//		return false;
		//	}
		//}
		//else if (actionId == EditorInfo.IME_NULL)
		//{
		//	if ((event.getAction() != KeyEvent.ACTION_DOWN)
		//		&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
		//	{
		//		closeKeyboard(tv);
		//		this.dismiss();
		//	}
		//}
		//else
		//{
		//	return false;
		//}
		//return true;
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
