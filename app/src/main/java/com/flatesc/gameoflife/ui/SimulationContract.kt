package com.flatesc.gameoflife.ui

import com.flatesc.gameoflife.data.SimulationRepository.Stats

data class UiState(
    val seed: String = "2403",
    val size: Int = 128,
    val parallelism: Int = 1,
    val tickRateMs: Int = 0,
    val isRunning: Boolean = false,
    val stats: Stats = Stats(generation = 0, population = 0, dropped = 0),
)

sealed interface SimulationEvent {
    data class SeedChanged(val seed: String) : SimulationEvent
    data class SizeChanged(val size: Int) : SimulationEvent
    data class ParallelismChanged(val parallelism: Int) : SimulationEvent
    data class SetTickRate(val rateMs: Int) : SimulationEvent
    data object Start : SimulationEvent
    data object Stop : SimulationEvent
}
