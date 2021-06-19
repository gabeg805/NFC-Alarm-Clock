package com.nfcalarmclock.maxsnooze;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;

import com.nfcalarmclock.dialog.NacScrollablePickerDialogFragment;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedDefaults;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.R;

/**
 * Preference that displays the max number of snoozes for an alarm.
 */
public class NacMaxSnoozePreference
	extends Preference
	implements NacScrollablePickerDialogFragment.OnScrollablePickerOptionSelectedListener
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
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		NacSharedConstants cons = new NacSharedConstants(getContext());
		int value = this.getValue();

		return NacSharedPreferences.getMaxSnoozeSummary(cons, value);
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
		return a.getInteger(index, defs.getMaxSnoozeIndex());
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
		NacMaxSnoozeDialog dialog = new NacMaxSnoozeDialog();
		int value = this.getValue();

		dialog.setDefaultScrollablePickerIndex(value);
		dialog.setOnScrollablePickerOptionSelectedListener(this);
		dialog.show(manager, NacMaxSnoozeDialog.TAG);
	}

}
