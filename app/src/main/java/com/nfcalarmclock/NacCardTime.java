package com.nfcalarmclock;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Locale;

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
     * @brief Alarm card.
     */
    private AlarmCard mCard;

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
    public NacCardTime(AlarmCard card, Context context)
    {
        this.mContext = context;
        this.mCard = card;
        View root = card.getRoot();
        this.mContainer = (RelativeLayout) root.findViewById(R.id.nacTimeContainer);
        this.mTime = (TextView) root.findViewById(R.id.nacTime);
        this.mMeridian = (TextView) root.findViewById(R.id.nacMeridian);
        this.mContainer.setOnClickListener(this);
    }

    /**
     * @brief Initialize the time.
     */
    public void init()
    {
        set();
    }

    /**
     * @brief Set the time.
     */
    public void set()
    {
        Alarm alarm = this.mCard.getAlarm();
        Locale locale = Locale.getDefault();
        int h = alarm.getHour();
        int m = alarm.getMinute();
        boolean format = DateFormat.is24HourFormat(this.mContext);
        String hour = String.valueOf(alarm.toFormat(h, format));
        String minute = String.format(locale, "%02d", m);
        String meridian = alarm.getMeridian(h, format);
        this.mTime.setText(hour+":"+minute);
        this.mMeridian.setText(meridian);
    }

    /**
     * @brief Display the time picker dialog.
     */
    @Override
    public void onClick(View v)
    {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        Alarm alarm = mCard.getAlarm();
        AlarmTimePicker dialog = new AlarmTimePicker();
        FragmentManager manager = activity.getSupportFragmentManager();
        dialog.init(alarm, mTime, mMeridian);
        dialog.show(manager, "AlarmTimePicker");
    }

}
