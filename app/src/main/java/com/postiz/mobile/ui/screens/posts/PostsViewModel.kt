package com.postiz.mobile.ui.screens.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.postiz.mobile.data.remote.dto.CustomerDto
import com.postiz.mobile.data.remote.dto.PostDto
import com.postiz.mobile.data.repository.PostizRepository
import com.postiz.mobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostizRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<PostDto>>>(Resource.Loading)
    val state: StateFlow<Resource<List<PostDto>>> = _state.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /** Brands = distinct customer groups across all connected channels, same concept as the web app's brand filter. */
    private val _brands = MutableStateFlow<List<CustomerDto>>(emptyList())
    val brands: StateFlow<List<CustomerDto>> = _brands.asStateFlow()

    private val _selectedBrandId = MutableStateFlow<String?>(null)
    val selectedBrandId: StateFlow<String?> = _selectedBrandId.asStateFlow()

    init {
        loadBrands()
        load()
    }

    private fun loadBrands() {
        viewModelScope.launch {
            val result = repository.getIntegrations()
            if (result is Resource.Success) {
                _brands.value = result.data
                    .mapNotNull { it.customer }
                    .distinctBy { it.id }
                    .sortedBy { it.name }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = Resource.Loading
            _state.value = repository.getPosts(customer = _selectedBrandId.value)
        }
    }

    /** null selects "All brands". Filtering happens server-side (GET /posts?customer=), since
     *  the post list response doesn't carry enough integration detail to filter client-side. */
    fun selectBrand(id: String?) {
        if (_selectedBrandId.value == id) return
        _selectedBrandId.value = id
        load()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /** Moves the visible week strip by 7 days without losing which weekday is selected. */
    fun shiftWeek(days: Long) {
        _selectedDate.value = _selectedDate.value.plusDays(days)
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.deletePost(id)
            load()
        }
    }
}
