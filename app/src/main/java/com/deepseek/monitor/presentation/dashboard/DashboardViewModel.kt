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
            _uiState.update { it.copy(currentMonth = now.monthValue, currentYear = now.year) }
            refreshInternal(checkConfig = true)
        }

        // 监听从设置页配置凭据后自动刷新
        viewModelScope.launch {
            configRepository.config.drop(1).collectLatest { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch { refreshInternal(checkConfig = false) }
    }

    /**
     * 统一刷新入口。checkConfig=true 时先检查凭据状态：
     * 无凭据 → 直接 Ok（显示 "-"），有凭据 → 发起网络请求。
     * checkConfig=false 时用户主动触发，始终发起请求。
     */
    private suspend fun refreshInternal(checkConfig: Boolean) {
        val config = configRepository.config.first()
        if (checkConfig && !config.apiKeyConfigured && !config.usageTokenConfigured) {
            _uiState.update { it.copy(
                balanceState = DataState.Ok,
                usageState = DataState.Ok,
                isRefreshing = false
            )}
            return
        }
        _uiState.update { it.copy(
            isRefreshing = true,
            balanceState = DataState.Loading,
            usageState = DataState.Loading
        )}
        doRefresh()
    }

    private suspend fun doRefresh() {
        val result = refreshAllUseCase()

        _uiState.update { it.copy(
            balance = result.balance ?: it.balance,
            usageResult = result.usage ?: it.usageResult,
            balanceState = when {
                result.balance != null -> DataState.Ok
                result.balanceError != null -> DataState.Error(result.balanceError)
                else -> DataState.Ok
            },
            usageState = when {
                result.usage != null -> DataState.Ok
                result.usageError != null -> DataState.Error(result.usageError)
                else -> DataState.Ok
            },
            isRefreshing = false
        )}
    }
}
