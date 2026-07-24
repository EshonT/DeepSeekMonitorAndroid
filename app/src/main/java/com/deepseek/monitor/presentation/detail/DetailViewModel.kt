package com.deepseek.monitor.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.monitor.domain.model.UsageResult
import com.deepseek.monitor.domain.usecase.GetUsageUseCase
import com.deepseek.monitor.presentation.dashboard.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 详情页 UI 状态。
 */
data class DetailUiState(
    val usageState: DataState = DataState.Idle,
    val usageResult: UsageResult? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getUsageUseCase: GetUsageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(model: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(usageState = DataState.Loading) }
            try {
                val result = getUsageUseCase()
                _uiState.update { it.copy(
                    usageState = DataState.Ok,
                    usageResult = result
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    usageState = DataState.Error(e.message ?: "加载失败")
                )}
            }
        }
    }
}
