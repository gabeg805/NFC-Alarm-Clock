package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;

/**
 * Preference that displays how long to wait before auto dismissing the alarm.
 */
public abstract class NacPreferenceValuePicker
	extends Preference
	implements Preference.OnPreferenceClickListener,NacDialog.OnDismissListener,NacDialog.OnBuildListener,NacDialog.OnShowListener,View.OnClickListener
{

	/**
	 * @return The title of the dialog.
	 */
	public abstract String getDialogTitle();

	/**
	 * Setup the value picker.
	 */
	public abstract void setupValuePicker(View root);

	/**
	 * Direction to scroll the number picker.
	 */
	protected enum Direction
	{
		INCREMENT,
		DECREMENT
	}

	/**
	 * Value of days.
	 */
	protected int mValue;

	/**
	 * Number picker.
	 */
	protected NumberPicker mPicker;

	/**
	 */
	public NacPreferenceValuePicker(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceValuePicker(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceValuePicker(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.pref_value_picker);
		setOnPreferenceClickListener(this);

		this.mPicker = null;
	}

	/**
	 * @return The layout for the dialog.
	 */
	public int getDialogLayout()
	{
		return R.layout.dlg_value_picker;
	}

	/**
	 * @return The max value of the value picker.
	 */
	public int getMaxValue()
	{
		return (this.mPicker == null) ? 0 : this.mPicker.getMaxValue();
	}

	/**
	 * @return The min value of the value picker.
	 */
	public int getMinValue()
	{
		return (this.mPicker == null) ? 0 : this.mPicker.getMinValue();
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
		builder.setTitle(getDialogTitle());
		dialog.setPositiveButton("OK");
		dialog.setNegativeButton("Cancel");
	}

	/**
	 * Listener for click events on the inc/decrement buttons.
	 */
	@Override
	public void onClick(View v)
	{
		Direction dir = (Direction) v.getTag();
		int min = this.getMinValue();
		int max = this.getMaxValue();
		int value = this.mValue;

		if (dir == Direction.INCREMENT)
		{
			this.mValue = ((value+1) < max) ? value+1 : max;
		}
		else if (dir == Direction.DECREMENT)
		{
			this.mValue = ((value-1) >= min) ? value-1 : min;
		}
		else
		{
			return;
		}

		this.mPicker.setValue(this.mValue);
	}

	/**
	 * Save the selected days when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.mValue = this.mPicker.getValue();

		this.setSummary(this.getSummary());
		persistInt(this.mValue);

		return true;
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
		dialog.build(context, getDialogLayout());
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
		this.mPicker = root.findViewById(R.id.picker);
		ImageButton increment = root.findViewById(R.id.increment);
		ImageButton decrement = root.findViewById(R.id.decrement);

		setupValuePicker(root);
		increment.setOnClickListener(this);
		decrement.setOnClickListener(this);
		increment.setTag(Direction.INCREMENT);
		decrement.setTag(Direction.DECREMENT);
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
