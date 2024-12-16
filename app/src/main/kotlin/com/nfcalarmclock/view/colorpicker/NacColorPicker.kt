package com.nfcalarmclock.view.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import com.nfcalarmclock.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Color picker.
 */
class NacColorPicker : RelativeLayout
{

	/**
	 * Attributes for the color picker.
	 */
	class Attributes(context: Context, attrs: AttributeSet?)
	{

		/**
		 * Width.
		 */
		var width = 0

		/**
		 * Height.
		 */
		var height = 0

		/**
		 * Spacing.
		 */
		var spacing = 0

		/**
		 * Constructor.
		 */
		init
		{
			// Create the typed array for the view
			val ta = context.theme.obtainStyledAttributes(attrs,
				R.styleable.NacColorPicker, 0, R.style.NacColorPicker)

			// Get attributes from the typed array
			try
			{
				width = ta.getDimension(R.styleable.NacColorPicker_nacWidth, -1f).toInt()
				height = ta.getDimension(R.styleable.NacColorPicker_nacHeight, -1f).toInt()
				spacing = ta.getDimension(R.styleable.NacColorPicker_nacSpacing, -1f).toInt()
			}
			// Recycle the typed array
			finally
			{
				ta.recycle()
			}
		}

	}

	/**
	 * Attributes assigned by the user.
	 */
	private var attributes: Attributes? = null

	/**
	 * Center x-coordinate.
	 */
	private var centerX = 0f

	/**
	 * Center y-coordinate.
	 */
	private var centerY = 0f

	/**
	 * Radius of the color picker.
	 */
	private var radius = 0f

	/**
	 * Color selector.
	 */
	private var colorSelector: ImageView? = null

	/**
	 * Shader selector.
	 */
	private var shaderSelector: ImageView? = null

	/**
	 * Hue of the colors (solid color).
	 */
	private var huePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

	/**
	 * Color saturation (gradient from white to the actual color).
	 */
	private var saturationPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

	/**
	 * Value of the color.
	 */
	private var valuePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

	/**
	 * Rectangle showing shades of the selected color.
	 */
	private var valueRect: RectF = RectF()

	/**
	 * Hue, saturation, and value of the selected color.
	 */
	private var hsv: FloatArray = floatArrayOf(0.0f, 0.0f, 1.0f)

	/**
	 * The color that was selected.
	 */
	var color: Int
		get() = Color.HSVToColor(hsv)
		set(color)
		{
			// Get RGB values
			val red = Color.red(color)
			val green = Color.green(color)
			val blue = Color.blue(color)

			// Update the HSV array with the RGB color
			Color.RGBToHSV(red, green, blue, hsv)
		}

	/**
	 * The height of the shader.
	 */
	private val shaderHeight: Float
		get() = getShaderHeight(measuredHeight)

