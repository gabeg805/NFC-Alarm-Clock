
package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Name preference.
 */
public class NacPreferenceName
	extends Preference
	implements Preference.OnPreferenceClickListener,View.OnClickListener
{

	/**
	 * Text view with name of alarm.
	 */
	private TextView mTextView;

	/**
	 * Check value.
	 */
	protected String mValue;

	/**
	 * Default constant value for the object.
	 */
	protected String mDefault;

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

		this.mTextView = (TextView) v.findViewById(R.id.widget);
		int width = (this.getDisplayWidth() / 4);

		this.mTextView.setOnClickListener(this);
		this.mTextView.setMaxWidth(width);
	}

	/**
	 */
	//@Override
	//public void onCheckedChanged(CompoundButton button, boolean state)
	//{
	//	this.mValue = state;
	//	this.mCheckBox.setChecked(state);
	//	notifyChanged();

	//	persistBoolean(this.mValue);
	//}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		NacUtility.printf("Preference was clicked!");
		return true;
	}

	@Override
	public void onClick(View v)
	{
		String text = ((TextView)v).getText().toString();

		Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
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

	/**
	 * @return The width of the display.
	 */
	private int getDisplayWidth()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		AppCompatActivity act = (AppCompatActivity) getContext();

		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		return metrics.widthPixels;
	}

}

