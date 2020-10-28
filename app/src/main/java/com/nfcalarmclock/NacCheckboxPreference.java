package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

/**
 * Preference that indicates repeating the alarm.
 */
public class NacCheckboxPreference
	extends Preference
	implements Preference.OnPreferenceClickListener,
		CompoundButton.OnCheckedChangeListener
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
	 * Summary text when checkbox is enabled.
	 */
	protected String mSummaryOn;

	/**
	 * Summary text when checkbox is disabled.
	 */
	protected String mSummaryOff;

	/**
	 * Color state list of the checkbox.
	 */
	protected ColorStateList mColorStateList;

	/**
	 * Binding view holder flag.
	 */
	protected boolean mBindFlag;

	/**
	 * Default value.
	 */
	protected static final boolean DEFAULT_VALUE = true;

	/**
	 */
	public NacCheckboxPreference(Context context)
	{
		super(context);
		this.init(context, null);
	}

	/**
	 */
	public NacCheckboxPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}

	/**
	 */
	public NacCheckboxPreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		this.init(context, attrs);
	}

	/**
	 * @return The checked status.
	 */
	public boolean getChecked()
	{
		return this.mValue;
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		return (this.mValue) ? this.mSummaryOn : this.mSummaryOff;
	}

	/**
	 * Initialize the preference.
	 */
	private void init(Context context, AttributeSet attrs)
	{
		setLayoutResource(R.layout.nac_preference_checkbox);
		setOnPreferenceClickListener(this);

		this.mColorStateList = null;
		this.mBindFlag = false;
		int[] array = new int[] { android.R.attr.summaryOn,
			android.R.attr.summaryOff };
		TypedArray ta = context.obtainStyledAttributes(attrs, array);

		try
		{
			this.mSummaryOn = ta.getString(0);
			this.mSummaryOff = ta.getString(1);
		}
		finally
		{
			ta.recycle();
		}
	}

	/**
	 */
	@Override
	public void onAttached()
	{
		super.onAttached();
		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int[][] states = new int[][] {
			new int[] {  android.R.attr.state_checked },
			new int[] { -android.R.attr.state_checked } };
		int[] colors = new int[] { shared.getThemeColor(), Color.LTGRAY };
		this.mColorStateList = new ColorStateList(states, colors);
	}

	/**
	 */
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		this.mCheckBox = (CheckBox) holder.findViewById(R.id.widget);

		this.mCheckBox.setOnCheckedChangeListener(null);
		this.mCheckBox.setChecked(this.mValue);
		this.mCheckBox.setOnCheckedChangeListener(this);
		this.mCheckBox.setButtonTintList(this.mColorStateList);
	}

	/**
	 * Handle checkbox changes.
	 */
	@Override
	public void onCheckedChanged(CompoundButton button, boolean state)
	{
		if (!callChangeListener(state))
		{
			return;
		}

		this.mValue = state;

		this.mCheckBox.setChecked(state);
		setSummary(this.getSummary());
		persistBoolean(state);
		notifyDependencyChange(!state);
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getBoolean(index, DEFAULT_VALUE);
	}

	/**
	 * Allow users to select the whole preference to change the checkbox.
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		this.mCheckBox.performClick();
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
			this.mValue = getPersistedBoolean(this.mValue);
		}
		else
		{
			this.mValue = (boolean) defaultValue;

			persistBoolean(this.mValue);
		}
	}

}
