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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flatesc.gameoflife.data.SimulationRepository.Stats

@Composable
fun SimulationView(
    modifier: Modifier = Modifier,
    viewModel: SimulationViewModel = hiltViewModel(),
) {
    val gridImage by viewModel.gridImage.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()

    SimulationScreen(
        gridImage = gridImage,
        stats = stats,
        isRunning = isRunning,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun SimulationScreen(
    gridImage: ImageBitmap?,
    stats: Stats,
    isRunning: Boolean,
    onEvent: (SimulationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var seed by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (gridImage != null) {
            GridCanvas(gridImage, Modifier.fillMaxWidth())
        } else {
            TitlePlaceholder(Modifier.fillMaxWidth())
        }
        Row(Modifier.fillMaxWidth()) {
            Stat("Generation", stats.generation.toString(), Modifier.weight(1f))
            Stat("Population", stats.population.toString(), Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = seed,
                onValueChange = { seed = it },
                label = { Text("Seed") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { onEvent(if (isRunning) SimulationEvent.Stop else SimulationEvent.Start(seed)) }) {
                Text(if (isRunning) "Stop" else "Start")
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
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
        )
    }
}
