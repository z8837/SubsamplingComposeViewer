package kr.co.humancare.subsamplingviewer.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlin.math.roundToLong
import kr.co.humancare.util.subsampling.ImageSource
import kr.co.humancare.util.subsampling.SubsamplingScaleImageView

class RotatableSubsamplingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SubsamplingScaleImageView(context, attrs) {

    private data class PreparedImageSource(
        val imageSource: ImageSource,
        val previewSource: ImageSource?,
    )

    var onAngleSettled: ((Float) -> Unit)? = null
    var onSingleTap: (() -> Unit)? = null

    private var currentImageKey: String? = null
    private var angleAnimator: ValueAnimator? = null
    private val singleTapDetector =
        GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(event: MotionEvent): Boolean = true

                override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                    onSingleTap?.invoke()
                    return true
                }
            },
        )

    init {
        setMinimumScaleType(SCALE_TYPE_CENTER_INSIDE)
        setPanEnabled(true)
        setZoomEnabled(true)
        setQuickScaleEnabled(true)
        setMaxScale(8f)
        setDoubleTapZoomScale(2.5f)
        setBackgroundColor(AndroidColor.rgb(10, 12, 16))
        setTileBackgroundColor(AndroidColor.TRANSPARENT)
    }

    fun render(uri: Uri, rotationDegrees: Float) {
        val nextKey = uri.toString()
        val isNewImage = currentImageKey != nextKey

        if (isNewImage) {
            angleAnimator?.cancel()
            angleAnimation = false
            angle = 0.0
            currentImageKey = nextKey
            val preparedImageSource = prepareImageSource(uri)
            if (preparedImageSource.previewSource != null) {
                setImage(preparedImageSource.imageSource, preparedImageSource.previewSource)
            } else {
                setImage(preparedImageSource.imageSource)
            }
        }

        syncRotation(rotationDegrees, animated = !isNewImage)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val singleTapHandled = singleTapDetector.onTouchEvent(event)
        val handled = super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> onAngleSettled?.invoke(currentRotation())
        }
        return handled || singleTapHandled
    }

    override fun onDetachedFromWindow() {
        angleAnimator?.cancel()
        angleAnimator = null
        super.onDetachedFromWindow()
    }

    private fun syncRotation(targetDegrees: Float, animated: Boolean) {
        val target = normalizeAngle(targetDegrees)
        if (abs(shortestDelta(currentRotation(), target)) < 0.5f) return

        if (!animated) {
            angleAnimator?.cancel()
            angle = target.toDouble()
            angleAnimation = false
            invalidate()
            return
        }

        animateRotationTo(target)
    }

    private fun animateRotationTo(target: Float) {
        angleAnimator?.cancel()

        val start = currentRotation()
        val delta = shortestDelta(start, target)

        if (abs(delta) < 0.5f) {
            finishRotation(target)
            return
        }

        val end = start + delta
        angleAnimation = true

        angleAnimator = ValueAnimator.ofFloat(start, end).apply {
            duration = (abs(delta) * 6f).roundToLong().coerceAtLeast(160L)
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                angle = (animator.animatedValue as Float).toDouble()
                invalidate()
            }
            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        finishRotation(target)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        finishRotation(target)
                    }
                },
            )
            start()
        }
    }

    private fun finishRotation(target: Float) {
        angle = normalizeAngle(target).toDouble()
        angleAnimation = false
        invalidate()
    }

    private fun currentRotation(): Float = normalizeAngle(angle.toFloat())

    private fun normalizeAngle(value: Float): Float {
        return ((value % 360f) + 360f) % 360f
    }

    private fun shortestDelta(from: Float, to: Float): Float {
        var delta = (to - from) % 360f
        if (delta > 180f) {
            delta -= 360f
        } else if (delta < -180f) {
            delta += 360f
        }
        return delta
    }

    private fun prepareImageSource(uri: Uri): PreparedImageSource {
        val bounds = decodeImageBounds(uri)
        val mainSource =
            if (bounds != null) {
                ImageSource.uri(uri).dimensions(bounds.first, bounds.second)
            } else {
                ImageSource.uri(uri)
            }
        val previewBitmap =
            bounds?.let { (width, height) ->
                decodePreviewBitmap(
                    uri = uri,
                    sourceWidth = width,
                    sourceHeight = height,
                )
            }
        return PreparedImageSource(
            imageSource = mainSource,
            previewSource = previewBitmap?.let(ImageSource::bitmap),
        )
    }

    private fun decodeImageBounds(uri: Uri): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
            if (options.outWidth > 0 && options.outHeight > 0) {
                options.outWidth to options.outHeight
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun decodePreviewBitmap(
        uri: Uri,
        sourceWidth: Int,
        sourceHeight: Int,
    ): Bitmap? {
        val sampleSize = calculatePreviewSampleSize(sourceWidth, sourceHeight)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun calculatePreviewSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
    ): Int {
        val maxPreviewDimension = 2048
        var sampleSize = 1
        var largestDimension = maxOf(sourceWidth, sourceHeight)
        while (largestDimension / sampleSize > maxPreviewDimension) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
