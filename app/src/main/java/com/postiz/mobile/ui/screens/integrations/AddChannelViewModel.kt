package com.postiz.mobile.ui.screens.integrations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.postiz.mobile.data.repository.PostizRepository
import com.postiz.mobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddChannelViewModel @Inject constructor(
    private val repository: PostizRepository
) : ViewModel() {

    var connectingId by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Fetches the OAuth URL for this provider; the caller opens it in a browser. */
    fun connect(identifier: String, onUrlReady: (String) -> Unit) {
        viewModelScope.launch {
            connectingId = identifier
            error = null
            when (val result = repository.getSocialConnectUrl(identifier)) {
                is Resource.Success -> onUrlReady(result.data)
                is Resource.Error -> error = result.message
                Resource.Loading -> Unit
            }
            connectingId = null
        }
    }

    fun dismissError() {
        error = null
    }
}
