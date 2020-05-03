package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;
import androidx.preference.Preference;

/**
 * Preference that prompts the user what format they want to display the next
 * alarm.
 */
public class NacNextAlarmFormatPreference
	extends Preference
	implements Preference.OnPreferenceClickListener,
		NacDialog.OnBuildListener,
		NacDialog.OnShowListener,
		NacDialog.OnDismissListener
{

	/**
	 * Preference value.
	 */
	private int mValue;

	/**
	 */
	public NacNextAlarmFormatPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacNextAlarmFormatPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacNextAlarmFormatPreference(Context context, AttributeSet attrs,
		int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference);
		setOnPreferenceClickListener(this);
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		Resources res = getContext().getResources();

		switch (this.mValue)
		{
			case 1:
				return res.getString(R.string.next_alarm_format_time_on);
			case 0:
			default:
				return res.getString(R.string.next_alarm_format_time_in);
		}
	}

	/**
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		Context context = dialog.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getTitleNextAlarmFormat());
		dialog.setPositiveButton(cons.getActionOk());
		dialog.setNegativeButton(cons.getActionCancel());
	}

	/**
	 * Save the spinner index value.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		View root = dialog.getRoot();
		RadioGroup days = (RadioGroup) root.findViewById(R.id.formats);
		int checkedId = days.getCheckedRadioButtonId();

		switch (checkedId)
		{
			case R.id.nexton:
				this.mValue = 1;
				break;
			case R.id.nextin:
			default:
				this.mValue = 0;
				break;
		}

		persistInt(this.mValue);
		notifyChanged();

		return true;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		Context context = getContext();
		NacSharedDefaults defaults = new NacSharedDefaults(context);
		return (Integer) a.getInteger(index,
			defaults.getNextAlarmFormatIndex());
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		Context context = getContext();
		NacDialog dialog = new NacDialog();

		dialog.saveData(this.mValue);
		dialog.setOnBuildListener(this);
		dialog.addOnDismissListener(this);
		dialog.addOnShowListener(this);
		dialog.build(context, R.layout.dlg_next_alarm_format);
		dialog.show();
		return true;
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	public void onSetInitialValue(Object defaultValue)
	{
		if (defaultValue == null)
		{
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defaultValue;
			persistInt(this.mValue);
		}
	}

	/**
	 * Show the dialog.
	 */
	public void onShowDialog(NacDialog dialog, View root)
	{
		RadioGroup days = (RadioGroup) root.findViewById(R.id.formats);

		switch (this.mValue)
		{
			case 1:
				days.check(R.id.nexton);
				break;
			case 0:
			default:
				days.check(R.id.nextin);
				break;
		}

		dialog.scale(0.9, 0.7, false, true);
	}

}
