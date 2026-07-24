package com.deepseek.monitor.domain.model

/**
 * 应用业务异常体系。
 * 网络层通过 [com.deepseek.monitor.data.remote.interceptor.ErrorInterceptor]
 * 抛出 IOException，由 Repository 层转换为下列业务异常后向 ViewModel 抛出。
 */
sealed class AppException(message: String) : Exception(message)

/** API Key 无效或已过期（HTTP 401） */
class ApiKeyInvalidException(message: String = "API Key 无效或已过期") : AppException(message)

/** 请求过于频繁（HTTP 429） */
class RateLimitException(message: String = "请求过于频繁，请稍后再试") : AppException(message)

/** DeepSeek 服务端错误（HTTP 5xx） */
class ServerException(message: String = "DeepSeek 服务器错误") : AppException(message)

/** 用量 Token 无效（HTTP 401） */
class TokenInvalidException(message: String = "用量 Token 无效或已过期，请重新登录") : AppException(message)

/** 未配置凭据 */
class CredentialNotConfiguredException(
    val credentialType: String  // "API Key" | "用量 Token"
) : AppException("未配置${credentialType}")
