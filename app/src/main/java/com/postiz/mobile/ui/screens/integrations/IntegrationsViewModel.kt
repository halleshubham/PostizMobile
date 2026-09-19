package com.postiz.mobile.ui.screens.integrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.postiz.mobile.data.remote.dto.IntegrationDto
import com.postiz.mobile.data.repository.PostizRepository
import com.postiz.mobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntegrationsViewModel @Inject constructor(
    private val repository: PostizRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<IntegrationDto>>>(Resource.Loading)
    val state: StateFlow<Resource<List<IntegrationDto>>> = _state.asStateFlow()

    private val _disconnectError = MutableStateFlow<String?>(null)
    val disconnectError: StateFlow<String?> = _disconnectError.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = Resource.Loading
            _state.value = repository.getIntegrations()
        }
    }

    fun disconnect(id: String) {
        viewModelScope.launch {
            when (val result = repository.deleteIntegration(id)) {
                is Resource.Error -> _disconnectError.value = result.message
                else -> load()
            }
        }
    }

    fun dismissDisconnectError() {
        _disconnectError.value = null
    }
}
