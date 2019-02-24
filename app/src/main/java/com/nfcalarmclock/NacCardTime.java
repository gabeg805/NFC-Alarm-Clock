package com.nfcalarmclock;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Locale; 

/**
 * The time to activate the alarm at. Users can change the time by selecting
 * the view.
 */
public class NacCardTime
	implements View.OnClickListener,TimePickerDialog.OnTimeSetListener
{

	/**
	 * Container of the time and meridian views.
	 */
	private RelativeLayout mContainer;

	/**
	 * Time text.
	 */
	private TextView mTime;

	/**
	 * Meridian text (AM/PM).
	 */
	private TextView mMeridian;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 */
	public NacCardTime(View root)
	{
		super();

		this.mContainer = (RelativeLayout) root.findViewById(
			R.id.nacTimeContainer);
		this.mTime = (TextView) root.findViewById(R.id.nacTime);
		this.mMeridian = (TextView) root.findViewById(R.id.nacMeridian);
		this.mAlarm = null;

		this.mContainer.setOnClickListener(this);
	}

	/**
	 * Initialize the time.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.setTime();
	}

	/**
	 * Set the time.
	 */
	public void setTime()
	{
		String time = this.mAlarm.getTime();
		String meridian = this.mAlarm.getMeridian();

		this.mTime.setText(time);
		this.mMeridian.setText(meridian);
	}

	/**
	 * Return the height of the view that is visible.
	 */
	public int getHeight()
	{
		return NacUtility.getHeight(this.mContainer);
	}

	/**
	 * Display the time picker dialog.
	 */
	@Override
	public void onClick(View v)
	{
		Context context = this.mContainer.getContext();
		int hour = this.mAlarm.getHour();
		int minute = this.mAlarm.getMinute();
		boolean format = this.mAlarm.get24HourFormat();
		//TimePickerDialog dialog = new TimePickerDialog(context, this, hour,
		//	minute, format);

		NacColorPickerDialog dialog = new NacColorPickerDialog();
		dialog.build(context, R.layout.dlg_color_picker);
		dialog.show();
		dialog.scale(0.8, 0.8, false, false);
	}

	@Override
	public void onTimeSet(TimePicker tp, int hr, int min)
	{
		NacUtility.printf("Time = %d:%02d", hr, min);
		this.mAlarm.setHour(hr);
		this.mAlarm.setMinute(min);
		this.setTime();
		this.mAlarm.changed();
	}

}
