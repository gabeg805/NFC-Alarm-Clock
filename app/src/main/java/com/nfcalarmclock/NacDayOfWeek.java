package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.support.annotation.Nullable;
import java.util.List;

import android.util.TypedValue;

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
	 * Number of days.
	 */
	private final int mLength = 7;

	/**
	 * Click event listener.
	 */
	private NacDayOfWeek.OnClickListener mListener;

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
	 * @brief Finish setting up the View.
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.finishSetup();
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
			case R.id.dowb_sun:
				index = 0;
				break;
			case R.id.dowb_mon:
				index = 1;
				break;
			case R.id.dowb_tue:
				index = 2;
				break;
			case R.id.dowb_wed:
				index = 3;
				break;
			case R.id.dowb_thu:
				index = 4;
				break;
			case R.id.dowb_fri:
				index = 5;
				break;
			case R.id.dowb_sat:
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
	 * Initialize the view.
	 */
	public void init(AttributeSet attrs)
	{
		Context context = getContext();

		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater.from(context).inflate(R.layout.nac_day_of_week,
			this, true);

		this.mButtons = new NacDayButton[this.mLength];
		this.mButtons[0] = (NacDayButton) findViewById(R.id.dowb_sun);
		this.mButtons[1] = (NacDayButton) findViewById(R.id.dowb_mon);
		this.mButtons[2] = (NacDayButton) findViewById(R.id.dowb_tue);
		this.mButtons[3] = (NacDayButton) findViewById(R.id.dowb_wed);
		this.mButtons[4] = (NacDayButton) findViewById(R.id.dowb_thu);
		this.mButtons[5] = (NacDayButton) findViewById(R.id.dowb_fri);
		this.mButtons[6] = (NacDayButton) findViewById(R.id.dowb_sat);
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
	 * Set the days that will be enabled/disabled.
	 *
	 * @param  days  The button days that will be enabled.
	 */
	public void setDays(int days)
	{
		NacAlarm alarm = new NacAlarm();
		alarm.setDays(days);
		this.setDays(alarm);
	}

	/**
	 * Set the days that will be enabled/disabled.
	 *
	 * @param  alarm  The alarm containing the days that will be enabled.
	 */
	public void setDays(NacAlarm alarm)
	{
		for (int i=0; i < this.mLength; i++)
		{
			if (alarm.isDay(i))
			{
				this.mButtons[i].enable();
			}
			else
			{
				this.mButtons[i].disable();
			}
		}
	}

	/**
	 * @brief Setup the buttons that represent the different days of the week.
	 */
	private void finishSetup()
	{
		if (this.mButtons == null)
		{
			throw new RuntimeException("Unable to find button views.");
		}

		int spacing = this.getButtonSpacing();
		//super.setBackgroundColor(this.mButtons[0].getDefaultButtonColor());

		for (int i=0; i < this.mLength; i++)
		{
			NacDayButton b = this.mButtons[i];
			LayoutParams params = (LayoutParams) b.getLayoutParams();

			if (i > 0)
			{
				params.setMargins(spacing, 0, 0, 0);
			}

			b.setLayoutParams(params);
			b.setTag(i);
		}
	}

	/**
	 * @brief Set an onClick listener for each of the day of week buttons.
	 */
	public void setOnClickListener(NacDayOfWeek.OnClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * @return The alarm days.
	 */
	public int getDays()
	{
		NacAlarm a = new NacAlarm();
		List<Byte> weekdays = a.getWeekDays();
		int days = NacAlarm.Days.NONE;

		for (int i=0; i < this.mLength; i++)
		{
			if (this.isDayEnabled(i))
			{
				days |= weekdays.get(i);
			}
		}

		return days;
	}

	/**
	 * @brief Determine the spacing between buttons.
	 * 
	 * @return The spacing between the different buttons.
	 */
	private int getButtonSpacing()
	{
		Resources r = getContext().getResources();
		DisplayMetrics metrics = r.getDisplayMetrics();
		float left = r.getDimension(R.dimen.ml_card);
		float right = r.getDimension(R.dimen.mr_card);
		double spacing = (metrics.widthPixels - 2.5*(left+right)
						 - 7*this.mButtons[0].getButtonWidth()) / 6.0;

		return (int) spacing;
	}

	/**
	 * @return True if the button is enabled and false if it is not.
	 */
	public boolean isDayEnabled(int index)
	{
		return this.mButtons[index].isEnabled();
	}

}
