package com.bnyro.recorder.canvas_overlay

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bnyro.recorder.ui.theme.RecordYouTheme
import com.bnyro.recorder.util.CustomLifecycleOwner
import com.bnyro.recorder.util.CustomViewModelStoreOwner


@RequiresApi(Build.VERSION_CODES.O)
class CanvasOverlay(context: Context) {
    private var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSPARENT
    )
    private var windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

    private var composeView = ComposeView(context).apply {
        setContent {
            RecordYouTheme() {
                OverlayView(
                    onDismissRequest = { this@CanvasOverlay.remove() })
            }
        }
    }

    init {
        val lifecycleOwner = CustomLifecycleOwner()
        val viewModelStoreOwner = CustomViewModelStoreOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
    }

    fun show() {
        try {
            if (composeView.windowToken == null) {
                if (composeView.parent == null) {
                    windowManager.addView(composeView, params)
                }
            }
        } catch (e: Exception) {
            Log.e("Show Overlay", e.toString())
        }
    }

    fun hide() {
        try {
            windowManager.removeView(composeView)
        } catch (e: Exception) {
            Log.e("Hide Overlay", e.toString())
        }
    }

    fun remove() {
        try {
            windowManager.removeView(composeView)
            composeView.invalidate()
            (composeView.parent as ViewGroup).removeAllViews()
        } catch (e: Exception) {
            Log.e("Remove Overlay", e.toString())
        }
    }

}