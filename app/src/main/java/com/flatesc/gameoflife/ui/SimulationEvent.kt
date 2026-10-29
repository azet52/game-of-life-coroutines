package com.flatesc.gameoflife.ui

sealed interface SimulationEvent {
    data class Start(val seed: String, val size: Int, val parallelism: Int) : SimulationEvent
    data object Stop : SimulationEvent
    data class SetTickRate(val rateMs: Int) : SimulationEvent
}
