package com.flatesc.gameoflife.data

import com.flatesc.gameoflife.domain.model.Grid
import com.flatesc.gameoflife.domain.model.next
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimulationRepository @Inject constructor(private val tickSource: TickSource) {

    data class Stats(val generation: Long, val population: Int)

    private val _grid = MutableStateFlow<Grid?>(null)
    val grid: StateFlow<Grid?> = _grid.asStateFlow()

    private val _stats = MutableStateFlow(Stats(generation = 0, population = 0))
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    val tickRateMs: StateFlow<Int> = tickSource.rate

    fun setTickRate(rateMs: Int) = tickSource.overclock(rateMs)

    suspend fun run(seed: String, size: Int = DEFAULT_SIZE) = withContext(Dispatchers.Default) {
        var generation = 0L
        var grid = Grid.fromSeed(seed, size)

        _grid.value = grid
        _stats.value = Stats(generation, grid.population)

        tickSource.ticks.collect {
            grid = grid.next()
            generation++
            _grid.value = grid
            _stats.value = Stats(generation, grid.population)
        }
    }

    companion object {
        private const val DEFAULT_SIZE = 512
    }
}
