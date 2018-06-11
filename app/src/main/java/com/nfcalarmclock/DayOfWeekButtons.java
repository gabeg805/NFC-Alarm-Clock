package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    private Context mContext;
    private Button[] mButtons;
    private int mButtonWidth;
    private int mButtonHeight;
    private int[] mButtonColor;
    private int mTextSize;
    private int[] mTextColor;
    private final int mLength = 7;

    /**
     * @brief The constructor.
     * 
     * @param context  The application context.
     * @param attrs    A collection of attributes associated with a tag in an XML
     *                 document.
     */
    public DayOfWeekButtons(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;
        int[] id = R.styleable.DayOfWeekButtons;
        int layout = R.layout.dayofweekbuttons;
        Theme theme = this.mContext.getTheme();
        TypedArray ta = theme.obtainStyledAttributes(attrs, id, 0, 0);
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(this.mContext).inflate(layout, this, true);
        init(ta);
    }

    /**
     * @brief Finish setting up the View.
     */
    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        setButtons();
    }

    /**
     * @brief Setup the contents of the button.
     * 
     * @param ta  Array of values that were retrieved from the context's theme.
     */
    private void init(TypedArray ta)
    {
        try
        {
            initButtons(ta);
        }
        finally
        {
            ta.recycle();
        }
    }

    /**
     * @brief Define all elements of the day of week buttons.
     * 
     * @param ta  Array of values that were retrieved from the context's theme.
     */
    private void initButtons(TypedArray ta)
    {
        Resources r = this.mContext.getResources();
        initButtonView();
        initButtonSize(ta, r);
        initButtonColor(ta);
        initTextSize(ta, r);
        initTextColor(ta);
    }

    /**
     * @brief Define the button views.
     */
    private void initButtonView()
    {
        this.mButtons = new Button[this.mLength];
        this.mButtons[0] = (Button) findViewById(R.id.dowb_sun);
        this.mButtons[1] = (Button) findViewById(R.id.dowb_mon);
        this.mButtons[2] = (Button) findViewById(R.id.dowb_tue);
        this.mButtons[3] = (Button) findViewById(R.id.dowb_wed);
        this.mButtons[4] = (Button) findViewById(R.id.dowb_thu);
        this.mButtons[5] = (Button) findViewById(R.id.dowb_fri);
        this.mButtons[6] = (Button) findViewById(R.id.dowb_sat);
        for (int i=0; i < this.mLength; i++)
        {
            if (this.mButtons[i] == null)
            {
                throw new RuntimeException("Unable to find button ID for #"+String.valueOf(i)+".");
            }
        }
    }

    /**
     * @brief Define the size of the buttons.
     * 
     * @param ta  Array of values that were retrieved from the context's theme.
     */
    private void initButtonSize(TypedArray ta, Resources r)
    {
        int wid = R.styleable.DayOfWeekButtons_nacWidth;
        int hid = R.styleable.DayOfWeekButtons_nacHeight;
        float width = r.getDimension(R.dimen.circle_size);
        float height = r.getDimension(R.dimen.circle_size);
        this.mButtonWidth = (int) ta.getDimension(wid, width);
        this.mButtonHeight = (int) ta.getDimension(hid, height);
    }

    /**
     * @brief Define the color of the buttons.
     * 
     * @param ta  Array of values that were retrieved from the context's theme.
     */
    private void initButtonColor(TypedArray ta)
    {
        this.mButtonColor = new int[this.mLength];
        int cid = R.styleable.DayOfWeekButtons_nacDrawableColor;
        int color = NacUtility.getThemeAttrColor(this.mContext,
                                                 R.attr.colorCardExpanded);
        int value = ta.getColor(cid, color);
        for (int i=0;i < this.mLength; i++)
        {
            this.mButtonColor[i] = value;
        }
    }

    /**
     * @brief Define the text size in the buttons.
     * 
     * @param ta  Array of values that were retrieved from the context's theme.
     */
    private void initTextSize(TypedArray ta, Resources r)
    {
        int tsid = R.styleable.DayOfWeekButtons_nacTextSize;
        float size = r.getDimension(R.dimen.tsz_card_days);
        this.mTextSize = (int) ta.getDimension(tsid, size);
    }

    /**
     * @brief Define the text color in the buttons.
     * 
     * @param ta  Array of values that were retrieved from the context's theme.
     */
    private void initTextColor(TypedArray ta)
    {
        this.mTextColor = new int[this.mLength];
        int cid = R.styleable.DayOfWeekButtons_nacTextColor;
        int color = NacUtility.getThemeAttrColor(this.mContext,
                                                 R.attr.colorCardText);
        int value = ta.getColor(cid, color);
        for (int i=0;i < this.mLength; i++)
        {
            this.mTextColor[i] = value;
        }
    }

    /**
     * @brief Setup the buttons that represent the different days of the week.
     */
    private void setButtons()
    {
        Button b;
        if (this.mButtons == null)
        {
            throw new RuntimeException("Unable to find button views.");
        }
        for (int i=0; i < this.mLength; i++)
        {
            b = this.mButtons[i];
            LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) b.getLayoutParams();
            params.width = this.mButtonWidth;
            params.height = this.mButtonHeight;
            if (i > 0)
            {
                params.setMargins(this.getButtonSpacing(), 0, 0, 0);
            }
            b.setLayoutParams(params);
            b.setBackgroundResource(R.drawable.circle);
            this.setButtonColor(i);
            b.setCompoundDrawablePadding(0);
            this.setTextColor(i);
            b.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.mTextSize);
            b.setPadding(0, 0, 0, 0);
            b.setPaddingRelative(0, 0, 0, 0);
            b.setOnClickListener(this.DayOfWeekButtonListener);
            b.setTag(i);
        }
    }

    /**
     * @brief Set the button color.
     * 
     * @param i  The index corresponding to the desired button to change.
     */
    private void setButtonColor(int i)
    {
        Button b = this.mButtons[i];
        int c = this.mButtonColor[i];
        Drawable d = b.getBackground();
        d.setColorFilter(c, PorterDuff.Mode.SRC);
    }

    /**
     * @brief Set the color of the text.
     * 
     * @param i  The index corresponding to the desired button to change.
     */
    private void setTextColor(int i)
    {
        Button b = this.mButtons[i];
        int c = this.mTextColor[i];
        b.setTextColor(c);
    }

    /**
     * @brief Set the button and text colors to their inverse values.
     * 
     * @param i  The index corresponding to the desired button to change.
     */
    private void setInverseColor(int i)
    {
        int tmp = this.mButtonColor[i];
        this.mButtonColor[i] = this.mTextColor[i];
        this.mTextColor[i] = tmp;
    }

    /**
     * @brief Inverse the color of the drawable and text.
     * 
     * @param b  The button to toggle.
     */
    private void toggleButton(Button b)
    {
        int i = (int) b.getTag();
        this.setInverseColor(i);
        this.setButtonColor(i);
        this.setTextColor(i);
    }

    /**
     * @brief Determine the spacing between buttons.
     * 
     * @return The spacing between the different buttons.
     */
    private int getButtonSpacing()
    {
        Resources r = this.mContext.getResources();
        DisplayMetrics metrics = r.getDisplayMetrics();
        float left = r.getDimension(R.dimen.ml_card);
        float right = r.getDimension(R.dimen.mr_card);
        double spacing = (metrics.widthPixels - 2.5*(left+right)
                         - 7*this.mButtonWidth) / 6.0;
        return (int) spacing;
    }

    /**
     * @brief Button click listener.
     */
    private View.OnClickListener DayOfWeekButtonListener =
        new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                toggleButton((Button)v);
            }
        };

}
