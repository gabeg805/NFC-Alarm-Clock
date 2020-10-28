package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.preference.Preference;

/**
 * Preference that displays the sound prompt dialog.
 */
public class NacMediaPreference
	extends Preference
{

	/**
	 * Path of the sound.
	 */
	protected String mValue;

	/**
	 */
	public NacMediaPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacMediaPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacMediaPreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		Context context = getContext();
		return NacSharedPreferences.getMediaSummary(context, this.mValue);
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getString(index);
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(Object defaultValue)
	{
		if (defaultValue == null)
		{
			this.mValue = getPersistedString(this.mValue);
		}
		else
		{
			this.mValue = (String) defaultValue;
			persistString(this.mValue);
		}
	}

	/**
	 * Set the sound.
	 */
	public void setMedia(String media)
	{
		if (media == null)
		{
			return;
		}

		this.mValue = media;

		persistString(this.mValue);
		notifyChanged();
	}

}
