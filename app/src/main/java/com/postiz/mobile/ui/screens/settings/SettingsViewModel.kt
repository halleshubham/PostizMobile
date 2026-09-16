package com.postiz.mobile.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.postiz.mobile.data.local.Session
import com.postiz.mobile.data.local.SessionManager
import com.postiz.mobile.data.remote.PostizApiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiProvider: PostizApiProvider
) : ViewModel() {

    val session: StateFlow<Session> = sessionManager.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Session())

    fun disconnect() {
        viewModelScope.launch {
            sessionManager.clearSession()
            apiProvider.invalidate()
        }
    }
}
