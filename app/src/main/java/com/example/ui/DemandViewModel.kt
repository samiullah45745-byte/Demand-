package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DemandItem
import com.example.data.DemandRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.network.*
import com.example.BuildConfig

sealed class SlipUiState {
    object Idle : SlipUiState()
    object Loading : SlipUiState()
    data class Success(val slipText: String) : SlipUiState()
    data class Error(val message: String) : SlipUiState()
}

class DemandViewModel(private val repository: DemandRepository) : ViewModel() {

    val uiState: StateFlow<List<DemandItem>> = repository.allItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _slipUiState = MutableStateFlow<SlipUiState>(SlipUiState.Idle)
    val slipUiState: StateFlow<SlipUiState> = _slipUiState

    fun generateSlip(items: List<DemandItem>) {
        if (items.isEmpty()) {
            _slipUiState.value = SlipUiState.Error("Please add some items first.")
            return
        }
        
        _slipUiState.value = SlipUiState.Loading

        viewModelScope.launch {
            try {
                val slipText = withContext(Dispatchers.IO) {
                    val apiKey = BuildConfig.GEMINI_API_KEY
                    
                    val itemsText = items.joinToString("\n") { 
                        "- ${it.name}: ${it.quantity.ifEmpty { "0" }} ${it.unit}" 
                    }
                    
                    val prompt = """
                        Create a professional and clean restaurant demand slip (inventory request) for 'Desi Tadka & Family Restaurant'. 
                        The slip should be in Urdu (with English names in brackets if provided). 
                        Format it nicely with a title, date, and a well-structured list of the following items:
                        
                        $itemsText
                        
                        Only output the formatted slip text, no markdown code blocks.
                    """.trimIndent()
                    
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt))))
                    )
                    
                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No slip generated."
                }
                
                _slipUiState.value = SlipUiState.Success(slipText)
            } catch (e: Exception) {
                _slipUiState.value = SlipUiState.Error(e.message ?: "An error occurred while generating the slip.")
            }
        }
    }
    
    fun dismissSlip() {
        _slipUiState.value = SlipUiState.Idle
    }

    fun initializeWithDefaultsIfEmpty() {
        viewModelScope.launch {
            if (repository.getItemsCount() == 0) {
                val defaultItems = listOf(
                    DemandItem(name = "ٹماٹر (Tomato)", unit = "کلو"),
                    DemandItem(name = "پیاز (Onion)", unit = "کلو"),
                    DemandItem(name = "کالی مرچ (Black Pepper)", unit = "گرام"),
                    DemandItem(name = "سفید مرچ (White Pepper)", unit = "گرام"),
                    DemandItem(name = "سفید زیرہ (White Cumin)", unit = "گرام"),
                    DemandItem(name = "کالا زیرہ (Black Cumin)", unit = "گرام")
                )
                defaultItems.forEach { repository.insert(it) }
            }
        }
    }

    fun addItem(name: String, unit: String) {
        viewModelScope.launch {
            repository.insert(DemandItem(name = name, unit = unit))
        }
    }

    fun updateDemandQuantity(item: DemandItem, newQuantity: String) {
        viewModelScope.launch {
            repository.update(item.copy(quantity = newQuantity))
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}

class DemandViewModelFactory(private val repository: DemandRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DemandViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DemandViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
