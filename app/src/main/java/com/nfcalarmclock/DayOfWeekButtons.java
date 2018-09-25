package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.support.annotation.Nullable;
import java.util.List;

/**
 * @class A button that consists of an image to the left, and text to the right
 *		  of it.
 */
public class DayOfWeekButtons
	extends LinearLayout
{

	/**
	 * @brief Context.
	 */
	private Context mContext;

	/**
	 * @brief A button for each day.
	 */
	private Button[] mButtons;

	/**
	 * @brief Button width.
	 */
	private int mButtonWidth;

	/**
	 * @brief Button height.
	 */
	private int mButtonHeight;

	/**
	 * @brief Button color.
	 */
	private int[] mButtonColorList;

	/**
	 * @brief Initial button color.
	 */
	private int mButtonColor;

	/**
	 * @brief Text color.
	 */
	private int[] mTextColorList;

	/**
	 * @brief Initial text color.
	 */
	private int mTextColor;

	/**
	 * @brief Text size.
	 */
	private int mTextSize;

	/**
	 * @brief Number of days.
	 */
	private final int mLength = 7;

	/**
	 */
	public DayOfWeekButtons(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public DayOfWeekButtons(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public DayOfWeekButtons(Context context, AttributeSet attrs,
		int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		this.mContext = context;
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
			R.styleable.DayOfWeekButtons, 0, 0);
		Resources r = context.getResources();

		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater.from(context).inflate(R.layout.dayofweekbuttons,
			this, true);

		try
		{

			float width = r.getDimension(R.dimen.circle_size);
			float height = r.getDimension(R.dimen.circle_size);
			float size = r.getDimension(R.dimen.tsz_card_days);
			int expandedcolor = NacUtility.getThemeAttrColor(this.mContext,
				R.attr.colorCardExpanded);
			int textcolor = NacUtility.getThemeAttrColor(this.mContext,
				R.attr.colorCardText);

			this.mButtonWidth = (int) a.getDimension(
				R.styleable.DayOfWeekButtons_nacWidth, width);
			this.mButtonHeight = (int) a.getDimension(
				R.styleable.DayOfWeekButtons_nacHeight, height);
			this.mTextSize = (int) a.getDimension(
				R.styleable.DayOfWeekButtons_nacTextSize, size);
			this.mButtonColorList = new int[this.mLength];
			this.mTextColorList = new int[this.mLength];
			this.mButtonColor = a.getColor(
				R.styleable.DayOfWeekButtons_nacDrawableColor, expandedcolor);
			this.mTextColor = a.getColor(
				R.styleable.DayOfWeekButtons_nacTextColor, textcolor);

			for (int i=0; i < this.mLength; i++)
			{
				this.mButtonColorList[i] = this.mButtonColor;
				this.mTextColorList[i] = this.mTextColor;
			}
		}
		finally
		{
			a.recycle();
		}

		init();
	}

	/**
	 * @brief Finish setting up the View.
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		finishSetup();
	}

	/**
	 * @brief Setup the contents of the button.
	 */
	private void init()
	{
		this.mButtons = new Button[this.mLength];
		this.mButtons[0] = (Button) findViewById(R.id.dowb_sun);
		this.mButtons[1] = (Button) findViewById(R.id.dowb_mon);
		this.mButtons[2] = (Button) findViewById(R.id.dowb_tue);
		this.mButtons[3] = (Button) findViewById(R.id.dowb_wed);
		this.mButtons[4] = (Button) findViewById(R.id.dowb_thu);
		this.mButtons[5] = (Button) findViewById(R.id.dowb_fri);
		this.mButtons[6] = (Button) findViewById(R.id.dowb_sat);

		for (int i=0; i < this.mLength; i++)
		{
			if (this.mButtons[i] == null)
			{
				throw new RuntimeException("Unable to find button ID for #"+String.valueOf(i)+".");
			}
		}
	}

	/**
	 * @brief Initialize the button and text colors.
	 *
	 * @param  days  The button days that will be highlighted.
	 */
	public void init(int days)
	{
		Alarm a = new Alarm();

		a.setDays(days);
		this.init(a);
	}

	/**
	 * @brief Initialize the button and text colors.
	 *
	 * @param  a  The alarm.
	 */
	public void init(Alarm a)
	{
		for (int i=0; i < 7; i++)
		{
			if (a.isDay(i))
			{
				enableButton(i);
			}
			else
			{
				disableButton(i);
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

		for (int i=0; i < this.mLength; i++)
		{
			Button b = this.mButtons[i];
			LayoutParams params = (LayoutParams) b.getLayoutParams();
			params.width = this.mButtonWidth;
			params.height = this.mButtonHeight;

			if (i > 0)
			{
				params.setMargins(spacing, 0, 0, 0);
			}

			b.setLayoutParams(params);
			b.setBackgroundResource(R.drawable.circle);
			this.drawButton(i);
			b.setCompoundDrawablePadding(0);
			this.drawText(i);
			b.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.mTextSize);
			b.setPadding(0, 0, 0, 0);
			b.setPaddingRelative(0, 0, 0, 0);
			b.setTag(i);
		}
	}

	/**
	 * @brief Enable a button.
	 * 
	 * @details Sets the button color to the initial color of the text, and the
	 *			text is set to the initial color of the button.
	 * 
	 * @param  index  The index of the desired button.
	 */
	public void enableButton(int index)
	{
		this.mButtonColorList[index] = this.mTextColor;
		this.mTextColorList[index] = this.mButtonColor;

		this.drawButton(index);
		this.drawText(index);
	}

	/**
	 * @brief Disable a button.
	 * 
	 * @details Sets the button color to its initial color, and the same is true
	 *			for the text.
	 * 
	 * @param  index  The index of the desired button.
	 */
	public void disableButton(int index)
	{
		this.mButtonColorList[index] = this.mButtonColor;
		this.mTextColorList[index] = this.mTextColor;

		this.drawButton(index);
		this.drawText(index);
	}

	/**
	 * @brief Inverse the color of the drawable and text.
	 * 
	 * @param  index  The index corresponding to the desired button to change.
	 * 
	 * @return True when the button is enabled and False when the button is
	 *		   disabled.
	 */
	public boolean toggleButton(int index)
	{
		this.inverseColors(index);
		this.drawButton(index);
		this.drawText(index);

		return (this.mButtonColorList[index] != this.mButtonColor);
	}

	/**
	 * @brief Inverse the colors of the button and text.
	 * 
	 * @param  index  The index corresponding to the desired button to change.
	 */
	private void inverseColors(int index)
	{
		int tmp = this.mButtonColorList[index];
		this.mButtonColorList[index] = this.mTextColorList[index];
		this.mTextColorList[index] = tmp;
	}

	/**
	 * @brief Draw the button with the desired color.
	 *
	 * @param  index  The index corresponding to the desired button.
	 * @param  color  The color to change the button to.
	 */
	public void drawButton(int index, int color)
	{
		Button button = this.mButtons[index];
		Drawable drawable = button.getBackground();
		this.mButtonColorList[index] = color;

		drawable.setColorFilter(color, PorterDuff.Mode.SRC);
		invalidate();
		requestLayout();
	}

	/**
	 * @brief Draw the text with the desired color.
	 * 
	 * @param  index  The index corresponding to the desired button.
	 * @param  color  The color to change the button to.
	 */
	private void drawText(int index, int color)
	{
		Button button = this.mButtons[index];
		this.mTextColorList[index] = color;

		button.setTextColor(color);
		invalidate();
		requestLayout();
	}

	/**
	 * @brief Set the button color.
	 * 
	 * @param  index  The index corresponding to the desired button to change.
	 */
	private void drawButton(int index)
	{
		this.drawButton(index, this.mButtonColorList[index]);
	}

	/**
	 * @brief Set the color of the text.
	 * 
	 * @param  index  The index corresponding to the desired button to change.
	 */
	private void drawText(int index)
	{
		this.drawText(index, this.mTextColorList[index]);
	}

	/**
	 * @brief Set the button color.
	 *
	 * @param  color  The button color.
	 */
	public void setButtonColor(int color)
	{
		this.mButtonColor = color;
	}

	/**
	 * @brief Set the text color.
	 *
	 * @param  color  The text color.
	 */
	public void setTextColor(int color)
	{
		this.mTextColor = color;
	}

	/**
	 * @brief Set an onClick listener for each of the day of week buttons.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		if (this.mButtons == null)
		{
			throw new RuntimeException("Unable to find button views.");
		}

		for (int i=0; i < this.mLength; i++)
		{
			Button b = this.mButtons[i];
			b.setOnClickListener(listener);
		}
	}

	/**
	 * @return The alarm days.
	 */
	public int getDays()
	{
		Alarm a = new Alarm();
		List<Byte> weekdays = a.getWeekDays();
		int days = Alarm.Days.NONE;

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
		Resources r = this.mContext.getResources();
		DisplayMetrics metrics = r.getDisplayMetrics();
		float left = r.getDimension(R.dimen.ml_card);
		float right = r.getDimension(R.dimen.mr_card);
		double spacing = (metrics.widthPixels - 2.5*(left+right)
						 - 7*this.mButtonWidth) / 6.0;

		return (int) spacing;
	}

	/**
	 * @return True if the button is enabled and false if it is not.
	 */
	public boolean isDayEnabled(int index)
	{
		return (this.mButtonColorList[index] == this.mTextColor)
			&& (this.mTextColorList[index] == this.mButtonColor);
	}

}
