package com.nfcalarmclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
//import android.widget.RelativeLayout.LayoutParams;

import android.view.LayoutInflater;
import android.support.v4.view.ViewCompat;

/**
 * Color picker.
 */
public class NacColorPicker
	extends RelativeLayout
	//extends View
{

	/**
	 * Center x-coordinate.
	 */
    private float mCenterX;

	/**
	 * Center y-coordinate.
	 */
    private float mCenterY;

	/**
	 * Radius of the color picker.
	 */
    private float mRadius;

	/**
	 * Hue of the colors (solid color).
	 */
    private Paint mHuePaint;

	/**
	 * Color saturation (gradient from white to the actual color).
	 */
    private Paint mSaturationPaint;

	/**
	 * Hue, saturation, and value of the selected color.
	 */
	private float[] mHSV;

	/**
	 */
    public NacColorPicker(Context context)
	{
        super(context);
		init(null);
    }

	/**
	 */
    public NacColorPicker(Context context, @Nullable AttributeSet attrs)
	{
        super(context, attrs);
		init(attrs);
    }

	/**
	 */
    public NacColorPicker(Context context, @Nullable AttributeSet attrs,
		int defStyleAttr)
	{
        super(context, attrs, defStyleAttr);
		init(attrs);
    }

	/**
	 * Draw the color gradient.
	 */
    private void drawGradient()
	{
		int[] colors = new int[] {Color.RED, Color.MAGENTA, Color.BLUE,
			Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED};
		float[] positions = new float[] {0.0000f, 0.1667f, 0.3333f, 0.5000f,
                0.6667f, 0.8333f, 1.0000f};
		int centerColor = Color.WHITE;
		int edgeColor = 0x00FFFFFF;
        Shader hueShader = new SweepGradient(this.mCenterX, this.mCenterY, colors, positions);
        Shader satShader = new RadialGradient(this.mCenterX, this.mCenterY,
			this.mRadius, centerColor, edgeColor, Shader.TileMode.CLAMP);

        this.mHuePaint.setShader(hueShader);
        this.mSaturationPaint.setShader(satShader);
    }

	/**
	 * @return The hue, saturation, and value of the color that was selected.
	 */
	public float[] getHSV()
	{
		return this.mHSV;
	}

	/**
	 * @return The color that was selected.
	 */
	public int getColor()
	{
		return Color.HSVToColor(this.getHSV());
	}

	/**
	 * @return The hex string of the selected color.
	 */
	public String getHexColor()
	{
		int color = this.getColor();
		String hex = Integer.toHexString(color);
		int length = hex.length();

		if (length > 6)
		{
			hex = hex.substring(length - 6);
		}

		return (hex.charAt(0) == '#') ? hex : "#"+hex;
	}

	private ImageView mColorSelector;

	/**
	 * Initialize the view attributes.
	 *
	 * @param  attrs  Attribute set.
	 */
	public void init(AttributeSet attrs)
    {
		if (attrs == null)
		{
			return;
		}

		super.setWillNotDraw(false);

		Context context = this.getContext();
		LayoutInflater.from(context).inflate(R.layout.nac_color_picker, this, true);
		this.mColorSelector = (ImageView) findViewById(R.id.color_selector);
        this.mHuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mSaturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mHSV = new float[3];
    }

	/**
	 * Measure dimensions of the view.
	 *
	 * @param  desiredSize  The desired size of the view.
	 * @param  measureSpec  The measure specification to extract the size from.
	 *
	 * @return The size of the view.
	 */
	private int measureDimension(int desiredSize, int measureSpec)
	{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY)
		{
			return specSize;
		}
		else if (specMode == MeasureSpec.AT_MOST)
		{
			return Math.min(desiredSize, specSize);
		}
		else
		{
			return desiredSize;
		}
	}

	/**
	 * Measure the height of the view.
	 *
	 * @see measureDimension
	 */
	private int measureHeight(int measureSpec)
	{
		int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop()
			+ getPaddingBottom();

		return this.measureDimension(desiredHeight, measureSpec);
	}

	/**
	 * Measure the width of the view.
	 *
	 * @see measureDimension
	 */
	private int measureWidth(int measureSpec)
	{
		int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft()
			+ getPaddingRight();

		return this.measureDimension(desiredWidth, measureSpec);
	}

	/**
	 */
    @Override
	protected void onDraw(Canvas canvas)
	{
		NacUtility.printf("onDraw! Rad : %f", mRadius);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mHuePaint);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mSaturationPaint);
		this.setSelectorPositionToColor();
    }

	/**
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int measuredHeight = this.measureHeight(heightMeasureSpec);
		int measuredWidth = this.measureWidth(widthMeasureSpec);
		//int size = Math.max(measuredWidth, measuredHeight);
		int size = Math.min(measuredWidth, measuredHeight);

		if ((measuredWidth == 0) || (measuredHeight == 0))
		{
			size = Math.max(measuredWidth, measuredHeight);
		}

		this.setMeasuredDimension(size, size);
	}

	/**
	 */
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		int min = Math.min(w, h);

        super.onSizeChanged(min, min, oldw, oldh);

        this.mRadius = min * 0.5f;
        this.mCenterX = w * 0.5f;
        this.mCenterY = h * 0.5f;

        drawGradient();
    }

	/**
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);

		float x = event.getX() - this.mRadius;
		float y = -1 * (event.getY() - this.mRadius);
		float distance = (float) Math.sqrt(x*x + y*y);

		if (distance > this.mRadius)
		{
			return true;
		}

		float theta = (float) Math.atan2(y, x);
		float hue = (float) (((theta >= 0) ? theta : (2*Math.PI + theta)) * 180f / Math.PI);
		float sat = distance / this.mRadius;
		float val = 1.0f;

		this.setHsv(hue, sat, val);
		this.setSelectorPosition(event.getX(), event.getY());

		return true;
	}

	/**
	 * Set the color of the color picker.
	 */
	public void setColor(int color)
	{
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);

		Color.RGBToHSV(red, green, blue, this.mHSV);
		this.setSelectorPositionToColor();
	}

	/**
	 * Set the hue, saturation, and value.
	 */
	public void setHsv(float hue, float sat, float val)
	{
		this.mHSV[0] = hue;
		this.mHSV[1] = sat;
		this.mHSV[2] = val;

		this.setSelectorPositionToColor();
	}

	/**
	 * Set selector position.
	 */
	public void setSelectorPosition(float x, float y)
	{
		ViewGroup.LayoutParams params = this.mColorSelector.getLayoutParams();

		this.mColorSelector.setX(x-(params.width/2.0f));
		this.mColorSelector.setY(y-(params.height/2.0f));
	}

	/**
	 * Set the selector position to the current color.
	 */
	public void setSelectorPositionToColor()
	{
		float x = this.mRadius + (float)(Math.cos(Math.toRadians(this.mHSV[0])) * this.mHSV[1] * this.mRadius);
		float y = this.mRadius + (float)(-1 * Math.sin(Math.toRadians(this.mHSV[0])) * this.mHSV[1] * this.mRadius);

		this.setSelectorPosition(x, y);
	}

}
