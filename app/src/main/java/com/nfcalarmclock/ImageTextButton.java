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
import android.widget.LinearLayout;
import android.widget.TextView;

import android.graphics.Typeface;

/**
 * @class A button that consists of an image to the left, and text to the right
 *        of it.
 */
public class ImageTextButton
    extends LinearLayout
{

    private Context mContext;
    private ImageView mImageView;
    private Drawable mImageDrawable;
    private int mImageWidth;
    private int mImageHeight;
    private int mImageSpacing;
    private int mImageColor;
    private TextView mTextView;
    private String mTextString;
    private int mTextSize;
    private int mTextColor;

    /**
     * @brief The constructor.
     */
    public ImageTextButton(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;
        int[] id = R.styleable.ImageTextButton;
        int layout = R.layout.imagetextbutton;
        Theme theme = this.mContext.getTheme();
        TypedArray ta = theme.obtainStyledAttributes(attrs, id, 0, 0);
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(this.mContext).inflate(layout, this, true);
        init(ta);
    }

    /**
     * @brief Finish setting up the view.
     */
    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        setImage();
        setText();
    }

    /**
     * @brief Setup the contents of the button.
     */
    private void init(TypedArray ta)
    {
        try
        {
            initImage(ta);
            initText(ta);
        }
        finally
        {
            ta.recycle();
        }
    }

    /**
     * @brief Initialize all elements of the image view.
     */
    private void initImage(TypedArray ta)
    {
        Resources r = this.mContext.getResources();
        initImageView();
        initImageDrawable(ta);
        initImageSize(ta, r);
        initImageColor(ta);
        initImageSpacing(ta, r);
    }

    /**
     * @brief Initialize all elements of the text view.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initText(TypedArray ta)
    {
        Resources r = this.mContext.getResources();
        initTextView();
        initTextString(ta);
        initTextColor(ta);
        initTextSize(ta, r);
    }

    /**
     * @brief Define the image view.
     */
    private void initImageView()
    {
        this.mImageView = (ImageView) findViewById(R.id.itb_image);
        if (this.mImageView == null)
        {
            throw new RuntimeException("Unable to find image ID.");
        }
    }

    /**
     * @brief Define the image drawable.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initImageDrawable(TypedArray ta)
    {
        int did = R.styleable.ImageTextButton_nacDrawable;
        this.mImageDrawable = ta.getDrawable(did);
    }

    /**
     * @brief Define the width and height of the image.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initImageSize(TypedArray ta, Resources r)
    {
        int wid = R.styleable.ImageTextButton_nacDrawableWidth;
        int hid = R.styleable.ImageTextButton_nacDrawableHeight;
        float width = r.getDimension(R.dimen.isz_card);
        float height = r.getDimension(R.dimen.isz_card);
        this.mImageWidth = (int) ta.getDimension(wid, width);
        this.mImageHeight = (int) ta.getDimension(hid, height);
    }

    /**
     * @brief Define the color of the image.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initImageColor(TypedArray ta)
    {
        int cid = R.styleable.ImageTextButton_nacDrawableColor;
        int color = NacUtility.getThemeAttrColor(this.mContext,
                                                 R.attr.colorCardDrawable);
        this.mImageColor = ta.getColor(cid, color);
    }

    /**
     * @brief Define the spacing between the image and text.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initImageSpacing(TypedArray ta, Resources r)
    {
        int sid = R.styleable.ImageTextButton_nacDrawableTextSpacing;
        float spacing = r.getDimension(R.dimen.sp_card);
        this.mImageSpacing = (int) ta.getDimension(sid, spacing);
    }

    /**
     * @brief Define the text view.
     */
    private void initTextView()
    {
        this.mTextView = (TextView) findViewById(R.id.itb_text);
        if (this.mTextView == null)
        {
            throw new RuntimeException("Unable to find text ID.");
        }
    }

    /**
     * @brief Define the text string.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initTextString(TypedArray ta)
    {
        int tid = R.styleable.ImageTextButton_nacText;
        this.mTextString = ta.getString(tid);
    }

    /**
     * @brief Define the color of the text.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initTextColor(TypedArray ta)
    {
        int cid = R.styleable.ImageTextButton_nacTextColor;
        int color = NacUtility.getThemeAttrColor(this.mContext,
                                                 R.attr.colorCardDrawable);
        this.mTextColor = ta.getColor(cid, color);
    }

    /**
     * @brief Define the size of the text.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initTextSize(TypedArray ta, Resources r)
    {
        int tsid = R.styleable.ImageTextButton_nacTextSize;
        int size = (int) r.getDimension(R.dimen.tsz_card);
        this.mTextSize = ta.getDimensionPixelSize(tsid, size);
    }

    /**
     * @brief Setup the image view.
     */
    private void setImage()
    {
        ImageView iv = this.mImageView;
        if (iv == null)
        {
            throw new RuntimeException("Unable to find image ID.");
        }
        LinearLayout.LayoutParams params =
            (LinearLayout.LayoutParams) iv.getLayoutParams();
        params.width = this.mImageWidth;
        params.height = this.mImageHeight;
        params.setMargins(0, 0, this.mImageSpacing, 0);
        iv.setLayoutParams(params);
        iv.setAdjustViewBounds(true);
        iv.setImageDrawable(this.mImageDrawable);
        iv.setColorFilter(this.mImageColor);
    }

    /**
     * @brief Setup the text view.
     */
    private void setText()
    {
        TextView tv = this.mTextView;
        if (tv == null)
        {
            throw new RuntimeException("Unable to find text ID.");
        }
        tv.setText(this.mTextString);
        tv.setTextColor(this.mTextColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.mTextSize);
    }

    /**
     * @brief Set the text to display in the text view.
     */
    public void setText(String text)
    {
        if (this.mTextView == null)
        {
            throw new RuntimeException("Unable to find text ID.");
        }
        this.mTextString = text;
        this.setText();
    }

	/**
	 * @return The layout TextView.
	 */
	public TextView getTextView()
	{
		return this.mTextView;
	}

	/**
	 * Set the text focus
	 *
	 * @param  focus  When true, alpha will be 1.0, and when false, alpha will be 0.5.
	 */
	public void setFocus(boolean focus)
	{
		float alpha = (focus) ? 1.0f : 0.5f;

		this.mTextView.setAlpha(alpha);
	}

}
