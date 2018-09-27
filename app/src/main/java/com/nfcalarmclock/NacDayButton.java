package com.nfcalarmclock;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import android.support.annotation.Nullable;

/**
 * A button that consists of an image to the left, and text to the right
 * of it.
 */
public class NacDayButton
	extends LinearLayout
	implements ValueAnimator.AnimatorUpdateListener,View.OnClickListener
{

	/**
	 * Listener for click events.
	 */
	public interface OnClickListener
	{
		public void onClick(NacDayButton button);
	}

	public class NacDayAttributes
	{
		public int duration;
		public int width;
		public int height;
		public int textSize;
		public int backgroundTint;
		public int textColor;
		public int textId;
		public int backgroundId;

		public NacDayAttributes()
		{
		}
	}

	/**
	 * Attributes for button and text.
	 */
	public class NacDayGraphics
	{
		public int defaultcolor;
		public int color;
		public ValueAnimator animator;

		public NacDayGraphics()
		{
		}
	}

	/**
	 * Types of views in the day layout.
	 */
	public enum NacDayViewType
	{
		BUTTON,
		TEXT
	}

	/**
	 * Day button.
	 */
	private Button mButton;

	/**
	 * Button attributes.
	 */
	private NacDayAttributes mButtonAttr;

	/**
	 * Text attributes.
	 */
	private NacDayAttributes mTextAttr;

	/**
	 * Animation duration.
	 */
	private int mDuration;

	/**
	 * Click listener.
	 */
	private NacDayButton.OnClickListener mListener;

	/**
	 */
	public NacDayButton(Context context)
	{
		super(context, null);
		init(null);
	}

	/**
	 */
	public NacDayButton(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		init(attrs);
	}

	/**
	 */
	public NacDayButton(Context context, AttributeSet attrs,
		int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	/**
	 * Finish setting up the View.
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
	public void onClick(View v)
	{
		if (this.mListener != null)
		{
			this.mListener.onClick(this);
		}
	}

	/**
	 */
	@Override
	public void onAnimationUpdate(ValueAnimator animator)
	{
		int color = (int) animator.getAnimatedValue();

		if (animator.equals(this.mButtonAttr.animator))
		{
			this.drawButton(color);
		}
		else if (animator.equals(this.mTextAttr.animator))
		{
			this.drawText(color);
		}
	}

	/**
	 * Initialize the view.
	 */
	public void init(AttributeSet attrs)
	{
		if (attrs == null)
		{
			return;
		}

		Context context = getContext();
		TypedArray ta = context.obtainStyledAttributes(attrs,
			R.styleable.NacDayButton, 0, R.style.NacDayButton);

		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater.from(context).inflate(R.layout.nac_day_button,
			this, true);

		try
		{
			initButton(ta);
			initAttributes(ta);
		}
		finally
		{
			ta.recycle();
		}
	}

	/**
	 * Initialize the button.
	 */
	private void initButton(TypedArray ta)
	{
		this.mButton = (Button) findViewById(R.id.nac_day_button);
		int textid = R.styleable.NacDayButton_android_text;
		int bgid = R.styleable.NacDayButton_android_background;

		if (this.mButton == null)
		{
			throw new RuntimeException("Unable to find NacDayButton ID.");
		}

		CharSequence text = ta.getText(textid);
		int resource = ta.getResourceId(bgid, -1);

		this.mButton.setPadding(0, 0, 0, 0);
		this.mButton.setBackgroundResource(resource);
		this.mButton.setText(text);
		this.mButton.setOnClickListener(this);

		this.mListener = null;
	}

	/**
	 * Initialize view attributes.
	 */
	private void initAttributes(TypedArray ta)
	{
		int durationid = R.styleable.NacDayButton_android_duration;
		int widthid = R.styleable.NacDayButton_android_width;
		int heightid = R.styleable.NacDayButton_android_height;
		int sizeid = R.styleable.NacDayButton_android_textSize;
		int bgcolorid = R.styleable.NacDayButton_android_backgroundTint;
		int textcolorid = R.styleable.NacDayButton_android_textColor;

		this.mButtonAttr = new NacDayAttributes();
		this.mTextAttr = new NacDayAttributes();
		this.mDuration = ta.getInt(durationid, 0);

		this.mButtonAttr.width = (int) ta.getDimension(widthid, -1);
		this.mButtonAttr.height = (int) ta.getDimension(heightid, -1);
		this.mTextAttr.size = (int) ta.getDimension(sizeid, -1);
		this.mButtonAttr.defaultcolor = ta.getColor(bgcolorid, -1);
		this.mTextAttr.defaultcolor = ta.getColor(textcolorid, -1);
		this.mButtonAttr.color = this.mButtonAttr.defaultcolor;
		this.mTextAttr.color = this.mTextAttr.defaultcolor;
	}

	public static NacDayAttributes parseAttributes(Context context)
	{
		int durationid = R.styleable.NacDayButton_android_duration;
		int widthid = R.styleable.NacDayButton_android_width;
		int heightid = R.styleable.NacDayButton_android_height;
		int sizeid = R.styleable.NacDayButton_android_textSize;
		int bgcolorid = R.styleable.NacDayButton_android_backgroundTint;
		int textcolorid = R.styleable.NacDayButton_android_textColor;
		int textid = R.styleable.NacDayButton_android_text;
		int bgid = R.styleable.NacDayButton_android_background;
	}

	/**
	 * Setup the buttons that represent the different days of the week.
	 */
	private void finishSetup()
	{
		Button b = this.mButton;
		LayoutParams params = (LayoutParams) b.getLayoutParams();
		params.width = this.getButtonWidth();
		params.height = this.getButtonHeight();

		b.setLayoutParams(params);
		b.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.getTextSize());
		this.drawDay();
	}

	/**
	 * Enable a button by setting the button color to the initial color of the
	 * text, and the text is set to the initial color of the button.
	 */
	public void enable()
	{
		this.setButtonColor(this.getDefaultTextColor());
		this.setTextColor(this.getDefaultButtonColor());
		this.drawDay();
	}

	/**
	 * Disable a button by setting the button color to its initial color, and
	 * the same is true for the text.
	 */
	public void disable()
	{
		this.setButtonColor(this.getDefaultButtonColor());
		this.setTextColor(this.getDefaultTextColor());
		this.drawDay();
	}

	/**
	 * Inverse the color of the drawable and text.
	 * 
	 * @return True when the button is enabled and False when the button is
	 *		   disabled.
	 */
	public boolean toggle()
	{
		this.inverseColors();
		this.drawDay();

		return this.isEnabled();
	}

	/**
	 * Enable and animate the view.
	 */
	public void animateEnable()
	{
		this.enable();
		this.animateDay();
	}

	/**
	 * Disable and animate the view.
	 */
	public void animateDisable()
	{
		this.disable();
		this.animateDay();
	}

	/**
	 * Toggle and animate the view.
	 */
	public void animateToggle()
	{
		this.toggle();
		this.animateDay();
	}

	/**
	 * Animate the button and text.
	 */
	public void animateDay()
	{
		this.animateButton();
		this.drawText();
	}

	/**
	 * Animate the button or text.
	 */
	public void animateDay(NacDayViewType type)
	{
		ArgbEvaluator evaluator = new ArgbEvaluator();
		int duration = this.getDuration();
		int from;
		int to;

		if (type == NacDayViewType.BUTTON)
		{
			from = this.getTextColor();
			to = this.getButtonColor();
		}
		else if (type == NacDayViewType.TEXT)
		{
			from = this.getButtonColor();
			to = this.getTextColor();
		}
		else
		{
			return;
		}

		ValueAnimator animator = ValueAnimator.ofObject(evaluator, from, to);

		animator.setDuration(duration);
		animator.addUpdateListener(this);
		animator.start();

		if (type == NacDayViewType.BUTTON)
		{
			this.mButtonAttr.animator = animator;
		}
		else if (type == NacDayViewType.TEXT)
		{
			this.mTextAttr.animator = animator;
		}
	}


	/**
	 * Animate the button's change in color.
	 */
	public void animateButton()
	{
		animateDay(NacDayViewType.BUTTON);
	}

	/**
	 * Animate the text's change in color.
	 */
	public void animateText()
	{
		animateDay(NacDayViewType.TEXT);
	}

	/**
	 * Draw the button with the desired color.
	 *
	 * @param  color  The color to change the button to.
	 */
	public void drawButton(int color)
	{
		Drawable drawable = this.mButton.getBackground();

		this.setButtonColor(color);
		drawable.setColorFilter(color, PorterDuff.Mode.SRC);
		invalidate();
		requestLayout();
	}

	/**
	 * Draw the text with the desired color.
	 * 
	 * @param  color  The color to change the button to.
	 */
	private void drawText(int color)
	{
		this.setTextColor(color);
		this.mButton.setTextColor(color);
		invalidate();
		requestLayout();
	}

	/**
	 * Draw the button and text.
	 */
	private void drawDay()
	{
		this.drawButton();
		this.drawText();
	}

	/**
	 * Draw the button.
	 */
	private void drawButton()
	{
		this.drawButton(this.mButtonAttr.color);
	}

	/**
	 * Draw the text.
	 */
	private void drawText()
	{
		this.drawText(this.mTextAttr.color);
	}

	/**
	 * Inverse the colors of the button and text.
	 */
	private void inverseColors()
	{
		int tmp = this.mButtonAttr.color;

		this.setButtonColor(this.mTextAttr.color);
		this.setTextColor(tmp);
	}

	/**
	 * Set the button color.
	 *
	 * @param  color  The button color.
	 */
	public void setButtonColor(int color)
	{
		this.mButtonAttr.color = color;
	}

	/**
	 * Set the default button color.
	 *
	 * @param  color  The default button color.
	 */
	public void setDefaultButtonColor(int color)
	{
		this.mButtonAttr.defaultcolor = color;
	}

	/**
	 * Set the text color.
	 *
	 * @param  color  The text color.
	 */
	public void setTextColor(int color)
	{
		this.mTextAttr.color = color;
	}

	/**
	 * Set the text size.
	 *
	 * @param  size  The text size.
	 */
	public void setTextSize(int size)
	{
		this.mTextAttr.size = size;
	}

	/**
	 * Set the default text color.
	 *
	 * @param  color  The default text color.
	 */
	public void setDefaultTextColor(int color)
	{
		this.mTextAttr.defaultcolor = color;
	}

	/**
	 * Set an onClick listener for each of the day of week buttons.
	 */
	public void setOnClickListener(NacDayButton.OnClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * @return The button color.
	 */
	public int getButtonColor()
	{
		return this.mButtonAttr.color;
	}

	/**
	 * @return The text color.
	 */
	public int getTextColor()
	{
		return this.mTextAttr.color;
	}

	/**
	 * @return The default button color.
	 */
	public int getDefaultButtonColor()
	{
		return this.mButtonAttr.defaultcolor;
	}

	/**
	 * @return The default text color.
	 */
	public int getDefaultTextColor()
	{
		return this.mTextAttr.defaultcolor;
	}

	/**
	 * @return The text size.
	 */
	public int getTextSize()
	{
		return this.mTextAttr.size;
	}

	/**
	 * @return The button width.
	 */
	public int getButtonWidth()
	{
		return this.mButtonAttr.width;
	}

	/**
	 * @return The button height.
	 */
	public int getButtonHeight()
	{
		return this.mButtonAttr.height;
	}

	/**
	 * @return The animation duration
	 */
	public int getDuration()
	{
		return (this.isEnabled()) ? this.mDuration : this.mDuration / 2;
	}

	/**
	 * @return True if the button is enabled and false if it is not.
	 */
	public boolean isEnabled()
	{
		return (this.mButtonAttr.color == this.mTextAttr.defaultcolor)
			&& (this.mTextAttr.color == this.mButtonAttr.defaultcolor);
	}

}

