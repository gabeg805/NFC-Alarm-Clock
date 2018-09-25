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
	implements CompoundButton.OnCheckedChangeListener,View.OnClickListener
{

	/**
	 * @brief Alarm.
	 */
	private Alarm mAlarm = null;

	/**
	 * @brief Text of days to repeat.
	 */
	private TextView mText = null;

	/**
	 * @brief Repeat checkbox.
	 */
	private CheckBox mCheckbox = null;

	/**
	 * @brief Buttons to select which days to repeat the alarm on.
	 */
	private DayOfWeekButtons mDays = null;

	/**
	 * @brief Constructor.
	 */
	public NacCardRepeat(View r)
	{
		super();

		this.mText = (TextView) r.findViewById(R.id.nacRepeatText);
		this.mCheckbox = (CheckBox) r.findViewById(R.id.nacRepeatCheckbox);
		this.mDays = (DayOfWeekButtons) r.findViewById(R.id.nacRepeatDays);
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

		this.mText.setText(days);
		this.mCheckbox.setChecked(state);
		this.mCheckbox.setOnCheckedChangeListener(this);
		this.mDays.init(alarm);
		this.mDays.setOnClickListener(this);
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
	@Override
	public void onClick(View v)
	{
		int index = (int) v.getTag();
		byte day = this.mAlarm.getWeekDays().get(index);

		this.mDays.toggleButton(index);
		this.mAlarm.toggleDay(day);
		this.mText.setText(mAlarm.getDaysString());
		this.mAlarm.changed();
	}

}
