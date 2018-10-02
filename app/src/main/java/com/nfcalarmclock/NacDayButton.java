package com.nfcalarmclock;

import android.support.v4.content.ContextCompat;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
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
import android.content.res.Resources.Theme;
import android.content.res.ColorStateList;

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

	/**
	 * Attributes for button and text.
	 */
	public static class NacDayAttributes
	{
		public int width;
		public int height;
		public int duration;
		public int textColor;
		public int textSize;
		public String text;
		public int backgroundTint;
		public int background;

		public NacDayAttributes()
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
	 * View attributes.
	 */
	private NacDayAttributes mAttributes;

	/**
	 * Button animator.
	 */
	private ValueAnimator mButtonAnimator;

	/**
	 * Text animator.
	 */
	private ValueAnimator mTextAnimator;

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
	 * Parse the view attributes.
	 */
	public static NacDayAttributes parseAttributes(Context context,
		AttributeSet attrs)
	{
		Resources res = context.getResources();
		Resources.Theme theme = context.getTheme();
		TypedArray ta = theme.obtainStyledAttributes(attrs,
			R.styleable.NacDayButton, 0, R.style.NacDayButton);
		int widthid = R.styleable.NacDayOfWeek_nacWidth;
		int heightid = R.styleable.NacDayOfWeek_nacHeight;
		int durationid = R.styleable.NacDayOfWeek_nacDuration;
		int textcolorid = R.styleable.NacDayOfWeek_nacTextColor;
		int textsizeid = R.styleable.NacDayOfWeek_nacTextSize;
		int textid = R.styleable.NacDayOfWeek_nacText;
		int bgcolorid = R.styleable.NacDayOfWeek_nacBackgroundColor;
		int drawableid = R.styleable.NacDayOfWeek_nacDrawable;
		//int widthid = R.styleable.NacDayButton_android_width;
		//int heightid = R.styleable.NacDayButton_android_height;
		//int durationid = R.styleable.NacDayButton_android_duration;
		//int textcolorid = R.styleable.NacDayButton_android_textColor;
		//int textsizeid = R.styleable.NacDayButton_android_textSize;
		//int textid = R.styleable.NacDayButton_android_text;
		//int bgid = R.styleable.NacDayButton_android_backgroundTint;
		//int backgroundid = R.styleable.NacDayButton_android_background;
		NacDayAttributes parsed = new NacDayAttributes();

		try
		{
			parsed.width = (int) ta.getDimension(widthid, -1);
			parsed.height = (int) ta.getDimension(heightid, -1);
			parsed.duration = ta.getInt(durationid, 0);
			parsed.textColor = ta.getColor(textcolorid, -1);
			parsed.textSize = (int) ta.getDimension(textsizeid, -1);
			parsed.text = ta.getText(textid).toString();
			parsed.backgroundTint = ta.getColor(bgcolorid, -1);
			parsed.background = ta.getResourceId(drawableid, -1);
		}
		finally
		{
			ta.recycle();
		}

		return parsed;
	}

	public void mergeAttributes(Context context, AttributeSet attrs)
	{
		//NacDayAttributes parsed = NacDayButton.parseAttributes(context, attrs);

		Resources res = context.getResources();
		TypedArray ta = res.obtainAttributes(attrs,
			R.styleable.NacDayButton);
		NacUtility.printf("Index Count : %d", ta.getIndexCount());
		NacUtility.printf("Length      : %d", ta.length());

		try
		{
			for (int i=0; i < ta.length(); i++)
			{
				//TypedValue value = new TypedValue();
				NacUtility.printf("Index : %d || Type : %d || Has : %b",
					i, ta.getType(i), ta.hasValue(i));

				if (!ta.hasValue(i))
				{
					continue;
				}

				switch (i)
				{
					case R.styleable.NacDayButton_nacWidth:
						NacUtility.printf("Width");
						this.mAttributes.width = (int) ta.getDimension(i, -1);
						break;
					case R.styleable.NacDayButton_nacHeight:
						NacUtility.printf("Height");
						this.mAttributes.height = (int) ta.getDimension(i, -1);
						break;
					case R.styleable.NacDayButton_nacDuration:
						NacUtility.printf("Duration");
						this.mAttributes.duration = ta.getInt(i, 0);
						break;
					case R.styleable.NacDayButton_nacTextColor:
						NacUtility.printf("TextColor");
						this.mAttributes.textColor = ta.getColor(i, -1);
						break;
					case R.styleable.NacDayButton_nacTextSize:
						NacUtility.printf("TextSize");
						this.mAttributes.textSize = (int) ta.getDimension(i, -1);
						break;
					case R.styleable.NacDayButton_nacText:
						NacUtility.printf("Text");
						this.mAttributes.text = ta.getText(i).toString();
						break;
					case R.styleable.NacDayButton_nacBackgroundColor:
						NacUtility.printf("Background");
						//this.mAttributes.backgroundTint = res.getColor(ta.getResourceId(i, -1));
						this.mAttributes.backgroundTint = ta.getColor(i, -1);
						//TypedValue value = new TypedValue();
						//TypedValue resolve = new TypedValue();
						//ta.getValue(i, value);
						//NacUtility.printf("Data : %d", value.data);
						//NacUtility.printf("Density : %d", value.density);
						//NacUtility.printf("Resource : %d", value.resourceId);
						//NacUtility.printf("String : %s", value.string);
						//NacUtility.printf("Type : %d", value.type);
						//boolean yo = context.getTheme().resolveAttribute(value.data, resolve, true);
						//NacUtility.printf("Yo : %b", yo);
						//NacUtility.printf("Data : %d", resolve.data);
						//NacUtility.printf("Density : %d", resolve.density);
						//NacUtility.printf("Resource : %d", resolve.resourceId);
						//NacUtility.printf("String : %s", resolve.string);
						//NacUtility.printf("Type : %d", resolve.type);
						//this.mAttributes.backgroundTint = resolve.data;
						break;
					case R.styleable.NacDayButton_nacDrawable:
						NacUtility.printf("Drawable");
						this.mAttributes.background = ta.getResourceId(i, -1);
						//this.mAttributes.background = 0;
						//this.mAttributes.background = ta.getColor(i, -1);
						break;
					default:
						NacUtility.printf("Default");
						break;
				}
			}
		}
		finally
		{
			ta.recycle();
		}
	}

	/**
	 * Finish setting up the View.
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.setViewAttributes();
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

		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater.from(context).inflate(R.layout.nac_day_button,
			this, true);

		this.mAttributes = parseAttributes(context, attrs);
		this.mButton = (Button) findViewById(R.id.nac_day_button);
		this.mButtonAnimator = null;
		this.mTextAnimator = null;
		this.mListener = null;

		if (this.mButton == null)
		{
			throw new RuntimeException("Unable to find NacDayButton ID.");
		}

		this.mButton.setPadding(0, 0, 0, 0);
		this.mButton.setOnClickListener(this);
	}

	/**
	 * Set view attributes.
	 */
	//public void setViewAttributes(NacDayAttributes attributes)
	//{
	//	this.mAttributes = attributes;

	//	setViewAttributes();
	//}

	private void setViewAttributes()
	{
		NacUtility.printf("Attributes");
		NacUtility.printf("Width      : %d", this.mAttributes.width);
		NacUtility.printf("Height     : %d", this.mAttributes.height);
		NacUtility.printf("Duration   : %d", this.mAttributes.duration);
		NacUtility.printf("TextColor  : %d", this.mAttributes.textColor);
		NacUtility.printf("TextSize   : %d", this.mAttributes.textSize);
		NacUtility.printf("Text       : %s", this.mAttributes.text);
		NacUtility.printf("Background : %d", this.mAttributes.backgroundTint);
		NacUtility.printf("Drawable   : %d", this.mAttributes.background);
		super.setElevation(0);
		this.setWidthAndHeight(this.getButtonWidth(), this.getButtonHeight());
		this.setBackground(this.getBackgroundResource());
		this.setButtonColor(this.getButtonColor());
		this.setTextColor(this.getTextColor());
		this.setText(this.getText());
		this.setTextSize(this.getTextSize());
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

		if (animator.equals(this.mButtonAnimator))
		{
			NacUtility.printf("Animating color : %d", color);
			this.setButtonColor(color);
		}
		else if (animator.equals(this.mTextAnimator))
		{
			this.setTextColor(color);
		}
	}

	/**
	 * Redraw the view.
	 */
	public void redraw()
	{
		invalidate();
		requestLayout();
	}

	/**
	 * Enable a button by setting the button color to the initial color of the
	 * text, and the text is set to the initial color of the button.
	 */
	public void enable()
	{
		this.setButtonColor(this.getDefaultTextColor());
		this.setTextColor(this.getDefaultButtonColor());
	}

	/**
	 * Disable a button by setting the button color to its initial color, and
	 * the same is true for the text.
	 */
	public void disable()
	{
		this.setButtonColor(this.getDefaultButtonColor());
		this.setTextColor(this.getDefaultTextColor());
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

		return this.isEnabled();
	}

	/**
	 * Enable and animate the view.
	 */
	public void animateEnable()
	{
		this.enable();
		this.animateButton();
	}

	/**
	 * Disable and animate the view.
	 */
	public void animateDisable()
	{
		this.disable();
		this.animateButton();
	}

	/**
	 * Toggle and animate the view.
	 */
	public void animateToggle()
	{
		this.toggle();
		this.animateButton();
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
			NacUtility.printf("From : %d || To : %d", from, to);
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
			this.mButtonAnimator = animator;
		}
		else if (type == NacDayViewType.TEXT)
		{
			this.mTextAnimator = animator;
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
	 * Inverse the colors of the button and text.
	 */
	private void inverseColors()
	{
		int buttoncolor = this.getButtonColor();
		int textcolor = this.getTextColor();

		this.setButtonColor(textcolor);
		this.setTextColor(buttoncolor);
	}

	/**
	 * Set an onClick listener for each of the day of week buttons.
	 */
	public void setOnClickListener(NacDayButton.OnClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * Set the button color.
	 *
	 * @param  color  The button color.
	 */
	public void setButtonColor(int color)
	{
		//this.mButtonGraphics.color = color;
		//Drawable drawable = this.mButton.getBackground();
		Drawable drawable = this.getBackground();

		drawable.setColorFilter(color, PorterDuff.Mode.SRC);
		this.mButton.setTag(color);
		this.redraw();
	}

	/**
	 * Set the default button color.
	 *
	 * @param  color  The default button color.
	 */
	public void setDefaultButtonColor(int color)
	{
		this.mAttributes.backgroundTint = color;
	}

	/**
	 * Set the text color.
	 *
	 * @param  color  The text color.
	 */
	public void setTextColor(int color)
	{
		this.mButton.setTextColor(color);
		this.redraw();
	}

	/**
	 * Set the default text color.
	 *
	 * @param  color  The default text color.
	 */
	public void setDefaultTextColor(int color)
	{
		this.mAttributes.textColor = color;
	}

	/**
	 * Set the text size.
	 *
	 * @param  size  The text size.
	 */
	public void setTextSize(int size)
	{
		this.mAttributes.textSize = size;
		this.mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
		this.redraw();
	}

	/**
	 * Set the text in the button.
	 *
	 * @param  text  The text to enter in the button.
	 */
	public void setText(String text)
	{
		this.mAttributes.text = text;
		this.mButton.setText(text);
		this.redraw();
	}

	/**
	 * Set the width and height of the button.
	 */
	public void setWidthAndHeight(int width, int height)
	{
		LayoutParams params = (LayoutParams) this.mButton.getLayoutParams();
		params.width = width;
		params.height = height;
		this.mAttributes.width = width;
		this.mAttributes.height = height;

		this.mButton.setLayoutParams(params);
		this.redraw();
	}

	/**
	 * Set the duration of the animation.
	 *
	 * @param  duration  The duration.
	 */
	public void setDuration(int duration)
	{
		this.mAttributes.duration = duration;
	}

	/**
	 * Set the background drawable.
	 * 
	 * @param  bg  The background.
	 */
	public void setBackground(Drawable bg)
	{
		this.mButton.setBackground(bg);
	}

	/**
	 * Set the background drawable.
	 * 
	 * @param  bg  The background.
	 */
	public void setBackground(int resid)
	{
		this.mAttributes.background = resid;
		this.mButton.setBackgroundResource(resid);
	}

	/**
	 * @return The button color.
	 */
	public int getButtonColor()
	{
		Object tag = this.mButton.getTag();

		int value = (tag != null) ? (Integer) tag : this.getDefaultButtonColor();
		NacUtility.printf("Getting button color : %d", value);
		return value;
		//return (tag != null) ? (Integer) tag : this.getDefaultButtonColor();
	}

	/**
	 * @return The default button color.
	 */
	public int getDefaultButtonColor()
	{
		NacUtility.printf("Getting default button color : %d", this.mAttributes.backgroundTint);
		return this.mAttributes.backgroundTint;
	}

	/**
	 * @return The text color.
	 */
	public int getTextColor()
	{
		ColorStateList colorlist = this.mButton.getTextColors();

		return (colorlist != null) ? colorlist.getDefaultColor() : this.getDefaultTextColor();
	}

	/**
	 * @return The default text color.
	 */
	public int getDefaultTextColor()
	{
		return this.mAttributes.textColor;
	}

	/**
	 * @return The text size.
	 */
	public int getTextSize()
	{
		return this.mAttributes.textSize;
	}

	/**
	 * @return The text in the button.
	 */
	public String getText()
	{
		return this.mAttributes.text;
	}

	/**
	 * @return The view width.
	 */
	public int getButtonWidth()
	{
		return this.mAttributes.width;
	}

	/**
	 * @return The view height.
	 */
	public int getButtonHeight()
	{
		return this.mAttributes.height;
	}

	/**
	 * @return The animation duration.
	 */
	public int getDuration()
	{
		return (this.isEnabled()) ? this.mAttributes.duration
			: this.mAttributes.duration / 2;
	}

	/**
	 * @return The background resource of the button.
	 */
	public int getBackgroundResource()
	{
		return this.mAttributes.background;
	}

	/**
	 * @return The background drawable of the button.
	 */
	public Drawable getBackground()
	{
		return this.mButton.getBackground();
	}

	/**
	 * @return True if the button is enabled and false if it is not.
	 */
	public boolean isEnabled()
	{
		return (this.getButtonColor() == this.getDefaultTextColor())
			&& (this.getTextColor() == this.getDefaultButtonColor());
	}

}

