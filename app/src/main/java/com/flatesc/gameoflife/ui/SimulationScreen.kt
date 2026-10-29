package com.flatesc.gameoflife.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flatesc.gameoflife.data.SimulationRepository.Stats
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun SimulationView(
    modifier: Modifier = Modifier,
    viewModel: SimulationViewModel = hiltViewModel(),
) {
    val gridImage by viewModel.gridImage.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val tickRateMs by viewModel.tickRateMs.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()

    SimulationScreen(
        gridImage = gridImage,
        stats = stats,
        tickRateMs = tickRateMs,
        isRunning = isRunning,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun SimulationScreen(
    gridImage: ImageBitmap?,
    stats: Stats,
    tickRateMs: Int,
    isRunning: Boolean,
    onEvent: (SimulationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var seed by remember { mutableStateOf("") }
    var sizeIndex by remember { mutableIntStateOf(GRID_SIZES.indexOf(DEFAULT_GRID_SIZE)) }
    val size = GRID_SIZES[sizeIndex]

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (gridImage != null) {
            GridCanvas(gridImage, Modifier.fillMaxWidth())
        } else {
            TitlePlaceholder(Modifier.fillMaxWidth())
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("Generation", stats.generation.toString())
            Stat("Population", stats.population.toString())
            Stat("Tick loss", stats.dropped.toString())
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = seed,
                onValueChange = { seed = it },
                label = { Text("Seed") },
                singleLine = true,
            )
            Button(onClick = { onEvent(if (isRunning) SimulationEvent.Stop else SimulationEvent.Start(seed, size)) }) {
                Text(if (isRunning) "Stop" else "Start")
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Tick: $tickRateMs ms")
            Slider(
                modifier = Modifier.weight(1f),
                value = tickRateToPosition(tickRateMs),
                onValueChange = { onEvent(SimulationEvent.SetTickRate(positionToTickRate(it))) },
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Size: $size")
            Slider(
                modifier = Modifier.weight(1f),
                value = sizeIndex.toFloat(),
                onValueChange = { sizeIndex = it.roundToInt() },
                valueRange = 0f..GRID_SIZES.lastIndex.toFloat(),
                steps = GRID_SIZES.size - 2,
                enabled = !isRunning,
            )
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TitlePlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Text(
            text = "Game of Life",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GridCanvas(image: ImageBitmap, modifier: Modifier = Modifier) {
    Canvas(modifier.aspectRatio(1f)) {
        drawImage(
            image = image,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            filterQuality = FilterQuality.None
        )
    }
}

private fun positionToTickRate(position: Float): Int =
    (MIN_TICK_RATE_MS * (MAX_TICK_RATE_MS / MIN_TICK_RATE_MS).pow(position)).roundToInt()

private fun tickRateToPosition(rateMs: Int): Float =
    ln(rateMs / MIN_TICK_RATE_MS) / ln(MAX_TICK_RATE_MS / MIN_TICK_RATE_MS)

private const val MIN_TICK_RATE_MS = 1f
private const val MAX_TICK_RATE_MS = 1000f

private val GRID_SIZES = listOf(16, 32, 64, 128, 256, 512, 1024, 2048)
private const val DEFAULT_GRID_SIZE = 128
