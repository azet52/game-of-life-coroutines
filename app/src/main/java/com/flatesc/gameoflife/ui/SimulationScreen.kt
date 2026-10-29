package com.flatesc.gameoflife.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
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

    SimulationScreen(
        gridImage = gridImage,
        stats = stats,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun SimulationScreen(
    gridImage: ImageBitmap?,
    stats: Stats,
    onEvent: (SimulationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var seed by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = seed,
            onValueChange = { seed = it },
            label = { Text("Seed") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(SimulationEvent.Start(seed)) }) { Text("Start") }
            OutlinedButton(onClick = { onEvent(SimulationEvent.Stop) }) { Text("Stop") }
        }
        Text("Generation: ${stats.generation}")
        Text("Population: ${stats.population}")
        gridImage?.let { GridCanvas(it, Modifier.fillMaxWidth()) }
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
