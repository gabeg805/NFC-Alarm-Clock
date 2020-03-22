package com.nfcalarmclock;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.CheckBox;

/**
 * Use NFC checkbox for an alarm card.
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
	private CheckBox mUseNfc;

	/**
	 */
	public NacCardNfc(View root)
	{
		this.mUseNfc = (CheckBox) root.findViewById(R.id.nac_nfc);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
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

		this.mUseNfc.setChecked(alarm.getUseNfc());
	}

	/**
	 * Set the on checked change listener.
	 */
	public void setOnCheckedChangeListener(
		CompoundButton.OnCheckedChangeListener listener)
	{
		this.mUseNfc.setOnCheckedChangeListener(
			(CompoundButton.OnCheckedChangeListener)listener);
	}

}
