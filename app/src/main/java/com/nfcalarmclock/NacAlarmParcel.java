package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
	 * The type of sound to play when the alarm is run.
	 */
	private int mSoundType;

	/**
	 * The path of the sound to play when the alarm is run.
	 */
	private String mSoundPath;

	/**
	 * The name of the sound to play when the alarm is run.
	 */
	private String mSoundName;

	/**
	 * The name of the alarm.
	 */
	private String mName;

	/**
	 * Populate values with input alarm.
	 */
	public NacAlarmParcel(NacAlarm alarm)
	{
		this.mId = alarm.getId();
		this.mEnabled = alarm.getEnabled();
		this.mHour = alarm.getHour();
		this.mMinute = alarm.getMinute();
		this.mDays = NacCalendar.daysToValue(alarm.getDays());
		this.mRepeat = alarm.getRepeat();
		this.mVibrate = alarm.getVibrate();
		this.mSoundType = alarm.getSoundType();
		this.mSoundPath = alarm.getSoundPath();
		this.mSoundName = alarm.getSoundName();
		this.mName = alarm.getName();
	}

	/**
	 * Populate values with input parcel.
	 */
	public NacAlarmParcel(Parcel in)
	{
		this.mId = in.readInt();
		this.mEnabled = (in.readInt() != 0);
		this.mHour = in.readInt();
		this.mMinute = in.readInt();
		this.mDays = in.readInt();
		this.mRepeat = (in.readInt() != 0);
		this.mVibrate = (in.readInt() != 0);
		this.mSoundType = in.readInt();
		this.mSoundPath = in.readString();
		this.mSoundName = in.readString();
		this.mName = in.readString();
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
	 * @param  intent  The intent.
	 *
	 * @return The alarm associated with the given Intent.
	 */
	public static NacAlarm getAlarm(Intent intent)
	{
		if (intent == null)
		{
			return null;
		}

		Bundle bundle = NacAlarmParcel.getExtra(intent);

		return NacAlarmParcel.getAlarm(bundle);
	}

	/**
	 * @see getAlarm
	 *
	 * @param  bundle  The extra bundle associated with an intent.
	 */
	public static NacAlarm getAlarm(Bundle bundle)
	{
		return (bundle != null) ? NacAlarmParcel.getParcel(bundle).toAlarm()
			: null;
	}

	/**
	 * @param  intent  The intent.
	 *
	 * @return The extra data bundle that is part of the intent.
	 */
	public static Bundle getExtra(Intent intent)
	{
		return (Bundle) intent.getBundleExtra("bundle");
	}

	/**
	 * @param  bundle  The extra bundle.
	 *
	 * @return The NacAlarmParcel that contains the NacAlarm within the bundle.
	 */
	public static NacAlarmParcel getParcel(Bundle bundle)
	{
		return (NacAlarmParcel) bundle.getParcelable("parcel");
	}

	/**
	 * @return The equivalent alarm given the parcel values.
	 */
	public NacAlarm toAlarm()
	{
		return new NacAlarm.Builder()
			.setId(this.mId)
			.setEnabled(this.mEnabled)
			.setHour(this.mHour)
			.setMinute(this.mMinute)
			.setDays(this.mDays)
			.setRepeat(this.mRepeat)
			.setVibrate(this.mVibrate)
			.setSoundType(this.mSoundType)
			.setSoundPath(this.mSoundPath)
			.setSoundName(this.mSoundName)
			.setName(this.mName)
			.build();
	}

	/**
	 * @return A bundle that contains the alarm.
	 */
	public static Bundle toBundle(NacAlarm alarm)
	{
		NacAlarmParcel parcel = new NacAlarmParcel(alarm);
		Bundle bundle = new Bundle();

		bundle.putParcelable("parcel", parcel);

		return bundle;
	}

	/**
	 * @return An intent with the alarm.
	 */
	public static Intent toIntent(Context packageContext, Class<?> cls,
		NacAlarm alarm)
	{
		Intent intent = new Intent(packageContext, cls);
		Bundle bundle = NacAlarmParcel.toBundle(alarm);

		intent.putExtra("bundle", bundle);

		return intent;
	}

	/**
	 * Write data into parcel.
	 */
	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		out.writeInt(this.mId);
		out.writeInt(this.mEnabled ? 1 : 0);
		out.writeInt(this.mHour);
		out.writeInt(this.mMinute);
		out.writeInt(this.mDays);
		out.writeInt(this.mRepeat ? 1 : 0);
		out.writeInt(this.mVibrate ? 1 : 0);
		out.writeInt(this.mSoundType);
		out.writeString(this.mSoundPath);
		out.writeString(this.mSoundName);
		out.writeString(this.mName);
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
