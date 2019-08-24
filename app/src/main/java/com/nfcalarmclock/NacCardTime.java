package com.nfcalarmclock;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * Time on an alarm card.
 */
public class NacCardTime
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Time text.
	 */
	private TextView mTime;

	/**
	 * Meridian text (AM/PM).
	 */
	private TextView mMeridian;

	/**
	 */
	public NacCardTime(Context context, View root)
	{
		this.mContext = context;
		this.mAlarm = null;
		this.mTime = (TextView) root.findViewById(R.id.nac_time);
		this.mMeridian = (TextView) root.findViewById(R.id.nac_meridian);
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
	 * Initialize the time.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.set();
	}

	/**
	 * Set the time.
	 */
	public void set()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String time = alarm.getTime(context);
		String meridian = alarm.getMeridian(context);

		this.mTime.setText(time);
		this.mMeridian.setText(meridian);
	}

	/**
	 * Set the time color.
	 */
	public void setColor(NacSharedPreferences shared)
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String meridian = alarm.getMeridian(context);
		int timeColor = shared.getTimeColor();
		int meridianColor = (meridian == "AM") ? shared.getAmColor()
			: shared.getPmColor();

		this.mTime.setTextColor(timeColor);
		this.mMeridian.setTextColor(meridianColor);
	}

	/**
	 * Show the time picker dialog.
	 */
	public void showDialog(TimePickerDialog.OnTimeSetListener listener)
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		boolean format = NacCalendar.Time.is24HourFormat(context);
		TimePickerDialog dialog = new TimePickerDialog(context, listener, hour,
			minute, format);

		dialog.show();
	}

}
