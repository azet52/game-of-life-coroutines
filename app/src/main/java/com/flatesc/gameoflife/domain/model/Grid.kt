package com.flatesc.gameoflife.domain.model

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sqrt

class Grid(private val cells: ByteArray) {

    val size: Int = sqrt(cells.size.toDouble()).toInt()

    val population: Int get() = cells.count { it == ALIVE }

    fun getCell(x: Int, y: Int): Boolean = cells[y * size + x] == ALIVE

    companion object {
        const val ALIVE: Byte = 1
        const val DEAD: Byte = 0

        fun fromSeed(seed: String, size: Int): Grid {
            val random = Random(seed.hashCode().toLong())
            return Grid(ByteArray(size * size) { if (random.nextBoolean()) ALIVE else DEAD })
        }
    }
}

suspend fun Grid.next(chunks: Int = 1): Grid = coroutineScope {
    val out = ByteArray(size * size)
    val rowsPerChunk = (size + chunks - 1) / chunks
    (0 until size step rowsPerChunk).map { startRow ->
        launch {
            for (y in startRow until minOf(startRow + rowsPerChunk, size)) {
                for (x in 0 until size) {
                    out[y * size + x] = if (applyRules(x, y)) Grid.ALIVE else Grid.DEAD
                }
            }
        }
    }.joinAll()
    Grid(out)
}

// live cells with two or three neighbors stay alive, otherwise die
// dead cells with exactly three neighbors become alive.
private fun Grid.applyRules(x: Int, y: Int): Boolean {
    val neighbours = countAliveNeighbors(x, y)
    return if (getCell(x, y)) neighbours == 2 || neighbours == 3 else neighbours == 3
}

private fun Grid.countAliveNeighbors(x: Int, y: Int): Int =
    OFFSET_MASK.count { (offsetY, offsetX) ->
        val neighborX = x + offsetX
        val neighborY = y + offsetY
        val insideGrid = neighborX in 0..<size && neighborY in 0..<size
        insideGrid && getCell(neighborX, neighborY)
    }

private val OFFSET_MASK = listOf(
    -1 to -1, -1 to 0, -1 to 1,
    0 to -1, /* 0-0 */ 0 to 1,
    1 to -1, 1 to 0, 1 to 1,
)
