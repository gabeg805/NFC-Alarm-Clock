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
     private ImageTextButton mDelete;

    /**
     */
    public NacCardDelete(View root)
    {
        this.mDelete = (ImageTextButton) root.findViewById(R.id.nacDelete);
    }

	/**
	 * Set the click listener for the delete button.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mDelete.setOnClickListener(listener);
	}

}
