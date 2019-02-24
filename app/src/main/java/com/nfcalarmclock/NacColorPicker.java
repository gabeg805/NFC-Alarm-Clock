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

/**
 * Created by yarolegovich on 4/1/18.
 */

public class NacColorPicker
	extends View
{

    private static final int DEFAULT_BRIGHTNESS = 224;

    private float mCenterX;
    private float mCenterY;
    private float mRadius;

    private Paint mHuePaint;
    private Paint mSaturationPaint;
    private Paint mBrightnessOverlayPaint;

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
	 */
	public void init(AttributeSet attrs)
    {
		if (attrs == null)
		{
			return;
		}

        this.mHuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mSaturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mBrightnessOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mBrightnessOverlayPaint.setColor(Color.BLACK);
        this.mBrightnessOverlayPaint.setAlpha(brightnessToAlpha(DEFAULT_BRIGHTNESS));
		this.mHSV = new float[3];
    }

	private int measureDimension(int desiredSize, int measureSpec)
	{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		int result = specSize;

		if (specMode != MeasureSpec.EXACTLY)
		{
			result = (specMode == MeasureSpec.AT_MOST) ?
				Math.min(result, specSize) : desiredSize;
		}

		if (result < desiredSize)
		{
			//Log.e("ChartView", "The view is too small, the content might get cut");
		}

		return result;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
		int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();
		int measuredWidth = measureDimension(desiredWidth, widthMeasureSpec);
		int measuredHeight = measureDimension(desiredHeight, heightMeasureSpec);
		int size = Math.min(measuredWidth, measuredHeight);

		if ((measuredWidth == 0) || (measuredHeight == 0))
		{
			size = Math.max(measuredWidth, measuredHeight);
		}

		NacUtility.printf("DW : %d || DH : %d || MW : %d || MH : %d || Size : %d",
			desiredWidth, desiredHeight, measuredWidth, measuredHeight, size);
		setMeasuredDimension(size, size);
	}

	/**
	 */
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		int min = Math.min(w, h);

        super.onSizeChanged(min, min, oldw, oldh);

        //this.mRadius = Math.min(w, h) * 0.48f;
        this.mRadius = min * 0.5f;
        this.mCenterX = w * 0.5f;
        this.mCenterY = h * 0.5f;
		NacUtility.printf("W : %d || H : %d || R : %f || OldW : %d || OldH : %d", w, h, this.mRadius, oldw, oldh);

        recomputeShader();
    }

	/**
	 */
    @Override
	protected void onDraw(Canvas canvas)
	{
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mHuePaint);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mSaturationPaint);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mBrightnessOverlayPaint);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);

		float x = event.getX() - this.mRadius;
		float y = -1 * (event.getY() - this.mRadius);
		float distance = (float) Math.sqrt(x*x + y*y);

		if (distance > this.mRadius)
		{
			NacUtility.printf("Outside of Radius!");
			return true;
		}

		float theta = (float) Math.atan2(y, x);
		float hue = (float) (((theta >= 0) ? theta : (2*Math.PI + theta)) * 180f / Math.PI);
		float sat = distance / this.mRadius;
		float val = 1.0f;
		float c = val * sat;
		float hp = hue / 60f;
		float comp = c * (1 - Math.abs((hp % 2) - 1));

		this.mHSV[0] = hue;
		this.mHSV[1] = sat;
		this.mHSV[2] = val;

		//NacUtility.printf("Calculated! X : %f | Y : %f | R : %f | Angle : %f",
		//	x, y, rad, theta);
		NacUtility.printf("More! H : %f | Sat : %f | C : %f | Hp : %f | Comp : %f", hue, sat, c, hp, comp);

		float r1 = 0;
		float g1 = 0;
		float b1 = 0;
		float m = val - c;

		if ((hp >= 0) && (hp <= 1))
		{
			r1 = c;
			g1 = comp;
		}
		else if ((hp > 1) && (hp <= 2))
		{
			r1 = comp;
			g1 = c;
		}
		else if ((hp > 2) && (hp <= 3))
		{
			g1 = c;
			b1 = comp;
		}
		else if ((hp > 3) && (hp <= 4))
		{
			g1 = comp;
			b1 = c;
		}
		else if ((hp > 4) && (hp <= 5))
		{
			r1 = comp;
			b1 = c;
		}
		else if ((hp > 5) && (hp <= 6))
		{
			r1 = c;
			b1 = comp;
		}

		float r = r1 + m;
		float g = g1 + m;
		float b = b1 + m;

		NacUtility.printf("LAST! R : %f | G: %f | B : %f", r, g, b);

		return true;
	}

	public float[] getHSV()
	{
		return this.mHSV;
	}

	/**
	 */
    private void recomputeShader()
	{
        Shader hueShader = new SweepGradient(this.mCenterX, this.mCenterY,
            new int[] {
                Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN,
                Color.GREEN, Color.YELLOW, Color.RED},
            new float[] {
                0.0000f, 0.1667f, 0.3333f, 0.5000f,
                0.6667f, 0.8333f, 1.0000f});

        this.mHuePaint.setShader(hueShader);

        Shader satShader = new RadialGradient(this.mCenterX, this.mCenterY, this.mRadius,
            Color.WHITE, 0x00FFFFFF,
            Shader.TileMode.CLAMP);

        this.mSaturationPaint.setShader(satShader);
    }

	/**
	 */
    public void setBrightness(int brightness)
	{
        this.mBrightnessOverlayPaint.setAlpha(brightnessToAlpha(brightness));
        invalidate();
    }

	/**
	 */
    private int brightnessToAlpha(int brightness)
	{
        return 255 - brightness;
    }

}
