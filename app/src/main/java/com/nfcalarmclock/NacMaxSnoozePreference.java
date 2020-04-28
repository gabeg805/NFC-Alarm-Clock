package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.preference.Preference;

/**
 * Preference that displays how long to snooze for.
 */
public class NacMaxSnoozePreference
	extends Preference
	implements Preference.OnPreferenceClickListener,
		NacDialog.OnDismissListener
{

	/**
	 * Preference value.
	 */
	private int mValue;

	/**
	 */
	public NacMaxSnoozePreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacMaxSnoozePreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacMaxSnoozePreference(Context context, AttributeSet attrs,
		int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference);
		setOnPreferenceClickListener(this);
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		return NacSharedPreferences.getMaxSnoozeSummary(this.mValue);
	}

	/**
	 * Save the spinner index value.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.mValue = ((NacSpinnerDialog)dialog).getValue();

		persistInt(this.mValue);
		notifyChanged();
		return true;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		NacSharedDefaults defaults = new NacSharedDefaults(getContext());
		return (Integer) a.getInteger(index, defaults.getMaxSnoozeIndex());
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		Context context = getContext();
		NacMaxSnoozeDialog dialog = new NacMaxSnoozeDialog();

		dialog.saveData(this.mValue);
		dialog.build(context);
		dialog.addOnDismissListener(this);
		dialog.show();
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

}
