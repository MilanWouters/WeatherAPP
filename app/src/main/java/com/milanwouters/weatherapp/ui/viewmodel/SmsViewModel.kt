package com.milanwouters.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milanwouters.weatherapp.domain.model.SmsDirection
import com.milanwouters.weatherapp.domain.model.SmsMessage
import com.milanwouters.weatherapp.domain.repository.SmsRepository
import com.milanwouters.weatherapp.domain.usecase.ProcessSmsCommandUseCase
import com.milanwouters.weatherapp.util.SeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsUiState(
    val allMessages: List<SmsMessage> = emptyList(),
    val inboundMessages: List<SmsMessage> = emptyList(),
    val outboundMessages: List<SmsMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val lastReply: String = ""
)

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val processSmsCommandUseCase: ProcessSmsCommandUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsUiState())
    val uiState: StateFlow<SmsUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        loadSeedSmsIfEmpty()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            smsRepository.getAllMessages().collect { messages ->
                _uiState.update { it.copy(
                    allMessages = messages,
                    inboundMessages = messages.filter { m -> m.direction == SmsDirection.INBOUND },
                    outboundMessages = messages.filter { m -> m.direction == SmsDirection.OUTBOUND }
                )}
            }
        }
    }

    private fun loadSeedSmsIfEmpty() {
        viewModelScope.launch {
            val recent = smsRepository.getRecentMessages(1)
            if (recent.isEmpty()) {
                // Load seed inbound messages
                SeedData.sampleInboundSms.forEach { smsRepository.saveMessage(it) }
            }
        }
    }

    fun simulateIncomingSms(phoneNumber: String, message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val reply = processSmsCommandUseCase(phoneNumber, message)
            _uiState.update { it.copy(isProcessing = false, lastReply = reply) }
        }
    }
}
