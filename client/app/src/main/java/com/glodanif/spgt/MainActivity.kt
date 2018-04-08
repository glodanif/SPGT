package com.glodanif.spgt

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import com.samsung.android.sdk.SsdkUnsupportedException
import com.samsung.android.sdk.pen.Spen
import com.samsung.android.sdk.pen.document.SpenNoteDoc
import com.samsung.android.sdk.pen.document.SpenPageDoc
import com.samsung.android.sdk.pen.engine.SpenSimpleSurfaceView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var spenSimpleSurfaceView: SpenSimpleSurfaceView
    private lateinit var spenNoteDoc: SpenNoteDoc
    private lateinit var spenPageDoc: SpenPageDoc

    private var horizontalScale = 1f
    private var verticalScale = 1f

    private val connection = WiFiConnection("192.168.0.102", 8003)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_main)

        connection.onResolution = {
            launch(UI) {
                Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
            }
        }
        connection.prepare()

        findViewById<ImageButton>(R.id.ib_remote_settings).setOnClickListener {
            connection.close()
            connection.prepare()
        }

        findViewById<ImageButton>(R.id.ib_clean).setOnClickListener {

        }

        val surfaceLayout = findViewById<FrameLayout>(R.id.fl_spen_surface)
        val padding = resources.getDimensionPixelSize(R.dimen.pan_radius_padding) * 2
        surfaceLayout.post {

            val remoteDisplaySize = Size(1920, 1080)
            val containerSize = Size(surfaceLayout.measuredWidth - padding, surfaceLayout.measuredHeight - padding)
            val scaledSize = getScaledSize(containerSize, remoteDisplaySize.width, remoteDisplaySize.height)

            horizontalScale = if (containerSize.width > remoteDisplaySize.width)
                containerSize.width / remoteDisplaySize.width.toFloat() else remoteDisplaySize.width / containerSize.width.toFloat()
            verticalScale = if (containerSize.height > remoteDisplaySize.height)
                containerSize.height / remoteDisplaySize.height.toFloat() else remoteDisplaySize.height / containerSize.height.toFloat()

            setupSpen(surfaceLayout, scaledSize)
        }
    }

    private fun setupSpen(container: ViewGroup, remoteDisplaySize: Size) {

        var isSpenEnabled = false
        val spenPackage = Spen()

        try {
            spenPackage.initialize(this)
            isSpenEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN)
        } catch (e: SsdkUnsupportedException) {
            Toast.makeText(this, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

        spenSimpleSurfaceView = SpenSimpleSurfaceView(this).apply {
            isZoomable = false
        }

        container.addView(spenSimpleSurfaceView)

        try {
            spenNoteDoc = SpenNoteDoc(this, remoteDisplaySize.width, remoteDisplaySize.height)
        } catch (e: IOException) {
            Toast.makeText(this, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }

        spenPageDoc = spenNoteDoc.appendPage().apply {
            backgroundColor = Color.DKGRAY
            clearHistory()
        }

        spenSimpleSurfaceView.setPageDoc(spenPageDoc, true)

        if (!isSpenEnabled) {
            spenSimpleSurfaceView.setToolTypeAction(SpenSimpleSurfaceView.TOOL_FINGER, SpenSimpleSurfaceView.ACTION_STROKE)
            Toast.makeText(this, "Device does not support Spen. \n You can draw stroke by finger.", Toast.LENGTH_SHORT).show()
        }

        spenSimpleSurfaceView.setOnHoverListener { _, event ->

            if (event.action == MotionEvent.ACTION_HOVER_MOVE) {
                if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER) {

                } else if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
                    val x = event.x / horizontalScale
                    val y = event.y / verticalScale
                    connection.write("$x:$y")
                }
            }

            false
        }

        spenSimpleSurfaceView.setTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_MOVE) {
                if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER) {

                } else if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
                    val x = event.x / horizontalScale
                    val y = event.y / verticalScale
                    connection.write("$x:$y")
                }
            }

            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        spenSimpleSurfaceView.close()
        spenNoteDoc.close()
        connection.close()
    }

    private fun getScaledSize(surfaceSize: Size, remoteWidth: Int, remoteHeight: Int): Size {

        val maxWidth = surfaceSize.width
        val maxHeight = surfaceSize.height

        var viewWidth = maxWidth
        var viewHeight = maxHeight

        if (remoteWidth > maxWidth || remoteHeight > maxHeight) {

            if (remoteWidth == remoteHeight) {

                if (remoteHeight > maxHeight) {
                    viewWidth = maxHeight
                    viewHeight = maxHeight
                }

                if (viewWidth > maxWidth) {
                    viewWidth = maxWidth
                    viewHeight = maxWidth
                }

            } else if (remoteWidth > maxWidth) {

                viewWidth = maxWidth
                viewHeight = (maxWidth.toFloat() / remoteWidth * remoteHeight).toInt()

                if (viewHeight > maxHeight) {
                    viewHeight = maxHeight
                    viewWidth = (maxHeight.toFloat() / remoteHeight * remoteWidth).toInt()
                }

            } else if (remoteHeight > maxHeight) {

                viewHeight = maxHeight
                viewWidth = (maxHeight.toFloat() / remoteHeight * remoteWidth).toInt()

                if (viewWidth > maxWidth) {
                    viewWidth = maxWidth
                    viewHeight = (maxWidth.toFloat() / remoteWidth * remoteHeight).toInt()
                }
            }
        }

        return Size(viewWidth, viewHeight)
    }
}
