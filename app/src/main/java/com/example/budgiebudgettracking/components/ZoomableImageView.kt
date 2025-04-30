package com.example.budgiebudgettracking.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs),
ScaleGestureDetector.OnScaleGestureListener {

	private val matrix = Matrix()
	private val scaleDetector = ScaleGestureDetector(context, this)
	private var scaleFactor = 1f

	private val last = PointF()
	private val start = PointF()
	private enum class Mode { NONE, DRAG, ZOOM }
	private var mode = Mode.NONE

	init {
		isClickable = true
		imageMatrix = matrix
		scaleType = ScaleType.MATRIX
	}

	override fun onDraw(canvas: Canvas) {
		canvas.save()
		canvas.concat(matrix)
		super.onDraw(canvas)
		canvas.restore()
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		scaleDetector.onTouchEvent(event)

		when (event.actionMasked) {
			MotionEvent.ACTION_DOWN -> {
				last.x = event.x; last.y = event.y
				start.x = event.x; start.y = event.y
				mode = Mode.DRAG
			}
			MotionEvent.ACTION_MOVE -> if (mode == Mode.DRAG) {
				val dx = event.x - last.x
				val dy = event.y - last.y
				matrix.postTranslate(dx, dy)
				last.x = event.x; last.y = event.y
			}
			MotionEvent.ACTION_POINTER_DOWN -> mode = Mode.ZOOM
			MotionEvent.ACTION_POINTER_UP   -> mode = Mode.DRAG
			MotionEvent.ACTION_UP          -> mode = Mode.NONE
		}

		imageMatrix = matrix
		invalidate()
		return true
	}

	override fun onScale(detector: ScaleGestureDetector): Boolean {
		val scale = detector.scaleFactor
		scaleFactor = (scaleFactor * scale).coerceIn(0.5f, 5.0f)
		matrix.postScale(scale, scale, detector.focusX, detector.focusY)
		return true
	}
	override fun onScaleBegin(detector: ScaleGestureDetector) = true
	override fun onScaleEnd(detector: ScaleGestureDetector) {}
}
