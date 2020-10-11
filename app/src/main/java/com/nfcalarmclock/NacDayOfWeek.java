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
	extends LinearLayout
	implements NacDayButton.OnClickListener
{

	/**
	 * Listener for click events.
	 */
	public interface OnClickListener
	{
		public void onClick(NacDayButton button, int index);
	}

	/**
	 * Button for each day.
	 */
	private NacDayButton[] mButtons;

	/**
	 * Click event listener.
	 */
	private NacDayOfWeek.OnClickListener mListener;

	/**
	 * Number of days.
	 */
	private static final int mLength = 7;

	/**
	 */
	public NacDayOfWeek(Context context)
	{
		super(context, null);
		init((AttributeSet)null);
	}

	/**
	 */
	public NacDayOfWeek(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		init(attrs);
	}

	/**
	 */
	public NacDayOfWeek(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	/**
	 * @return True if any days are selected, and False otherwise.
	 */
	public boolean areDaysSelected()
	{
		return !this.getDays().isEmpty();
	}

	/**
	 * Determine the spacing between buttons.
	 * 
	 * @return The spacing between the different buttons.
	 */
	private int getButtonSpacing()
	{
		Resources r = getContext().getResources();
		DisplayMetrics metrics = r.getDisplayMetrics();
		float left = r.getDimension(R.dimen.normal) + getPaddingLeft();
		float right = r.getDimension(R.dimen.normal) + getPaddingRight();
		double spacing = (metrics.widthPixels - (left+right)
			- 7*this.mButtons[0].getButtonWidth()) / 16.0;

		return (int) spacing;
	}

	/**
	 * @return The alarm days.
	 */
	public EnumSet<NacCalendar.Day> getDays()
	{
		EnumSet<NacCalendar.Day> days = EnumSet.noneOf(NacCalendar.Day.class);
		int index = 0;

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			if (this.isDayEnabled(index))
			{
				days.add(d);
			}

			index++;
		}

		return days;
	}

	/**
	 * Initialize the view.
	 */
	public void init(AttributeSet attrs)
	{
		Context context = getContext();

		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater.from(context).inflate(R.layout.nac_day_of_week,
			this, true);

		this.mButtons = new NacDayButton[this.mLength];
		this.mButtons[0] = (NacDayButton) findViewById(R.id.dow_sun);
		this.mButtons[1] = (NacDayButton) findViewById(R.id.dow_mon);
		this.mButtons[2] = (NacDayButton) findViewById(R.id.dow_tue);
		this.mButtons[3] = (NacDayButton) findViewById(R.id.dow_wed);
		this.mButtons[4] = (NacDayButton) findViewById(R.id.dow_thu);
		this.mButtons[5] = (NacDayButton) findViewById(R.id.dow_fri);
		this.mButtons[6] = (NacDayButton) findViewById(R.id.dow_sat);
		this.mListener = null;

		for (int i=0; i < this.mLength; i++)
		{
			if (this.mButtons[i] == null)
			{
				throw new RuntimeException("Unable to find NacDayButton ID for #"+String.valueOf(i)+".");
			}

			this.mButtons[i].setOnClickListener(this);
			this.mButtons[i].mergeAttributes(context, attrs);
			this.mButtons[i].setViewAttributes();
		}
	}

	/**
	 * @return True if the button is enabled and false if it is not.
	 */
	public boolean isDayEnabled(int index)
	{
		return this.mButtons[index].isEnabled();
	}

	/**
	 */
	@Override
	public void onClick(NacDayButton button)
	{
		int id = button.getId();
		int index = -1;

		switch (id)
		{
			case R.id.dow_sun:
				index = 0;
				break;
			case R.id.dow_mon:
				index = 1;
				break;
			case R.id.dow_tue:
				index = 2;
				break;
			case R.id.dow_wed:
				index = 3;
				break;
			case R.id.dow_thu:
				index = 4;
				break;
			case R.id.dow_fri:
				index = 5;
				break;
			case R.id.dow_sat:
				index = 6;
				break;
			default:
				return;
		}

		this.mButtons[index].animateToggle();

		if (this.mListener == null)
		{
			return;
		}

		this.mListener.onClick(button, index);
	}

	/**
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		if (this.mButtons == null)
		{
			throw new RuntimeException("Unable to find button views.");
		}

		int spacing = this.getButtonSpacing();

		for (int i=0; i < this.mLength; i++)
		{
			NacDayButton b = this.mButtons[i];

			b.setPadding(spacing, spacing, spacing, spacing);
			b.setTag(i);
		}
	}

	/**
	 * Set the days that will be enabled/disabled.
	 *
	 * @param  days  The button days that will be enabled.
	 */
	public void setDays(EnumSet<NacCalendar.Day> days)
	{
		int index = 0;

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			if (days.contains(d))
			{
				this.mButtons[index].enable();
			}
			else
			{
				this.mButtons[index].disable();
			}

			index++;
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
		NacDayButton sunday = this.mButtons[0];
		NacDayButton monday = this.mButtons[1];
		View firstChild = getChildAt(0);
		View lastChild = getChildAt(6);

		if (start == 1)
		{
			if (firstChild.getId() != monday.getId())
			{
				removeView(firstChild);
				addView(firstChild, 6);
			}
		}
		else
		{
			if (firstChild.getId() != sunday.getId())
			{
				removeView(lastChild);
				addView(lastChild, 0);
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
