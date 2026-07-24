package com.deepseek.monitor.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.deepseek.monitor.presentation.theme.LightColors

/**
 * API Key 配置区域。
 * 输入框 + 保存/清除按钮 + 验证状态反馈。
 */
@Composable
fun ApiKeySection(
    configured: Boolean,
    preview: String?,
    input: String,
    saving: Boolean,
    feedback: String?,
    isError: Boolean,
    onInputChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showKey by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        SectionHeader(icon = "🔑", title = "API Key")

        Spacer(modifier = Modifier.height(12.dp))

        if (configured) {
            // 已配置状态：显示预览 + 状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已配置",
                    tint = LightColors.success,
                    modifier = Modifier.height(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = preview ?: "已保存",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClear,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("清除")
            }
        }

        // 输入框（始终显示）
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = input,
            onValueChange = onInputChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("粘贴 API Key（sk-xxx...）") },
            singleLine = true,
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (input.isNotBlank()) onSave()
            }),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showKey) "隐藏" else "显示"
                    )
                }
            },
            shape = RoundedCornerShape(10.dp)
        )

        // 保存按钮
        if (input.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSave,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColors.primary)
            ) {
                Text(if (saving) "验证中..." else "验证并保存")
            }
        }

        // 反馈信息
        if (feedback != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) LightColors.error else LightColors.success
            )
        }
    }
}

/** 区域小标题 */
@Composable
fun SectionHeader(icon: String, title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$icon  $title",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
