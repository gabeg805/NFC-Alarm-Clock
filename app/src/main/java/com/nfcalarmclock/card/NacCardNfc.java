package com.nfcalarmclock;

import android.view.View;
//import android.widget.CompoundButton;
//import android.widget.CheckBox;
import android.widget.RelativeLayout;

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
	private RelativeLayout mUseNfc;
	//private CheckBox mUseNfc;

	/**
	 */
	public NacCardNfc(View root)
	{
		this.mUseNfc = (RelativeLayout) root.findViewById(R.id.nac_nfc);
		//this.mUseNfc = (CheckBox) root.findViewById(R.id.nac_nfc);
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
	private RelativeLayout getNfc()
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
		RelativeLayout view = this.getNfc();

		view.setAlpha(useNfc ? 1.0f : 0.3f);
		//this.mUseNfc.setChecked(alarm.getUseNfc());
	}

	/**
	 * Set the on checked change listener.
	 */
	//public void setOnCheckedChangeListener(
	//	CompoundButton.OnCheckedChangeListener listener)
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mUseNfc.setOnClickListener((View.OnClickListener)listener);
		//this.mUseNfc.setOnCheckedChangeListener(
		//	(CompoundButton.OnCheckedChangeListener)listener);
	}

}
