package com.nfcalarmclock.view

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.animation.addListener
import com.nfcalarmclock.R

//class NacCustomArcView(context: Context, attributeSet: AttributeSet): View(context, attributeSet)
//{
//
//	private var arcSize = 0f // Dynamic size of the arc
//	private val maxArcSize = 45f // Maximum size of the arc
//	private val minArcSize = 0f // Minimum size of the arc
//
//	private val paint = Paint().apply {
//		color = Color.RED
//		strokeWidth = 4f
//		style = Paint.Style.STROKE
//		isAntiAlias = true
//	}
//
//	private val rect = RectF()
//
//	//init {
//	//	startPulsingAnimation()
//	//}
//
//	override fun onDraw(canvas: Canvas) {
//		super.onDraw(canvas)
//
//		val density = resources.displayMetrics.density
//		val scaledStrokeWidth = 4 * density
//		paint.strokeWidth = scaledStrokeWidth
//
//		rect.set(scaledStrokeWidth, scaledStrokeWidth, width - scaledStrokeWidth, height - scaledStrokeWidth)
//
//		// Use arcSize for the sweep angle
//		canvas.drawArc(rect, 0f, arcSize, false, paint)
//	}
//
//	fun startPulsingAnimation() {
//		val animator = ValueAnimator.ofFloat(minArcSize, maxArcSize).apply {
//			 duration = 1000 // Duration of one pulse
//			 interpolator = AccelerateDecelerateInterpolator() // Smooth animation
//			 repeatCount = ValueAnimator.INFINITE
//			 repeatMode = ValueAnimator.REVERSE
//
//			 addUpdateListener { animation ->
//				  arcSize = animation.animatedValue as Float
//				  invalidate() // Redraw the view
//			 }
//		}
//		animator.start()
//	}
//
//}

//class NacCustomArcViewPulsingWifiSymbolView(context: Context, attrs: AttributeSet) : View(context, attrs) {
class NacCustomArcView(context: Context, attrs: AttributeSet) : View(context, attrs) {

	private val paint = Paint()
	//private val animator = ValueAnimator.ofFloat(1f, 1.5f, 1f) // Values for pulsing animation
	//private val animator1 = ValueAnimator.ofFloat(0f, 1f) // Values for pulsing animation
	//private val animator2 = ValueAnimator.ofFloat(0f, 1f) // Values for pulsing animation
	//private val animator3 = ValueAnimator.ofFloat(0f, 1f) // Values for pulsing animation
	private var animator1 = AnimatorInflater.loadAnimator(context, R.animator.scan_nfc_wifi) as ValueAnimator
	private var animator2 = AnimatorInflater.loadAnimator(context, R.animator.scan_nfc_wifi) as ValueAnimator
	private var animator3 = AnimatorInflater.loadAnimator(context, R.animator.scan_nfc_wifi) as ValueAnimator
	private var shouldClearCanvas: Boolean = false

	init {
		paint.color = Color.RED // Set your desired color
		paint.strokeWidth = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			5f,
			resources.displayMetrics
		) // Set the stroke width in dp
		paint.style = Paint.Style.STROKE
		paint.strokeJoin = Paint.Join.ROUND

		setupAnimator()
	}

	private fun setupAnimator() {
		animator1.addUpdateListener { invalidate() }
		animator2.addUpdateListener { invalidate() }
		animator3.addUpdateListener { invalidate() }

		animator1.addListener(onEnd = { animator2.start() })
		animator2.addListener(onEnd = { animator3.start() })
		animator3.addListener(
			onEnd = {
				val handler = Handler(context.mainLooper)

				handler.postDelayed({
					animator1 = AnimatorInflater.loadAnimator(context, R.animator.scan_nfc_wifi) as ValueAnimator
					animator2 = AnimatorInflater.loadAnimator(context, R.animator.scan_nfc_wifi) as ValueAnimator
					animator3 = AnimatorInflater.loadAnimator(context, R.animator.scan_nfc_wifi) as ValueAnimator

					shouldClearCanvas = true

					setupAnimator()
					invalidate()

					//val newHandler = Handler(context.mainLooper)
					//newHandler.postDelayed({ startAnimation() }, 1000L)

					startAnimation()
				}, 1000L)
			}
		)
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		if (shouldClearCanvas)
		{
			//canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
			//shouldClearCanvas = false
			//return
		}

		val centerX = width / 2f
		val centerY = height / 2f
		val radius1 = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			//10f * animator.animatedValue as Float,
			10f * 1f,
			resources.displayMetrics
		)
		val radius2 = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			//20f * animator.animatedValue as Float,
			20f * 1f,
			resources.displayMetrics
		)
		val radius3 = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			//30f * animator.animatedValue as Float,
			30f * 1f,
			resources.displayMetrics
		)

		drawArc(canvas, centerX, centerY, radius1, animator1.animatedValue as Float)
		drawArc(canvas, centerX, centerY, radius2, animator2.animatedValue as Float)
		drawArc(canvas, centerX, centerY, radius3, animator3.animatedValue as Float)
	}

	private fun drawArc(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, angle: Float) {
		val path = Path()
		path.arcTo(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 315f, 90f, true)
		//paint.alpha = Math.max(0, Math.min(255, -127 + (angle*255*2))
		paint.alpha = (angle*255).toInt()
		canvas.drawPath(path, paint)
	}

	fun startAnimation() {
		animator1.start()
	}

	fun stopAnimation() {
		animator1.cancel()
		animator2.cancel()
		animator3.cancel()
	}

}
