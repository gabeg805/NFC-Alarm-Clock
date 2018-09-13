package com.nfcalarmclock;

import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Locale; 

/**
 * @brief The time to activate the alarm at. Users can change the time by
 *		  selecting the view.
 */
public class NacCardTime
	implements View.OnClickListener,TimePickerDialog.OnTimeSetListener
{

	/**
	 * @brief Context.
	 */
	private Context mContext = null;

	/**
	 * @brief Alarm.
	 */
	private Alarm mAlarm = null;

	/**
	 * @brief Container of the time and meridian views.
	 */
	private RelativeLayout mContainer = null;

	/**
	 * @brief Time text.
	 */
	private TextView mTime = null;

	/**
	 * @brief Meridian text (AM/PM).
	 */
	private TextView mMeridian = null;

	/**
	 * @param  context	The app context.
	 * @param  r  The root view.
	 */
	public NacCardTime(Context context, View r)
	{
		super();

		this.mContext = context;
		this.mContainer = (RelativeLayout) r.findViewById(R.id.nacTimeContainer);
		this.mTime = (TextView) r.findViewById(R.id.nacTime);
		this.mMeridian = (TextView) r.findViewById(R.id.nacMeridian);

		this.mContainer.setOnClickListener(this);
	}

	/**
	 * @brief Initialize the time.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;

		this.setTime();
	}

	/**
	 * @brief Set the time.
	 */
	public void setTime()
	{
		String time = this.mAlarm.getTime();
		String meridian = this.mAlarm.getMeridian();

		this.mTime.setText(time);
		this.mMeridian.setText(meridian);
	}

	/**
	 * @brief Return the height of the view that is visible.
	 */
	public int getHeight()
	{
		return NacUtility.getHeight(this.mContainer);
	}

	/**
	 * @brief Display the time picker dialog.
	 */
	@Override
	public void onClick(View v)
	{
		int hour = this.mAlarm.getHour();
		int minute = this.mAlarm.getMinute();
		boolean format = this.mAlarm.get24HourFormat();
		TimePickerDialog dialog = new TimePickerDialog(this.mContext, this,
			hour, minute, format);

		dialog.show();
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
