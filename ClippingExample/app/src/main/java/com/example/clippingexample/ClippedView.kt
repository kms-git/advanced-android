import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.example.clippingexample.R

class ClippedView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        // Smooth out edges of what is drawn without affecting shape.
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.strokeWidth)
        textSize = resources.getDimension(R.dimen.textSize)
    }
    private val path = Path()

    private val clipRectRight = resources.getDimension(R.dimen.clipRectRight)
    private val clipRectBottom = resources.getDimension(R.dimen.clipRectBottom)
    private val clipRectTop = resources.getDimension(R.dimen.clipRectTop)
    private val clipRectLeft = resources.getDimension(R.dimen.clipRectLeft)
    private val rectInset = resources.getDimension(R.dimen.rectInset)
    private val smallRectOffset = resources.getDimension(R.dimen.smallRectOffset)
    private val circleRadius = resources.getDimension(R.dimen.circleRadius)
    private val textOffset = resources.getDimension(R.dimen.textOffset)
    private val textSize = resources.getDimension(R.dimen.textSize)

    private val columnOne = rectInset
    private val columnTwo = columnOne + rectInset + clipRectRight
    private val rowOne = rectInset
    private val rowTwo = rowOne + rectInset + clipRectBottom
    private val rowThree = rowTwo + rectInset + clipRectBottom
    private val rowFour = rowThree + rectInset + clipRectBottom
    private val textRow = rowFour + (1.5f * clipRectBottom)
    private var rectF = RectF(
            rectInset,
            rectInset,
            clipRectRight - rectInset,
            clipRectBottom - rectInset
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackAndUnclippedRectangle(canvas)
        drawDifferenceClippingExample(canvas)
        drawCircularClippingExample(canvas)
        drawIntersectionClippingExample(canvas)
        drawCombinedClippingExample(canvas)
        drawRoundedRectangleClippingExample(canvas)
        drawOutsideClippingExample(canvas)
        drawSkewedTextExample(canvas)
        drawTranslatedTextExample(canvas)
    }

    private fun drawClippedRectangle(canvas: Canvas) {
        // Clip rectangle and color with white
        canvas.clipRect(clipRectLeft, clipRectTop, clipRectRight, clipRectBottom)
        canvas.drawColor(Color.WHITE)

        // Draw a diagonal line inside the clipping rectangle
        paint.color = Color.RED
        canvas.drawLine(clipRectLeft, clipRectTop, clipRectRight, clipRectBottom, paint)

        // Draw a circle inside the clipping rectangle
        paint.color = Color.GREEN
        canvas.drawCircle(circleRadius, clipRectBottom - circleRadius, circleRadius, paint)

        // Draw a text
        paint.color = Color.BLUE
        paint.textSize = textSize

        // Aligning the right side of the text to the origin places it on the left of the origin
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.clipping), clipRectRight, textOffset, paint)
    }

    private fun drawBackAndUnclippedRectangle(canvas: Canvas) {
        canvas.drawColor(Color.GRAY)
        canvas.save()
        canvas.translate(columnOne,rowOne)
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    /*
    1. Save the canvas.
    2. Translate the origin of the canvas into open space to the first row, second column, to the right of the first rectangle.
    3. Apply two clipping rectangles. The DIFFERENCE operator subtracts the second rectangle from the first one.
    4. Call the drawClippedRectangle() method to draw the modified canvas.
    5. Restore the canvas state.
     */
    private fun drawDifferenceClippingExample(canvas: Canvas) {
        canvas.save()
        // Move the origin to the right for the next rectangle.
        canvas.translate(columnTwo,rowOne)
        // Use the subtraction of two clipping rectangles to create a frame.
        canvas.clipRect(
                2 * rectInset,2 * rectInset,
                clipRectRight - 2 * rectInset,
                clipRectBottom - 2 * rectInset
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            canvas.clipRect(
                    4 * rectInset,4 * rectInset,
                    clipRectRight - 4 * rectInset,
                    clipRectBottom - 4 * rectInset,
                    Region.Op.DIFFERENCE
            )
        else {
            canvas.clipOutRect(
                    4 * rectInset,4 * rectInset,
                    clipRectRight - 4 * rectInset,
                    clipRectBottom - 4 * rectInset
            )
        }
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    private fun drawCircularClippingExample(canvas: Canvas) {
        canvas.save()
        canvas.translate(columnOne, rowTwo)
        // Clears any lines and curves from the path but unlike reset(),
        // keeps the internal data structure for faster reuse.
        path.rewind()
        path.addCircle(
                circleRadius,clipRectBottom - circleRadius,
                circleRadius,Path.Direction.CCW
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            canvas.clipPath(path, Region.Op.DIFFERENCE)
        } else {
            canvas.clipOutPath(path)
        }
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    private fun drawIntersectionClippingExample(canvas: Canvas) {
        canvas.save()
        canvas.translate(columnTwo,rowTwo)
        canvas.clipRect(
                clipRectLeft,clipRectTop,
                clipRectRight - smallRectOffset,
                clipRectBottom - smallRectOffset
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            canvas.clipRect(
                    clipRectLeft + smallRectOffset,
                    clipRectTop + smallRectOffset,
                    clipRectRight,clipRectBottom,
                    Region.Op.INTERSECT
            )
        } else {
            canvas.clipRect(
                    clipRectLeft + smallRectOffset,
                    clipRectTop + smallRectOffset,
                    clipRectRight,clipRectBottom
            )
        }
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    private fun drawCombinedClippingExample(canvas: Canvas) {
        canvas.save()
        canvas.translate(columnOne, rowThree)
        path.rewind()
        path.addCircle(
                clipRectLeft + rectInset + circleRadius,
                clipRectTop + circleRadius + rectInset,
                circleRadius,Path.Direction.CCW
        )
        path.addRect(
                clipRectRight / 2 - circleRadius,
                clipRectTop + circleRadius + rectInset,
                clipRectRight / 2 + circleRadius,
                clipRectBottom - rectInset,Path.Direction.CCW
        )
        canvas.clipPath(path)
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    private fun drawRoundedRectangleClippingExample(canvas: Canvas) {
        canvas.save()
        canvas.translate(columnTwo,rowThree)
        path.rewind()
        path.addRoundRect(
                rectF,clipRectRight / 4,
                clipRectRight / 4, Path.Direction.CCW
        )
        canvas.clipPath(path)
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    private fun drawOutsideClippingExample(canvas: Canvas) {
        canvas.save()
        canvas.translate(columnOne,rowFour)
        canvas.clipRect(2 * rectInset,2 * rectInset,
                clipRectRight - 2 * rectInset,
                clipRectBottom - 2 * rectInset)
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    private fun drawTranslatedTextExample(canvas: Canvas) {
        canvas.save()
        paint.color = Color.GREEN
        // Align the RIGHT side of the text with the origin.
        paint.textAlign = Paint.Align.LEFT
        // Apply transformation to canvas.
        canvas.translate(columnTwo,textRow)
        // Draw text.
        canvas.drawText(context.getString(R.string.translated),
                clipRectLeft,clipRectTop,paint)
        canvas.restore()
    }

    private fun drawSkewedTextExample(canvas: Canvas) {
        canvas.save()
        paint.color = Color.YELLOW
        paint.textAlign = Paint.Align.RIGHT
        // Position text.
        canvas.translate(columnTwo, textRow)
        // Apply skew transformation.
        canvas.skew(0.2f, 0.3f)
        canvas.drawText(context.getString(R.string.skewed),
                clipRectLeft, clipRectTop, paint)
        canvas.restore()
    }
}

