package com.bnyro.recorder.canvas_overlay

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bnyro.recorder.ui.theme.RecordYouTheme
import com.bnyro.recorder.util.CustomLifecycleOwner

@RequiresApi(Build.VERSION_CODES.O)
class CanvasOverlay(context: Context) {
    private var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSPARENT
    )
    private var windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private var canvasView = ComposeView(context).apply {
        setContent {
            RecordYouTheme() {
                MainCanvas()
            }
        }
    }
    private var toolbarView = ComposeView(context).apply {
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
        val lifecycleOwner = CustomLifecycleOwner()
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        canvasView.setViewTreeLifecycleOwner(lifecycleOwner)
        canvasView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
        canvasView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        toolbarView.setViewTreeLifecycleOwner(lifecycleOwner)
        toolbarView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
        toolbarView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        hideCanvas()
    }

    fun showAll() {
        try {
            if (canvasView.windowToken == null && canvasView.parent == null) {
                windowManager.addView(canvasView, params)
            }
            if (toolbarView.windowToken == null && toolbarView.parent == null) {
                val toolbarParams = params
                toolbarParams.gravity = Gravity.TOP or Gravity.END
                windowManager.addView(toolbarView, toolbarParams)
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
