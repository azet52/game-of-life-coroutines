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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    val stats: StateFlow<SimulationRepository.Stats> = repository.stats

    val tickRateMs: StateFlow<Int> = repository.tickRateMs

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var simulation: Job? = null

    fun onEvent(event: SimulationEvent) {
        when (event) {
            is SimulationEvent.Start -> start(event.seed, event.size)
            SimulationEvent.Stop -> stop()
            is SimulationEvent.SetTickRate -> repository.setTickRate(event.rateMs)
        }
    }

    private fun start(seed: String, size: Int) {
        simulation?.cancel()
        // default empty seed stabilizes around generation 1400
        // cool glider visible top of the screen at around generation 680
        simulation = viewModelScope.launch { repository.run(seed, size) }
        _isRunning.value = true
    }

    private fun stop() {
        simulation?.cancel()
        _isRunning.value = false
    }
}

private fun Grid.toImageBitmap(): ImageBitmap {
    val pixels = IntArray(size * size) { i -> if (getCell(i % size, i / size)) ALIVE_COLOR else DEAD_COLOR }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
}

private const val DEAD_COLOR = 0xFFFFFFFF.toInt()
private const val ALIVE_COLOR = 0xFF101010.toInt()
