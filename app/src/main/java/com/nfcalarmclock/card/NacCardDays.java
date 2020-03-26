package com.nfcalarmclock;

import android.view.animation.AccelerateInterpolator;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import java.lang.Float;
import java.util.EnumSet;

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
	private NacDayOfWeek mDayButtons;

	/**
	 * Repeat alarm view.
	 */
	private RelativeLayout mRepeat;

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
		this.mDayButtons = (NacDayOfWeek) root.findViewById(R.id.nac_days);
		this.mRepeat = (RelativeLayout) root.findViewById(R.id.nac_repeat);
		this.mDaysAnimation = new NacSlideAnimation(this.mDayButtons);
		this.mMeasure = measure;
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
	private NacDayOfWeek getDayButtons()
	{
		return this.mDayButtons;
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
	public RelativeLayout getRepeat()
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
		NacDayOfWeek dayButtons = this.getDayButtons();

		dayButtons.setStartWeekOn(shared.getStartWeekOn());
		dayButtons.setDays(days);
	}

	/**
	 * Set the repeat button value.
	 */
	public void setRepeat()
	{
		NacAlarm alarm = this.getAlarm();
		boolean repeat = alarm.getRepeat();
		RelativeLayout view = this.getRepeat();

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
		this.mDayButtons.setOnClickListener((NacDayOfWeek.OnClickListener)listener);
		this.mRepeat.setOnClickListener((View.OnClickListener)listener);
		this.mRepeat.setOnLongClickListener((View.OnLongClickListener)listener);
	}

}
