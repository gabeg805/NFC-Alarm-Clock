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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.Keep;
import androidx.core.content.ContextCompat;

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
		public String text;

		/**
		 * Initialize the attributes.
		 */
		public NacDayAttributes(Context context, AttributeSet attrs)
		{
			Resources.Theme theme = context.getTheme();
			TypedArray ta = theme.obtainStyledAttributes(attrs,
				R.styleable.NacDayButton, 0, R.style.NacDayButton);
			TypedArray androidTa = theme.obtainStyledAttributes(attrs,
				new int[] { android.R.attr.text }, 0, 0);

			try
			{
				Resources res = context.getResources();
				this.width = (int) ta.getDimension(R.styleable.NacDayButton_nacWidth,
					ViewGroup.LayoutParams.WRAP_CONTENT);
				this.height = (int) ta.getDimension(R.styleable.NacDayButton_nacHeight,
					ViewGroup.LayoutParams.WRAP_CONTENT);
				this.duration = ta.getInt(R.styleable.NacDayButton_nacDuration, 1000);
				this.text = androidTa.getString(0);
			}
			finally
			{
				ta.recycle();
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
		//NacUtility.printf("Animating button!");
		animateDay(NacDayViewType.BUTTON);
	}

	/**
	 * Animate the button or text.
	 */
	public void animateDay(NacDayViewType type)
	{
		//NacUtility.printf("YOYOYO Animating!");
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
		//NacUtility.printf("Animating text!");
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
	//public Drawable getBackground()
	//{
	//	return this.mButton.getBackground();
	//}

	/**
	 * @return The background resource of the button.
	 */
	//public int getBackgroundResource()
	//{
	//	return this.mAttributes.drawable;
	//}

	/**
	 * @return The button.
	 */
	public Button getButton()
	{
		return this.mButton;
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
		Context context = getContext();
		return ContextCompat.getColor(context, R.color.gray_light);
	}

	/**
	 * @return The default text color.
	 */
	public int getDefaultTextColor()
	{
		Context context = getContext();
		return ContextCompat.getColor(context, R.color.white);
	}

	/**
	 * @return The animation duration.
	 */
	public int getDuration()
	{
		return (this.isEnabled()) ? this.mAttributes.duration
			: this.mAttributes.duration * 2 / 3;
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
		return (colorlist != null)
			? colorlist.getDefaultColor()
			: this.getDefaultTextColor();
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
	 */
	@Override
	public void onClick(View view)
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
		//NacUtility.printf("REDRAWING!");
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
	//public void setBackground(Drawable bg)
	//{
	//	this.mButton.setBackground(bg);
	//}

	/**
	 * Set the background drawable.
	 * 
	 * @param  bg  The background.
	 */
	//public void setBackground(int resid)
	//{
	//	this.mAttributes.drawable = resid;
	//	this.mButton.setBackgroundResource(resid);
	//}

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
		Drawable drawable = this.getButton().getBackground();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			BlendModeColorFilter blendFilter = new BlendModeColorFilter(
				color, BlendMode.SRC);

			drawable.setColorFilter(blendFilter);
		}
		else
		{
			drawable.setColorFilter(color, PorterDuff.Mode.SRC);
		}

		//NacUtility.printf("Setting button color : %d", color);
		//this.mButton.setBackgroundColor(color);
		this.mButton.setTag(color);
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
	 * Set the end values.
	 */
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
		//NacUtility.printf("Setting text color : %d", color);
		this.mButton.setTextColor(color);
		this.redraw();
	}

	/**
	 * Set view attributes.
	 */
	public void setViewAttributes()
	{
		this.setWidthAndHeight(this.getButtonWidth(), this.getButtonHeight());
		//this.setBackground(this.getBackgroundResource());
		//this.setButtonColor(this.getDefaultButtonColor());
		//this.setTextColor(this.getDefaultTextColor());
		this.setText(this.getText());
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

