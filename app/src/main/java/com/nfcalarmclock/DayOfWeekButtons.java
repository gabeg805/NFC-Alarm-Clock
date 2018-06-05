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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import java.util.List;

/**
 * @class A button that consists of an image to the left, and text to the right
 *        of it.
 */
public class DayOfWeekButtons
    extends LinearLayout
{

    private static final String NAME = "NFCAlarmClock";

    private float mWidth;
    private float mHeight;
    private int mDrawableColor;
    private int mTextColor;
    private float mTextSize;

    public DayOfWeekButtons(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.dayofweekbuttons, this,
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
                                                     R.styleable.DayOfWeekButtons,
                                                     0, 0);
        Resources r = context.getResources();
        try
        {
            initDefaults(r);
            initButtons(ta, r);
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
        this.mWidth = r.getDimension(R.dimen.circle_size);
        this.mHeight = r.getDimension(R.dimen.circle_size);
        this.mDrawableColor = r.getColor(R.color.black);
        this.mTextColor = r.getColor(R.color.white);
        this.mTextSize = r.getDimension(R.dimen.tsz_card_days);
        // android:background="@drawable/circle"
        // android:textColor="#fff"
        // Log.e(NAME, "Defaults:");
        // Log.e(NAME, "Width="+String.valueOf(this.mWidth));
        // Log.e(NAME, "Height="+String.valueOf(this.mHeight));
        // Log.e(NAME, "Color="+String.valueOf(this.mColor));
        // Log.e(NAME, "Spacing="+String.valueOf(this.mSpacing));
        // Log.e(NAME, "TextSize="+String.valueOf(this.mTextSize));
        // Log.e(NAME, "\n");
    }

    /**
     * @brief Setup the image for the button.
     */
    private void initButtons(TypedArray ta, Resources r)
    {
        float width = ta.getDimension(R.styleable.DayOfWeekButtons_widthz,
                                      this.mWidth);
        float height = ta.getDimension(R.styleable.DayOfWeekButtons_heightz,
                                       this.mHeight);
        int drawablecolor = ta.getColor(R.styleable.DayOfWeekButtons_drawableColorz,
                                        this.mDrawableColor);
        int textcolor = ta.getColor(R.styleable.DayOfWeekButtons_textColorz,
                                    this.mTextColor);
        float textsize = ta.getDimension(R.styleable.DayOfWeekButtons_textSizez,
                                         this.mTextSize);
        Log.e(NAME, "Image:");
        Log.e(NAME, "Width="+String.valueOf(width));
        Log.e(NAME, "Height="+String.valueOf(height));
        Log.e(NAME, "DrawableColor="+String.valueOf(drawablecolor));
        Log.e(NAME, "TextColor="+String.valueOf(textcolor));
        Log.e(NAME, "TextSize="+String.valueOf(textsize));
        Log.e(NAME, "\n");
        setButtons(width, height, drawablecolor, textcolor, textsize);
    }

    /**
     * @brief Call set functions on the image view.
     */
        // Button sun = (Button) findViewById(R.id.dowb_sun);
        // Button mon = (Button) findViewById(R.id.dowb_mon);
        // Button tue = (Button) findViewById(R.id.dowb_tue);
        // Button wed = (Button) findViewById(R.id.dowb_wed);
        // Button thu = (Button) findViewById(R.id.dowb_thu);
        // Button fri = (Button) findViewById(R.id.dowb_fri);
        // Button sat = (Button) findViewById(R.id.dowb_sat);
        // Button[] week = {sun, mon, tue, wed, thu, fri, sat};
    private void setButtons(float width, float height, int drawablecolor,
                            int textcolor, float textsize)
    {
        int w = (int) width;
        int h = (int) height;
        Button[] week = {(Button) findViewById(R.id.dowb_sun),
                         (Button) findViewById(R.id.dowb_mon),
                         (Button) findViewById(R.id.dowb_tue),
                         (Button) findViewById(R.id.dowb_wed),
                         (Button) findViewById(R.id.dowb_thu),
                         (Button) findViewById(R.id.dowb_fri),
                         (Button) findViewById(R.id.dowb_sat)};

        for (int i=0; i < week.length; i++)
        {
            week[i].setBackgroundResource(R.drawable.circle);
            // week[i].setAdjustViewBounds(true);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)week[i].getLayoutParams();
            params.width = w;
            params.height = h;
            week[i].setLayoutParams(params);
            // week[i].setColorFilter(color);
            week[i].setCompoundDrawablePadding(0);
            week[i].setTextColor(textcolor);
            week[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
            // week[i].setHeight(h);
            week[i].setPadding(0, 0, 0, 0);
            week[i].setPaddingRelative(0, 0, 0, 0);
            week[i].requestLayout();
        }

    }

    /**
     * @brief Return the display width.
     * 
     * @return The display width.
     */
    private float getDisplayWidth()
    {
        Context context = getContext();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    /**
     * @brief Return the display height.
     * 
     * @return The display height.
     */
    private float getDisplayHeight()
    {
        Context context = getContext();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
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
        // ((Activity) getContext()).getWindowManager()
        //     .getDefaultDisplay()
        //     .getMetrics(displayMetrics);
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
