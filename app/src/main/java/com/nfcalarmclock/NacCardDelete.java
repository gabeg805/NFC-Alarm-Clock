package com.nfcalarmclock;

import android.view.View;
import android.widget.LinearLayout;

/**
 * Delete button for an alarm card.
 */
public class NacCardDelete
{

	/**
	 * Delete listener.
	 */
	public interface OnDeleteListener
	{
		public void onDelete(int pos);
	}

	/**
	 * Delete button.
	 */
	 private LinearLayout mDelete;

	/**
	 * Delete listener.
	 */
	public OnDeleteListener mListener;

	/**
	 */
	public NacCardDelete(View root)
	{
		this.mDelete = (LinearLayout) root.findViewById(R.id.nac_delete);
	}

	/**
	 * Call the listener.
	 */
	public void delete(int pos)
	{
		if (this.hasListener())
		{
			this.mListener.onDelete(pos);
		}
	}

	/**
	 * @return True if there is a listener, and False otherwise.
	 */
	public boolean hasListener()
	{
		return (this.mListener != null);
	}

	/**
	 * Set the OnClick listener.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mDelete.setOnClickListener(listener);
	}

	/**
	 * Set the listener to delete the card.
	 */
	public void setOnDeleteListener(OnDeleteListener listener)
	{
		this.mListener = listener;
	}

}
