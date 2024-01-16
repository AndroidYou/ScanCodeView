# ScanCodeView
背景
最近在开发新项目时，使用了扫描二维码的功能，一般扫描二维码的效果是一条横线从上到下循环移动，这次却换成了网格图片。网上的大多数第三方库实现类似效果时 网格图片被拉伸变形。为了实现效果，只能动手写。

横线效果


网格效果


基础属性
这里自定义了一些常见属性：
scan_image	扫描图片资源
scan_duration	扫描一次时间 ms
scan_width	正方形扫描框宽度
scan_bg_color	除正方形扫描框之外的背景颜色
scan_rect_width	正方形扫描框边框宽度
scan_rect_color	正方形扫描框边框颜色
scan_border_width	扫描框四个边角线的宽度
scan_border_length	扫描框四个边角线的长度
scan_border_color	扫描框四个边角线的颜色
绘制背景色
首先定义正方形扫描框矩形的位置，这么默认使用屏幕中心的位置
 private fun createRect() {
        val leftOffset = (width - mScanWidth) / 2f
        val topOffset = (height - mScanWidth) / 2f
        mRectFrameRect =
            RectF(leftOffset, topOffset, leftOffset + mScanWidth, topOffset + mScanWidth)

        val scaleHeight = mScanWidth.toFloat() / mLineBitmap!!.width * mLineBitmap!!.height
        mLineBitmap =
            Bitmap.createScaledBitmap(mLineBitmap!!, mScanWidth, scaleHeight.toInt(), true)


    }
绘制背景色
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
将阴影部分分为四块，使用canvas.drawRect分别绘制。

绘制边框线

/**
     * 画边框线
     */
    private fun drawBorderLine(canvas: Canvas?) {
        mPaint?.color = mRectColor
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth = mRectWidth.toFloat()
        mRectFrameRect?.let { mPaint?.let { it1 -> canvas?.drawRect(it, it1) } }
    }
通过上面定义的扫描框矩形，绘制扫描框的边框线。

绘制四个边角线
四个边角线为折线，使用自定义view中的path实现比较简单。
    /**
     * 画四个角
     */
    private fun drawBorderCorner(canvas: Canvas?) {
        mPaint?.color = mBorderColor
        mPaint?.style = Paint.Style.STROKE
        val connerWidth = mBorderWidth / 2
        mPaint?.strokeWidth = mBorderWidth.toFloat()

        mPath.reset()
        //左上     
        mPath.moveTo(mRectFrameRect!!.left, mRectFrameRect!!.top - connerWidth + mBorderLength)
        mPath.lineTo(mRectFrameRect!!.left, mRectFrameRect!!.top)
        mPath.lineTo(mRectFrameRect!!.left - connerWidth + mBorderLength, mRectFrameRect!!.top)
         //右上     
        mPath.moveTo(mRectFrameRect!!.right + connerWidth - mBorderLength, mRectFrameRect!!.top)
        mPath.lineTo(mRectFrameRect!!.right, mRectFrameRect!!.top)
        mPath.lineTo(mRectFrameRect!!.right, mRectFrameRect!!.top - mBorderWidth + mBorderLength)
        //左下    
        mPath.moveTo(mRectFrameRect!!.left, mRectFrameRect!!.bottom + mBorderWidth - mBorderLength)
        mPath.lineTo(mRectFrameRect!!.left, mRectFrameRect!!.bottom)
        mPath.lineTo(mRectFrameRect!!.left - mBorderWidth + mBorderLength, mRectFrameRect!!.bottom)
         //右下 
        mPath.moveTo(mRectFrameRect!!.right, mRectFrameRect!!.bottom + mBorderWidth - mBorderLength)
        mPath.lineTo(mRectFrameRect!!.right, mRectFrameRect!!.bottom)
        mPath.lineTo(mRectFrameRect!!.right + mBorderWidth - mBorderLength, mRectFrameRect!!.bottom)

        canvas?.drawPath(mPath, mPaint!!)
    }
扫描线绘制及移动
绘制扫描线使用了canvas.drawBitmap 方法 ，通过裁剪显示位置绘制扫描图片。
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
这里通过调用postInvalidateDelayed 不停延迟绘制图片来实现扫描图的移动效果。

特点
像zxing 等三方库 直接使用扫描图片来绘制效果，由于扫描框是正方形，如果网格扫描图是长方形图片，则会导致被拉伸为正方形显示，图片变形。为了解决网格图的变形问题，这里对图片进行缩放处理，避免变形。
 val scaleHeight = mScanWidth.toFloat() / mLineBitmap!!.width * mLineBitmap!!.height
        mLineBitmap =
            Bitmap.createScaledBitmap(mLineBitmap!!, mScanWidth, scaleHeight.toInt(), true)

使用时，只需传入需要的扫描图片即可。

