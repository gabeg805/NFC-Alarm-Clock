package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import java.util.EnumSet;

/**
 * A button that consists of an image to the left, and text to the right of it.
 */
public class NacDayOfWeek
	implements NacDayButton.OnClickListener
{

	/**
	 * Listener for click events.
	 */
	public interface OnClickListener
	{
		public void onClick(NacDayButton button, NacCalendar.Day day);
	}

	/**
	 * Day of week view.
	 */
	private LinearLayout mDayOfWeekView;

	/**
	 * Click event listener.
	 */
	private NacDayOfWeek.OnClickListener mListener;

	/**
	 */
	public NacDayOfWeek(LinearLayout view)
	{
		this.mDayOfWeekView = view;
		this.mListener = null;

		int count = view.getChildCount();

		for (int i=0; i < count; i++)
		{
			NacDayButton button = (NacDayButton) view.getChildAt(i);
			button.setOnClickListener(this);
			//button.setTag(i);
		}
	}

	/**
	 * @return True if any days are selected, and False otherwise.
	 */
	public boolean areDaysSelected()
	{
		return !this.getDays().isEmpty();
	}

	/**
	 * Convert a particular day to its corresponding view ID.
	 */
	private int dayToId(NacCalendar.Day day)
	{
		switch (day)
		{
			case SUNDAY:
				return R.id.dow_sun;
			case MONDAY:
				return R.id.dow_mon;
			case TUESDAY:
				return R.id.dow_tue;
			case WEDNESDAY:
				return R.id.dow_wed;
			case THURSDAY:
				return R.id.dow_thu;
			case FRIDAY:
				return R.id.dow_fri;
			case SATURDAY:
				return R.id.dow_sat;
			default:
				break;
		}
		return 0;
	}

	/**
	 * @return The day button given a particular day.
	 */
	private NacDayButton getDayButton(NacCalendar.Day day)
	{
		LinearLayout view = this.getDayOfWeekView();
		int id = this.dayToId(day);
		return (NacDayButton) view.findViewById(id);
	}

	/**
	 * @return The day of week view.
	 */
	public LinearLayout getDayOfWeekView()
	{
		return this.mDayOfWeekView;
	}

	/**
	 * @return The alarm days.
	 */
	public EnumSet<NacCalendar.Day> getDays()
	{
		EnumSet<NacCalendar.Day> days = EnumSet.noneOf(NacCalendar.Day.class);

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			if (this.isDayEnabled(d))
			{
				days.add(d);
			}
		}

		return days;
	}

	/**
	 * Convert an view ID to its corresponding day.
	 */
	private NacCalendar.Day idToDay(int id)
	{
		switch (id)
		{
			case R.id.dow_sun:
				return NacCalendar.Day.SUNDAY;
			case R.id.dow_mon:
				return NacCalendar.Day.MONDAY;
			case R.id.dow_tue:
				return NacCalendar.Day.TUESDAY;
			case R.id.dow_wed:
				return NacCalendar.Day.WEDNESDAY;
			case R.id.dow_thu:
				return NacCalendar.Day.THURSDAY;
			case R.id.dow_fri:
				return NacCalendar.Day.FRIDAY;
			case R.id.dow_sat:
				return NacCalendar.Day.SATURDAY;
			default:
				break;
		}
		return null;
	}

	/**
	 * @return True if the button is enabled and false if it is not.
	 */
	public boolean isDayEnabled(NacCalendar.Day day)
	{
		NacDayButton button = this.getDayButton(day);
		return button.isEnabled();
	}

	/**
	 */
	@Override
	public void onClick(NacDayButton button)
	{
		int id = button.getId();
		NacCalendar.Day day = this.idToDay(id);

		if (day == null)
		{
			return;
		}

		//this.mButtons[index].animateToggle();
		button.animateToggle();

		if (this.mListener == null)
		{
			return;
		}

		this.mListener.onClick(button, day);
	}

	/**
	 * Set the days that will be enabled/disabled.
	 *
	 * @param  days  The button days that will be enabled.
	 */
	public void setDays(EnumSet<NacCalendar.Day> days)
	{
		//int index = 0;
		//NacUtility.printf("Setting days!");

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			if (days.contains(d))
			{
				//NacUtility.printf("Enable day : %s", d.name());
				this.getDayButton(d).enable();
				//this.mButtons[index].enable();
			}
			else
			{
				//NacUtility.printf("Disable day : %s", d.name());
				//this.mButtons[index].disable();
				this.getDayButton(d).disable();
			}

			//index++;
		}
	}

	/**
	 * @see setDays
	 */
	public void setDays(int value)
	{
		EnumSet<NacCalendar.Day> days = NacCalendar.Days.valueToDays(value);
		this.setDays(days);
	}

	/**
	 * Set the day to start week on.
	 */
	public void setStartWeekOn(int start)
	{
		//NacDayButton sunday = this.mButtons[0];
		//NacDayButton monday = this.mButtons[1];
		//View firstChild = getChildAt(0);
		//View lastChild = getChildAt(6);

		LinearLayout view = this.getDayOfWeekView();
		NacDayButton sunday = this.getDayButton(NacCalendar.Day.SUNDAY);
		NacDayButton monday = this.getDayButton(NacCalendar.Day.MONDAY);
		View firstChild = view.getChildAt(0);
		View lastChild = view.getChildAt(6);

		if (start == 1)
		{
			if (firstChild.getId() != monday.getId())
			{
				view.removeView(firstChild);
				view.addView(firstChild, 6);
			}
		}
		else
		{
			if (firstChild.getId() != sunday.getId())
			{
				view.removeView(lastChild);
				view.addView(lastChild, 0);
			}
		}
	}

	/**
	 * Set an onClick listener for each of the day of week buttons.
	 */
	public void setOnClickListener(NacDayOfWeek.OnClickListener listener)
	{
		this.mListener = listener;
	}

}
