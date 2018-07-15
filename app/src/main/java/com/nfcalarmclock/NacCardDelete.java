package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * @brief Delete the alarm and its card.
 */
public class NacCardDelete
{

    /**
     * @brief Delete button.
     */
     private ImageTextButton mDelete;

    /**
     * @brief Constructor.
     */
    public NacCardDelete(View r)
    {
        this.mDelete = (ImageTextButton) r.findViewById(R.id.nacDelete);
    }

	/**
	 * @brief Initialize the delete button.
	 */
	public void init(int pos)
	{
		this.mDelete.setTag(pos);
	}

	/**
	 * @brief Set the click listener for the delete button.
	 */
	public void setListener(View.OnClickListener listener)
	{
		this.mDelete.setOnClickListener(listener);
	}

}
