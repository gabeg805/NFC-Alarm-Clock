package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * @brief Container for all repeat views. Users are able to repeat the alarm on
 *		  the requested days.
 * 
 * @details The repeat views are:
 *				* The text displayed by default beneath the time.
 *				* The checkbox indicating whether or not the user wants to
 *				  repeat the alarm.
 *				* The alarm day buttons.
 */
public class NacCardRepeat
	implements CompoundButton.OnCheckedChangeListener,NacDayOfWeek.OnClickListener
{

	/**
	 * @brief Alarm.
	 */
	private Alarm mAlarm = null;

	/**
	 * @brief Text of days to repeat.
	 */
	private TextView mTextView = null;

	/**
	 * @brief Repeat checkbox.
	 */
	private CheckBox mCheckBox = null;

	/**
	 * @brief Buttons to select which days to repeat the alarm on.
	 */
	private NacDayOfWeek mDayOfWeek = null;

	/**
	 * @brief Constructor.
	 */
	public NacCardRepeat(View r)
	{
		super();

		this.mTextView = (TextView) r.findViewById(R.id.nacRepeatText);
		this.mCheckBox = (CheckBox) r.findViewById(R.id.nacRepeatCheckbox);
		this.mDayOfWeek = (NacDayOfWeek) r.findViewById(R.id.nacRepeatDays);
	}

	/**
	 * @brief Initialize the repeat text, checkbox, and day buttons.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;
		boolean state = this.mAlarm.getRepeat();
		String days = this.mAlarm.getDaysString();
		int data = this.mAlarm.getDays();

		this.mTextView.setText(days);
		this.mCheckBox.setChecked(state);
		this.mCheckBox.setOnCheckedChangeListener(this);
		this.mDayOfWeek.setDays(alarm);
		this.mDayOfWeek.setOnClickListener(this);
	}

	/**
	 * @brief Save the repeat state of the alarm.
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
	//public void onClick(View v)
	@Override
	public void onClick(NacDayButton button, int index)
	{
		byte day = this.mAlarm.getWeekDays().get(index);

		button.animateToggle();
		this.mAlarm.toggleDay(day);
		this.mTextView.setText(this.mAlarm.getDaysString());
		this.mAlarm.changed();
	}

}
