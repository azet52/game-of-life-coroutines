package com.flatesc.gameoflife.domain.model

import kotlin.math.sqrt

class Grid(private val cells: ByteArray) {

    val size: Int = sqrt(cells.size.toDouble()).toInt()

    fun getCell(x: Int, y: Int): Boolean = cells[y * size + x] == ALIVE

    companion object {
        const val ALIVE: Byte = 1
        const val DEAD: Byte = 0
    }
}

fun Grid.next(): Grid {
    val out = ByteArray(size * size) { index ->
        val y = index / size
        val x = index % size
        val isAlive = applyRules(x, y)
        if (isAlive) Grid.ALIVE else Grid.DEAD
    }
    return Grid(out)
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
