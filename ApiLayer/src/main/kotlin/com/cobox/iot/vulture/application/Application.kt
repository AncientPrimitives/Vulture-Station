package com.cobox.iot.vulture.application

import com.cobox.iot.vulture.auth.Authority
import com.cobox.iot.vulture.data.IDatabaseRepo
import com.cobox.iot.vulture.iot.Iot
import com.cobox.iot.vulture.nas.Nas

interface Application {
    val configuration: Configuration
    val databaseRepo: IDatabaseRepo
    val nas: Nas
    val iot: Iot
    val authority: Authority
}