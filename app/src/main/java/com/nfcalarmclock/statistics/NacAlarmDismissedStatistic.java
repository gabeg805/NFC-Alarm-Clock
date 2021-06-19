package com.nfcalarmclock.statistics;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Statistics for when an alarm is dismissed.
 */
@Entity(tableName="alarm_dismissed_statistic")
public class NacAlarmDismissedStatistic
{

	/**
	 * Embded the ID into this class.
	 */
	@PrimaryKey(autoGenerate=true)
	@Embedded
	NacAlarmStatisticId statisticId;

	/**
	 * Embded the columns from the statistic class into this class.
	 */
	@Embedded
	NacAlarmStatistic statistic;

	/**
	 * Whether the alarm used NFC to dismiss or not.
	 */
	@ColumnInfo(name="used_nfc")
	private boolean mUsedNfc;

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
