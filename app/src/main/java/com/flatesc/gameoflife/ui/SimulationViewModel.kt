package com.flatesc.gameoflife.ui

import androidx.lifecycle.ViewModel
import com.flatesc.gameoflife.data.SimulationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val repository: SimulationRepository,
) : ViewModel()
