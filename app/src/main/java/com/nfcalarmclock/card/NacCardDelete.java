package com.nfcalarmclock;

import android.view.View;

/**
 * Delete the alarm and its card.
 */
public class NacCardDelete
{

	/**
	 * Delete button.
	 */
	 private NacImageTextButton mDelete;

	/**
	 */
	public NacCardDelete(View root)
	{
		this.mDelete = (NacImageTextButton) root.findViewById(R.id.nacDelete);
	}

	/**
	 * Set the click listener for the delete button.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mDelete.setOnClickListener(listener);
	}

}
