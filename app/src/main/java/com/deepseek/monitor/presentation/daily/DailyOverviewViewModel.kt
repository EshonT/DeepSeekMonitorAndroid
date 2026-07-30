package com.deepseek.monitor.presentation.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.domain.repository.ConfigRepository
import com.deepseek.monitor.domain.usecase.GetTodayUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 每日详情页 UI 状态。
 */
data class DailyOverviewUiState(
    val todayUsage: UsageDay? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DailyOverviewViewModel @Inject constructor(
    private val getTodayUsage: GetTodayUsageUseCase,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyOverviewUiState())
    val uiState: StateFlow<DailyOverviewUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            // 首次加载
            refresh()
            // 监听配置变化（凭据变更时重新加载）
            configRepository.config.drop(1).collectLatest {
                refresh()
            }
        }
        // 监听自动刷新配置，动态启动/停止定时器
        viewModelScope.launch {
            configRepository.config.collectLatest { config ->
                refreshJob?.cancel()
                if (config.autoRefreshEnabled) {
                    refreshJob = launchAutoRefresh(config.refreshIntervalSeconds)
                }
            }
        }
    }

    /**
     * 重新加载今日用量数据。
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val today = getTodayUsage()
                _uiState.value = _uiState.value.copy(
                    todayUsage = today,
                    isLoading = false,
                    error = if (today == null) "今日暂无用量数据" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    /**
     * 按配置间隔循环刷新。
     * 仅在页面可见时生效（ViewModel scope 内）。
     */
    private fun launchAutoRefresh(intervalSeconds: Int): Job {
        return viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(intervalSeconds * 1000L)
                refresh()
            }
        }
    }
}
