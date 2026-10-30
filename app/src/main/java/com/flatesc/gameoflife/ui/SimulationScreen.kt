package com.flatesc.gameoflife.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun SimulationView(
    modifier: Modifier = Modifier,
    viewModel: SimulationViewModel = hiltViewModel(),
) {
    val gridImage by viewModel.gridImage.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SimulationScreen(
        gridImage = gridImage,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun SimulationScreen(
    gridImage: ImageBitmap?,
    uiState: UiState,
    onEvent: (SimulationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats = uiState.stats

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
        Spacer(Modifier.height(SECTION_GAP * 2))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Tick: ${uiState.tickRateMs} ms", Modifier.weight(SMALLER_ITEM_WEIGHT))
            Slider(
                modifier = Modifier.weight(BIGGER_ITEM_WEIGHT),
                value = tickRateToPosition(uiState.tickRateMs),
                onValueChange = { onEvent(SimulationEvent.SetTickRate(positionToTickRate(it))) },
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Size: ${uiState.size}", Modifier.weight(SMALLER_ITEM_WEIGHT))
            Slider(
                modifier = Modifier.weight(BIGGER_ITEM_WEIGHT),
                value = GRID_SIZES.indexOf(uiState.size).toFloat(),
                onValueChange = { onEvent(SimulationEvent.SizeChanged(GRID_SIZES[it.roundToInt()])) },
                valueRange = 0f..GRID_SIZES.lastIndex.toFloat(),
                steps = GRID_SIZES.size - 2,
                enabled = !uiState.isRunning,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Parallelism: ${uiState.parallelism}", Modifier.weight(SMALLER_ITEM_WEIGHT))
            Slider(
                modifier = Modifier.weight(BIGGER_ITEM_WEIGHT),
                value = uiState.parallelism.toFloat(),
                onValueChange = { onEvent(SimulationEvent.ParallelismChanged(it.roundToInt())) },
                valueRange = MIN_PARALLELISM.toFloat()..MAX_PARALLELISM.toFloat(),
                steps = MAX_PARALLELISM - MIN_PARALLELISM - 1,
                enabled = !uiState.isRunning,
            )
        }
        Spacer(Modifier.height(SECTION_GAP * 2))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(BIGGER_ITEM_WEIGHT),
                value = uiState.seed,
                onValueChange = { onEvent(SimulationEvent.SeedChanged(it)) },
                label = { Text("Seed") },
                singleLine = true,
                enabled = !uiState.isRunning,
            )
            Button(
                modifier = Modifier.weight(SMALLER_ITEM_WEIGHT).padding(start = 12.dp).height(56.dp),
                onClick = { onEvent(if (uiState.isRunning) SimulationEvent.Stop else SimulationEvent.Start) },
                colors = if (uiState.isRunning) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                },
                contentPadding = PaddingValues(horizontal = 32.dp),
            ) {
                Text(
                    text = if (uiState.isRunning) "Stop" else "Start",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "John Conway's",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Game of Life",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "as producer-consumer problem",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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

private val SECTION_GAP = 20.dp

private const val SMALLER_ITEM_WEIGHT = 1f
private const val BIGGER_ITEM_WEIGHT = 2f

private const val MIN_PARALLELISM = 1
private const val MAX_PARALLELISM = 4
