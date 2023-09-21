package com.nfcalarmclock.statistics.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.nfcalarmclock.alarm.db.NacAlarm;

/**
 * Statistics for when an alarm is snoozed.
 */
@Entity(tableName="alarm_snoozed_statistic",
	foreignKeys={
		@ForeignKey(entity=NacAlarm.class,
			parentColumns={"id"},
			childColumns={"alarm_id"},
			onDelete=ForeignKey.SET_NULL)
		},
	inheritSuperIndices=true)
public class NacAlarmSnoozedStatistic
	extends NacAlarmStatistic
{

	/**
	 * Duration of the snooze.
	 */
	@ColumnInfo(name="duration", defaultValue="0")
	private long mDuration;

	/**
	 */
	public NacAlarmSnoozedStatistic()
	{
		super();
	}

	/**
	 */
	public NacAlarmSnoozedStatistic(NacAlarm alarm)
	{
		super(alarm);
	}

	/**
	 */
	public NacAlarmSnoozedStatistic(NacAlarm alarm, long duration)
	{
		this(alarm);

		this.setDuration(duration);
	}

	/**
	 * @return The duration of the snooze.
	 */
	public long getDuration()
	{
		return this.mDuration;
	}

	/**
	 * Set the duration of the snooze.
	 *
	 * @param  duration  The duration of the snooze.
	 */
	public void setDuration(long duration)
	{
		this.mDuration = duration;
	}

}
