package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A button that consists of an image to the left, and text to the right of it.
 */
public class NacImageTextButton
	extends LinearLayout
{

	/**
	 * Attributes for the view.
	 */
	public static class Attributes
	{
		public int imageColor;
		public int imageHeight;
		public int imageId;
		public int imageWidth;
		public int spacing;
		public String text;
		public int textColor;
		public int textSize;

		/**
		 * Initialize the attributes.
		 */
		public Attributes(Context context, AttributeSet attrs)
		{
			if (attrs == null)
			{
				return;
			}

			TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.NacImageTextButton, 0, 0);
				//R.styleable.NacDayButton, 0, R.style.NacDayButton);

			try
			{
				Resources res = context.getResources();
				int spacing = (int) res.getDimension(R.dimen.sp_card);
				float width = res.getDimension(R.dimen.isz_card);
				float height = res.getDimension(R.dimen.isz_card);
				int textsize = (int) res.getDimension(R.dimen.tsz_card_days);
				this.imageColor = ta.getColor(R.styleable.NacImageTextButton_nacDrawableColor, Color.WHITE);
				this.imageHeight = (int) ta.getDimension(R.styleable.NacImageTextButton_nacDrawableHeight, height);
				this.imageId = ta.getResourceId(R.styleable.NacImageTextButton_nacDrawable, R.drawable.circle);
				this.imageWidth = (int) ta.getDimension(R.styleable.NacImageTextButton_nacDrawableWidth, width);
				this.spacing= (int) ta.getDimension(R.styleable.NacImageTextButton_nacSpacing, spacing);
				this.text = ta.getString(R.styleable.NacImageTextButton_nacText);
				this.textColor = ta.getColor(R.styleable.NacImageTextButton_nacTextColor, Color.WHITE);
				this.textSize = (int) ta.getDimension(R.styleable.NacImageTextButton_nacTextSize, textsize);
			}
			finally
			{
				ta.recycle();
			}
		}

	}

	/**
	 * Image.
	 */
	private ImageView mImage;

	/**
	 * Text.
	 */
	private TextView mText;

	/**
	 * Attributes.
	 */
	public Attributes mAttributes;

	/**
	 */
	public NacImageTextButton(Context context)
	{
		super(context, null);
		init(null);
	}

	/**
	 */
	public NacImageTextButton(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		init(attrs);
	}

	/**
	 */
	public NacImageTextButton(Context context, AttributeSet attrs,
		int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	/**
	 * Finish setting up the view.
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.setViewAttributes();
	}

	/**
	 * Setup the contents of the button.
	 */
	private void init(AttributeSet attrs)
	{
		Context context = getContext();

		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater.from(context).inflate(R.layout.nac_image_text_button,
			this, true);

		this.mAttributes = new Attributes(context, attrs);
		this.mImage = (ImageView) findViewById(R.id.itb_image);
		this.mText = (TextView) findViewById(R.id.itb_text);

		if ((this.mImage == null) || (this.mText == null))
		{
			throw new RuntimeException("Unable to find Image or Text view IDs.");
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
	 * Set the attributes member variable.
	 */
	public void setAttributes(Attributes attr)
	{
		this.mAttributes = attr;
	}

	/**
	 * Set view attributes.
	 */
	public void setViewAttributes()
	{
		this.setImageSize(this.getImageWidth(), this.getImageHeight());
		this.setImageBackground(this.getImageBackgroundResource());
		this.setImageColor(this.getImageColor());
		this.setSpacing(this.getSpacing());
		this.setText(this.getText());
		this.setTextColor(this.getTextColor());
		this.setTextSize(this.getTextSize());
	}

	/**
	 * Set the image width and height.
	 */
	public void setImageSize(int width, int height)
	{
		LayoutParams params = (LayoutParams) this.mImage.getLayoutParams();
		params.width = width;
		params.height = height;
		this.mAttributes.imageWidth = width;
		this.mAttributes.imageHeight = height;

		this.mImage.setLayoutParams(params);
		//iv.setAdjustViewBounds(true);
		this.redraw();
	}

	/**
	 * Set the image background drawable.
	 * 
	 * @param  bg  The background.
	 */
	public void setImageBackground(Drawable bg)
	{
		this.mImage.setBackground(bg);
	}

	/**
	 * Set the image background drawable.
	 * 
	 * @param  resid  The background resource ID.
	 */
	public void setImageBackground(int resid)
	{
		if (resid == View.NO_ID)
		{
			return;
		}

		this.mAttributes.imageId = resid;

		this.mImage.setImageResource(resid);
		this.redraw();
	}

	/**
	 * Set the image color.
	 *
	 * @param  color  The image color.
	 */
	public void setImageColor(int color)
	{
		ColorStateList colorlist = ColorStateList.valueOf(color);

		this.mImage.setImageTintList(colorlist);
		this.redraw();
	}

	/**
	 * Set margins.
	 */
	public void setMargins(int start, int top, int end, int bottom)
	{
		LayoutParams params = (LayoutParams) this.getLayoutParams();

		params.setMargins(start, top, end, bottom);
		this.setLayoutParams(params);
	}

	/**
	 * Set the spacing between the image and text.
	 * 
	 * @param  spacing	The spacing.
	 */
	public void setSpacing(int spacing)
	{
		this.mAttributes.spacing = spacing;
		LayoutParams params = (LayoutParams) this.mImage.getLayoutParams();

		params.setMargins(0, 0, spacing, 0);
		this.mImage.setLayoutParams(params);
	}

	/**
	 * Set the color of the text.
	 *
	 * @param  color  The color.
	 */
	public void setTextColor(int color)
	{
		this.mText.setTextColor(color);
		this.redraw();
	}

	/**
	 * Set the text.
	 *
	 * @param  text  The text.
	 */
	public void setText(String text)
	{
		this.mAttributes.text = text;
		this.mText.setText(text);
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
		this.mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
		this.redraw();
	}

	/**
	 * @return The view width.
	 */
	public int getImageWidth()
	{
		return this.mAttributes.imageWidth;
	}

	/**
	 * @return The view height.
	 */
	public int getImageHeight()
	{
		return this.mAttributes.imageHeight;
	}

	/**
	 * @return The background resource of the image.
	 */
	public int getImageBackgroundResource()
	{
		return this.mAttributes.imageId;
	}

	/**
	 * @return The background drawable of the image.
	 */
	public Drawable getImageBackground()
	{
		return this.mImage.getBackground();
	}

	/**
	 * @return The image color.
	 */
	public int getImageColor()
	{
		return this.mAttributes.imageColor;
	}

	/**
	 * @return The spacing between image and text.
	 */
	public int getSpacing()
	{
		return this.mAttributes.spacing;
	}

	/**
	 * @return The color of the title text.
	 */
	public int getTextColor()
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
	 * @return The text in the title.
	 */
	public String getText()
	{
		return this.mAttributes.text;
	}

	/**
	 * @return The layout TextView.
	 */
	public TextView getTextView()
	{
		return this.mText;
	}

	/**
	 * Set the text focus
	 *
	 * @param  focus  When true, alpha will be 1.0, and when false, alpha will be 0.5.
	 */
	public void setFocus(boolean focus)
	{
		float alpha = (focus) ? 1.0f : 0.5f;

		this.mText.setAlpha(alpha);
	}

}
