package com.nfcalarmclock;

import android.view.View;
import android.widget.NumberPicker;
import com.google.android.material.button.MaterialButton;

/**
 * Create a spinner dialog.
 */
public class NacSpinnerDialog
	extends NacDialog
	implements View.OnClickListener,
		NacDialog.OnShowListener
{

	/**
	 * Direction to scroll the number picker.
	 */
	public enum Direction
	{
		INCREMENT,
		DECREMENT
	}

	/**
	 * Number picker.
	 */
	private NumberPicker mPicker;

	/**
	 */
	public NacSpinnerDialog()
	{
		super(R.layout.dlg_value_picker);
		this.mPicker = null;
		this.addOnShowListener(this);
	}

	/**
	 * @return The displayed value.
	 */
	@SuppressWarnings("unused")
	public String getCurrentDisplayedValue()
	{
		int index = this.getValue();
		return this.getDisplayedValue(index);
	}

	/**
	 * @return The displayed value.
	 */
	public String getDisplayedValue(int index)
	{
		NumberPicker picker = this.getPicker();

		if (picker == null)
		{
			return null;
		}
		else if (index < 0)
		{
			return "";
		}
		else
		{
			return picker.getDisplayedValues()[index];
		}
	}

	/**
	 * @return The max value of the value picker.
	 */
	public int getMaxValue()
	{
		NumberPicker picker = this.getPicker();
		return (picker != null) ? picker.getMaxValue() : -1;
	}

	/**
	 * @return The min value of the value picker.
	 */
	public int getMinValue()
	{
		NumberPicker picker = this.getPicker();
		return (picker != null) ? picker.getMinValue() : -1;
	}

	/**
	 * @return The value picker.
	 */
	public NumberPicker getPicker()
	{
		return this.mPicker;
	}

	/**
	 * @return The current value.
	 */
	public int getValue()
	{
		NumberPicker picker = this.getPicker();
		return (picker != null) ? picker.getValue() : -1;
	}

	/**
	 * Listener for click events on the inc/decrement buttons.
	 */
	@Override
	public void onClick(View view)
	{
		Direction dir = (Direction) view.getTag();
		int max = this.getMaxValue();
		int value = this.mPicker.getValue();

		if (dir == Direction.INCREMENT)
		{
			value = (value+1) % (max+1);
		}
		else if (dir == Direction.DECREMENT)
		{
			value = (max+value-1) % (max+1);
		}
		else
		{
			return;
		}

		this.setValue(value);
	}

	/**
	 * Set the days in the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		this.mPicker = root.findViewById(R.id.picker);
		MaterialButton increment = root.findViewById(R.id.increment);
		MaterialButton decrement = root.findViewById(R.id.decrement);

		increment.setTag(Direction.INCREMENT);
		decrement.setTag(Direction.DECREMENT);
		increment.setOnClickListener(this);
		decrement.setOnClickListener(this);
		dialog.scale(0.7, 0.7, false, true);
	}

	/**
	 * Set the displayed values.
	 */
	public void setDisplayedValues(String[] values)
	{
		NumberPicker picker = this.getPicker();
		int min = this.getMinValue();
		int max = this.getMaxValue();

		if (min == max)
		{
			max = values.length - 1;
		}

		picker.setDisplayedValues(values);
		picker.setMinValue(min);
		picker.setMaxValue(max);
	}

	/**
	 * Set the value.
	 */
	public void setValue(int value)
	{
		NumberPicker picker = this.getPicker();

		if (picker == null)
		{
			return;
		}

		int min = this.getMinValue();
		int max = this.getMaxValue();

		if ((value >= min) && (value <= max))
		{
			picker.setValue(value);
		}
	}

}
