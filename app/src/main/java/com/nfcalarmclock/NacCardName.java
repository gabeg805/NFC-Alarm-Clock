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
 * @brief The alarm name. Users can change the name upon clicking the view.
 */
public class NacCardName
	implements View.OnClickListener,NacDialog.OnDismissedListener
{

	/**
	 * Alarm.
	 */
	 private Alarm mAlarm;

	/**
	 * Name view.
	 */
	 private ImageTextButton mName;

	/**
	 */
	public NacCardName(View r)
	{
		super();

		this.mAlarm = null;
		this.mName = (ImageTextButton) r.findViewById(R.id.nacName);

		this.mName.setOnClickListener(this);
	}

	/**
	 * Initialize the name.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;
		String name = alarm.getName();

		this.mName.setText(name);
	}

	/**
	 * Display the dialog that allows users to set the name of thee alarm.
	 */
	@Override
	public void onClick(View v)
	{
		NacNameDialog dialog = new NacNameDialog();
		Context context = v.getContext();

		dialog.build(context, R.layout.dlg_alarm_name);
		dialog.addDismissListener(this);
		dialog.show();
	}

	/**
	 * Notify alarm listener that the alarm has been modified.
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		Object data = dialog.getData();
		String name = (data != null) ? (String) data : "";

		NacUtility.printf("Name : %s", name);
		this.mName.setText(name);
		this.mAlarm.setName(name);
		this.mAlarm.changed();
	}

}
