package com.karishma.canvas

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {


    private var file: File? = null
    private val canvasLL: LinearLayout? = null
    private val view: View? = null
    var mSignature: Signature?= null
    private val bitmap: Bitmap? = null

    private lateinit var storageDirectory : String
    private lateinit var picName: String
    private lateinit var storedPath : String
    private var isImage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path.toString()
        picName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        storedPath = "$storageDirectory$picName.png"

        mSignature = Signature(this, null)

        linear_signature.addView(mSignature)

        buttonSubmit.setOnClickListener(this)
        txtClear.setOnClickListener(this)
        linear_signature.setOnClickListener(this)
    }

    class Signature(
        context: Context?,
        attrs: AttributeSet?
    ) :
        View(context, attrs) {
        val dirtyRect = RectF()
        var paint = Paint()
        var path = Path()
        var lastTouchX = 0f
        var lastTouchY = 0f
        var isDraw  = false

        fun clearSign() {
            isDraw = false
            path.reset()
            invalidate()
        }

        fun save(view: View? , StoredPath : String, parentView : View ) {

            val returnedBitmap = Bitmap.createBitmap(parentView.width, parentView.height, Bitmap.Config.RGB_565)
            val canvas = Canvas(returnedBitmap)
            val bgDrawable: Drawable = parentView.background
            if (bgDrawable != null) {
                bgDrawable.draw(canvas)
            } else canvas.drawColor(Color.WHITE)
            parentView.draw(canvas)
            val bs = ByteArrayOutputStream()
            returnedBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs)

            try {
                // Output the file
                val mFileOutStream = FileOutputStream(StoredPath)
                view!!.draw(canvas)

                // Convert the output file to Image such as .png
                returnedBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream)
                mFileOutStream.flush()
                mFileOutStream.close()
                Log.e("SAVE IMAGE PATH", "Path: $StoredPath")
            } catch (e: Exception) {
                Log.v("log_tag", e.toString())
            }
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawPath(path, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val eventX = event.x
            val eventY = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDraw = true
                    path.moveTo(eventX, eventY)
                    lastTouchX = eventX
                    lastTouchY = eventY
                    return true
                }
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    resetDirtyRect(eventX, eventY)
                    val historySize = event.historySize
                    var i = 0
                    while (i < historySize) {
                        val historicalX = event.getHistoricalX(i)
                        val historicalY = event.getHistoricalY(i)
                        path.lineTo(historicalX, historicalY)
                        i++
                    }
                    path.lineTo(eventX, eventY)
                    isDraw = true
                }
            }
            invalidate(
                (dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
                (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
                (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
                (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt()
            )
            lastTouchX = eventX
            lastTouchY = eventY
            return true
        }

        private fun resetDirtyRect(eventX: Float, eventY: Float) {
            dirtyRect.left = Math.min(lastTouchX, eventX)
            dirtyRect.right = Math.max(lastTouchX, eventX)
            dirtyRect.top = Math.min(lastTouchY, eventY)
            dirtyRect.bottom = Math.max(lastTouchY, eventY)
        }

        companion object {
            const val STROKE_WIDTH = 6f
            const val HALF_STROKE_WIDTH = STROKE_WIDTH / 2
        }

        init {
            paint.isAntiAlias = true
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeWidth =
                STROKE_WIDTH
        }
    }

    fun prepareBDEData(imagePath: String?) {
        val file = File(imagePath)
       // val requestFile = RequestBody.create(MediaType.parse(AppConstant.MULTI_PART_FORM_DATA), file)
       // val body = MultipartBody.Part.createFormData(AppConstant.IMAGE_PARAM_BDE, file.name, requestFile)


    }

    override fun onClick(view: View?) {
        when (view?.id) {

            txtClear.id -> {
                mSignature?.clearSign()
            }

            linear_signature.id -> {
                linear_text.visibility = View.GONE
            }

            buttonSubmit.id -> {

                intent.extras?.let { it ->
                    view.isDrawingCacheEnabled = true
                    mSignature?.save(view, storedPath, linear_signature)
                    isImage = mSignature?.isDraw!!
                    if (isImage) {
                        prepareBDEData(storedPath)
                    } else
                        Toast.makeText(
                            this@MainActivity,
                            "Please draw signature ",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        }
    }

}