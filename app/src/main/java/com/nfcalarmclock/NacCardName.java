package com.nfcalarmclock;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

/**
 * Name for the alarm card.
 */
public class NacCardName
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Name view.
	 */
	 private NacImageTextButton mName;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 */
	public NacCardName(Context context, View root)
	{
		this.mContext = context;
		this.mAlarm = null;
		this.mName = (NacImageTextButton) root.findViewById(R.id.nac_name);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * Initialize.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.set();
	}

	/**
	 * Set the alarm name.
	 */
	public void set()
	{
		NacAlarm alarm = this.getAlarm();
		String name = alarm.getName();
		boolean focus = true;

		if (name.isEmpty())
		{
			name = NacSharedPreferences.DEFAULT_NAME_MESSAGE;
			focus = false;
		}

		this.mName.setText(name);
		this.mName.setFocus(focus);
		this.mName.getTextView().setMaxLines(1);
		this.mName.getTextView().setEllipsize(TextUtils.TruncateAt.END);
	}

	/**
	 * Set OnClick listener.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mName.setOnClickListener(listener);
	}

	/**
	 * Show the dialog.
	 */
	public void showDialog(NacDialog.OnDismissListener listener)
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		NacNameDialog dialog = new NacNameDialog();

		dialog.build(context);
		dialog.saveData(alarm.getName());
		dialog.addOnDismissListener(listener);
		dialog.show();
	}

}
