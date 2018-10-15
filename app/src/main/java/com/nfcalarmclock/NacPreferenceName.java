package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
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
	 * Alarm.
	 */
	protected Alarm mAlarm;

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

		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(
			context);
		this.mAlarm = new Alarm();
		this.mValue = shared.getString("pref_name", this.mDefault);
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

		NacUtility.printf("Name onBindView : %s", this.mValue);
		this.setSummary(this.mValue);
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		return this.mValue;
	}

	/**
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		this.mValue = this.mAlarm.getName();
		SharedPreferences.Editor editor = getEditor();

		NacUtility.printf("Name onDialogDismissed : %s", this.mValue);
		this.setSummary(this.mValue);
		editor.putString("pref_name", this.mValue);
		editor.apply();
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		NacUtility.printf("Name preference clicked.");
		Context context = getContext();
		NacCardNameDialog dialog = new NacCardNameDialog(this.mAlarm);

		dialog.build(context, R.layout.dlg_alarm_name);
		dialog.addDismissListener(this);
		dialog.show();

		return true;
	}

}

