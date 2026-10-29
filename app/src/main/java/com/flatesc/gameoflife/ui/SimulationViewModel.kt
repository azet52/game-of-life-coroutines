package com.flatesc.gameoflife.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatesc.gameoflife.data.SimulationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val repository: SimulationRepository,
) : ViewModel() {

    val stats: StateFlow<SimulationRepository.Stats> = repository.stats

    private var simulation: Job? = null

    fun onEvent(event: SimulationEvent) {
        when (event) {
            is SimulationEvent.Start -> start(event.seed)
            SimulationEvent.Stop -> stop()
        }
    }

    private fun start(seed: String) {
        simulation?.cancel()
        simulation = viewModelScope.launch { repository.run(seed) }
    }

    private fun stop() {
        simulation?.cancel()
    }
}
