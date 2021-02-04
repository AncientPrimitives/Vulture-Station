package com.cobox.iot.vulture.auth

/**
 * 认证接口，由认证服务返回
 */
interface Authority {

    /**
     * 认证结果
     */
    data class Result (
        val isGranted: Boolean,
        val token: String
    )

    /**
     * 认证
     */
    fun authority(key: String, secret: String): Result

    /**
     * 认证
     */
    fun authority(token: String): Result

    /**
     * 取消认证
     * 取消认证后当前token失效，再次认证时会重新生成新的token
     */
    fun unauthority(token: String): Boolean

    /**
     * 注册
     */
    fun register(key: String, secret: String): Result

    /**
     * 注销
     */
    fun unregister(token: String): Boolean

}