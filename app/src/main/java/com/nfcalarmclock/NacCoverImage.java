package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 */
@SuppressWarnings("ResourceType")
public class NacCoverImage
	extends LinearLayout
{

	/**
	 * Attributes for the view.
	 */
	public static class Attributes
	{
		public int imageHeight;
		public int imageId;
		public int imageWidth;
		public int spacing;
		public String text;
		public int textColor;
		public int textSize;
		public int textStyle;

		/**
		 *Initialize the attributes.
		 */
		public Attributes(Context context, AttributeSet attrs)
		{
			if (attrs == null)
			{
				return;
			}

			TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.NacCoverImage, 0, 0);

			try
			{
				this.imageHeight = (int) ta.getDimension(R.styleable.NacCoverImage_nacDrawableHeight, 0);
				this.imageWidth = (int) ta.getDimension(R.styleable.NacCoverImage_nacDrawableWidth, 0);
				this.spacing = (int) ta.getDimension(R.styleable.NacCoverImage_nacSpacing, 0);
			}
			finally
			{
				ta.recycle();
			}

			int[] array = new int[] { android.R.attr.src, android.R.attr.text,
				android.R.attr.textColor, android.R.attr.textSize,
				android.R.attr.textStyle };
			ta = context.obtainStyledAttributes(attrs, array);

			try
			{
				// Set default text size
				this.imageId = ta.getResourceId(0, View.NO_ID);
				this.text = ta.getString(1);
				this.textColor = ta.getColor(2, Color.WHITE);
				this.textSize = (int) ta.getDimension(3, 20);
				this.textStyle = ta.getInt(4, 0);
			}
			finally
			{
				ta.recycle();
			}
		}

	}

	/**
	 * Attributes.
	 */
	private Attributes mAttributes;

	/**
	 * Cover container.
	 */
	private LinearLayout mContainer;

	/**
	 * Cover image.
	 */
	private ImageView mImageView;

	/**
	 * Cover subtitle.
	 */
	private TextView mTextView;

	/**
	 */
	public NacCoverImage(Context context)
	{
		super(context, null);
		init(null);
	}

	/**
	 */
	public NacCoverImage(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs);
	}

	/**
	 */
	public NacCoverImage(Context context, AttributeSet attrs,
		int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	/**
	 * @return The view height.
	 */
	public int getImageHeight()
	{
		return this.mAttributes.imageHeight;
	}

	/**
	 * @return The image resource of the view.
	 */
	public int getImageId()
	{
		return this.mAttributes.imageId;
	}

	/**
	 * @return The view width.
	 */
	public int getImageWidth()
	{
		return this.mAttributes.imageWidth;
	}

	/**
	 * @return The spacing between image and text.
	 */
	public int getSpacing()
	{
		return this.mAttributes.spacing;
	}

	/**
	 * @return The text in the title.
	 */
	public String getText()
	{
		return this.mAttributes.text;
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
	 * @return The text style.
	 */
	public int getTextStyle()
	{
		return this.mAttributes.textStyle;
	}

	/**
	 * Setup the contents of the cover image.
	 */
	private void init(AttributeSet attrs)
	{
		Context context = getContext();

		//setGravity(Gravity.CENTER);
		//setOrientation(LinearLayout.VERTICAL);
		LayoutInflater.from(context).inflate(R.layout.nac_cover_image,
			this, true);

		this.mAttributes = new Attributes(context, attrs);
		this.mContainer = (LinearLayout) findViewById(R.id.cover_container);
		this.mImageView = (ImageView) findViewById(R.id.cover_image);
		this.mTextView = (TextView) findViewById(R.id.cover_subtitle);

		if ((this.mImageView == null) || (this.mTextView == null))
		{
			throw new RuntimeException("Unable to find Image or Text view.");
		}
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
	 * Redraw the view.
	 */
	public void redraw()
	{
		invalidate();
		requestLayout();
	}

	/**
	 * Set the view attributes.
	 */
	public void setViewAttributes()
	{
		this.setImage(this.getImageId());
		this.setImageSize(this.getImageWidth(), this.getImageHeight());
		this.setSpacing(this.getSpacing());
		this.setText(this.getText());
		this.setTextColor(this.getTextColor());
		this.setTextSize(this.getTextSize());
		this.setTextStyle(this.getTextStyle());
	}

	/**
	 * Set image resource.
	 */
	public void setImage(int id)
	{
		if (id == View.NO_ID)
		{
			return;
		}

		this.mAttributes.imageId = id;

		this.mImageView.setImageResource(id);
		this.redraw();
	}

	/**
	 * Set the image bitmap.
	 */
	public void setImage(Bitmap bitmap)
	{
		this.mImageView.setImageBitmap(bitmap);
		this.redraw();
	}

	/**
	 * Set image size.
	 */
	public void setImageSize(int width, int height)
	{
		LayoutParams rootParams = (LayoutParams) this.mContainer.getLayoutParams();
		LayoutParams imageParams = (LayoutParams) this.mImageView.getLayoutParams();

		if (rootParams == null)
		{
			rootParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		}

		if (imageParams == null)
		{
			imageParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		}

		rootParams.width = width;
		imageParams.width = width;
		imageParams.height = height;
		this.mAttributes.imageWidth = width;
		this.mAttributes.imageHeight = height;

		this.mImageView.setLayoutParams(imageParams);
		this.mContainer.setLayoutParams(rootParams);
		this.redraw();
	}

	/**
	 * Set the spacing between the cover image and subtitle.
	 */
	public void setSpacing(int spacing)
	{
		this.mAttributes.spacing = spacing;
		LayoutParams params = (LayoutParams) this.mImageView.getLayoutParams();

		if (params == null)
		{
			params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		}

		params.setMargins(0, 0, 0, spacing);
		this.mImageView.setLayoutParams(params);
	}

	/**
	 * Set the text.
	 *
	 * @param  text  The text.
	 */
	public void setText(String text)
	{
		this.mAttributes.text = text;
		this.mTextView.setText(text);
		this.redraw();
	}

	/**
	 * Set the color of the text.
	 *
	 * @param  color  The color.
	 */
	public void setTextColor(int color)
	{
		this.mTextView.setTextColor(color);
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
		this.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
		this.redraw();
	}

	/**
	 * Set the text style.
	 *
	 * @param  size  The text size.
	 */
	public void setTextStyle(int style)
	{
		this.mAttributes.textStyle = style;
		this.mTextView.setTypeface(null, style);
		this.redraw();
	}

}
