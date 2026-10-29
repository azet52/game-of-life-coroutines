package com.flatesc.gameoflife.ui

sealed interface SimulationEvent {
    data class Start(val seed: String) : SimulationEvent
    data object Stop : SimulationEvent
}
