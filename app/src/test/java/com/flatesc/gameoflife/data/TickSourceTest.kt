package com.flatesc.gameoflife.data

import android.os.SystemClock
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class TickSourceTest {

    @Before
    fun setUp() {
        mockkStatic(SystemClock::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(SystemClock::class)
    }

    private fun TestScope.tickSource(): TickSource {
        every { SystemClock.elapsedRealtime() } answers { testScheduler.currentTime }
        return TickSource(backgroundScope)
    }

    @Test
    fun `first tick appears only after 100ms`() = runTest {
        val source = tickSource()
        val ticks = mutableListOf<Long>()
        backgroundScope.launch { source.ticks.toList(ticks) }

        advanceTimeBy(99.milliseconds)
        runCurrent()
        assertEquals(emptyList<Long>(), ticks)

        advanceTimeBy(1.milliseconds)
        runCurrent()
        assertEquals(listOf(0L), ticks)
    }

    @Test
    fun `ticks at steady 100ms rate`() = runTest {
        val source = tickSource()
        val ticks = mutableListOf<Long>()
        backgroundScope.launch { source.ticks.toList(ticks) }

        advanceTimeBy(1_000.milliseconds); runCurrent()

        assertEquals((0L..9L).toList(), ticks)
    }

    @Test
    fun `slow consumer does not delay the ticker`() = runTest {
        val source = tickSource()
        val fast = mutableListOf<Long>()
        val slow = mutableListOf<Long>()
        backgroundScope.launch { source.ticks.toList(fast) }
        backgroundScope.launch {
            source.ticks.collect {
                slow += it
                delay(230.milliseconds)
            }
        }

        advanceTimeBy(1_000.milliseconds)
        runCurrent()

        // ticker kept its schedule regardless of the slow collector
        assertEquals((0L..9L).toList(), fast)
        // slow collector is free at 330, 560, 790 and gets only the latest tick each time
        assertEquals(listOf(0L, 2L, 4L, 6L), slow)
    }

    @Test
    fun `overclock speeds up and slows down ticks`() = runTest {
        val source = tickSource()
        val tickTimes = mutableListOf<Long>()
        backgroundScope.launch { source.ticks.collect { tickTimes += testScheduler.currentTime } }

        advanceTimeBy(125.milliseconds)
        source.overclock(50)
        advanceTimeBy(175.milliseconds)
        source.overclock(200)
        advanceTimeBy(400.milliseconds)
        runCurrent()

        assertEquals(listOf(100L, 175L, 225L, 275L, 500L, 700L), tickTimes)
    }

    @Test
    fun `overclock does not reset the counter`() = runTest {
        val source = tickSource()
        val ticks = mutableListOf<Long>()
        backgroundScope.launch { source.ticks.toList(ticks) }

        advanceTimeBy(250.milliseconds)
        source.overclock(50)
        advanceTimeBy(50.milliseconds)
        runCurrent()

        assertEquals(listOf(0L, 1L, 2L), ticks)
    }

    @Test
    fun `overclock before subscription applies on first collect`() = runTest {
        val source = tickSource()
        source.overclock(50)
        val ticks = mutableListOf<Long>()
        backgroundScope.launch { source.ticks.toList(ticks) }

        advanceTimeBy(49.milliseconds)
        runCurrent()
        assertEquals(emptyList<Long>(), ticks)

        advanceTimeBy(1.milliseconds)
        runCurrent()
        assertEquals(listOf(0L), ticks)
    }
}
