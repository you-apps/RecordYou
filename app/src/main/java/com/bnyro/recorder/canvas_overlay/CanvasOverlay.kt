package com.bnyro.recorder.canvas_overlay

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bnyro.recorder.ui.theme.RecordYouTheme

@RequiresApi(Build.VERSION_CODES.O)
class CanvasOverlay(context: Context) {
    private var windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private var canvasView = ComposeView(context).apply {
        val activity = context as ComponentActivity
        setViewTreeLifecycleOwner(activity)
        setViewTreeViewModelStoreOwner(activity)
        setViewTreeSavedStateRegistryOwner(activity)
        setContent {
            RecordYouTheme() {
                MainCanvas()
            }
        }
    }
    private var toolbarView = ComposeView(context).apply {
        val activity = context as ComponentActivity
        setViewTreeLifecycleOwner(activity)
        setViewTreeViewModelStoreOwner(activity)
        setViewTreeSavedStateRegistryOwner(activity)
        setContent {
            RecordYouTheme {
                ToolbarView(hideCanvas = { hide ->
                    if (hide) {
                        hideCanvas()
                    } else {
                        showCanvas()
                    }
                })
            }
        }
    }

    init {
        hideCanvas()
    }

    private fun getWindowLayoutParams(size: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )
    }

    fun showAll() {
        try {
            if (canvasView.windowToken == null && canvasView.parent == null) {
                val params = getWindowLayoutParams(WindowManager.LayoutParams.MATCH_PARENT)
                windowManager.addView(canvasView, params)
            }
            if (toolbarView.windowToken == null && toolbarView.parent == null) {
                val params = getWindowLayoutParams(WindowManager.LayoutParams.WRAP_CONTENT)
                params.gravity = Gravity.TOP or Gravity.END
                windowManager.addView(toolbarView, params)
            }
        } catch (e: Exception) {
            Log.e("Show Overlay", e.toString())
        }
    }

    fun showCanvas() {
        canvasView.isVisible = true
    }

    fun hideCanvas() {
        canvasView.isInvisible = true
    }

    fun remove() {
        try {
            windowManager.removeView(canvasView)
            canvasView.invalidate()
            windowManager.removeView(toolbarView)
            toolbarView.invalidate()
        } catch (e: Exception) {
            Log.e("Remove Overlay", e.toString())
        }
    }
}
