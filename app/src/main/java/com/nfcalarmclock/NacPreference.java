package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.util.TypedValue;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NacPreference
	extends Preference
{

	/**
	 * Types of values for the default objects.
	 */
	public enum ValueType
	{
		NONE,
		BOOLEAN,
		FLOAT,
		INT,
		LONG,
		STRING
	}

	/**
	 * Title of the preference.
	 */
	protected TextView mTitle;

	/**
	 * Summary of the preference.
	 */
	protected TextView mSummary;

	/**
	 * Array of the summary text(s).
	 */
	protected String[] mSummaryText;

	/**
	 * Generic object that serves as the value.
	 */
	protected Object mObject;

	/**
	 * Default constant value for the object.
	 */
	protected Object mDefault;

	/**
	 * Value type of the objects.
	 */
	protected ValueType mType; // = ValueType.NONE;

	/**
	 * Summary text when enabling/disabling the preference.
	 */
	protected String[] mSummaryChoice;

	/**
	 */
	public NacPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);

		try
		{
			this.mSummaryChoice = null;
			Theme theme = context.getTheme();
			TypedArray a = theme.obtainStyledAttributes(attrs,
				R.styleable.NacPreference, 0, 0);
			String enabled = a.getString(R.styleable.NacPreference_summaryEnabled);
			String disabled = a.getString(R.styleable.NacPreference_summaryDisabled);

			if (enabled == null || disabled == null)
			{
				throw new Resources.NotFoundException();
			}

			this.mSummaryChoice = new String[2];
			this.mSummaryChoice[0] = enabled;
			this.mSummaryChoice[1] = disabled;
		}
		catch (Resources.NotFoundException e)
		{
		}
	}

	/**
	 * Inflate the desired layout.
	 *
	 * @see onCreateView
	 */
	protected View onCreatePreferenceView(ViewGroup parent, int id)
	{
		Context context = getContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(id, parent, false);
	}

	/**
	 * Bind the title and summary sections of the preference view, and leave
	 * the rest to the user.
	 *
	 * Set the width of the title and summary to leave space for the other
	 * view(s) the user may use.
	 */
	protected void onBindPreferenceView(View v)
	{
		this.mTitle = (TextView) v.findViewById(android.R.id.title);
		this.mSummary = (TextView) v.findViewById(android.R.id.summary);
		float width = 3f * this.getDisplayWidth() / 4f;

		this.setSummary();
		this.setTextWidth((int)width);
	}

	/**
	 * Set the default value.
	 */
	public void setDefault(Object obj)
	{
		this.mDefault = obj;
		this.mObject = obj;
	}

	/**
	 * Set the summary text.
	 */
	public void setSummary()
	{
		if (this.mType == ValueType.BOOLEAN)
		{
			this.setSummary((boolean)this.mObject);
		}
	}

	/**
	 * Set the summary text.
	 */
	public void setSummary(String text)
	{
		this.mSummary.setText(text);
	}

	/**
	 * Set the summary text from the enable/disable choices.
	 */
	public void setSummary(boolean state)
	{
		if (this.mSummaryChoice == null)
		{
			return;
		}

		String text = this.mSummary.getText().toString();
		String enabled = this.mSummaryChoice[0];
		String disabled = this.mSummaryChoice[1];

		NacUtility.printf("Text : %s", text);
		NacUtility.printf("Enabled : %s", enabled);
		NacUtility.printf("Disabled : %s", disabled);
		if (state)
		{
			this.mSummary.setText(enabled);
		}
		else
		{
			this.mSummary.setText(disabled);
		}
	}

	/**
	 * Set the width of the title and summary text.
	 */
	public void setTextWidth(int width)
	{
		this.mTitle.setWidth(width);
		this.mSummary.setWidth(width);
	}

	/**
	 * Set the value type.
	 */
	public void setType(ValueType type)
	{
		this.mType = type;
	}

	/**
	 * Set the persist value.
	 */
	public Object setPreferencePersist(Object defval)
	{
		switch (this.mType)
		{
			case BOOLEAN:
				persistBoolean((boolean)defval);
				break;
			case FLOAT:
				persistFloat((float)defval);
				break;
			case INT:
				persistInt((Integer)defval);
				break;
			case LONG:
				persistLong((long)defval);
				break;
			case STRING:
				persistString((String)defval);
				break;
			case NONE:
			default:
				return null;
		}

		return (this.mObject = defval);
	}

	/**
	 * @return The value.
	 */
	public Object getValue()
	{
		return this.mObject;
	}

	/**
	 * @return The summary.
	 */
	public String getSummary(boolean state)
	{
		return (state) ? this.mSummaryText[0] : this.mSummaryText[1];
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

	public String getString(int id)
	{
		return getContext().getResources().getString(id);
	}

	/**
	 * @return The persisted value.
	 */
	public Object getPreferencePersisted(Object obj)
	{
		switch (this.mType)
		{
			case BOOLEAN:
				return getPersistedBoolean((boolean)obj);
			case FLOAT:
				return getPersistedFloat((float)obj);
			case INT:
				return getPersistedInt((Integer)obj);
			case LONG:
				return getPersistedLong((long)obj);
			case STRING:
				return getPersistedString((String)obj);
			case NONE:
			default:
				return null;
		}
	}

	/**
	 * Create the preference view.
	 */
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		super.onCreateView(parent);

		return this.onCreatePreferenceView(parent, 0);
	}

	/**
	 * Bind the preference view.
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);
		this.onBindPreferenceView(v);
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		if (this.mType == null)
		{
			return null;
		}

		switch (this.mType)
		{
			case BOOLEAN:
				return (boolean) a.getBoolean(index, (boolean) mDefault);
			case FLOAT:
				return (float) a.getFloat(index, (float) mDefault);
			case INT:
				return (Integer) a.getInt(index, (Integer) mDefault);
			case LONG:
				return (long) a.getInt(index, (int) mDefault);
			case STRING:
				return (String) a.getString(index);
			case NONE:
			default:
				return null;
		}
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(boolean restore, Object defval)
	{
		this.mObject = (restore) ? getPreferencePersisted(this.mObject)
			: setPreferencePersist(defval);
	}

}
