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
	public class NacDayAttributes
	{
		public int width;
		public int height;
		public int duration;
		public int textColor;
		public int textSize;
		public String text;
		public int backgroundColor;
		public int drawable;

		/**
		 * Initialize the attributes.
		 */
		public NacDayAttributes(Context context, AttributeSet attrs)
		{
			TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.NacDayButton, 0, R.style.NacDayButton);

			try
			{
				Resources res = context.getResources();
				int textsize = (int) res.getDimension(R.dimen.tsz_card_days);
				this.width = (int) ta.getDimension(R.styleable.NacDayButton_nacWidth, 2*textsize);
				this.height = (int) ta.getDimension(R.styleable.NacDayButton_nacHeight, 2*textsize);
				this.duration = ta.getInt(R.styleable.NacDayButton_nacDuration, 500);
				this.textColor = ta.getColor(R.styleable.NacDayButton_nacTextColor, Color.WHITE);
				this.textSize = (int) ta.getDimension(R.styleable.NacDayButton_nacTextSize, textsize);
				this.text = ta.getString(R.styleable.NacDayButton_nacText);
				this.backgroundColor = ta.getColor(R.styleable.NacDayButton_nacBackgroundColor, Color.WHITE);
				this.drawable = ta.getResourceId(R.styleable.NacDayButton_nacDrawable, R.drawable.day_button);
			}
			finally
			{
				ta.recycle();
			}
		}

		/**
		 * Merge the attribute corresponding to the given index.
		 *
		 * @param  index  The index of the attribute to overwrite.
		 * @param  attributes  The attribute class to merge with this one.
		 */
		public void merge(int index, NacDayAttributes attributes)
		{
			if (index == R.styleable.NacDayButton_nacWidth)
			{
				this.width = attributes.width;
			}
			else if (index == R.styleable.NacDayButton_nacHeight)
			{
				this.height = attributes.height;
			}
			else if (index == R.styleable.NacDayButton_nacDuration)
			{
				this.duration = attributes.duration;
			}
			else if (index == R.styleable.NacDayButton_nacTextColor)
			{
				this.textColor = attributes.textColor;
			}
			else if (index == R.styleable.NacDayButton_nacTextSize)
			{
				this.textSize = attributes.textSize;
			}
			else if (index == R.styleable.NacDayButton_nacText)
			{
				this.text = attributes.text;
			}
			else if (index == R.styleable.NacDayButton_nacBackgroundColor)
			{
				this.backgroundColor = attributes.backgroundColor;
			}
			else if (index == R.styleable.NacDayButton_nacDrawable)
			{
				this.drawable = attributes.drawable;
			}
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
	 * Merge attributes of another class that utilizes this one, with this
	 * class.
	 */
	public void mergeAttributes(Context context, AttributeSet attrs)
	{
		NacDayAttributes parsed = new NacDayAttributes(context, attrs);
		Resources res = context.getResources();
		TypedArray ta = res.obtainAttributes(attrs, R.styleable.NacDayButton);

		try
		{
			for (int index=0; index < ta.length(); index++)
			{
				if (!ta.hasValue(index))
				{
					continue;
				}

				this.mAttributes.merge(index, parsed);
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

		this.mAttributes = new NacDayAttributes(context, attrs);
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
	public void setViewAttributes()
	{
		this.setWidthAndHeight(this.getButtonWidth(), this.getButtonHeight());
		this.setBackground(this.getBackgroundResource());
		this.setButtonColor(this.getDefaultButtonColor());
		this.setTextColor(this.getDefaultTextColor());
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
		this.mAttributes.backgroundColor = color;
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
		this.mAttributes.drawable = resid;
		this.mButton.setBackgroundResource(resid);
	}

	/**
	 * @return The button color.
	 */
	public int getButtonColor()
	{
		Object tag = this.mButton.getTag();

		return (tag != null) ? (Integer) tag : this.getDefaultButtonColor();
	}

	/**
	 * @return The default button color.
	 */
	public int getDefaultButtonColor()
	{
		return this.mAttributes.backgroundColor;
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
		return this.mAttributes.drawable;
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

