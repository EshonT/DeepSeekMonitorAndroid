package com.deepseek.monitor.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepseek.monitor.presentation.theme.LightColors
import com.deepseek.monitor.presentation.theme.LocalEInkMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCaptureDialog by remember { mutableStateOf(false) }
    var showManualInput by remember { mutableStateOf(false) }
    var showClearApiKeyDialog by remember { mutableStateOf(false) }
    var showClearTokenDialog by remember { mutableStateOf(false) }
    val eink = LocalEInkMode.current

    val config = state.config
    val apiKeyInput = state.apiKeyInput
    val usageTokenInput = state.usageTokenInput
    val usageTokenSaving = state.usageTokenSaving
    val usageTokenFeedback = state.usageTokenFeedback
    val usageTokenError = state.usageTokenError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // ── 显示与刷新 ──
            SectionTitle("显示与刷新")
            Spacer(modifier = Modifier.height(8.dp))

            // 主题行
            ThemePicker(current = config.themeMode, onSelect = viewModel::setThemeMode)
            Spacer(modifier = Modifier.height(14.dp))

            // 自动刷新 — 滑块五档（最左=关闭）
            RefreshIntervalSlider(
                current = config.refreshIntervalSeconds,
                enabled = config.autoRefreshEnabled,
                onSelect = { seconds, on ->
                    viewModel.setRefreshInterval(seconds)
                    viewModel.setAutoRefresh(on)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            // ── API Key ──
            SectionTitle("API Key")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (config.apiKeyConfigured) "✓" else "✗",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    config.apiKeyPreview ?: "未配置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (config.apiKeyConfigured) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                if (config.apiKeyConfigured) {
                    TextButton(text = "清除", eink = eink, onClick = { showClearApiKeyDialog = true })
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = viewModel::onApiKeyInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("粘贴 API Key（sk-xxx...）") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            if (apiKeyInput.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = if (state.apiKeySaving) "验证中..." else "验证并保存",
                    onClick = viewModel::saveApiKey,
                    enabled = !state.apiKeySaving,
                    eink = eink
                )
            }
            val ak = state.apiKeyFeedback
            if (ak != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(ak, style = MaterialTheme.typography.bodySmall,
                    color = if (state.apiKeyError) LightColors.error else LightColors.success)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            // ── 用量同步 ──
            SectionTitle("用量同步")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (config.usageTokenConfigured) "✓" else "✗",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (config.usageTokenConfigured) "已配置" else "未配置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(text = "网页登录", eink = eink, onClick = { showCaptureDialog = true })
                Spacer(modifier = Modifier.width(4.dp))
                if (config.usageTokenConfigured) {
                    TextButton(text = "清除", eink = eink, onClick = { showClearTokenDialog = true })
                }
            }

            // 手动粘贴（可折叠）
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (showManualInput) "▾ 手动粘贴" else "▸ 手动粘贴",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.clickable { showManualInput = !showManualInput }.padding(vertical = 4.dp)
            )
            if (showManualInput) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = usageTokenInput,
                    onValueChange = viewModel::onUsageTokenInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("粘贴用量 Token") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                if (usageTokenInput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PrimaryButton("保存 Token", viewModel::saveUsageToken, !usageTokenSaving, eink)
                }
                val fb = usageTokenFeedback
                if (fb != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(fb, style = MaterialTheme.typography.bodySmall,
                        color = if (usageTokenError) LightColors.error else LightColors.success)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            Text("v1.0.0", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showCaptureDialog) {
        LaunchedEffect(Unit) { viewModel.onUsageTokenInputChanged("") }
        TokenCaptureDialog(
            onTokenCaptured = { viewModel.onUsageTokenInputChanged(it) },
            onDismiss = { showCaptureDialog = false }
        )
    }

    // 清除 API Key 确认
    if (showClearApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showClearApiKeyDialog = false },
            title = { Text("清除 API Key") },
            text = { Text("确定要清除已保存的 API Key 吗？") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.clearApiKey()
                    showClearApiKeyDialog = false
                }) { Text("确定", color = LightColors.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearApiKeyDialog = false }) { Text("取消") }
            }
        )
    }

    // 清除 Token 确认
    if (showClearTokenDialog) {
        AlertDialog(
            onDismissRequest = { showClearTokenDialog = false },
            title = { Text("清除用量 Token") },
            text = { Text("确定要清除已保存的用量 Token 吗？") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.clearUsageToken()
                    showClearTokenDialog = false
                }) { Text("确定", color = LightColors.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearTokenDialog = false }) { Text("取消") }
            }
        )
    }
}

