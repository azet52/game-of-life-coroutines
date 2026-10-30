package com.flatesc.gameoflife.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatesc.gameoflife.data.SimulationRepository
import com.flatesc.gameoflife.domain.model.Grid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val repository: SimulationRepository,
) : ViewModel() {

    val gridImage: StateFlow<ImageBitmap?> = repository.grid
        .map { grid -> grid?.toImageBitmap() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val settings = MutableStateFlow(UiState())

    val uiState: StateFlow<UiState> = combine(
        settings, repository.stats, repository.tickRateMs,
    ) { state, stats, rateMs ->
        state.copy(stats = stats, tickRateMs = rateMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private var simulation: Job? = null

    fun onEvent(event: SimulationEvent) {
        when (event) {
            is SimulationEvent.SeedChanged -> settings.update { if (it.isRunning) it else it.copy(seed = event.seed) }
            is SimulationEvent.SizeChanged -> settings.update { if (it.isRunning) it else it.copy(size = event.size) }
            is SimulationEvent.ParallelismChanged -> settings.update { if (it.isRunning) it else it.copy(parallelism = event.parallelism) }
            is SimulationEvent.SetTickRate -> repository.setTickRate(event.rateMs)
            SimulationEvent.Start -> start()
            SimulationEvent.Stop -> stop()
        }
    }

    private fun start() {
        simulation?.cancel()
        val state = settings.value
        simulation = viewModelScope.launch { repository.run(state.seed, state.size, state.parallelism) }
        settings.update { it.copy(isRunning = true) }
    }

    private fun stop() {
        simulation?.cancel()
        settings.update { it.copy(isRunning = false) }
    }
}

private fun Grid.toImageBitmap(): ImageBitmap {
    val pixels = IntArray(size * size) { i -> if (getCell(i % size, i / size)) ALIVE_COLOR else DEAD_COLOR }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
}

private const val DEAD_COLOR = 0xFFFFFFFF.toInt()
private const val ALIVE_COLOR = 0xFF101010.toInt()
