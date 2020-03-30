package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Locale;

/**
 * Summary information for an alarm card.
 */
public class NacCardSummary
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Text of days to repeat.
	 */
	private TextView mDays;

	/**
	 * Name of the alarm.
	 */
	private TextView mName;

	/**
	 * Card measurement.
	 */
	private NacCardMeasure mMeasure;

	/**
	 */
	public NacCardSummary(Context context, View root, NacCardMeasure measure)
	{
		this.mContext = context;
		this.mAlarm = null;
		this.mDays = (TextView) root.findViewById(R.id.nac_summary_days);
		this.mName = (TextView) root.findViewById(R.id.nac_summary_name);
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
	 * @return The card padding.
	 */
	private int getCardPadding()
	{
		return this.mMeasure.getCardPadding();
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The layout params of the name.
	 */
	private RelativeLayout.LayoutParams getNameLayoutParams()
	{
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
			this.mName.getLayoutParams();
		
		return (params != null) ? params : new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT);
	}

	/**
	 * @return The max width of the summary name before it gets ellipsized.
	 */
	private int getNameMaxWidth()
	{
		int screenWidth = this.getScreenWidth();
		int padding = this.getCardPadding();
		int textsize = (int) this.mName.getTextSize();
		int summaryDays = this.mDays.getText().length() * textsize / 2;
		int expandImage = 3 * (int) this.getResources().getDimension(
			R.dimen.isz_main);

		return screenWidth - summaryDays - padding - expandImage;
	}

	/**
	 * @return The context resources.
	 */
	public Resources getResources()
	{
		return this.getContext().getResources();
	}

	/**
	 * @return The screen width.
	 */
	private int getScreenWidth()
	{
		return this.mMeasure.getScreenWidth();
	}

	/**
	 * Initialize the summary information.
	 */
	public void init(NacSharedPreferences shared, NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.set(shared);
	}

	/**
	 * Set the summary name and days.
	 */
	public void set(NacSharedPreferences shared)
	{
		this.setDays(shared);
		this.setName();
	}

	/**
	 * Set the summary colors.
	 */
	public void setColor(NacSharedPreferences shared)
	{
		int daysColor = shared.getDaysColor();
		int nameColor = shared.getNameColor();

		this.mDays.setTextColor(daysColor);
		this.mName.setTextColor(nameColor);
	}

	/**
	 * Set the repeat days text.
	 */
	public void setDays(NacSharedPreferences shared)
	{
		NacAlarm alarm = this.getAlarm();
		String string = NacCalendar.Days.toString(alarm,
			shared.getStartWeekOn());
		Locale locale = Locale.getDefault();

		//this.mDays.setText(string);
		this.mDays.setText(string.toLowerCase(locale));
		this.mDays.requestLayout();
	}

	/**
	 * Set ellipsis for the summary name if it is too long.
	 */
	public void setName()
	{
		NacAlarm alarm = this.getAlarm();
		String name = alarm.getNameNormalized();

		this.mName.setText(name);
		this.mName.setVisibility(name.isEmpty() ? View.GONE : View.VISIBLE);
		//this.mName.setSelected(true); // For marquee
	}

}
