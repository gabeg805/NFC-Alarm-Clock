package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Container for all repeat views. Users are able to repeat the alarm on the
 * requested days.
 */
public class NacCardRepeat
	implements CompoundButton.OnCheckedChangeListener,NacDayOfWeek.OnClickListener
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Text of days to repeat.
	 */
	private TextView mTextView;

	/**
	 * Repeat checkbox.
	 */
	private CheckBox mCheckBox;

	/**
	 * Buttons to select which days to repeat the alarm on.
	 */
	private NacDayOfWeek mDayOfWeek;

	/**
	 * On/off switch.
	 */
	 private Switch mSwitch;

	/**
	 */
	public NacCardRepeat(View root)
	{
		super();

		this.mTextView = (TextView) root.findViewById(R.id.nacRepeatText);
		this.mCheckBox = (CheckBox) root.findViewById(R.id.nacRepeatCheckbox);
		this.mDayOfWeek = (NacDayOfWeek) root.findViewById(R.id.nacRepeatDays);
		this.mSwitch = (Switch) root.findViewById(R.id.nacSwitch);
		this.mRoot = root;
	}

	private View mRoot;

	/**
	 * Initialize the repeat text, checkbox, and day buttons.
	 */
	public void init(NacAlarm alarm)
	{
		Context context = this.mTextView.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mAlarm = alarm;

		this.setRepeatText();
		this.mCheckBox.setChecked(alarm.getRepeat());
		this.mDayOfWeek.setDays(alarm.getDays());
		this.mCheckBox.setOnCheckedChangeListener(this);
		this.mDayOfWeek.setOnClickListener(this);
		this.mTextView.setTextColor(shared.getDaysColor());
	}

	/**
	 * Save the repeat state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		//if (!state)
		//{
		//	this.mDayOfWeek.setVisibility(View.GONE);
		//}
		//else
		//{
		//	this.mDayOfWeek.setVisibility(View.VISIBLE);
		//}

		this.mAlarm.setRepeat(state);
		this.mAlarm.changed();
	}

	/**
	 * Save which day was selected to be repeated, or deselected so that it is
	 * not repeated.
	 */
	@Override
	public void onClick(NacDayButton button, int index)
	{
		this.mAlarm.toggleIndex(index);

		if (this.mAlarm.getDays().isEmpty())
		{
			this.mSwitch.setChecked(false);
			this.mAlarm.setEnabled(false);
		}

		this.mAlarm.changed();
		this.setRepeatText();
	}

	/**
	 * Set the repeat summary text.
	 */
	public void setRepeatText()
	{
		String string = NacCalendar.toString(this.mAlarm.getDays());

		if (string.isEmpty())
		{
			string = NacSharedPreferences.DEFAULT_DAYS_MESSAGE;
		}

		this.mTextView.setText(string);
	}

}
