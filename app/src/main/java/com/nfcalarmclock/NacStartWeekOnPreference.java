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
 * Preference that prompts the user what day to start the week on.
 */
public class NacStartWeekOnPreference
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
	public NacStartWeekOnPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacStartWeekOnPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacStartWeekOnPreference(Context context, AttributeSet attrs,
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
		Context context = getContext();
		NacSharedConstants cons = new NacSharedConstants(context);

		switch (this.mValue)
		{
			//case 6:
			//	return "Saturday";
			case 1:
				return cons.getMonday();
			case 0:
			default:
				return cons.getSunday();
		}
	}

	/**
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		Context context = getContext();
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getStartWeekOnTitle());
		dialog.setPositiveButton(cons.getOk());
		dialog.setNegativeButton(cons.getCancel());
	}

	/**
	 * Save the spinner index value.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		View root = dialog.getRoot();
		RadioGroup days = (RadioGroup) root.findViewById(R.id.days);
		int checkedId = days.getCheckedRadioButtonId();

		switch (checkedId)
		{
			//case R.id.saturday:
			//	this.mValue = 6;
			//	break;
			case R.id.monday:
				this.mValue = 1;
				break;
			case R.id.sunday:
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
			defaults.getStartWeekOnIndex());
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
		dialog.build(context, R.layout.dlg_start_week_on);
		dialog.show();
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
		RadioGroup days = (RadioGroup) root.findViewById(R.id.days);

		switch (this.mValue)
		{
			//case 6:
			//	break;
			case 1:
				days.check(R.id.monday);
				break;
			case 0:
			default:
				days.check(R.id.sunday);
				break;
		}

		dialog.scale(0.6, 0.7, false, true);
	}

}
