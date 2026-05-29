package com.example.androiddevelopertask.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevelopertask.model.ModelEntry
import kotlin.math.max

@Composable
fun SceneScreen() {
    val entries = remember { mutableStateListOf<ModelEntry>() }
    var nextId by remember { mutableLongStateOf(1L) }
    var pickerOpen by remember { mutableStateOf(false) }
    var sceneSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101015))
            .onSizeChanged { sceneSize = it },
    ) {

        entries.forEach { entry ->
            key(entry.id) {
                ModelContainer(
                    entry = entry,
                    bounds = sceneSize,
                    onClose = { entries.removeAll { it.id == entry.id } },
                )
            }
        }

        if (entries.isEmpty()) {
            EmptySceneHint(modifier = Modifier.align(Alignment.Center))
        }

        ExtendedFloatingActionButton(
            onClick = { pickerOpen = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Add model") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )

        if (entries.isNotEmpty()) {
            ModelCountHud(
                count = entries.size,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp),
            )
        }

        if (pickerOpen) {
            ModelPickerDialog(
                onPick = { kind ->
                    pickerOpen = false
                    val (initX, initY) = nextSpawnPoint(
                        existing = entries.size,
                        bounds = sceneSize,
                        containerSizePx = with(density) { 220.dp.toPx() },
                    )
                    entries.add(
                        ModelEntry(
                            id = nextId,
                            kind = kind,
                            initialX = initX,
                            initialY = initY,
                        )
                    )
                    nextId += 1
                },
                onDismiss = { pickerOpen = false },
            )
        }
    }
}


private fun nextSpawnPoint(
    existing: Int,
    bounds: IntSize,
    containerSizePx: Float,
): Pair<Float, Float> {
    if (bounds.width == 0 || bounds.height == 0) return 24f to 24f
    val maxX = max(0f, bounds.width - containerSizePx)
    val maxY = max(0f, bounds.height - containerSizePx)
    val step = 36f
    val xs = (existing * step) % (maxX.coerceAtLeast(1f))
    val ys = (existing * step) % (maxY.coerceAtLeast(1f))
    return xs to ys
}

@Composable
private fun EmptySceneHint(modifier: Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "No models yet",
            color = Color(0xFFE6E6EA),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Tap “Add model” to drop a 3D model onto the scene.",
            color = Color(0xFF9999A2),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ModelCountHud(count: Int, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xCC1F1F22),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$count model${if (count == 1) "" else "s"}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
