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
	 private Alarm mAlarm = null;

	/**
	 * Name view.
	 */
	 private ImageTextButton mName = null;

	/**
	 */
	public NacCardName(View r)
	{
		super();
		this.mName = (ImageTextButton) r.findViewById(R.id.nacName);
		this.mName.setOnClickListener(this);
	}

	/**
	 * Initialize the name.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;
		this.setName();
	}

	/**
	 * Set the name.
	 */
	public void setName()
	{
		String name = this.mAlarm.getName();
		this.mName.setText(name);
	}

	/**
	 * Display the dialog that allows users to set the name of thee alarm.
	 */
	@Override
	public void onClick(View v)
	{
		NacCardNameDialog dialog = new NacCardNameDialog(this.mAlarm);
		Context context = v.getContext();

		dialog.build(context, R.layout.dlg_alarm_name);
		dialog.addDismissListener(this);
		dialog.show();
	}

	/**
	 */
	@Override
	public void onDialogDismissed()
	{
		this.setName();
	}

}
