package com.cms.scancodeview

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.cms.scancodeview.R

/**
 * AUTHOR：YF
 * DATE: 2024/1/15 0015
 * DESCRIBE:
 */
class ScanCodeView @JvmOverloads constructor(
    context: Context,
    private var attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var mPaint: Paint? = null

    /**
     * 正方形扫描框的宽度
     */
    private var mScanWidth = dp2px(200F)

    /**
     * 正方形扫描框矩形
     */
    private var mRectFrameRect: RectF? = null

    private val mPath = Path()

    /**
     * 扫描图片
     */
    private var mLineBitmap: Bitmap? = null

    /**
     * 扫描图片每次移动的距离
     */
    private var mMoveStep: Float = 0F

    /**
     * 扫描时间
     */
    private var mDelayTimes: Int = 500

    /**
     * 除扫描框之外的背景色
     */
    private var mBackgroundColor = Color.GRAY

    /**
     * 扫描框四边的颜色
     */
    private var mRectColor = Color.RED

    /**
     * 扫描框四个顶点角的颜色
     */
    private var mBorderColor = Color.RED

    /**
     * 扫描框边框线的宽度
     */
    private var mRectWidth = dp2px(0.5f)

    /**
     * 扫描框四边角的粗线的长度
     */
    private var mBorderLength = dp2px(20f)

    /**
     * 扫描框四边角的粗线的宽度
     */
    private var mBorderWidth = dp2px(2f)

    /**
     * 扫描线的Drawable文件
     */
    private var mDrawable: Drawable? = null


    init {
        initAttr()
        mPaint = getDefaultPaint()
        mMoveStep = dp2px(2F).toFloat()

    }

    /**
     * 解析属性
     */
    private fun initAttr() {
        attrs?.let {
            val attributes = context.obtainStyledAttributes(it, R.styleable.ScanCodeView)
            for (index in 0.until(attributes.indexCount)) {
                when (val attr = attributes.getIndex(index)) {
                    R.styleable.ScanCodeView_scan_image -> {
                        mDrawable = attributes.getDrawable(attr)
                    }

                    R.styleable.ScanCodeView_scan_duration -> {
                        mDelayTimes = attributes.getInteger(attr, mDelayTimes)
                    }

                    R.styleable.ScanCodeView_scan_width -> {
                        mScanWidth = attributes.getInteger(attr, mScanWidth)
                    }

                    R.styleable.ScanCodeView_scan_bg_color -> {
                        mBackgroundColor = attributes.getColor(attr, mBackgroundColor)
                    }

                    R.styleable.ScanCodeView_scan_rect_width -> {
                        mRectWidth = attributes.getInteger(attr, mRectWidth)
                    }

                    R.styleable.ScanCodeView_scan_rect_color -> {
                        mRectColor = attributes.getColor(attr, mRectColor)
                    }

                    R.styleable.ScanCodeView_scan_border_color -> {
                        mBorderColor = attributes.getColor(attr, mBorderColor)
                    }

                    R.styleable.ScanCodeView_scan_border_length -> {
                        mBorderLength = attributes.getInteger(attr, mBorderLength)
                    }

                    R.styleable.ScanCodeView_scan_border_width -> {
                        mBorderWidth = attributes.getInteger(attr, mBorderWidth)
                    }

                }

            }
            attributes.recycle()

        }
        mLineBitmap = if (mDrawable != null) {
            (mDrawable as? BitmapDrawable)?.bitmap
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.qrcode_default_scan_line)
        }
    }

    /**
     * 创建画笔
     */
    private fun getDefaultPaint(): Paint {
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.RED
            isAntiAlias = true

        }
        return paint
    }

    override fun onDraw(canvas: Canvas?) {
        /**绘制背景色**/
        drawScanBackground(canvas)
        /**绘制正方形扫描框**/
        drawBorderLine(canvas)
        /**绘制正方形四个边角**/
        drawBorderCorner(canvas)
        /**绘制扫描线**/
        drawScanLine(canvas)
        super.onDraw(canvas)
    }

    /**
     * 绘制背景色
     */
    private fun drawScanBackground(canvas: Canvas?) {
        mPaint?.style = Paint.Style.FILL
        mPaint?.color = mBackgroundColor
        val canvasWidth = canvas?.width
        val canvasHeight = canvas?.height
        mPaint?.let {
            canvas?.drawRect(0f, 0f, canvasWidth!!.toFloat(), mRectFrameRect!!.top, it)
            canvas?.drawRect(
                0f,
                mRectFrameRect!!.top - mBorderWidth / 2,
                mRectFrameRect!!.left,
                mRectFrameRect!!.bottom + mBorderWidth / 2,
                it
            )
            canvas?.drawRect(
                mRectFrameRect!!.right,
                mRectFrameRect!!.top - mBorderWidth / 2,
                canvasWidth!!.toFloat(),
                mRectFrameRect!!.bottom,
                it
            )
            canvas?.drawRect(
                0f,
                mRectFrameRect!!.bottom - mBorderWidth / 2,
                canvasWidth!!.toFloat(),
                canvasHeight!!.toFloat(),
                it
            )
        }

    }


    /**
     * 画边框线
     */
    private fun drawBorderLine(canvas: Canvas?) {
        mPaint?.color = mRectColor
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth = mRectWidth.toFloat()
        mRectFrameRect?.let { mPaint?.let { it1 -> canvas?.drawRect(it, it1) } }
    }

    /**
     * 画四个角
     */
    private fun drawBorderCorner(canvas: Canvas?) {
        mPaint?.color = mBorderColor
        mPaint?.style = Paint.Style.STROKE
        val connerWidth = mBorderWidth / 2
        mPaint?.strokeWidth = mBorderWidth.toFloat()

        mPath.reset()

        mPath.moveTo(mRectFrameRect!!.left, mRectFrameRect!!.top - connerWidth + mBorderLength)
        mPath.lineTo(mRectFrameRect!!.left, mRectFrameRect!!.top)
        mPath.lineTo(mRectFrameRect!!.left - connerWidth + mBorderLength, mRectFrameRect!!.top)

        mPath.moveTo(mRectFrameRect!!.right + connerWidth - mBorderLength, mRectFrameRect!!.top)
        mPath.lineTo(mRectFrameRect!!.right, mRectFrameRect!!.top)
        mPath.lineTo(mRectFrameRect!!.right, mRectFrameRect!!.top - mBorderWidth + mBorderLength)

        mPath.moveTo(mRectFrameRect!!.left, mRectFrameRect!!.bottom + mBorderWidth - mBorderLength)
        mPath.lineTo(mRectFrameRect!!.left, mRectFrameRect!!.bottom)
        mPath.lineTo(mRectFrameRect!!.left - mBorderWidth + mBorderLength, mRectFrameRect!!.bottom)

        mPath.moveTo(mRectFrameRect!!.right, mRectFrameRect!!.bottom + mBorderWidth - mBorderLength)
        mPath.lineTo(mRectFrameRect!!.right, mRectFrameRect!!.bottom)
        mPath.lineTo(mRectFrameRect!!.right + mBorderWidth - mBorderLength, mRectFrameRect!!.bottom)

        canvas?.drawPath(mPath, mPaint!!)
    }

    /**
     * 绘制扫描线
     */
    private fun drawScanLine(canvas: Canvas?) {
        canvas?.save()
        canvas?.restore()

        val dstGridRectF = RectF(
            mRectFrameRect!!.left,
            mRectFrameRect!!.top,
            mRectFrameRect!!.right,
            mRectFrameRect!!.top + mMoveStep
        )
        val srcRect = Rect(
            0,
            (mLineBitmap!!.height - dstGridRectF.height()).toInt(),
            mLineBitmap!!.width,
            mLineBitmap!!.height
        )
        mLineBitmap?.let {
            canvas?.drawBitmap(it, srcRect, dstGridRectF, mPaint)
        }

        mMoveStep += dp2px(3F)
        if (mMoveStep >= mScanWidth + mLineBitmap!!.height / 2) {
            mMoveStep = 0F
        }
        postInvalidateDelayed(mDelayTimes.toLong())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createRect()
    }

    private fun createRect() {
        val leftOffset = (width - mScanWidth) / 2f
        val topOffset = (height - mScanWidth) / 2f
        mRectFrameRect =
            RectF(leftOffset, topOffset, leftOffset + mScanWidth, topOffset + mScanWidth)

        val scaleHeight = mScanWidth.toFloat() / mLineBitmap!!.width * mLineBitmap!!.height
        mLineBitmap =
            Bitmap.createScaledBitmap(mLineBitmap!!, mScanWidth, scaleHeight.toInt(), true)

        mDelayTimes =
            ((1.0f * mDelayTimes * mMoveStep) / (mScanWidth + mLineBitmap!!.height / 2)).toInt()

    }

    private fun dp2px(dp: Float): Int {
        val dimension = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        )
        return (dimension + 0.5).toInt()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mLineBitmap?.recycle()
        mDrawable = null
    }
}