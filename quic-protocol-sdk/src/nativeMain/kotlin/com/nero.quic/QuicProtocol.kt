package com.nero.quic

import kotlinx.cinterop.*
import msquic.*
import platform.posix.alloca

@ExperimentalUnsignedTypes
class QuicProtocol {
    private val arena = Arena()
    private var config:CValue<QUIC_REGISTRATION_CONFIG> = cValue {
        AppName = "quicServer".cstr.getPointer(arena)
        ExecutionProfile = QUIC_EXECUTION_PROFILE.QUIC_EXECUTION_PROFILE_LOW_LATENCY
    }
    private var registration : CPointerVar<HQUICVar>? = arena.alloc()


    fun listen(port:Int) {
        val api = cValue<COpaquePointerVar>()
        val status = MsQuicOpenVersion(1, api)
        if (status != QUIC_STATUS_SUCCESS) {
            println("can not open quic, status is $status")
            return
        }
        val apiTable = interpretCPointer<QUIC_API_TABLE>(api.getPointer(arena).rawValue)
        val invokeStatus = apiTable?.pointed?.RegistrationOpen?.invoke(config.getPointer(arena), registration?.value)
        if (invokeStatus != QUIC_STATUS_SUCCESS) {
            println("can't set config for quic, status is $invokeStatus")
            return
        }
        //初始化status
        var loadStatus = QUIC_STATUS_SUCCESS
        memScoped {
            //创建监听指针
            var listener:CPointerVar<HQUICVar> = alloc()
            val address = cValue<QUIC_ADDR>()
            //设置地址
            QuicAddrSetFamily(address, QUIC_ADDRESS_FAMILY_UNSPEC)
            //设置端口
            QuicAddrSetPort(address, port.toUShort());
        }
    }

    fun loadServerConfig() {
        val quicSetting = cValue<QUIC_SETTINGS>{
            //设置服务器time out
            val timeOut = 5000L
            IdleTimeoutMs = timeOut.toULong()
            IsSet.IdleTimeoutMs = TRUE.toULong()
            //
            // Configures the server's resumption level to allow for resumption and
            // 0-RTT.
            //
            ServerResumptionLevel = QUIC_SERVER_RESUMPTION_LEVEL.QUIC_SERVER_RESUME_AND_ZERORTT.value.toUByte()
            IsSet.ServerResumptionLevel = TRUE.toULong()
        }
    }
}