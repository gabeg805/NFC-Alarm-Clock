package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Preference that displays the max allowable number of snoozes.
 */
public class NacPreferenceMaxSnooze
	extends Preference
	implements Preference.OnPreferenceClickListener,NacDialog.OnDismissedListener,NacDialog.OnBuildListener,NacDialog.OnShowListener
{

	/**
	 * Value of days.
	 */
	protected int mValue;

	/**
	 */
	public NacPreferenceMaxSnooze(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceMaxSnooze(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceMaxSnooze(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.pref_days);
		setOnPreferenceClickListener(this);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		if (this.mValue == 0)
		{
			return "None";
		}
		else if (this.mValue < 0)
		{
			return "Unlimited";
		}
		else
		{
			return String.valueOf(this.mValue);
		}
	}

	/**
	 * Set the summary text.
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);
		this.setSummary(this.getSummary());
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		String title = "Max Snoozes";

		builder.setTitle(title);
		dialog.setPositiveButton("OK");
		dialog.setNegativeButton("Cancel");
	}

	/**
	 * Save the selected days when the dialog is dismissed.
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		View root = dialog.getRootView();
		int[] ids = {R.id.radio_none, R.id.radio1, R.id.radio2, R.id.radio3, R.id.radio4, R.id.radio5, R.id.radio_unlimited};
		RadioButton button;

		for (int i=0; i < ids.length; i++)
		{
			button = root.findViewById(ids[i]);

			if (button.isChecked())
			{
				this.mValue = (i+1 == ids.length) ? -1 : i;
				break;
			}
		}

		this.setSummary(this.getSummary());
		persistInt(this.mValue);
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index, -1);
	}

	/**
	 * Display the dialog when the preference is selected.
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		Context context = getContext();
		NacDialog dialog = new NacDialog();

		dialog.setOnBuildListener(this);
		dialog.setOnShowListener(this);
		dialog.build(context, R.layout.dlg_max_snoozes);
		dialog.addDismissListener(this);
		dialog.show();

		return true;
	}

	/**
	 * Set the days in the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		RadioGroup group = root.findViewById(R.id.radio_group);
		int[] ids = {R.id.radio_none, R.id.radio1, R.id.radio2, R.id.radio3, R.id.radio4, R.id.radio5, R.id.radio_unlimited};
		RadioButton button;

		for (int i=0; i < ids.length; i++)
		{
			button = root.findViewById(ids[i]);

			if ((this.mValue < 0) && (i+1 == ids.length))
			{
				button.setChecked(true);
			}
			else
			{
				if (i == this.mValue)
				{
					button.setChecked(true);
				}
			}
		}
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(boolean restore, Object defval)
	{
		if (restore)
		{
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defval;

			persistInt(this.mValue);
		}
	}

}
