
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
	implements Preference.OnPreferenceClickListener,View.OnClickListener,NacDialog.OnDismissedListener
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

		this.mAlarm = new Alarm();
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

		this.mTextView.setText(this.mValue);
		this.mTextView.setOnClickListener(this);
		this.mTextView.setMaxWidth(width);
	}

	/**
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		this.mValue = this.mAlarm.getName();

		this.mTextView.setText(this.mValue);
		persistString(this.mValue);
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		NacCardNameDialog dialog = new NacCardNameDialog(this.mAlarm);
		Context context = this.mTextView.getContext();

		dialog.build(context, R.layout.dlg_alarm_name);
		dialog.addDismissListener(this);
		dialog.show();

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

