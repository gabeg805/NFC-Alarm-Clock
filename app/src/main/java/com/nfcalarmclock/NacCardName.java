package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	 * Parent view of the name view.
	 */
	 private LinearLayout mNameParent;

	/**
	 * Name view.
	 */
	 private TextView mName;

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
		this.mNameParent = (LinearLayout) root.findViewById(R.id.nac_name);
		this.mName = (TextView) root.findViewById(R.id.name);
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
		String alarmName = alarm.getNameNormalized();
		String name = NacSharedPreferences.getNameMessage(alarmName);
		float alpha = ((alarmName != null) && !alarmName.isEmpty()) ? 1.0f
			: 0.3f;

		this.mName.setText(name);
		this.mName.setAlpha(alpha);
	}

	/**
	 * Set OnClick listener.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mNameParent.setOnClickListener(listener);
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
