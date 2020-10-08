package com.nfcalarmclock;

import android.view.View;
import android.widget.ImageView;
//import android.widget.RelativeLayout;

/**
 * NFC view for an alarm card.
 */
public class NacCardNfc
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Use NFC checkbox.
	 */
	private ImageView mUseNfc;
	//private RelativeLayout mUseNfc;

	/**
	 */
	public NacCardNfc(View root)
	{
		this.mUseNfc = (ImageView) root.findViewById(R.id.nac_nfc);
		//this.mUseNfc = (RelativeLayout) root.findViewById(R.id.nac_nfc);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The NFC view.
	 */
	//private RelativeLayout getNfcView()
	private ImageView getNfcView()
	{
		return this.mUseNfc;
	}

	/**
	 * Initialize the nfc checkbox.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;
		this.set();
	}

	/**
	 * Set the sound.
	 */
	public void set()
	{
		NacAlarm alarm = this.getAlarm();
		boolean useNfc = alarm.getUseNfc();
		View view = this.getNfcView();

		if (view != null)
		{
			view.setAlpha(useNfc ? 1.0f : 0.3f);
		}
	}

	/**
	 * Set the on checked change listener.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		View view = this.getNfcView();
		if (view != null)
		{
			view.setOnClickListener((View.OnClickListener)listener);
		}
	}

}
