package com.nfcalarmclock.statistics;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Statistics for when an alarm is dismissed.
 */
@Entity(tableName="alarm_dismissed_statistic")
public class NacAlarmDismissedStatistic
	extends NacAlarmStatistic
{

	/**
	 * Whether the alarm used NFC to dismiss or not.
	 */
	@ColumnInfo(name="used_nfc")
	private boolean mUsedNfc;

	/**
	 * @return Whether the alarm used NFC to dismiss or not.
	 */
	public boolean usedNfc()
	{
		return this.mUsedNfc;
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

}
