package com.deepseek.monitor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.monitor.background.RefreshScheduler
import com.deepseek.monitor.domain.model.AppConfig
import com.deepseek.monitor.domain.repository.ConfigRepository
import com.deepseek.monitor.domain.usecase.SaveApiKeyUseCase
import com.deepseek.monitor.domain.usecase.SaveUsageTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页 UI 状态。
 */
data class SettingsUiState(
    val config: AppConfig = AppConfig(
        apiKeyConfigured = false,
        apiKeyPreview = null,
        usageTokenConfigured = false,
        themeMode = "auto",
        refreshIntervalSeconds = 60,
        autoRefreshEnabled = false
    ),

    // API Key 输入
    val apiKeyInput: String = "",
    val apiKeySaving: Boolean = false,
    val apiKeyFeedback: String? = null,     // 成功/错误提示
    val apiKeyError: Boolean = false,

    // 用量 Token 输入
    val usageTokenInput: String = "",
    val usageTokenSaving: Boolean = false,
    val usageTokenFeedback: String? = null,
    val usageTokenError: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val saveUsageTokenUseCase: SaveUsageTokenUseCase,
    private val refreshScheduler: RefreshScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            configRepository.config.collect { config ->
                _uiState.update { it.copy(config = config) }
            }
        }
    }

    // ── API Key ──

    fun onApiKeyInputChanged(value: String) {
        _uiState.update { it.copy(apiKeyInput = value, apiKeyFeedback = null) }
    }

    fun saveApiKey() {
        val key = _uiState.value.apiKeyInput.trim()
        if (key.isEmpty()) {
            _uiState.update { it.copy(
                apiKeyFeedback = "请输入 API Key", apiKeyError = true
            )}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(apiKeySaving = true, apiKeyFeedback = null) }
            try {
                saveApiKeyUseCase(key)
                _uiState.update { it.copy(
                    apiKeySaving = false,
                    apiKeyInput = "",
                    apiKeyFeedback = "验证通过，API Key 已保存",
                    apiKeyError = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    apiKeySaving = false,
                    apiKeyFeedback = e.message ?: "保存失败",
                    apiKeyError = true
                )}
            }
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            configRepository.clearApiKey()
            _uiState.update { it.copy(apiKeyFeedback = null, apiKeyError = false) }
        }
    }

    // ── 用量 Token ──

    fun onUsageTokenInputChanged(value: String) {
        _uiState.update { it.copy(usageTokenInput = value, usageTokenFeedback = null) }
    }

    /**
     * 保存用户手动输入的 Token。
     */
    fun saveUsageToken() {
        saveUsageToken(_uiState.value.usageTokenInput.trim())
    }

    /**
     * 保存指定 Token（供 WebView 自动捕获回调使用，避免状态更新竞态）。
     */
    fun saveUsageToken(token: String) {
        if (token.isEmpty()) {
            _uiState.update { it.copy(
                usageTokenFeedback = "请输入 Token", usageTokenError = true
            )}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(
                usageTokenSaving = true,
                usageTokenFeedback = null,
                usageTokenInput = token
            )}
            try {
                saveUsageTokenUseCase(token)
                _uiState.update { it.copy(
                    usageTokenSaving = false,
                    usageTokenInput = "",
                    usageTokenFeedback = "Token 已保存",
                    usageTokenError = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    usageTokenSaving = false,
                    usageTokenFeedback = e.message ?: "保存失败",
                    usageTokenError = true
                )}
            }
        }
    }

    fun clearUsageToken() {
        viewModelScope.launch {
            configRepository.clearUsageToken()
            _uiState.update { it.copy(usageTokenFeedback = null, usageTokenError = false) }
        }
    }

    // ── 刷新配置 ──

    /**
     * 同时设置刷新间隔和自动刷新开关。
     * 合并为单次协程避免并发写 DataStore 导致读到旧值。
     */
    fun setRefreshConfig(seconds: Int, enabled: Boolean) {
        viewModelScope.launch {
            configRepository.setRefreshInterval(seconds)
            configRepository.setAutoRefreshEnabled(enabled)
            if (enabled) {
                refreshScheduler.schedule(seconds)
            } else {
                refreshScheduler.cancel()
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { configRepository.setThemeMode(mode) }
    }
}
