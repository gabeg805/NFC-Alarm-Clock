package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;

/**
 * Preference that displays how long before an alarm is auto dismissed.
 */
public class NacAutoDismissPreference
	extends Preference
	implements NacScrollablePickerDialogFragment.OnScrollablePickerOptionSelectedListener
{

	/**
	 * Preference value.
	 */
	private int mValue;

	/**
	 */
	public NacAutoDismissPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacAutoDismissPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacAutoDismissPreference(Context context, AttributeSet attrs,
		int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference);
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		NacSharedConstants cons = new NacSharedConstants(getContext());
		int value = this.getValue();

		return NacSharedPreferences.getAutoDismissSummary(cons, value);
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
		return a.getInteger(index, defs.getAutoDismissIndex());
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
		NacAutoDismissDialog dialog = new NacAutoDismissDialog();
		int value = this.getValue();

		dialog.setDefaultScrollablePickerIndex(value);
		dialog.setOnScrollablePickerOptionSelectedListener(this);
		dialog.show(manager, NacAutoDismissDialog.TAG);
	}

}
