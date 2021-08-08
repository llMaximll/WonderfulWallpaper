package com.github.llmaximll.wonderfulwallpaper.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.github.llmaximll.wonderfulwallpaper.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    /**
     * Отвечает за состояние [MainActivity]. true - BottomNavigationView виден, false - нет
     */
    private val _mainState = MutableStateFlow(true)
    val mainState = _mainState.asStateFlow()

    fun toggleMainState(state: Boolean? = null) {
        _mainState.value = state ?: !_mainState.value
    }
}