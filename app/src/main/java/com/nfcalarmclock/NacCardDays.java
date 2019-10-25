package com.nfcalarmclock;

import android.view.animation.AccelerateInterpolator;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
	 * Repeat checkbox.
	 */
	private CheckBox mRepeat;

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
		this.mRepeat = (CheckBox) root.findViewById(R.id.nac_repeat);
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
		//NacDayOfWeek dayButtons = this.getDayButtons();

		//return NacUtility.getHeight(dayButtons);
	}

	/**
	 * @return The repeat checkbox.
	 */
	public CheckBox getRepeat()
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
		//this.changeVisibility(animate);
	}

	public void changeVisibility(boolean animate)
	{
		NacAlarm alarm = this.getAlarm();
		CheckBox repeat = this.getRepeat();
		NacDayOfWeek dayButtons = this.getDayButtons();
		int currentVisibility = dayButtons.getVisibility();
		int newVisibility = alarm.areDaysSelected() ? View.VISIBLE: View.GONE;

		if (!animate)
		{
			dayButtons.setVisibility(newVisibility);
		}
		else if (currentVisibility != newVisibility)
		{
			if (newVisibility == View.GONE)
			{
				this.mDaysAnimation.setupForClose();
			}
			else
			{
				this.mDaysAnimation.setupForOpen();
			}

			dayButtons.setAnimation(this.mDaysAnimation);
			dayButtons.startAnimation(this.mDaysAnimation);
		}
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
		CheckBox repeat = this.getRepeat();

		repeat.setChecked(alarm.getRepeat());
	}

	/**
	 * Set the on click listener for the day buttons.
	 */
	public void setListeners(Object listener)
	{
		this.mDayButtons.setOnClickListener((NacDayOfWeek.OnClickListener)listener);
		this.mRepeat.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener)listener);
	}

}