	/**
	 * Constructor.
	 */
	constructor(context: Context?) : super(context)
	{
		init(null)
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
	{
		init(attrs)
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context?, attrs: AttributeSet?,
		defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		init(attrs)
	}

	/**
	 * Calculate the color wheel selection.
	 */
	private fun calculateColorSelection(eventX: Float, eventY: Float)
	{
		val x = getColorSelectionX(eventX)
		val y = getColorSelectionY(eventY)
		val distance = sqrt((x * x + y * y).toDouble()).toFloat()

		// Distance is outside of color radius so do nothing
		if (distance > radius)
		{
			return
		}

		// Determine the angle
		val theta = atan2(y.toDouble(), x.toDouble()).toFloat()
		val angle = if (theta >= 0)
		{
			theta
		}
		else
		{
			(2 * Math.PI + theta).toFloat()
		}

		// Calculate the hue and saturation. The value in HSV does not change
		hsv[0] = (angle * 180f / Math.PI).toFloat()
		hsv[1] = distance / radius

		// Set the new color selector position
		setColorSelectorPosition(eventX, eventY)

		// Redraw everything
		drawColorRect()
		drawColorShader()
		invalidate()
	}

	/**
	 * Calculate the value shader selection.
	 */
	private fun calculateShaderSelection(eventX: Float, eventY: Float)
	{
		// Touch event was not in the color picker rectangle
		if (!valueRect.contains(eventX, eventY))
		{
			return
		}

		// Calculate the value in HSV. Hue and saturation do not change
		hsv[2] = getShaderSelectionValue(eventX)

		// Set the new shader position
		setShaderSelectorPosition(eventX)
	}

	/**
	 * Draw the color wheel gradient.
	 */
	private fun drawColorWheel()
	{
		// Define the colors
		val colors = intArrayOf(Color.RED, Color.MAGENTA, Color.BLUE,
			Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED)
		val centerColor = Color.WHITE
		val edgeColor = 0x00FFFFFF

		// Define the coordinates
		val positions = floatArrayOf(0.0000f, 0.1667f, 0.3333f, 0.5000f,
			0.6667f, 0.8333f, 1.0000f)
		val centerX = centerX
		val centerY = centerY

		// Create the shaders
		val hueShader: Shader = SweepGradient(centerX, centerY, colors,
			positions)
		val satShader: Shader = RadialGradient(centerX, centerY, radius,
			centerColor, edgeColor, Shader.TileMode.CLAMP)

		// Set the shaders
		huePaint.shader = hueShader
		saturationPaint.shader = satShader
	}

	/**
	 * Draw the color shading rectangle.
	 */
	private fun drawColorRect()
	{
		// Define the bounds of the rectangle
		val left = paddingLeft.toFloat()
		val right = (measuredWidth - paddingRight).toFloat()
		val top = measuredHeight - paddingTop - shaderHeight
		val bottom = (measuredHeight - paddingBottom).toFloat()

		// Set the rectangle
		valueRect.set(left, top, right, bottom)
	}

	/**
	 * Draw the color shading gradient.
	 */
	private fun drawColorShader()
	{
		// Define the colors and shader
		val colorStart = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 0.0f))
		val colorEnd = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 1.0f))
		val valueShader: Shader = LinearGradient(0f, 0f, 2 * radius, 0f, colorStart,
			colorEnd, Shader.TileMode.CLAMP)

		// Set the shader
		valuePaint.shader = valueShader
	}

	/**
	 * Get the x-coordinate of the selected color, given where the user
	 * touched.
	 *
	 * @return The x-coordinate of the selected color, given where the user
	 *         touched.
	 */
	private fun getColorSelectionX(eventX: Float): Float
	{
		return eventX - radius - paddingLeft
	}

	/**
	 * Get the x-coordinate of the selected color given the color.
	 *
	 * @return The x-coordinate of the selected color given the color.
	 */
	private fun getColorSelectionX(hsv: FloatArray): Float
	{
		val theta = Math.toRadians(hsv[0].toDouble())
		val xComp = cos(theta).toFloat()

		return paddingLeft + radius + xComp * hsv[1] * radius
	}

	/**
	 * Get the y-coordinate of the selected color, given where the user
	 * touched.
	 *
	 * @return The y-coordinate of the selected color, given where the user
	 *         touched.
	 */
	private fun getColorSelectionY(eventY: Float): Float
	{
		return -1 * (eventY - radius - paddingTop)
	}

	/**
	 * @return The y-coordinate of the selected color given the color.
	 */
	private fun getColorSelectionY(hsv: FloatArray): Float
	{
		val theta = Math.toRadians(hsv[0].toDouble())
		val yComp = sin(theta).toFloat()

		return paddingTop + radius + -1 * yComp * hsv[1] * radius
	}

	/**
	 * Get the height of the shader.
	 *
	 * @return The height of the shader.
	 */
	private fun getShaderHeight(height: Int): Float
	{
		return height / 13.0f
	}

	/**
	 * Get the shader selection value (w.r.t. HSV), given where the user
	 * touched.
	 *
	 * @return The shader selection value (w.r.t. HSV), given where the user
	 *         touched.
	 */
	private fun getShaderSelectionValue(eventX: Float): Float
	{
		val width = (measuredWidth - paddingLeft - paddingRight).toFloat()

		return (eventX - paddingLeft) / width
	}

	/**
	 * Get the x-coordinate of the shader selector, given the color.
	 *
	 * @return The x-coordinate of the shader selector, given the color.
	 */
	private fun getShaderSelectionX(hsv: FloatArray): Float
	{
		val width = (measuredWidth - paddingLeft - paddingRight).toFloat()

		return paddingLeft + hsv[2] * width
	}

	/**
	 * Initialize the view attributes.
	 *
	 * @param  attrs  Attribute set.
	 */
	fun init(attrs: AttributeSet?)
	{
		// Do nothing if the attribute set is null
		if (attrs == null)
		{
			return
		}

		// Get the layout inflater
		val inflater = LayoutInflater.from(context)

		// Set will not draw?
		setWillNotDraw(false)

		// Inflate the view
		inflater.inflate(R.layout.nac_color_picker, this, true)

		// Define the member variables
		attributes = Attributes(context, attrs)
		colorSelector = findViewById(R.id.color_selector)
		shaderSelector = findViewById(R.id.shader_selector)
	}

	/**
	 * Measure dimensions of the view.
	 *
	 * @param  desiredSize  The desired size of the view.
	 * @param  measureSpec  The measure specification to extract the size from.
	 *
	 * @return The size of the view.
	 */
	private fun measureDimension(desiredSize: Int, measureSpec: Int): Int
	{
		val specMode = MeasureSpec.getMode(measureSpec)
		val specSize = MeasureSpec.getSize(measureSpec)

		return when (specMode)
			{
				MeasureSpec.EXACTLY -> specSize
				MeasureSpec.AT_MOST -> desiredSize.coerceAtMost(specSize)
				else -> desiredSize
			}
	}

	/**
	 * Measure the height of the view.
	 *
	 * @see .measureDimension
	 */
	private fun measureHeight(measureSpec: Int): Int
	{
		val desiredHeight = (suggestedMinimumHeight + paddingTop + paddingBottom)

		return measureDimension(desiredHeight, measureSpec)
	}

	/**
	 * Measure the width of the view.
	 *
	 * @see .measureDimension
	 */
	private fun measureWidth(measureSpec: Int): Int
	{
		val desiredWidth = (suggestedMinimumWidth + paddingLeft + paddingRight)

		return measureDimension(desiredWidth, measureSpec)
	}

	/**
	 * Called when the view is drawn.
	 */
	override fun onDraw(canvas: Canvas)
	{
		// Get the curve of the radius
		val curve = context.resources.getDimension(R.dimen.radius)

		// Draw the circle
		canvas.drawCircle(centerX, centerY, radius, huePaint)
		canvas.drawCircle(centerX, centerY, radius, saturationPaint)
		canvas.drawRoundRect(valueRect, curve, curve, valuePaint)

		// Set the color and shader selector positions
		setColorSelectorPosition()
		setShaderSelectorPosition()
	}

	/**
	 * Called when the view is measured.
	 */
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		// Super
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		// Measure the width and height
		val measuredHeight = measureHeight(heightMeasureSpec)
		val measuredWidth = measureWidth(widthMeasureSpec)
		var size = measuredWidth.coerceAtMost(measuredHeight)

		// Either the width or height was not measured accurately. Redefine the
		// size
		if (measuredWidth == 0 || measuredHeight == 0)
		{
			size = measuredWidth.coerceAtLeast(measuredHeight)
		}

		// Determine the new width from the attributes
		val width = if (attributes!!.width != -1)
		{
			attributes!!.width
		}
		else
		{
			size
		}

		// Determine the new height from the attributes
		var height = if (attributes!!.height != -1)
		{
			attributes!!.height
		}
		else
		{
			size + getShaderHeight(size).toInt()
		}

		// Add spacing to the height if it is nonzero
		if (attributes!!.spacing != -1)
		{
			height += attributes!!.spacing
		}

		// Set the new width and height
		setMeasuredDimension(width, height)
	}

	/**
	 * Called when the size of the view has changed.
	 */
	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
	{
		// Calculate a new min
		val min = w.coerceAtMost(h)

		// Super
		super.onSizeChanged(min, min, oldw, oldh)

		// Calculate the new size
		val offset = if (min == w)
		{
			paddingLeft + paddingRight
		}
		else
		{
			paddingTop + paddingBottom
		}

		// Calculate the new size
		val size = min - offset

		// Redefine member variables
		radius = size * 0.5f
		centerX = paddingLeft + radius
		centerY = paddingTop + radius

		// Get the layout parameters of the shader selector
		val params = shaderSelector!!.layoutParams

		// Set the new height
		params.height = shaderHeight.roundToInt()

		// Set the layout parameters
		shaderSelector!!.layoutParams = params

		// Redraw everything
		drawColorWheel()
		drawColorRect()
		drawColorShader()
	}

	/**
	 * Called when there is a touch event on the view
	 */
	override fun onTouchEvent(event: MotionEvent): Boolean
	{
		// Super
		super.onTouchEvent(event)

		// Calculate the new color and shader selector positions
		calculateColorSelection(event.x, event.y)
		calculateShaderSelection(event.x, event.y)

		// Perform a click
		if (event.action == MotionEvent.ACTION_UP)
		{
			performClick()
		}

		return true
	}

	/**
	 * Perform click.
	 *
	 * Need to override this because onTouchEvent() is also overridden. Need to do
	 * that to handle accessibility features correctly.
	 */
	override fun performClick(): Boolean
	{
		super.performClick()
		return true
	}

	/**
	 * Select a color.
	 *
	 * @param newColor The new color to select.
	 */
	fun selectColor(newColor: Int)
	{
		// Set the new color
		color = newColor

		// Set the position of the color selector
		setColorSelectorPosition()

		// Redraw the color shader for the new color
		drawColorShader()
		invalidate()
	}

	/**
	 * Set the color selector position.
	 */
	private fun setColorSelectorPosition(x: Float, y: Float)
	{
		// Get the layout parameters
		val params = colorSelector!!.layoutParams

		// Set the X and Y positions
		colorSelector!!.x = x - params.width / 2.0f
		colorSelector!!.y = y - params.height / 2.0f
	}

	/**
	 * Set the color selector position to the current color.
	 */
	private fun setColorSelectorPosition()
	{
		// Get the X and Y positions
		val x = getColorSelectionX(hsv)
		val y = getColorSelectionY(hsv)

		// Set the new color selector position
		setColorSelectorPosition(x, y)
	}

	/**
	 * Set shader selector position.
	 */
	private fun setShaderSelectorPosition(x: Float)
	{
		// Get the layout parameters
		val params = shaderSelector!!.layoutParams

		// Determine the center in the Y direction
		val centerY = valueRect.centerY()

		// Set the new shader selector position
		shaderSelector!!.x = x - params.width / 2.0f
		shaderSelector!!.y = centerY - params.height / 2.0f
	}

	/**
	 * Set shader selector position to the current color.
	 */
	private fun setShaderSelectorPosition()
	{
		// Get the shader selector position in the X direction
		val x = getShaderSelectionX(hsv)

		// Set the shader selector position in the X direction
		setShaderSelectorPosition(x)
	}

}