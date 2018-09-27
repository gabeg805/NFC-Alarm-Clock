package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Repeat preference.
 */
public class NacPreferenceRepeat
	extends Preference
	implements CompoundButton.OnCheckedChangeListener
{

	/**
	 * Check box button.
	 */
	private CheckBox mCheckBox;

	/**
	 * Check value.
	 */
	protected boolean mValue;

	/**
	 * Default constant value for the object.
	 */
	protected boolean mDefault;

	/**
	 * Summary text when enabling/disabling the preference.
	 */
	protected String[] mSummaryState;

	/**
	 */
	public NacPreferenceRepeat(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceRepeat(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceRepeat(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.pref_repeat);

		Resources.Theme theme = context.getTheme();
		TypedArray a = theme.obtainStyledAttributes(attrs,
			R.styleable.NacPreference, 0, 0);
		this.mDefault = true;
		this.mSummaryState = new String[2];
		this.mSummaryState[0] = a.getString(R.styleable.NacPreference_summaryEnabled);
		this.mSummaryState[1] = a.getString(R.styleable.NacPreference_summaryDisabled);

		if (this.mSummaryState[0] == null || this.mSummaryState[1] == null)
		{
			throw new Resources.NotFoundException();
		}
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

		this.mCheckBox = (CheckBox) v.findViewById(R.id.widget);

		this.mCheckBox.setChecked(this.mValue);
		this.mCheckBox.setOnCheckedChangeListener(this);
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		return (this.mValue) ? this.mSummaryState[0] : this.mSummaryState[1];
	}

	/**
	 */
	@Override
	public void onCheckedChanged(CompoundButton button, boolean state)
	{
		this.mValue = state;

		this.mCheckBox.setChecked(this.mValue);
		persistBoolean(this.mValue);
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (boolean) a.getBoolean(index, mDefault);
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(boolean restore, Object defval)
	{
		if (restore)
		{
			this.mValue = getPersistedBoolean(this.mValue);
		}
		else
		{
			this.mValue = (boolean) defval;
			persistBoolean(this.mValue);
		}
	}

}
