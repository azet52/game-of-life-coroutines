package com.flatesc.gameoflife.data

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class TickSource(scope: CoroutineScope) {
    private val rateMs = MutableStateFlow(INITIAL_RATE_MS)

    // to overflow Long with 1 tick/ms it would take about 292 million years
    private var tickNumber = 0L

    val ticks: Flow<Long> = rateMs
        .flatMapLatest { period -> tickFlow(period) }
        .shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 0)
        // slow consumers should not delay the ticker
        .buffer(1, BufferOverflow.DROP_OLDEST)

    fun overclock(rateMs: Int) {
        this@TickSource.rateMs.value = rateMs
    }

    private fun tickFlow(periodMs: Int): Flow<Long> = flow {
        var next = SystemClock.elapsedRealtime()
        while (true) {
            next += periodMs
            delay((next - SystemClock.elapsedRealtime()).milliseconds)
            emit(tickNumber++)
        }
    }

    companion object {
        private const val INITIAL_RATE_MS = 100
    }
}