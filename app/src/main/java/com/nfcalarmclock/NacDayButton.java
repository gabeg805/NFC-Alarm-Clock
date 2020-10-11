package com.nfcalarmclock;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.Keep;

/**
 * A button that consists of an image to the left, and text to the right
 * of it.
 */
public class NacDayButton
	extends LinearLayout
	implements View.OnClickListener
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
		public int paddingTop;
		public int paddingBottom;
		public int paddingStart;
		public int paddingEnd;

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
				int textsize = (int) res.getDimension(R.dimen.tsz_normal);
				this.width = (int) ta.getDimension(R.styleable.NacDayButton_nacWidth, 2*textsize);
				this.height = (int) ta.getDimension(R.styleable.NacDayButton_nacHeight, 2*textsize);
				this.duration = ta.getInt(R.styleable.NacDayButton_nacDuration, 1000);
				this.textColor = ta.getColor(R.styleable.NacDayButton_nacTextColor, Color.WHITE);
				this.textSize = (int) ta.getDimension(R.styleable.NacDayButton_nacTextSize, textsize);
				this.text = ta.getString(R.styleable.NacDayButton_nacText);
				this.backgroundColor = ta.getColor(R.styleable.NacDayButton_nacBackgroundColor, Color.WHITE);
				this.drawable = ta.getResourceId(R.styleable.NacDayButton_nacDrawable, R.drawable.day_button);
				this.paddingTop = (int) ta.getDimension(R.styleable.NacDayButton_nacPaddingTop, 0);
				this.paddingBottom = (int) ta.getDimension(R.styleable.NacDayButton_nacPaddingBottom, 0);
				this.paddingStart = (int) ta.getDimension(R.styleable.NacDayButton_nacPaddingStart, 0);
				this.paddingEnd = (int) ta.getDimension(R.styleable.NacDayButton_nacPaddingEnd, 0);

				if (this.paddingStart == 0)
				{
					this.paddingStart = (int) ta.getDimension(R.styleable.NacDayButton_nacPaddingLeft, 0);
				}

				if (this.paddingEnd == 0)
				{
					this.paddingEnd = (int) ta.getDimension(R.styleable.NacDayButton_nacPaddingRight, 0);
				}
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
			else if (index == R.styleable.NacDayButton_nacPaddingTop)
			{
				this.paddingTop = attributes.paddingTop;
			}
			else if (index == R.styleable.NacDayButton_nacPaddingBottom)
			{
				this.paddingBottom = attributes.paddingBottom;
			}
			else if (index == R.styleable.NacDayButton_nacPaddingStart)
			{
				this.paddingStart = attributes.paddingStart;
			}
			else if (index == R.styleable.NacDayButton_nacPaddingEnd)
			{
				this.paddingEnd = attributes.paddingEnd;
			}
			else if (index == R.styleable.NacDayButton_nacPaddingLeft)
			{
				if (this.paddingStart == 0)
				{
					this.paddingStart = attributes.paddingStart;
				}
			}
			else if (index == R.styleable.NacDayButton_nacPaddingRight)
			{
				if (this.paddingEnd == 0)
				{
					this.paddingEnd = attributes.paddingEnd;
				}
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
	//private ValueAnimator mButtonAnimator;
	private ObjectAnimator mButtonAnimator;

	/**
	 * Text animator.
	 */
	//private ValueAnimator mTextAnimator;
	private ObjectAnimator mTextAnimator;

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
	 * Animate the button's change in color.
	 */
	public void animateButton()
	{
		animateDay(NacDayViewType.BUTTON);
	}

	/**
	 * Animate the button or text.
	 */
	public void animateDay(NacDayViewType type)
	{
		this.cancelAnimator();

		ObjectAnimator animator = this.startAnimator(type);

		this.setAnimator(animator, type);


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
	 * Enable and animate the view.
	 */
	public void animateEnable()
	{
		this.enable();
		this.animateButton();
	}

	/**
	 * Animate the text's change in color.
	 */
	public void animateText()
	{
		animateDay(NacDayViewType.TEXT);
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
	 * Cancel the animator.
	 */
	public void cancelAnimator()
	{
		if (this.mButtonAnimator != null)
		{
			this.mButtonAnimator.cancel();
			this.setEndValues();
		}

		if (this.mTextAnimator != null)
		{
			this.mTextAnimator.cancel();
			this.setEndValues();
		}
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
	 * Enable a button by setting the button color to the initial color of the
	 * text, and the text is set to the initial color of the button.
	 */
	public void enable()
	{
		this.setButtonColor(this.getDefaultTextColor());
		this.setTextColor(this.getDefaultButtonColor());
	}

	/**
	 * @return The background drawable of the button.
	 */
	public Drawable getBackground()
	{
		return this.mButton.getBackground();
	}

	/**
	 * @return The background resource of the button.
	 */
	public int getBackgroundResource()
	{
		return this.mAttributes.drawable;
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
	 * @return The view height.
	 */
	public int getButtonHeight()
	{
		return this.mAttributes.height;
	}

	/**
	 * @return The view width.
	 */
	public int getButtonWidth()
	{
		return this.mAttributes.width;
	}

	/**
	 * @return The default button color.
	 */
	public int getDefaultButtonColor()
	{
		return this.mAttributes.backgroundColor;
	}

	/**
	 * @return The default text color.
	 */
	public int getDefaultTextColor()
	{
		return this.mAttributes.textColor;
	}

	/**
	 * @return The animation duration.
	 */
	public int getDuration()
	{
		return (this.isEnabled()) ? this.mAttributes.duration
			: this.mAttributes.duration * 2 / 3;
	}

	private void setEndValues()
	{
		int textColor = this.getTextColor();
		int buttonColor = this.getButtonColor();
		int defTextColor = this.getDefaultTextColor();
		int defButtonColor = this.getDefaultButtonColor();

		if (buttonColor == defButtonColor)
		{
			textColor = defTextColor;
		}
		else if (buttonColor == defTextColor)
		{
			textColor = defButtonColor;
		}
		else
		{
			if (textColor == defTextColor)
			{
				buttonColor = defButtonColor;
			}
			else if (textColor == defButtonColor)
			{
				buttonColor = defTextColor;
			}
			else
			{
				buttonColor = defButtonColor;
				textColor = defTextColor;
			}
		}

		this.setButtonColor(buttonColor);
		this.setTextColor(textColor);
	}

	public int getFromValue(NacDayViewType type)
	{
		if (type == NacDayViewType.BUTTON)
		{
			return this.getTextColor();
		}
		else if (type == NacDayViewType.TEXT)
		{
			return this.getButtonColor();
		}
		else
		{
			return Color.WHITE;
		}
	}

	public int getToValue(NacDayViewType type)
	{
		if (type == NacDayViewType.BUTTON)
		{
			return this.getButtonColor();
		}
		else if (type == NacDayViewType.TEXT)
		{
			return this.getTextColor();
		}
		else
		{
			return Color.WHITE;
		}
	}

	//private void setAnimator(NacDayViewType type)
	//{
	//	if (type == NacDayViewType.BUTTON)
	//	{

	//		this.mButtonAnimator = animator;
	//	}
	//	else if (type == NacDayViewType.TEXT)
	//	{

	//		this.mTextAnimator = animator;
	//	}
	//}

	/**
	 * @return The bottom padding.
	 */
	public int getPaddingBottom()
	{
		return this.mAttributes.paddingBottom;
	}

	/**
	 * @return The end padding.
	 */
	public int getPaddingEnd()
	{
		return this.mAttributes.paddingEnd;
	}

	/**
	 * @return The start padding.
	 */
	public int getPaddingStart()
	{
		return this.mAttributes.paddingStart;
	}

	/**
	 * @return The top padding.
	 */
	public int getPaddingTop()
	{
		return this.mAttributes.paddingTop;
	}

	/**
	 * @return The text in the button.
	 */
	public String getText()
	{
		return this.mAttributes.text;
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
	 * @return The text size.
	 */
	public int getTextSize()
	{
		return this.mAttributes.textSize;
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
		setOnClickListener(this);
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
	 * @return True if the button is enabled and false if it is not.
	 */
	public boolean isEnabled()
	{
		return (this.getButtonColor() == this.getDefaultTextColor())
			&& (this.getTextColor() == this.getDefaultButtonColor());
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
	 * Finish setting up the View.
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.setViewAttributes();
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
	 * Set the currently used animator for the give view type.
	 */
	private void setAnimator(ObjectAnimator animator, NacDayViewType type)
	{
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
	 * Set the button color.
	 *
	 * @param  color  The button color.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Keep
	@TargetApi(Build.VERSION_CODES.Q)
	public void setButtonColor(int color)
	{
		Drawable drawable = this.getBackground();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			BlendModeColorFilter blendFilter = new BlendModeColorFilter(
				color, BlendMode.SRC_IN);

			drawable.setColorFilter(blendFilter);
		}
		else
		{
			drawable.setColorFilter(color, PorterDuff.Mode.SRC);
		}

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
	 * Set the default text color.
	 *
	 * @param  color  The default text color.
	 */
	public void setDefaultTextColor(int color)
	{
		this.mAttributes.textColor = color;
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
	 * Set an onClick listener for each of the day of week buttons.
	 */
	public void setOnClickListener(NacDayButton.OnClickListener listener)
	{
		this.mListener = listener;
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
	 * Set the text color.
	 *
	 * @param  color  The text color.
	 */
	@Keep
	public void setTextColor(int color)
	{
		this.mButton.setTextColor(color);
		this.redraw();
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
		setPadding(this.getPaddingStart(), this.getPaddingTop(),
			this.getPaddingEnd(), this.getPaddingBottom());
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
	 * @return An ObjectAnimator to animate the day button.
	 */
	private ObjectAnimator startAnimator(NacDayViewType type)
	{
		int duration = this.getDuration();
		int from = this.getFromValue(type);
		int to = this.getToValue(type);
		ObjectAnimator animator;

		if (type == NacDayViewType.BUTTON)
		{
			animator = ObjectAnimator.ofArgb(this, "buttonColor", from, to);
		}
		else if (type == NacDayViewType.TEXT)
		{
			animator = ObjectAnimator.ofArgb(this, "textColor", from, to);
		}
		else
		{
			return null;
		}

		animator.setDuration(duration);
		animator.start();

		return animator;
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

}

