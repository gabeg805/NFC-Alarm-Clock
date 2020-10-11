package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;

/**
 * Color picker.
 */
public class NacColorPicker
	extends RelativeLayout
{

	/**
	 * Attributes for the color picker.
	 */
	public static class Attributes
	{
		private int mHeight;
		private int mWidth;
		private int mSpacing;

		/**
		 */
		public Attributes(Context context, AttributeSet attrs)
		{
			TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.NacColorPicker, 0, R.style.NacColorPicker);

			try
			{
				Resources res = context.getResources();
				this.mHeight = (int) ta.getDimension(R.styleable.NacColorPicker_nacHeight, -1);
				this.mWidth = (int) ta.getDimension(R.styleable.NacColorPicker_nacWidth, -1);
				this.mSpacing = (int) ta.getDimension(R.styleable.NacColorPicker_nacSpacing, -1);
			}
			finally
			{
				ta.recycle();
			}
		}

		/**
		 * @return The height.
		 */
		public int getHeight()
		{
			return this.mHeight;
		}

		/**
		 * @return The width.
		 */
		public int getWidth()
		{
			return this.mWidth;
		}

		/**
		 * @return The spacing.
		 */
		public int getSpacing()
		{
			return this.mSpacing;
		}

	}

	/**
	 * Attributes assigned by the user.
	 */
	private Attributes mAttributes;

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
	 * Color selector.
	 */
	private ImageView mColorSelector;

	/**
	 * Shader selector.
	 */
	private ImageView mShaderSelector;

	/**
	 * Hue of the colors (solid color).
	 */
    private Paint mHuePaint;

	/**
	 * Color saturation (gradient from white to the actual color).
	 */
    private Paint mSaturationPaint;

	/**
	 * Value of the color.
	 */
    private Paint mValuePaint;

	/**
	 * Rectangle showing shades of the selected color.
	 */
	private RectF mValueRect;

	/**
	 * Hue, saturation, and value of the selected color.
	 */
	private float[] mHsv;

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
	 * Calculate the color wheel selection.
	 */
	private void calculateColorSelection(float eventX, float eventY)
	{
		float radius = this.getRadius();
		float x = this.getColorSelectionX(eventX);
		float y = this.getColorSelectionY(eventY);
		float distance = (float) Math.sqrt(x*x + y*y);

		if (distance > radius)
		{
			return;
		}

		float[] hsv = this.getHsv();
		float theta = (float) Math.atan2(y, x);
		float hue = (float) (((theta >= 0) ? theta : (2*Math.PI + theta)) * 180f / Math.PI);
		float sat = distance / radius;
		float val = hsv[2];

		this.setHsv(hue, sat, val);
		this.setColorSelectorPosition(eventX, eventY);
		this.drawColorShader();
		invalidate();
	}

	/**
	 * Calculate the value shader selection.
	 */
	private void calculateShaderSelection(float eventX, float eventY)
	{
		if (!this.mValueRect.contains(eventX, eventY))
		{
			return;
		}

		float[] hsv = this.getHsv();
		float hue = hsv[0];
		float sat = hsv[1];
		float val = this.getShaderSelectionValue(eventX);

		this.setHsv(hue, sat, val);
		this.setShaderSelectorPosition(eventX);
	}

	/**
	 * Draw the color wheel gradient.
	 */
    private void drawColorWheel()
	{
		int[] colors = new int[] {Color.RED, Color.MAGENTA, Color.BLUE,
			Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED};
		int centerColor = Color.WHITE;
		int edgeColor = 0x00FFFFFF;
		float[] positions = new float[] {0.0000f, 0.1667f, 0.3333f, 0.5000f,
                0.6667f, 0.8333f, 1.0000f};
		float centerX = this.getCenterX();
		float centerY = this.getCenterY();
		float radius = this.getRadius();

        Shader hueShader = new SweepGradient(centerX, centerY, colors,
			positions);
        Shader satShader = new RadialGradient(centerX, centerY, radius,
			centerColor, edgeColor, Shader.TileMode.CLAMP);

        this.mHuePaint.setShader(hueShader);
        this.mSaturationPaint.setShader(satShader);
    }

	/**
	 * Draw the color shading gradient.
	 */
	private void drawColorShader()
	{
		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();
		float shaderHeight = this.getShaderHeight(measuredHeight);
		float left = getPaddingLeft();
		float right = measuredWidth - getPaddingRight();
		float top = measuredHeight - getPaddingTop() - shaderHeight;
		float bottom = measuredHeight - getPaddingBottom();
		float radius = this.getRadius();
		float[] hsv = this.getHsv();

		int colorStart = Color.HSVToColor(new float[] {hsv[0], hsv[1], 0.0f});
		int colorEnd = Color.HSVToColor(new float[] {hsv[0], hsv[1], 1.0f});
		Shader valueShader = new LinearGradient(0, 0, 2*radius, 0, colorStart,
			colorEnd, Shader.TileMode.CLAMP);

		if (this.mValueRect == null)
		{
			this.mValueRect = new RectF();
		}

		this.mValueRect.set(left, top, right, bottom);
		this.mValuePaint.setShader(valueShader);
	}

	/**
	 * @return The attributes.
	 */
	private Attributes getAttributes()
	{
		return this.mAttributes;
	}

	/**
	 * @return The center X-coordinate.
	 */
	public float getCenterX()
	{
		return this.mCenterX;
	}

	/**
	 * @return The center Y-coordinate.
	 */
	public float getCenterY()
	{
		return this.mCenterY;
	}

	/**
	 * @return The color that was selected.
	 */
	public int getColor()
	{
		return Color.HSVToColor(this.getHsv());
	}

	/**
	 * @return The x-coordinate of the selected color, given where the user
	 *         touched.
	 */
	private float getColorSelectionX(float eventX)
	{
		return eventX - this.getRadius()- getPaddingLeft();
	}

	/**
	 * @return The x-coordinate of the selected color given the color.
	 */
	private float getColorSelectionX(float[] hsv)
	{
		float xComp = (float) Math.cos(Math.toRadians(hsv[0]));
		float radius = this.getRadius();
		int paddingLeft = getPaddingLeft();

		return paddingLeft + radius + (xComp * hsv[1] * radius);
	}

	/**
	 * @return The y-coordinate of the selected color, given where the user
	 *         touched.
	 */
	private float getColorSelectionY(float eventY)
	{
		return -1 * (eventY - this.getRadius() - getPaddingTop());
	}

	/**
	 * @return The y-coordinate of the selected color given the color.
	 */
	private float getColorSelectionY(float[] hsv)
	{
		float yComp = (float) Math.sin(Math.toRadians(hsv[0]));
		float radius = this.getRadius();
		int paddingTop = getPaddingTop();

		return paddingTop + radius + (-1 * yComp * hsv[1] * radius);
	}

	/**
	 * @return The color selector.
	 */
	private ImageView getColorSelector()
	{
		return this.mColorSelector;
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

	/**
	 * @return The hue paint.
	 */
	private Paint getHuePaint()
	{
		return this.mHuePaint;
	}

	/**
	 * @return The hue, saturation, and value of the color that was selected.
	 */
	public float[] getHsv()
	{
		return this.mHsv;
	}

	/**
	 * @return The radius of the color picker wheel.
	 */
	public float getRadius()
	{
		return this.mRadius;
	}

	/**
	 * @return The saturation paint.
	 */
	private Paint getSaturationPaint()
	{
		return this.mSaturationPaint;
	}

	/**
	 * @return The height of the shader.
	 */
	public float getShaderHeight(int height)
	{
		return height / 13.0f;
	}

	/**
	 * @return The height of the shader.
	 */
	public float getShaderHeight()
	{
		return this.getShaderHeight(getMeasuredHeight());
	}

	/**
	 * @return The shader selection value (w.r.t. HSV), given where the user
	 *         touched.
	 */
	private float getShaderSelectionValue(float eventX)
	{
		int measuredWidth = getMeasuredWidth();
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		float width = measuredWidth - paddingLeft - paddingRight;

		return (eventX - paddingLeft) / width;
	}

	/**
	 * @return The x-coordinate of the shader selector, given the color.
	 */
	private float getShaderSelectionX(float[] hsv)
	{
		int measuredWidth = getMeasuredWidth();
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		float width = measuredWidth - paddingLeft - paddingRight;

		return paddingLeft + (hsv[2] * width);
	}

	/**
	 * @return The shader selector.
	 */
	private ImageView getShaderSelector()
	{
		return this.mShaderSelector;
	}

	/**
	 * @return The value paint.
	 */
	private Paint getValuePaint()
	{
		return this.mValuePaint;
	}

	/**
	 * @return The value rectangle.
	 */
	private RectF getValueRect()
	{
		return this.mValueRect;
	}

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

		setWillNotDraw(false);

		Context context = getContext();
		LayoutInflater.from(context).inflate(R.layout.nac_color_picker, this, true);
		this.mAttributes = new Attributes(context, attrs);
		this.mColorSelector = (ImageView) findViewById(R.id.color_selector);
		this.mShaderSelector = (ImageView) findViewById(R.id.shader_selector);
        this.mHuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mSaturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mHsv = new float[] {0.0f, 0.0f, 1.0f};
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
		Context context = getContext();
		Resources res = context.getResources();
		float curve = res.getDimension(R.dimen.radius);
		float centerX = this.getCenterX();
		float centerY = this.getCenterY();
		float radius = this.getRadius();
		Paint huePaint = this.getHuePaint();
		Paint satPaint = this.getSaturationPaint();
		Paint valPaint = this.getValuePaint();
		RectF valRect = this.getValueRect();

        canvas.drawCircle(centerX, centerY, radius, huePaint);
        canvas.drawCircle(centerX, centerY, radius, satPaint);
		canvas.drawRoundRect(valRect, curve, curve, valPaint);
		this.setColorSelectorPosition();
		this.setShaderSelectorPosition();
    }

	/**
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int measuredHeight = this.measureHeight(heightMeasureSpec);
		int measuredWidth = this.measureWidth(widthMeasureSpec);
		int size = Math.min(measuredWidth, measuredHeight);

		if ((measuredWidth == 0) || (measuredHeight == 0))
		{
			size = Math.max(measuredWidth, measuredHeight);
		}

		Attributes attr = this.getAttributes();
		int shaderHeight = (int) this.getShaderHeight(size);
		int width = (attr.getWidth() != -1) ? attr.getWidth() : size;
		int height = (attr.getHeight() != -1) ? attr.getHeight() : size+shaderHeight;
		int spacing = attr.getSpacing();

		if (spacing != -1)
		{
			height += spacing;
		}

		this.setMeasuredDimension(width, height);
	}

	/**
	 */
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		int min = Math.min(w, h);

        super.onSizeChanged(min, min, oldw, oldh);

		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingTop = getPaddingTop();
		int paddingBottom = getPaddingBottom();
		int size = min - ((min == w) ? paddingLeft+paddingRight :
			paddingTop+paddingBottom);

        this.mRadius = size * 0.5f;
        this.mCenterX = paddingLeft + this.getRadius();
        this.mCenterY = paddingTop + this.getRadius();

		ViewGroup.LayoutParams params = this.getShaderSelector().getLayoutParams();
		params.height = (int) Math.round(this.getShaderHeight());

		this.getShaderSelector().setLayoutParams(params);
        drawColorWheel();
        drawColorShader();
    }

	/**
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);

		float eventX = event.getX();
		float eventY = event.getY();

		this.calculateColorSelection(eventX, eventY);
		this.calculateShaderSelection(eventX, eventY);

		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			performClick();
		}

		return true;
	}

	/**
	 */
	@Override
	public boolean performClick()
	{
		super.performClick();

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

		Color.RGBToHSV(red, green, blue, this.getHsv());
		this.setColorSelectorPosition();
	}

	/**
	 * Set the hue, saturation, and value.
	 */
	public void setHsv(float hue, float sat, float val)
	{
		this.mHsv[0] = hue;
		this.mHsv[1] = sat;
		this.mHsv[2] = val;

		this.setColorSelectorPosition();
	}

	/**
	 * Set the color selector position.
	 */
	public void setColorSelectorPosition(float x, float y)
	{
		ViewGroup.LayoutParams params = this.mColorSelector.getLayoutParams();

		this.mColorSelector.setX(x-(params.width/2.0f));
		this.mColorSelector.setY(y-(params.height/2.0f));
	}

	/**
	 * Set the color selector position to the current color.
	 */
	public void setColorSelectorPosition()
	{
		float[] hsv = this.getHsv();
		float x = this.getColorSelectionX(hsv);
		float y = this.getColorSelectionY(hsv);

		this.setColorSelectorPosition(x, y);
	}

	/**
	 * Set shader selector position.
	 */
	public void setShaderSelectorPosition(float x)
	{
		ViewGroup.LayoutParams params = this.mShaderSelector.getLayoutParams();
		float centerY = this.getValueRect().centerY();

		this.mShaderSelector.setX(x-(params.width/2.0f));
		this.mShaderSelector.setY(centerY-(params.height/2.0f));
	}

	public void setShaderSelectorPosition()
	{
		float[] hsv = this.getHsv();
		float x = this.getShaderSelectionX(hsv);

		this.setShaderSelectorPosition(x);
	}

}
