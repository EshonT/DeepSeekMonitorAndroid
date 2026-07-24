package com.deepseek.monitor.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.monitor.domain.model.Balance
import com.deepseek.monitor.domain.model.UsageResult
import com.deepseek.monitor.domain.repository.ConfigRepository
import com.deepseek.monitor.domain.usecase.RefreshAllUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val balanceState: DataState = DataState.Idle,
    val usageState: DataState = DataState.Idle,

    val balance: Balance? = null,
    val usageResult: UsageResult? = null,

    val currentMonth: Int = 0,
    val currentYear: Int = 0,

    val isRefreshing: Boolean = false
)

sealed class DataState {
    data object Idle : DataState()
    data object Loading : DataState()
    data object Ok : DataState()
    data object NoCredential : DataState()
    data class Error(val message: String) : DataState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val refreshAllUseCase: RefreshAllUseCase,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = configRepository.config.first()
            val now = java.time.LocalDate.now()
            _uiState.update { it.copy(
                currentMonth = now.monthValue,
                currentYear = now.year,
                balanceState = if (config.apiKeyConfigured) DataState.Loading else DataState.NoCredential,
                usageState = if (config.usageTokenConfigured) DataState.Loading else DataState.NoCredential
            )}

            if (config.apiKeyConfigured || config.usageTokenConfigured) {
                doRefresh()
            }
        }

        // 监听从设置页配置凭据后自动刷新
        viewModelScope.launch {
            configRepository.config.drop(1).collectLatest { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val config = configRepository.config.first()
            _uiState.update { it.copy(
                isRefreshing = true,
                balanceState = if (config.apiKeyConfigured) DataState.Loading else DataState.NoCredential,
                usageState = if (config.usageTokenConfigured) DataState.Loading else DataState.NoCredential
            )}

            if (config.apiKeyConfigured || config.usageTokenConfigured) {
                doRefresh()
            } else {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private suspend fun doRefresh() {
        val result = refreshAllUseCase()

        _uiState.update { it.copy(
            balance = result.balance ?: it.balance,
            usageResult = result.usage ?: it.usageResult,
            balanceState = when {
                result.balance != null -> DataState.Ok
                result.balanceError != null -> DataState.Error(result.balanceError)
                else -> DataState.NoCredential
            },
            usageState = when {
                result.usage != null -> DataState.Ok
                result.usageError != null -> DataState.Error(result.usageError)
                else -> DataState.NoCredential
            },
            isRefreshing = false
        )}
    }
}
