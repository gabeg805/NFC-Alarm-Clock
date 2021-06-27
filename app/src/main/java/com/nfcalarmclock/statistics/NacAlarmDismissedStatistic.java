package com.nfcalarmclock.statistics;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.nfcalarmclock.alarm.NacAlarm;

/**
 * Statistics for when an alarm is dismissed.
 */
@Entity(tableName="alarm_dismissed_statistic",
	foreignKeys={
		@ForeignKey(entity=NacAlarm.class,
			parentColumns={"id"},
			childColumns={"alarm_id"},
			onDelete=ForeignKey.SET_NULL)
		})
public class NacAlarmDismissedStatistic
	extends NacAlarmStatistic
{

	/**
	 * Whether the alarm used NFC to dismiss or not.
	 */
	@ColumnInfo(name="used_nfc")
	@NonNull
	private boolean mUsedNfc;

	/**
	 */
	public NacAlarmDismissedStatistic()
	{
		super();
	}

	/**
	 */
	public NacAlarmDismissedStatistic(NacAlarm alarm)
	{
		super(alarm);
	}

	/**
	 */
	public NacAlarmDismissedStatistic(NacAlarm alarm, boolean usedNfc)
	{
		this(alarm);

		this.setUsedNfc(usedNfc);
	}

	/**
	 * Set whether the alarm used NFC to dismiss or not.
	 *
	 * @param  usedNfc  Whether the alarm used NFC to dismiss or not.
	 */
	public void setUsedNfc(boolean usedNfc)
	{
		this.mUsedNfc = usedNfc;
	}

	/**
	 * @return Whether the alarm used NFC to dismiss or not.
	 */
	public boolean usedNfc()
	{
		return this.mUsedNfc;
	}

}
