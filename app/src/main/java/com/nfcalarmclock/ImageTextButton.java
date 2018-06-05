package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * @class A button that consists of an image to the left, and text to the right
 *        of it.
 */
public class ImageTextButton
    extends LinearLayout
{

    private static final String NAME = "NFCAlarmClock";

    private float mDefaultWidth;
    private float mDefaultHeight;
    private int mDefaultColor;
    private float mDefaultSpacing;
    private float mDefaultTextSize;

    public ImageTextButton(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.imagetextbutton, this,
                                             true);
        init(context, attrs);
    }

    /**
     * @brief Setup the contents of the button.
     */
    private void init(Context context, @Nullable AttributeSet attrs)
    {
        Resources.Theme theme = context.getTheme();
        TypedArray ta = theme.obtainStyledAttributes(attrs,
                                                     R.styleable.ImageTextButton,
                                                     0, 0);
        Resources r = context.getResources();
        try
        {
            initDefaults(r);
            initImage(ta, r);
            initText(ta, r);
        }
        finally
        {
            ta.recycle();
        }
    }

    /**
     * @brief Setup the default values.
     */
    private void initDefaults(Resources r)
    {
        this.mDefaultWidth = r.getDimension(R.dimen.isz_card);
        this.mDefaultHeight = r.getDimension(R.dimen.isz_card);
        this.mDefaultColor = r.getColor(R.color.white);
        this.mDefaultSpacing = r.getDimension(R.dimen.sp_card);
        this.mDefaultTextSize = r.getDimension(R.dimen.tsz_card);
        // Log.e(NAME, "Defaults:");
        // Log.e(NAME, "Width="+String.valueOf(this.mDefaultWidth));
        // Log.e(NAME, "Height="+String.valueOf(this.mDefaultHeight));
        // Log.e(NAME, "Color="+String.valueOf(this.mDefaultColor));
        // Log.e(NAME, "Spacing="+String.valueOf(this.mDefaultSpacing));
        // Log.e(NAME, "TextSize="+String.valueOf(this.mDefaultTextSize));
        // Log.e(NAME, "\n");
    }

    /**
     * @brief Setup the image for the button.
     */
    private void initImage(TypedArray ta, Resources r)
    {
        float width = ta.getDimension(R.styleable.ImageTextButton_imageWidth,
                                      this.mDefaultWidth);
        float height = ta.getDimension(R.styleable.ImageTextButton_imageHeight,
                                       this.mDefaultHeight);
        int color = ta.getColor(R.styleable.ImageTextButton_imageColor,
                                this.mDefaultColor);
        Drawable src = ta.getDrawable(R.styleable.ImageTextButton_image);
        float spacing = ta.getDimension(R.styleable.ImageTextButton_spacing,
                                        this.mDefaultSpacing);
        // Log.e(NAME, "Image:");
        // Log.e(NAME, "Width="+String.valueOf(width));
        // Log.e(NAME, "Height="+String.valueOf(height));
        // Log.e(NAME, "Color="+String.valueOf(color));
        // Log.e(NAME, "Spacing="+String.valueOf(spacing));
        // Log.e(NAME, "\n");
        setImage(src, width, height, color);
        setSpacing(spacing);
    }

    /**
     * @brief Setup the text for the button.
     */
    private void initText(TypedArray ta, Resources r)
    {
        int color = ta.getColor(R.styleable.ImageTextButton_textColor,
                                this.mDefaultColor);
        int size = ta.getDimensionPixelSize(R.styleable.ImageTextButton_textSize,
                                            (int)this.mDefaultTextSize);
        String text = ta.getString(R.styleable.ImageTextButton_text);
        // Log.e(NAME, "Text:");
        // Log.e(NAME, "Color="+String.valueOf(color));
        // Log.e(NAME, "TextSize="+String.valueOf(size));
        // Log.e(NAME, "Text="+String.valueOf(text));
        // Log.e(NAME, "\n");
        setText(text, color, size);
    }

    /**
     * @brief Call set functions on the image view.
     */
    private void setImage(Drawable src, float width, float height, int color)
    {
        ImageView iv = (ImageView) findViewById(R.id.itb_image);
        int w = (int) width;
        int h = (int) height;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
        if (src == null)
        {
            throw new RuntimeException("No image was provided.");
        }
        if (iv == null)
        {
            throw new RuntimeException("Unable to find image ID.");
        }
        iv.setImageDrawable(src);
        iv.setAdjustViewBounds(true);
        iv.setLayoutParams(params);
        iv.setColorFilter(color);
    }

    /**
     * @brief Call set functions on the text view.
     */
    private void setText(String text, int color, int size)
    {
        TextView tv = (TextView) findViewById(R.id.itb_text);
        if (text == null)
        {
            throw new RuntimeException("No text was provided.");
        }
        if (tv == null)
        {
            throw new RuntimeException("Unable to find text ID.");
        }
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    /**
     * @brief Call set functions on the text view.
     */
    public void setText(String text)
    {
        TextView tv = (TextView) findViewById(R.id.itb_text);
        if (text == null)
        {
            throw new RuntimeException("No text was provided.");
        }
        if (tv == null)
        {
            throw new RuntimeException("Unable to find text ID.");
        }
        tv.setText(text);
    }

    /**
     * @brief Set the spacing between the image and text view.
     */
    private void setSpacing(float spacing)
    {
        ImageView iv = (ImageView) findViewById(R.id.itb_image);
        float left = 0;
        float right = this.toDp(spacing);
        LinearLayout.LayoutParams params = 
            (LinearLayout.LayoutParams) iv.getLayoutParams();
        if (params == null)
        {
            return;
        }
        params.setMargins((int)left, 0, (int)right, 0);
        iv.setLayoutParams(params);
    }

    /**
     * @brief Determine the display density of the current context and return
     *        it.
     * 
     * @return The display density.
     */
    private float getDensity()
    {
        Context context = getContext();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    /**
     * @brief Convert the given pixels to dp.
     *
     * @param pixels The number of pixels.
     * 
     * @return Number of pixels in units of dp.
     */
    private float toDp(float pixels)
    {
        return pixels / this.getDensity();
    }

}
