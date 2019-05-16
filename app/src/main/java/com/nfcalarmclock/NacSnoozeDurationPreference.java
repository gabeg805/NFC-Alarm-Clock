package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * Preference that displays how long to snooze for.
 */
public class NacSnoozeDurationPreference
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
	public NacSnoozeDurationPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacSnoozeDurationPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacSnoozeDurationPreference(Context context, AttributeSet attrs,
		int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference);
		setOnPreferenceClickListener(this);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		return NacSharedPreferences.getSnoozeDurationSummary(this.mValue);
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
		return (Integer) a.getInteger(index,
			NacSharedPreferences.DEFAULT_SNOOZE_DURATION_INDEX);
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		Context context = getContext();
		NacSnoozeDurationDialog dialog = new NacSnoozeDurationDialog();

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
	protected void onSetInitialValue(boolean restore, Object defval)
	{
		if (restore)
		{
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defval;

			persistInt(this.mValue);
		}
	}

}
