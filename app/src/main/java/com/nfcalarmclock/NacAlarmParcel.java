package com.nfcalarmclock;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Make the NacAlarm object serializable by using a parcel.
 */
public class NacAlarmParcel
	implements Parcelable
{

	/**
	 * The alarm ID.
	 */
	private int mId;

	/**
	 * Indicates whether the alarm is enabled or not.
	 */
	private boolean mEnabled;

	/**
	 * Indicator if using a 24 hour format to display time or not.
	 */
	private boolean m24HourFormat;

	/**
	 * The hour at which to run the alarm.
	 */
	private int mHour;

	/**
	 * The minute at which to run the alarm.
	 */
	private int mMinute;

	/**
	 * The days on which to run the alarm.
	 */
	private int mDays;

	/**
	 * Indicates whether the alarm should be repeated or not.
	 */
	private boolean mRepeat;

	/**
	 * Indicates whether the phone should vibrate when the alarm is run.
	 */
	private boolean mVibrate;

	/**
	 * The sound to play when the alarm is run.
	 */
	private String mSound;

	/**
	 * The name of the alarm.
	 */
	private String mName;

	/**
	 * Populate values with input alarm.
	 */
	public NacAlarmParcel(NacAlarm a)
	{
		mId = a.getId();
		mEnabled = a.getEnabled();
		m24HourFormat = a.get24HourFormat();
		mHour = a.getHour();
		mMinute = a.getMinute();
		mDays = a.getDays();
		mRepeat = a.getRepeat();
		mVibrate = a.getVibrate();
		mSound = a.getSound();
		mName = a.getName();
	}

	/**
	 * Populate values with input parcel.
	 */
	public NacAlarmParcel(Parcel in)
	{
		mId = in.readInt();
		mEnabled = (in.readInt() != 0);
		m24HourFormat = (in.readInt() != 0);
		mHour = in.readInt();
		mMinute = in.readInt();
		mDays = in.readInt();
		mRepeat = (in.readInt() != 0);
		mVibrate = (in.readInt() != 0);
		mSound = in.readString();
		mName = in.readString();
	}

	/**
	 * @return The equivalent alarm given the parcel values.
	 */
	public NacAlarm toAlarm()
	{
		NacAlarm a = new NacAlarm();

		a.setId(this.mId);
		a.setEnabled(this.mEnabled);
		a.set24HourFormat(this.m24HourFormat);
		a.setHour(this.mHour);
		a.setMinute(this.mMinute);
		a.setDays(this.mDays);
		a.setRepeat(this.mRepeat);
		a.setVibrate(this.mVibrate);
		a.setSound(this.mSound);
		a.setName(this.mName);

		return a;
	}

	/**
	 * Describe contents.
	 */
	@Override
	public int describeContents()
	{
		return 0;
	}

	/**
	 * Write data into parcel.
	 */
	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		out.writeInt(mId);
		out.writeInt(mEnabled ? 1 : 0);
		out.writeInt(m24HourFormat ? 1 : 0);
		out.writeInt(mHour);
		out.writeInt(mMinute);
		out.writeInt(mDays);
		out.writeInt(mRepeat ? 1 : 0);
		out.writeInt(mVibrate ? 1 : 0);
		out.writeString(mSound);
		out.writeString(mName);
	}

	/**
	 * Generate object.
	 */
	public static final Parcelable.Creator<NacAlarmParcel> CREATOR = new
		Parcelable.Creator<NacAlarmParcel>()
	{
		public NacAlarmParcel createFromParcel(Parcel in)
		{
			return new NacAlarmParcel(in);
		}

		public NacAlarmParcel[] newArray(int size)
		{
			return new NacAlarmParcel[size];
		}
	};

}
