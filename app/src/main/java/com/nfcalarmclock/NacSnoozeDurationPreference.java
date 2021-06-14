package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;

/**
 * Preference that displays how long to snooze for.
 */
public class NacSnoozeDurationPreference
	extends Preference
	implements NacScrollablePickerDialogFragment.OnScrollablePickerOptionSelectedListener
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
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		NacSharedConstants cons = new NacSharedConstants(getContext());
		int value = this.getValue();

		return NacSharedPreferences.getSnoozeDurationSummary(cons, value);
	}

	/**
	 * Get the preference value.
	 *
	 * @return The preference value.
	 */
	public int getValue()
	{
		return this.mValue;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		NacSharedDefaults defs = new NacSharedDefaults(getContext());
		return a.getInteger(index, defs.getSnoozeDurationIndex());
	}

	/**
	 * Save the selected value from the scrollable picker.
	 */
	@Override
	public void onScrollablePickerOptionSelected(int index)
	{
		this.mValue = index;

		persistInt(index);
		notifyChanged();
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
	 * Show the auto dismiss dialog.
	 */
	public void showDialog(FragmentManager manager)
	{
		NacSnoozeDurationDialog dialog = new NacSnoozeDurationDialog();
		int value = this.getValue();

		dialog.setDefaultScrollablePickerIndex(value);
		dialog.setOnScrollablePickerOptionSelectedListener(this);
		dialog.show(manager, NacSnoozeDurationDialog.TAG);
	}

}