// ── 可复用小组件 ──

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, eink: Boolean = false) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().then(
            if (eink) Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp)) else Modifier
        ),
        shape = RoundedCornerShape(10.dp),
        colors = if (eink) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors(containerColor = LightColors.primary)
    ) {
        Text(text)
    }
}

@Composable
private fun TextButton(text: String, eink: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.then(
            if (eink) Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(6.dp)) else Modifier
        ),
        shape = RoundedCornerShape(6.dp),
        colors = if (eink) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ThemePicker(current: String, onSelect: (String) -> Unit) {
    val options = listOf("auto" to "跟随系统", "light" to "日间", "dark" to "夜间", "eink" to "水墨屏")
    val currentLabel = options.find { it.first == current }?.second ?: "日间"
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("主题", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.weight(1f))
        Text(currentLabel, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(4.dp))
        Text("›", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("选择主题") },
            text = {
                Column {
                    options.forEach { (value, label) ->
                        val sel = current == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(value); showDialog = false }
                                .padding(vertical = 14.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (sel) "●" else "○",
                                color = if (sel) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun RefreshIntervalSlider(
    current: Int, enabled: Boolean,
    onSelect: (seconds: Int, on: Boolean) -> Unit
) {
    val options = listOf(0 to "关闭", 60 to "1分钟", 300 to "5分钟", 1800 to "30分钟", 3600 to "1小时")
    val idx = if (!enabled) 0 else options.indexOfFirst { it.first == current }.coerceAtLeast(0)
    var sliderPos by remember { mutableStateOf(idx.toFloat()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("自动刷新", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f))
            Text(options[idx].second,
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                color = if (idx == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                else LightColors.primary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 自绘档位滑块
        val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        val dotColor = LightColors.primary
        val inset = 16.dp
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = inset)
                .height(40.dp)
                .pointerInput(options.size) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        val stepW = size.width / (options.size - 1)
                        val i = ((down.position.x + stepW / 2) / stepW).toInt().coerceIn(0, options.size - 1)
                        sliderPos = i.toFloat()
                        val (s, _) = options[i]; onSelect(s.coerceAtLeast(60), i != 0)
                        var rel = false
                        while (!rel) {
                            val ev = awaitPointerEvent()
                            when (ev.type) {
                                PointerEventType.Move -> {
                                    val x = ev.changes.firstOrNull()?.position?.x ?: break
                                    val j = ((x + stepW / 2) / stepW).toInt().coerceIn(0, options.size - 1)
                                    sliderPos = j.toFloat()
                                    val (ss, _) = options[j]; onSelect(ss.coerceAtLeast(60), j != 0)
                                }
                                PointerEventType.Release -> rel = true
                                else -> {}
                            }
                        }
                    }
                }
        ) {
            val w = size.width; val h = size.height
            val steps = options.size
            val stepW = w / (steps - 1)
            val trackY = h * 0.35f; val dotY = trackY
            val trackH = 4.dp.toPx()
            val labelY = h * 0.7f

            // 轨道
            drawRoundRect(trackColor, topLeft = Offset(0f, trackY - trackH / 2),
                size = Size(w, trackH), cornerRadius = CornerRadius(2.dp.toPx()))
            // 已选段
            val fillW = sliderPos * stepW
            drawRoundRect(dotColor, topLeft = Offset(0f, trackY - trackH / 2),
                size = Size(fillW, trackH), cornerRadius = CornerRadius(2.dp.toPx()))

            // 档位圆点 + 标签
            options.forEachIndexed { i, (_, label) ->
                val cx = i * stepW
                drawCircle(if (i <= sliderPos) dotColor else trackColor, radius = 6.dp.toPx(), center = Offset(cx, dotY))
            }
        }

        // 标签行
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = inset)) {
            options.forEachIndexed { i, (_, label) ->
                Text(label,
                    fontSize = 11.sp,
                    color = if (i == idx) LightColors.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    fontWeight = if (i == idx) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f))
            }
        }
    }
}
