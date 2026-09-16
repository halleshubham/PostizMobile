package com.postiz.mobile.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.postiz.mobile.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Drives the top-level "which screen set are we in" decision: Connect
 * screen vs the main app. Because it reads straight from SessionManager's
 * Flow, saving/clearing a session anywhere in the app automatically flips
 * this and the UI recomposes -- no manual nav calls needed for that jump.
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {
    // null = still loading the initial value from DataStore
    val hasSession: StateFlow<Boolean?> = sessionManager.sessionFlow
        .map { it.isValid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
