package com.nfcalarmclock;

/**
 * @brief Alarm card information object.
 */
public class NacCardInfo
{

	/**
	 * @brief Information listener.
	 */
	private OnChangedListener mListener;

	/**
	 * @brief Definition for the information listener object.
	 */
	public interface OnChangedListener
	{
		public void onChanged(Alarm alarm);
	}

	/**
	 */
	public NacCardInfo()
	{
		this.mListener = null;
	}

	/**
	 * @brief Call the listener when alarm info has changed.
	 */
	public void changed(Alarm alarm)
	{
		if (this.mListener != null)
		{
			NacUtility.print("Calling NacCardInfo Changed().");
			this.mListener.onChanged(alarm);
		}
	}

	/**
	 * @brief Set an event listener.
	 * 
	 * @param  listener  The event listener.
	 */
	public void setOnChangedListener(OnChangedListener listener)
	{
		this.mListener = listener;
	}

}
