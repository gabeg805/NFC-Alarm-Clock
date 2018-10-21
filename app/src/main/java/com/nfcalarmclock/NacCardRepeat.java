package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CheckBox;
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
	private Alarm mAlarm;

	/**
	 * Text of days to repeat.
	 */
	private TextView mTextView = null;

	/**
	 * Repeat checkbox.
	 */
	private CheckBox mCheckBox = null;

	/**
	 * Buttons to select which days to repeat the alarm on.
	 */
	private NacDayOfWeek mDayOfWeek = null;

	/**
	 */
	public NacCardRepeat(View root)
	{
		super();

		this.mTextView = (TextView) root.findViewById(R.id.nacRepeatText);
		this.mCheckBox = (CheckBox) root.findViewById(R.id.nacRepeatCheckbox);
		this.mDayOfWeek = (NacDayOfWeek) root.findViewById(R.id.nacRepeatDays);
	}

	/**
	 * Initialize the repeat text, checkbox, and day buttons.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;

		this.setRepeatText();
		this.mCheckBox.setChecked(alarm.getRepeat());
		this.mDayOfWeek.setDays(alarm.getDays());
		this.mCheckBox.setOnCheckedChangeListener(this);
		this.mDayOfWeek.setOnClickListener(this);
	}

	/**
	 * Save the repeat state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		NacUtility.printf("Repeat : %b", state);
		this.mAlarm.setRepeat(state);
		this.mAlarm.changed();
	}

	/**
	 * @brief Save which day was selected to be repeated, or deselected so that
	 *		  it is not repeated.
	 */
	@Override
	public void onClick(NacDayButton button, int index)
	{
		byte day = this.mAlarm.getWeekDays().get(index);

		this.mAlarm.toggleDay(day);
		this.setRepeatText();
		this.mAlarm.changed();
	}

	/**
	 * Set the repeat summary text.
	 */
	public void setRepeatText()
	{
		String string = this.mAlarm.getDaysString();

		if (string.isEmpty())
		{
			string = Alarm.getDaysStringMessage();
		}

		this.mTextView.setText(string);
	}

}
