package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import androidx.preference.Preference;

/**
 * Preference that displays the sound prompt dialog.
 */
public class NacSoundPreference
	extends Preference
{

	/**
	 * Path of the sound.
	 */
	protected String mValue;

	/**
	 */
	public NacSoundPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacSoundPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacSoundPreference(Context context, AttributeSet attrs, int style)
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
		return NacSharedPreferences.getSoundSummary(getContext(), this.mValue);
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (String) a.getString(index);
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
	public void setSound(NacSound sound)
	{
		if (sound == null)
		{
			return;
		}

		this.mValue = sound.getPath();

		persistString(this.mValue);
		notifyChanged();
	}

}
