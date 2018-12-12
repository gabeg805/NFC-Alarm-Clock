package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.graphics.Typeface;
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
 * @class A button that consists of an image to the left, and text to the right
 *        of it.
 */
public class NacImageSubTextButton
    extends RelativeLayout
{

	/**
	 * Attributes for the view.
	 */
	public class NacImageSubTextButtonAttributes
	{
		public int imageWidth;
		public int imageHeight;
		public int imageColor;
		public int imageId;
		public int spacing;
		public String textTitle;
		public String textSubtitle;
		public int textSize;
		public int textTitleColor;
		public int textSubtitleColor;

		/**
		 * Initialize the attributes.
		 */
		public NacImageSubTextButtonAttributes(Context context, AttributeSet attrs)
		{
			if (attrs == null)
			{
				return;
			}

			TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.NacImageSubTextButton, 0, 0);
				//R.styleable.NacDayButton, 0, R.style.NacDayButton);

			try
			{
				Resources res = context.getResources();
				int textsize = (int) res.getDimension(R.dimen.tsz_card_days);
				int spacing = (int) res.getDimension(R.dimen.sp_card);
				this.imageWidth = (int) ta.getDimension(R.styleable.NacImageSubTextButton_nacDrawableWidth, 2*textsize);
				this.imageHeight = (int) ta.getDimension(R.styleable.NacImageSubTextButton_nacDrawableHeight, 2*textsize);
				this.imageColor = ta.getColor(R.styleable.NacImageSubTextButton_nacDrawableColor, Color.WHITE);
				this.imageId = ta.getResourceId(R.styleable.NacImageSubTextButton_nacDrawable, R.drawable.circle);
				this.spacing= (int) ta.getDimension(R.styleable.NacImageSubTextButton_nacSpacing, spacing);
				this.textSize = (int) ta.getDimension(R.styleable.NacImageSubTextButton_nacTextSize, textsize);
				this.textTitleColor = ta.getColor(R.styleable.NacImageSubTextButton_nacTextColor, Color.WHITE);
				this.textSubtitleColor = ta.getColor(R.styleable.NacImageSubTextButton_nacSubTextColor, Color.WHITE);
				this.textTitle = ta.getString(R.styleable.NacImageSubTextButton_nacText);
				this.textSubtitle = ta.getString(R.styleable.NacImageSubTextButton_nacSubText);
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
	 * Title.
	 */
	private TextView mTitle;

	/**
	 * Subtitle.
	 */
	private TextView mSubtitle;

	/**
	 * Attributes.
	 */
	public NacImageSubTextButtonAttributes mAttributes;

	/**
	 */
	public NacImageSubTextButton(Context context)
	{
		super(context, null);
		init(null);
	}

	/**
	 */
	public NacImageSubTextButton(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		init(attrs);
	}

    /**
     */
    public NacImageSubTextButton(Context context, AttributeSet attrs,
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

        LayoutInflater.from(context).inflate(R.layout.poopimagetextbutton, this,
			true);

		this.mAttributes = new NacImageSubTextButtonAttributes(context, attrs);
		this.mImage = (ImageView) findViewById(R.id.itb_image);
		this.mTitle = (TextView) findViewById(R.id.itb_text_title);
		this.mSubtitle = (TextView) findViewById(R.id.itb_text_subtitle);

		if ((this.mImage == null) || (this.mTitle == null) || (this.mSubtitle == null))
		{
			throw new RuntimeException("Unable to find Image or Title or Subtitle view IDs.");
		}

		// set padding and/or click listener
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
	 * Set view attributes.
	 */
	public void setViewAttributes()
	{
		//NacUtility.printf("Width : %d", getImageWidth());
		//NacUtility.printf("Height: %d", getImageHeight());
		//NacUtility.printf("Resource : %d", getImageBackgroundResource());
		//NacUtility.printf("Image Color : %d", getImageColor());
		//NacUtility.printf("Spacing : %d", getSpacing());
		//NacUtility.printf("Title : %s", getTextTitle());
		//NacUtility.printf("Subtitle : %s", getTextSubtitle());
		//NacUtility.printf("Title Color : %d", getTextTitleColor());
		//NacUtility.printf("Subtitle Color : %d", getTextSubtitleColor());
		//NacUtility.printf("Text Size : %d", getTextSize());

		this.setImageSize(this.getImageWidth(), this.getImageHeight());
		this.setImageBackground(this.getImageBackgroundResource());
		this.setImageColor(this.getImageColor());
		this.setSpacing(this.getSpacing());
		this.setTextTitle(this.getTextTitle());
		this.setTextSubtitle(this.getTextSubtitle());
		this.setTextTitleColor(this.getTextTitleColor());
		this.setTextSubtitleColor(this.getTextSubtitleColor());
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
	 * Set the spacing between the image and text.
	 * 
	 * @param  spacing  The spacing.
	 */
	public void setSpacing(int spacing)
	{
		this.mAttributes.spacing = spacing;
		LayoutParams params = (LayoutParams) this.mImage.getLayoutParams();
        params.setMargins(0, 0, spacing, 0);
        this.mImage.setLayoutParams(params);
	}

	/**
	 * Set the color of the title text.
	 *
	 * @param  color  The text color.
	 */
	public void setTextTitleColor(int color)
	{
		this.mTitle.setTextColor(color);
		this.redraw();
	}

	/**
	 * Set the color of the subtitle text.
	 *
	 * @param  color  The text color.
	 */
	public void setTextSubtitleColor(int color)
	{
		this.mSubtitle.setTextColor(color);
		this.redraw();
	}

	/**
	 * Set the title text.
	 *
	 * @param  text  The title text.
	 */
	public void setTextTitle(String text)
	{
		this.mAttributes.textTitle = text;
		this.mTitle.setText(text);
		this.redraw();
	}

	/**
	 * Set the subtitle text.
	 *
	 * @param  text  The subtitle text.
	 */
	public void setTextSubtitle(String text)
	{
		this.mAttributes.textSubtitle = text;
		this.mSubtitle.setText(text);
		this.mSubtitle.setAlpha(0.5f);
		this.redraw();
	}

	/**
	 * Set the title text size.
	 *
	 * @param  size  The title text size.
	 */
	public void setTextSize(int size)
	{
		this.mAttributes.textSize = size;
		this.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
		this.mSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0.8f*size);
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
	public int getTextTitleColor()
	{
		return this.mAttributes.textTitleColor;
	}

	/**
	 * @return The color of the subtitle text.
	 */
	public int getTextSubtitleColor()
	{
		return this.mAttributes.textSubtitleColor;
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
	public String getTextTitle()
	{
		return this.mAttributes.textTitle;
	}

	/**
	 * @return The text in the subtitle.
	 */
	public String getTextSubtitle()
	{
		return this.mAttributes.textSubtitle;
	}

}
