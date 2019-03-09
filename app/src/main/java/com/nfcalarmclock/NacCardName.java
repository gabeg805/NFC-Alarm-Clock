package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * @brief The alarm name. Users can change the name upon clicking the view.
 */
public class NacCardName
	implements View.OnClickListener,NacDialog.OnDismissListener
{

	/**
	 * Alarm.
	 */
	 private NacAlarm mAlarm;

	/**
	 * Name view.
	 */
	 private NacImageTextButton mName;

	/**
	 * Text of days to repeat.
	 */
	private TextView mTextView;

	/**
	 * Divider view.
	 */
	private View mDivider;

	/**
	 * Front margin of view.
	 */
	private int mMarginStart;

	/**
	 */
	public NacCardName(View root)
	{
		super();

		this.mAlarm = null;
		this.mName = (NacImageTextButton) root.findViewById(R.id.nacName);
		this.mTextView = (TextView) root.findViewById(R.id.nacRepeatTextName);
		this.mDivider = (View) root.findViewById(R.id.alarmExpandDivider);
		this.mMarginStart = root.getResources().getDimensionPixelSize(R.dimen.sp_text);

		this.mName.setOnClickListener(this);
	}

	/**
	 * Initialize the name.
	 */
	public void init(NacAlarm alarm)
	{
		Context context = this.mTextView.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mAlarm = alarm;

		this.setName();
		this.mTextView.setTextColor(shared.nameColor);
		this.mDivider.setBackgroundTintList(ColorStateList.valueOf(shared.themeColor));
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
		dialog.saveData(this.mAlarm.getName());
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Notify alarm listener that the alarm has been modified.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		Object data = dialog.getData();
		String name = (data != null) ? (String) data : "";

		this.mAlarm.setName(name);
		this.setName();
		this.mAlarm.changed();

		return true;
	}

	/**
	 * Set the name of the alarm.
	 */
	public void setName()
	{
		String name = this.mAlarm.getName();
		String text = name+"  ";
		boolean focus = true;
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
			LayoutParams.WRAP_CONTENT);
		int margin = this.mMarginStart;

		if (name.isEmpty())
		{
			name = NacAlarm.getNameMessage();
			text = "";
			focus = false;
			margin = 0;
		}

		NacUtility.printf("Name : %s", name);
		params.setMarginStart(margin);
		this.mTextView.setLayoutParams(params);
		this.mName.setText(name);
		this.mName.setFocus(focus);
		this.mTextView.setText(text);
	}

}
