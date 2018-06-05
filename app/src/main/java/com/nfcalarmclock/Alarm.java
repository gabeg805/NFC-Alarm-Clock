package com.nfcalarmclock;
import android.app.AlarmManager;

/**
 * @brief Alarm.
 */
public class Alarm
{

    private int mHour = 0;
    private int mMinute = 0;
    private int mDays = 0;
    private boolean mEnabled = true;
    private boolean mRepeat = true;
    private boolean mVibrate = false;
    private String mName = "";
    private String mSound = "";
    private long mInterval = -1;
    private int mType = AlarmManager.RTC_WAKEUP;

    /**
     * @brief Use the default set values for an alarm.
     * 
     * @details The default alarm will run once a day at midnight and have no
     *          label name.
     */
    public Alarm()
    {
        this("", 7, 0);
    }

    /**
     * @brief Set the time to run the alarm at.
     */
    public Alarm(int hour, int minute)
    {
        this("", hour, minute);
    }

    /**
     * @brief Set the name and the time to run the alarm at.
     */
    public Alarm(String name, int hour, int minute)
    {
        this.mName = name;
        this.mHour = hour;
        this.mMinute = minute;
        this.mEnabled = true;
        this.mRepeat = true;
        this.mVibrate = true;
    }

    /**
     * @brief Set the interval at which to run the alarm at.
     */
    public Alarm(int interval)
    {
        this("", interval);
    }

    /**
     * @brief Set the name and the interval at which to run the alarm at.
     */
    public Alarm(String name, int interval)
    {
        this.mName = name;
        this.mInterval = interval;
        this.mType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
    }

    /**
     * @brief Convert hour to the appropriate form (either 12 or 24 hour
     *        format).
     * 
     * @note See if you can change this to a static method.
     */
    public int toFormat(int hour, boolean is24hourformat)
    {
        int converted = hour;
        if (!is24hourformat)
        {
            if (hour >= 12)
            {
                converted = hour % 12;
            }
            if (converted == 0)
            {
                converted = 12;
            }
        }
        return converted;
    }

    /**
     * @brief Set the hour at which to run the alarm.
     */
    public void setHour(int hour)
    {
        this.mHour = hour;
    }

    /**
     * @brief Set the minute at which to run the alarm.
     */
    public void setMinute(int minute)
    {
        this.mMinute = minute;
    }

    /**
     * @brief Set the days on which the alarm will be run.
     */
    public void setDays(int days)
    {
        this.mDays = days;
    }

    /**
     * @brief Set the enabled/disabled status of the alarm.
     */
    public void setEnabled(boolean state)
    {
        this.mEnabled = state;
    }

    /**
     * @brief Set whether the alarm should be repeated or not.
     */
    public void setRepeat(boolean state)
    {
        this.mRepeat = state;
    }

    /**
     * @brief Set whether or not the phone should vibrate when the alarm is
     *        activated.
     */
    public void setVibrate(boolean state)
    {
        this.mVibrate = state;
    }

    /**
     * @brief Set the name of the alarm.
     */
    public void setName(String name)
    {
        this.mName = name;
    }

    /**
     * @brief Set the sound that will be played when the alarm is activated.
     */
    public void setSound(String sound)
    {
        this.mSound = sound;
    }

    /**
     * @brief Set the interval at which to run the alarm.
     */
    public void setInterval(int interval)
    {
        this.mInterval = interval;
    }

    /**
     * @brief Set the type of alarm to run.
     */
    public void setType(int type)
    {
        this.mType = type;
    }

    /**
     * @brief Return the name of the alarm.
     */
    public String getName()
    {
        return this.mName;
    }

    /**
     * @brief Return the hour at which to run the alarm.
     */
    public int getHour()
    {
        return this.mHour;
    }

    /**
     * @brief Return the minutes at which to run the alarm.
     */
    public int getMinute()
    {
        return this.mMinute;
    }

    /**
     * @brief Return the interval at which to run the alarm.
     */
    public long getInterval()
    {
        return this.mInterval;
    }

    /**
     * @brief Return the type of the alarm to run.
     */
    public int getType()
    {
        return this.mType;
    }

    /**
     * @brief Return the meridian (AM or PM).
     */
    public String getMeridian(int hour, boolean is24hourformat)
    {
        if (is24hourformat)
        {
            return "";
        }
        if (hour < 12)
        {
            return "AM";
        }
        else
        {
            return "PM";
        }
    }

}
