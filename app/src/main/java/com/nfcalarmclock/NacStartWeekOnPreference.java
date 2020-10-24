package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.preference.Preference;
import java.util.List;

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
	 * @return The index of the radio button that is currently checked.
	 */
	private int getCheckedIndex(View root)
	{
		RadioGroup group = this.getRadioGroup(root);
		int count = group.getChildCount();

		for (int i=0; i < count; i++)
		{
			RadioButton button = (RadioButton) group.getChildAt(i);
			if (button.isChecked())
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * @return The radio button at the given index.
	 */
	public RadioButton getRadioButton(View root, int index)
	{
		RadioGroup group = this.getRadioGroup(root);
		return (RadioButton) group.getChildAt(index);
	}

	/**
	 * @return The radio group.
	 */
	public RadioGroup getRadioGroup(View root)
	{
		return root.findViewById(R.id.start_week_on);
	}

	/**
	 */
	@Override
	public CharSequence getSummary()
	{
		Context context = getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		List<String> week = cons.getDaysOfWeek();
		int index = this.mValue;

		switch (index)
		{
			default:
				index = 0;
			//case 6:
			//	return "Saturday";
			case 1:
			case 0:
				return week.get(index);
		}
	}

	/**
	 * Inflate the radio buttons.
	 */
	private void inflateRadioButtons(View root, NacSharedConstants cons)
	{
		Context context = getContext();
		RadioGroup group = this.getRadioGroup(root);
		List<String> week = cons.getDaysOfWeek();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);

		for (int i=0; i < 2; i++)
		{
			View view = inflater.inflate(R.layout.radio_button, group, true);
			RadioButton button = view.findViewById(R.id.radio_button);
			String day = week.get(i);

			button.setId(button.generateViewId());
			button.setText(day);
		}
	}

	/**
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		Context context = getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		View root = dialog.getRoot();

		builder.setTitle(cons.getStartWeekOnTitle());
		dialog.setPositiveButton(cons.getActionOk());
		dialog.setNegativeButton(cons.getActionCancel());
		this.inflateRadioButtons(root, cons);
	}

	/**
	 * Save the spinner index value.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		View root = dialog.getRoot();
		this.mValue = this.getCheckedIndex(root);

		persistInt(this.mValue);
		shared.editShouldRefreshMainActivity(true);
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
		this.setCheckedRadioButton(root);
		dialog.scale(0.6, 0.7, false, true);
	}

	/**
	 * Set checked radio button.
	 */
	protected void setCheckedRadioButton(View root)
	{
		int index = this.mValue;
		RadioButton button = this.getRadioButton(root, index);

		button.setChecked(true);
	}

}
