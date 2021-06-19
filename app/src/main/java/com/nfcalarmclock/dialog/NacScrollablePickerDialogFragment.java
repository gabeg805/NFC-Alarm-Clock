package com.nfcalarmclock.dialog;

import android.app.AlertDialog;
import android.widget.NumberPicker;

import com.nfcalarmclock.R;

import java.util.List;

/**
 */
public abstract class NacScrollablePickerDialogFragment
	extends NacDialogFragment
{

	/**
	 * Listener for when the scrollable picker option is selected.
	 */
	public interface OnScrollablePickerOptionSelectedListener
	{
		public void onScrollablePickerOptionSelected(int index);
	}

	/**
	 * Scrollable picker.
	 */
	private NumberPicker mScrollablePicker;

	/**
	 * Default index for the scrollable picker.
	 */
	private int mDefaultScrollablePickerIndex;

	/**
	 * Listener for when the scrollable picker option is selected.
	 */
	private OnScrollablePickerOptionSelectedListener
		mOnScrollablePickerOptionSelectedListener;

	/**
	 * Call the OnScrollablePickerOptionSelectedListener object, if it has been set.
	 */
	public void callOnScrollablePickerOptionSelectedListener()
	{
		OnScrollablePickerOptionSelectedListener listener =
			this.getOnScrollablePickerOptionSelectedListener();

		if (listener != null)
		{
			NumberPicker picker = this.getScrollablePicker();
			int index = picker.getValue();

			listener.onScrollablePickerOptionSelected(index);
		}
	}

	/**
	 * Get the default index for the scrollable picker.
	 *
	 * @return The default index for the scrollable picker.
	 */
	public int getDefaultScrollablePickerIndex()
	{
		return this.mDefaultScrollablePickerIndex;
	}

	/**
	 * Get the OnScrollablePickerOptionSelectedListener object.
	 *
	 * @return The OnScrollablePickerOptionSelectedListener object.
	 */
	public OnScrollablePickerOptionSelectedListener
		getOnScrollablePickerOptionSelectedListener()
	{
		return this.mOnScrollablePickerOptionSelectedListener;
	}

	/**
	 * Get the scrollable picker.
	 *
	 * @return The scrollable picker.
	 */
	private NumberPicker getScrollablePicker()
	{
		return this.mScrollablePicker;
	}

	/**
	 * Get the list of values for the scrollable picker.
	 *
	 * @return The list of values for the scrollable picker for the scrollable
	 *     picker.
	 */
	public abstract List<String> getScrollablePickerValues();

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		AlertDialog dialog = (AlertDialog) getDialog();
		this.mScrollablePicker = dialog.findViewById(R.id.scrollable_picker);

		this.setupScrollablePicker();
	}

	/**
	 * Set the default index for the scrollable picker.
	 *
	 * @param  index  The default index for the scrollable picker.
	 */
	public void setDefaultScrollablePickerIndex(int index)
	{
		this.mDefaultScrollablePickerIndex = index;
	}

	/**
	 * Set the OnScrollablePickerOptionSelectedListener object.
	 *
	 * @param  listener  The OnScrollablePickerOptionSelectedListener object.
	 *
	 * @return This class.
	 */
	public void setOnScrollablePickerOptionSelectedListener(
		OnScrollablePickerOptionSelectedListener listener)
	{
		this.mOnScrollablePickerOptionSelectedListener = listener;
	}

	/**
	 * Setup the scrollable picker.
	 */
	private void setupScrollablePicker()
	{
		NumberPicker picker = this.getScrollablePicker();
		List<String> values = this.getScrollablePickerValues();
		int index = this.getDefaultScrollablePickerIndex();

		picker.setMinValue(0);
		picker.setMaxValue(values.size()-1);
		picker.setDisplayedValues(values.toArray(new String[0]));
		picker.setValue(index);
	}

}
