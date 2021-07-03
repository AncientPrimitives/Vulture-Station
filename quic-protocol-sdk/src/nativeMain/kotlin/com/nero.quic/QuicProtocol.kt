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
    }
}