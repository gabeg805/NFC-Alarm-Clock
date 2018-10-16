package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Name preference.
 */
public class NacPreferenceName
	extends Preference
	implements Preference.OnPreferenceClickListener,NacDialog.OnDismissedListener
{

	/**
	 * Name of the alarm.
	 */
	protected String mValue;

	/**
	 * Default constant value for the object.
	 */
	protected static final String mDefault = "None";

	/**
	 */
	public NacPreferenceName(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceName(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceName(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.pref_name);
		setOnPreferenceClickListener(this);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		return (this.mValue != null) ? this.mValue : this.mDefault;
	}

	/**
	 * Bind the title and summary sections of the preference view, and leave
	 * the rest to the user.
	 *
	 * Set the width of the title and summary to leave space for the other
	 * view(s) the user may use.
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);
		this.setSummary(this.getSummary());
	}

	/**
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		Object data = dialog.getData();
		this.mValue = (data != null) ? (String) data : "";

		this.setSummary(this.getSummary());
		persistString(this.mValue);
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
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		Context context = getContext();
		NacNameDialog dialog = new NacNameDialog();

		dialog.build(context, R.layout.dlg_alarm_name);
		dialog.addDismissListener(this);
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
			this.mValue = getPersistedString(this.mValue);
		}
		else
		{
			this.mValue = (String) defval;

			persistString(this.mValue);
		}
	}

}

