package com.example.androiddevelopertask.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androiddevelopertask.filament.ModelSurfaceView
import com.example.androiddevelopertask.model.InteractionMode
import com.example.androiddevelopertask.model.ModelEntry
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun ModelContainer(
    entry: ModelEntry,
    bounds: IntSize,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val minSizePx = with(density) { 140.dp.toPx() }
    val maxSizePx = with(density) {
        max(280.dp.toPx(), max(bounds.width, bounds.height) * 0.95f)
    }
    val initialSizePx = with(density) { 220.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(entry.initialX) }
    var offsetY by remember { mutableFloatStateOf(entry.initialY) }
    var widthPx by remember { mutableFloatStateOf(initialSizePx) }
    var heightPx by remember { mutableFloatStateOf(initialSizePx) }
    var mode by remember { mutableStateOf(InteractionMode.Normal) }

    var modelView by remember { mutableStateOf<ModelSurfaceView?>(null) }

    val isInteracting = mode == InteractionMode.Interact

    Box(
        modifier = modifier

            .offset() { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(
                width = with(density) { widthPx.toDp() },
                height = with(density) { heightPx.toDp() },
            )

            .shadow(elevation = 6.dp)
            .background(Color(0xFF1B1B1F))
            .border(
                width = if (isInteracting) 2.dp else 1.dp,
                color = if (isInteracting) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color(0xFF2E2E33)
                },
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(
                        if (isInteracting) Color(0xFF2A2640) else Color(0xFF26262C)
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.kind.displayName,
                    color = Color(0xFFE6E6EA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                )
                IconButton(
                    onClick = {
                        mode = if (mode == InteractionMode.Normal) {
                            InteractionMode.Interact
                        } else {
                            InteractionMode.Normal
                        }
                    },
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        imageVector = if (isInteracting) {
                            Icons.Filled.OpenWith
                        } else {
                            Icons.Filled.DataThresholding
                        },
                        contentDescription = if (isInteracting) {
                            "Exit interaction mode"
                        } else {
                            "Enter interaction mode"
                        },
                        tint = if (isInteracting) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color(0xFFE6E6EA)
                        },
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove model",
                        tint = Color(0xFFE6E6EA),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()

                    .pointerInput(mode) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (mode == InteractionMode.Normal) {
                                val newWidth =
                                    (widthPx * zoom).coerceIn(minSizePx, maxSizePx)
                                val newHeight =
                                    (heightPx * zoom).coerceIn(minSizePx, maxSizePx)
                                widthPx = newWidth
                                heightPx = newHeight
                                val maxX = max(0f, bounds.width - newWidth)
                                val maxY = max(0f, bounds.height - newHeight)
                                offsetX = (offsetX + pan.x).coerceIn(0f, maxX)
                                offsetY = (offsetY + pan.y).coerceIn(0f, maxY)
                            } else {
                                val view = modelView
                                if (view != null) {
                                    if (pan.x != 0f || pan.y != 0f) {
                                        view.rotateBy(pan.x, pan.y)
                                    }
                                    if (zoom != 1f) {
                                        view.zoomBy(zoom)
                                    }
                                }
                            }
                        }
                    },
            ) {
                AndroidView(
                    factory = { ctx ->
                        ModelSurfaceView(ctx).also { v ->
                            v.loadModel(entry.kind.assetPath)
                            modelView = v
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
