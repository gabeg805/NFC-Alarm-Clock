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
@SuppressWarnings("ResourceType")
public class NacButtonGroup
    extends LinearLayout
{

	/**
	 * Attributes for the view.
	 */
	public class NacButtonGroupAttributes
	{
		public int backgroundId;
		public int paddingBottom;
		public int paddingEnd;
		public int paddingStart;
		public int paddingTop;
		public int imageColor;
		public int imageHeight;
		public int imageId;
		public int imageWidth;
		public int spacing;
		public String text;
		public int textColor;
		public int textSize;
		public String textSubtitle;
		public int textSubtitleColor;
		public String textTitle;
		public int textTitleColor;

		/**
		 * Initialize the attributes.
		 */
		public NacButtonGroupAttributes(Context context, AttributeSet attrs)
		{
			if (attrs == null)
			{
				return;
			}

			TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.NacButtonGroup, 0, 0);
				//R.styleable.NacDayButton, 0, R.style.NacDayButton);

			try
			{
				Resources res = context.getResources();
				int spacing = (int) res.getDimension(R.dimen.sp_card);
				float width = res.getDimension(R.dimen.isz_card);
				float height = res.getDimension(R.dimen.isz_card);
				int textsize = (int) res.getDimension(R.dimen.tsz_card_days);

				this.paddingBottom = (int) ta.getDimension(R.styleable.NacButtonGroup_nacPaddingBottom, 0);
				this.paddingEnd = (int) ta.getDimension(R.styleable.NacButtonGroup_nacPaddingEnd, 0);
				this.paddingStart = (int) ta.getDimension(R.styleable.NacButtonGroup_nacPaddingStart, 0);
				this.paddingTop = (int) ta.getDimension(R.styleable.NacButtonGroup_nacPaddingTop, 0);
				this.imageColor = ta.getColor(R.styleable.NacButtonGroup_nacDrawableColor, Color.WHITE);
				this.imageHeight = (int) ta.getDimension(R.styleable.NacButtonGroup_nacDrawableHeight, height);
				this.imageId = ta.getResourceId(R.styleable.NacButtonGroup_nacDrawable, R.drawable.circle);
				this.imageWidth = (int) ta.getDimension(R.styleable.NacButtonGroup_nacDrawableWidth, width);
				this.spacing= (int) ta.getDimension(R.styleable.NacButtonGroup_nacSpacing, spacing);
				this.text = ta.getString(R.styleable.NacButtonGroup_nacText);
				this.textColor = ta.getColor(R.styleable.NacButtonGroup_nacTextColor, Color.WHITE);
				this.textSize = (int) ta.getDimension(R.styleable.NacButtonGroup_nacTextSize, textsize);
				this.textSubtitle = ta.getString(R.styleable.NacButtonGroup_nacSubText);
				this.textSubtitleColor = ta.getColor(R.styleable.NacButtonGroup_nacSubTextColor, Color.WHITE);
				this.textTitle = ta.getString(R.styleable.NacButtonGroup_nacText);
				this.textTitleColor = ta.getColor(R.styleable.NacButtonGroup_nacTextColor, Color.WHITE);
			}
			finally
			{
				ta.recycle();
			}

			int[] array = new int[] { android.R.attr.background };
			ta = context.obtainStyledAttributes(attrs, array);

			try
			{
				this.backgroundId = ta.getResourceId(0, View.NO_ID);
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
	public NacButtonGroupAttributes mAttributes;

	/**
	 */
	public NacButtonGroup(Context context)
	{
		super(context, null);
		init(null);
	}

	/**
	 */
	public NacButtonGroup(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		init(attrs);
	}

    /**
     */
    public NacButtonGroup(Context context, AttributeSet attrs,
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
    }

    /**
     * Setup the contents of the button.
     */
    private void init(AttributeSet attrs)
    {
		Context context = getContext();
		this.mAttributes = new NacButtonGroupAttributes(context, attrs);

        setOrientation(LinearLayout.VERTICAL);
    }

	/**
	 * Add view to container.
	 */
	public void add(NacImageTextButton button)
	{
		NacImageTextButton.Attributes attr =
			new NacImageTextButton.Attributes(null, null);

		attr.imageColor = this.mAttributes.imageColor;
		attr.imageHeight = this.mAttributes.imageHeight;
		attr.imageId = this.mAttributes.imageId;
		attr.imageWidth = this.mAttributes.imageWidth;
		attr.spacing = this.mAttributes.spacing;
		attr.text = this.mAttributes.text;
		attr.textColor = this.mAttributes.textColor;
		attr.textSize = this.mAttributes.textSize;

		button.setAttributes(attr);
		button.setViewAttributes();
		this.addView(button);
		button.setPadding(this.mAttributes.paddingStart,
			this.mAttributes.paddingTop, this.mAttributes.paddingEnd,
			this.mAttributes.paddingBottom);
		button.setBackgroundResource(this.mAttributes.backgroundId);
	}

	/**
	 * Add view to container.
	 */
	public void add(NacImageSubTextButton button)
	{
		NacImageSubTextButton.Attributes attr =
			new NacImageSubTextButton.Attributes(null, null);

		attr.imageColor = this.mAttributes.imageColor;
		attr.imageHeight = this.mAttributes.imageHeight;
		attr.imageId = this.mAttributes.imageId;
		attr.imageWidth = this.mAttributes.imageWidth;
		attr.spacing = this.mAttributes.spacing;
		attr.textSize = this.mAttributes.textSize;
		attr.textTitle = this.mAttributes.textTitle;
		attr.textSubtitle = this.mAttributes.textSubtitle;
		attr.textTitleColor = this.mAttributes.textTitleColor;
		attr.textSubtitleColor = this.mAttributes.textSubtitleColor;

		button.setAttributes(attr);
		button.setViewAttributes();
		this.addView(button);
		button.setPadding(this.mAttributes.paddingStart,
			this.mAttributes.paddingTop, this.mAttributes.paddingEnd,
			this.mAttributes.paddingBottom);
		button.setBackgroundResource(this.mAttributes.backgroundId);
	}

}
