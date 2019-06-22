package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;;

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
	 */
	public NacCheckboxPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacCheckboxPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacCheckboxPreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference_checkbox);
		setOnPreferenceClickListener(this);

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
	 */
	@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);

		Context context = view.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int[][] states = new int[][] {
			new int[] {  android.R.attr.state_checked },
			new int[] { -android.R.attr.state_checked } };
		int[] colors = new int[] { shared.getThemeColor(), Color.LTGRAY };
		ColorStateList colorStateList = new ColorStateList(states, colors);
		this.mCheckBox = (CheckBox) view.findViewById(R.id.widget);

		this.mCheckBox.setChecked(this.mValue);
		this.mCheckBox.setOnCheckedChangeListener(this);
		this.mCheckBox.setButtonTintList(colorStateList);

		this.setSummary();
	}

	/**
	 * Handle checkbox changes.
	 */
	@Override
	public void onCheckedChanged(CompoundButton button, boolean state)
	{
		this.mValue = state;

		this.mCheckBox.setChecked(state);
		this.setSummary();
		persistBoolean(state);
	}

	/**
	 * @return The default value.
	 */
	//@Override
	//protected Object onGetDefaultValue(TypedArray a, int index)
	//{
	//	return (boolean) a.getBoolean(index, NacSharedPreferences.DEFAULT_REPEAT);
	//}

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

	/**
	 * Set the summary text.
	 */
	public void setSummary()
	{
		View root = (View) this.mCheckBox.getParent();
		TextView tv = root.findViewById(android.R.id.summary);
		CharSequence summary = this.getSummary();
		boolean state = this.getChecked();

		if (tv != null)
		{
			tv.setText(summary);
		}

		notifyDependencyChange(!state);
	}

}
