package com.nfcalarmclock;
import android.app.AlarmManager;

/**
 * @brief Alarm.
 */
public class Alarm
{
    private String mName = "";
    private int mHour = 0;
    private int mMinute = 0;
    private long mInterval = -1;
    private int mType = AlarmManager.RTC_WAKEUP;

    /* MAYBE I SHOULD TAKE IN A CONTEXT INTO THE CONSTRUCTOR */

    /**
     * @brief Use the default set values for an alarm.
     * 
     * @details The default alarm will run once a day at midnight and have no
     *          label name.
     */
    public Alarm()
    {
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
     * @brief Set the name of the alarm.
     */
    public void setName(String name)
    {
        this.mName = name;
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

}
