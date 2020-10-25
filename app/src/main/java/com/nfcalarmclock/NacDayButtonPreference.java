package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

/**
 * Preference that allows a user to select a style for the day buttons.
 */
public class NacDayButtonPreference
	extends Preference
	implements Preference.OnPreferenceClickListener
{

	/**
	 * Day button.
	 */
	protected NacDayButton mDayButton;

	/**
	 * Style value.
	 */
	protected int mValue;

	/**
	 * Shared constants.
	 */
	protected NacSharedConstants mSharedConstants;

	/**
	 */
	public NacDayButtonPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacDayButtonPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacDayButtonPreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference_day_button);
		setOnPreferenceClickListener(this);

		this.mSharedConstants = new NacSharedConstants(context);
	}

	/**
	 * @return The day button.
	 */
	public NacDayButton getDayButton()
	{
		return this.mDayButton;
	}

	/**
	 * @return The shared constants.
	 */
	public NacSharedConstants getSharedConstants()
	{
		return this.mSharedConstants;
	}

	/**
	 * @return The summary text to use for the preference.
	 */
	@Override
	public CharSequence getSummary()
	{
		NacSharedConstants cons = this.getSharedConstants();
		int value = this.mValue;

		if (value == 1)
		{
			return cons.getDescriptionDayButtonStyleFilled();
		}
		else if (value == 2)
		{
			return cons.getDescriptionDayButtonStyleOutlined();
		}
		else
		{
			return cons.getDescriptionDayButtonStyleFilled();
		}
	}

	/**
	 * Setup the checkbox and summary text.
	 */
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);
		this.mDayButton = (NacDayButton) holder.findViewById(R.id.widget);
		this.setupDayButton();
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		Context context = getContext();
		NacSharedDefaults defs = new NacSharedDefaults(context);
		return (Integer) a.getInteger(index, defs.getDayButtonStyle());
	}

	/**
	 * Allow users to select the whole preference to change the checkbox.
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		NacDayButton button = this.getDayButton();
		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int style = shared.getDayButtonStyle();
		this.mValue = (style % 2) + 1;

		button.setStyle(this.mValue);
		persistInt(this.mValue);
		notifyChanged();
		callChangeListener(this.mValue);
		return true;
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(Object defaultValue)
	{
		if (defaultValue == null)
		{
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defaultValue;
			persistInt(this.mValue);
		}
	}

	/**
	 * Setup the day button.
	 */
	protected void setupDayButton()
	{
		NacSharedConstants cons = this.getSharedConstants();
		NacDayButton button = this.getDayButton();

		button.setText(cons.getDaysOfWeek().get(1));
		button.enable();
	}

}
