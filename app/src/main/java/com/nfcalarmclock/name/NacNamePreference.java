package com.nfcalarmclock.name;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import com.nfcalarmclock.view.dialog.NacDialog;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Preference that displays the name of the alarm.
 */
public class NacNamePreference
	extends Preference
	implements Preference.OnPreferenceClickListener,
		NacDialog.OnDismissListener
{

	/**
	 * Name of the alarm.
	 */
	protected String mValue;

	/**
	 */
	public NacNamePreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacNamePreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacNamePreference(Context context, AttributeSet attrs, int style)
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
		NacSharedConstants cons = new NacSharedConstants(getContext());
		String value = this.getValue();

		return NacSharedPreferences.getNameSummary(cons, value);
	}

	/**
	 * Get the preference value.
	 *
	 * @return The preference value.
	 */
	public String getValue()
	{
		return this.mValue;
	}

	/**
	 * Persist the summary string and set the new summary when the dialog is
	 * dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.mValue = dialog.getDataString();

		setSummary(this.getSummary());
		persistString(this.mValue);
		return true;
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
	 * Display the dialog when the preference is clicked.
	 */
	@Override
	public boolean onPreferenceClick(@NonNull Preference pref)
	{
		Context context = getContext();
		NacNameDialog dialog = new NacNameDialog();

		dialog.build(context);
		dialog.addOnDismissListener(this);
		dialog.saveData(this.mValue);
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
			this.mValue = getPersistedString(this.mValue);
		}
		else
		{
			this.mValue = (String) defaultValue;
			persistString(this.mValue);
		}
	}

}

