package com.cobox.iot.vulture.auth

import com.cobox.iot.vulture.application.Application
import com.cobox.iot.vulture.msgbus.Eventable
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Closeable

/**
 * 认证服务
 */
class AuthorityService(
    private val app: Application,
    private val database: AuthorityDatabase
) : Authority, Closeable, Eventable {

    companion object EventAddress {
        const val AUTH_NAME_SECRET = "/authority/authority/name_secret"
        const val AUTH_TOKEN = "/authority/authority/token"
    }

    /**
     * 认证
     * @param key 用户名
     * @param secret 加密后的密码
     * @return 认证结果
     *         如果认证不通过，则返回的token为""
     */
    override fun authority(key: String, secret: String): Authority.Result {
        database.authority(key, secret).let { token ->
            return Authority.Result(
                isGranted = token.isNotBlank(),
                token = token
            )
        }
    }

    /**
     * 认证
     * @param token 用户认证码
     * @return 认证结果
     *         如果不存在此token，则返回的token为""
     */
    override fun authority(token: String): Authority.Result {
        database.authority(token).let { grantedToken ->
            return Authority.Result(
                isGranted = grantedToken.isNotBlank(),
                token = grantedToken
            )
        }
    }

    /**
     * 取消认证
     * 取消认证后当前token失效，再次认证时会重新生成新的token
     * @param token 用户认证码
     * @return 如果token存在且成功取消认证，返回true
     */
    override fun unauthority(token: String): Boolean =
        database.unauthority(token)

    /**
     * 注册
     * @param username 用户名
     * @param secret 加密后的密码
     * @return 认证结果
     *         如果注册不通过，则返回的token为""，如果用户名已存在则会导致注册不通过
     */
    override fun register(username: String, secret: String): Authority.Result {
        return if (!database.hasUser(username)) {
            database.register(username, secret, calculateToken(username)).let { token ->
                val isGranted = token.isNotBlank()
                if (isGranted) {
                    app.iot.createBusinessFor(username)
                    app.nas.createBusinessFor(username)
                }
                Authority.Result(
                    isGranted = isGranted,
                    token = token
                )
            }
        } else {
            Authority.Result(
                isGranted = false,
                token = ""
            )
        }
    }

    /**
     * 注销
     * @param token 用户认证码
     * @return 如果存在此token，则注销账户信息，同时返回注销成功
     */
    override fun unregister(token: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    /**
     * 计算Token
     * @param key 用户名
     * @return 对应的用户Token
     */
    fun calculateToken(key: String): String
            = "${(key).hashCode()}"

    /**
     * 注册EventBus地址，注册后即可接受其他组件发往此地址的消息
     */
    override fun onRegisterAddress(bus: EventBus) {
        // authority(key: String, secret: String): Authority.Result
        bus.consumer<String>(AUTH_NAME_SECRET) { message: Message<String> ->
            JsonObject(message.body()).apply {
                val key = map["key"] as String
                val secret = map["secret"] as String

                print("[authority] (key: '${key}', secret: '${secret}')")
                GlobalScope.launch(Dispatchers.IO) {
                    authority(key = key, secret = secret).let { result ->
                        message.reply(
                            JsonObject().apply {
                                put("isGranted", "${result.isGranted}")
                                put("token", result.token)
                            }
                        )
                    }
                }
            }
        }

        // authority(token: String): Authority.Result
        bus.consumer<String>(AUTH_TOKEN) { message: Message<String> ->
            JsonObject(message.body()).apply {
                val token = map["token"] as String

                print("[authority] (token: '$token')")
                GlobalScope.launch(Dispatchers.IO) {
                    authority(token = token).let { result ->
                        message.reply(
                            JsonObject().apply {
                                put("isGranted", "${result.isGranted}")
                                put("token", result.token)
                            }
                        )
                    }
                }
            }
        }
    }

}