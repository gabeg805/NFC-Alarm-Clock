package com.nfcalarmclock;

import android.view.animation.AccelerateInterpolator;
import android.view.View;
import android.widget.LinearLayout;
import java.util.EnumSet;
import com.google.android.material.button.MaterialButton;

/**
 * Days and repeat button for an alarm card.
 */
public class NacCardDays
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Buttons to select which days to repeat the alarm on.
	 */
	private NacDayOfWeek mDayOfWeek;

	/**
	 * Repeat alarm view.
	 */
	private MaterialButton mRepeat;

	/**
	 * Slide animation for the day buttons.
	 */
	private NacSlideAnimation mDaysAnimation;

	/**
	 * Card measurement.
	 */
	private NacCardMeasure mMeasure;

	/**
	 */
	public NacCardDays(View root, NacCardMeasure measure)
	{
		//this.mDayOfWeek = (NacDayOfWeek) root.findViewById(R.id.nac_days);
		//this.mDaysAnimation = new NacSlideAnimation(this.mDayOfWeek);
		LinearLayout dowView = root.findViewById(R.id.nac_days);
		this.mRepeat = (MaterialButton) root.findViewById(R.id.nac_repeat);
		this.mDaysAnimation = new NacSlideAnimation(dowView);
		this.mMeasure = measure;

		this.mDayOfWeek = new NacDayOfWeek(dowView);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The day of week buttons.
	 */
	private NacDayOfWeek getDayOfWeek()
	{
		return this.mDayOfWeek;
	}

	/**
	 * @return The height of the day buttons.
	 */
	public int getHeight()
	{
		return this.mMeasure.getDayButtonsHeight();
	}

	/**
	 * @return The repeat view.
	 */
	public MaterialButton getRepeatView()
	{
		return this.mRepeat;
	}

	/**
	 * Initialize the days and repeat button.
	 */
	public void init(NacSharedPreferences shared, NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.mDaysAnimation.setInterpolator(new AccelerateInterpolator());
		this.mDaysAnimation.setDuration(400);
		this.mDaysAnimation.setHideOnEnd();
		this.set(shared, false);
	}

	/**
	 * @see set
	 */
	public void set(NacSharedPreferences shared)
	{
		this.set(shared, true);
	}

	/**
	 * Set the days and repeat button values.
	 */
	public void set(NacSharedPreferences shared, boolean animate)
	{
		this.setDays(shared);
		this.setRepeat();
	}

	/**
	 * Set the day button values.
	 */
	public void setDays(NacSharedPreferences shared)
	{
		NacAlarm alarm = this.getAlarm();
		EnumSet<NacCalendar.Day> days = alarm.getDays();
		NacDayOfWeek dow = this.getDayOfWeek();

		dow.setStartWeekOn(shared.getStartWeekOn());
		dow.setDays(days);
	}

	/**
	 * Set the repeat button value.
	 */
	public void setRepeat()
	{
		NacAlarm alarm = this.getAlarm();
		boolean repeat = alarm.getRepeat();
		View view = this.getRepeatView();

		if (!alarm.areDaysSelected())
		{
			view.setEnabled(false);
			view.setAlpha(0.3f);
		}
		else
		{
			view.setEnabled(true);
			view.setAlpha(repeat ? 1.0f : 0.3f);
		}
	}

	/**
	 * Set the on click listener for the day buttons.
	 */
	public void setListeners(Object listener)
	{
		NacDayOfWeek dayButtons = this.getDayOfWeek();
		View repeatView = this.getRepeatView();

		dayButtons.setOnClickListener((NacDayOfWeek.OnClickListener)listener);
		repeatView.setOnClickListener((View.OnClickListener)listener);
		repeatView.setOnLongClickListener((View.OnLongClickListener)listener);
	}

}
