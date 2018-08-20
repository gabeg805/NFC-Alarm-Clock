package com.nfcalarmclock;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView; import java.util.Locale; 
/**
 * @brief The time to activate the alarm at. Users can change the time by
 *        selecting the view.
 */
public class NacCardTime
    implements View.OnClickListener
{

    /**
     * @brief Context.
     */
    private Context mContext;

    /**
     * @brief Alarm.
     */
    private Alarm mAlarm;

    /**
     * @brief Container of the time and meridian views.
     */
    private RelativeLayout mContainer;

    /**
     * @brief Time text.
     */
    private TextView mTime;

    /**
     * @brief Meridian text (AM/PM).
     */
    private TextView mMeridian;

    /**
     * @brief Constructor.
     */
    public NacCardTime(Context context, View r)
    {
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
        Locale locale = Locale.getDefault();
        int h = this.mAlarm.getHour();
        int m = this.mAlarm.getMinute();
        boolean format = DateFormat.is24HourFormat(this.mContext);
        String hour = String.valueOf(this.mAlarm.toFormat(h, format));
        String minute = String.format(locale, "%02d", m);
        String meridian = this.mAlarm.getMeridian(h, format);
		String time = hour+":"+minute;

        this.mTime.setText(time);
        this.mMeridian.setText(meridian);
    }

    /**
     * @brief Display the time picker dialog.
     */
    @Override
    public void onClick(View v)
    {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        AlarmTimePicker dialog = new AlarmTimePicker();
        FragmentManager manager = activity.getSupportFragmentManager();

        dialog.init(mAlarm, mTime, mMeridian);
        dialog.show(manager, "AlarmTimePicker");
    }

}
