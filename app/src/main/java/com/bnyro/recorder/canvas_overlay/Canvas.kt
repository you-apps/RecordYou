package com.bnyro.recorder.canvas_overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.lifecycle.viewmodel.compose.viewModel

enum class MotionEvent {
    Up, Down, Idle, Move
}

enum class DrawMode {
    Pen, Eraser
}

@Composable
fun MainCanvas(canvasViewModel: CanvasViewModel = viewModel()) {
    var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }

    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }

    val drawModifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            awaitEachGesture {
                val downEvent = awaitFirstDown()
                currentPosition = downEvent.position
                motionEvent = MotionEvent.Down
                if (downEvent.pressed != downEvent.previousPressed) downEvent.consume()
                do {
                    val event = awaitPointerEvent()
                    if (event.changes.size == 1) {
                        currentPosition = event.changes[0].position
                        motionEvent = MotionEvent.Move
                        if (event.changes[0].positionChange() != Offset.Zero) event.changes[0].consume()
                    }
                } while (event.changes.any { it.pressed })
                motionEvent = MotionEvent.Up
            }
        }
    Canvas(modifier = drawModifier) {
        with(drawContext.canvas.nativeCanvas) {
            val checkPoint = saveLayer(null, null)
            when (motionEvent) {
                MotionEvent.Idle -> Unit
                MotionEvent.Down -> {
                    canvasViewModel.paths.add(canvasViewModel.currentPath)
                    canvasViewModel.currentPath.path.moveTo(
                        currentPosition.x,
                        currentPosition.y
                    )
                }

                MotionEvent.Move -> {
                    canvasViewModel.currentPath.path.lineTo(
                        currentPosition.x,
                        currentPosition.y
                    )
                    drawCircle(
                        center = currentPosition,
                        color = Color.Gray,
                        radius = canvasViewModel.currentPath.strokeWidth / 2,
                        style = Stroke(
                            width = 1f
                        )
                    )
                }

                MotionEvent.Up -> {
                    canvasViewModel.currentPath.path.lineTo(
                        currentPosition.x,
                        currentPosition.y
                    )
                    canvasViewModel.currentPath = PathProperties(
                        path = Path(),
                        strokeWidth = canvasViewModel.currentPath.strokeWidth,
                        color = canvasViewModel.currentPath.color,
                        drawMode = canvasViewModel.currentPath.drawMode
                    )
                    currentPosition = Offset.Unspecified
                    motionEvent = MotionEvent.Idle
                }
            }
            canvasViewModel.paths.forEach { path ->
                path.draw(this@Canvas)
            }
            restoreToCount(checkPoint)
        }
    }
}

class PathProperties(
    var path: Path = Path(),
    var strokeWidth: Float = 10f,
    var color: Color = Color.Red,
    var drawMode: DrawMode = DrawMode.Pen
) {
    fun draw(scope: DrawScope) {
        when (drawMode) {
            DrawMode.Pen -> {
                scope.drawPath(
                    color = color,
                    path = path,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            DrawMode.Eraser -> {
                scope.drawPath(
                    color = Color.Transparent,
                    path = path,
                    style = Stroke(
                        width = 50f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    blendMode = BlendMode.Clear
                )
            }
        }
    }
}
