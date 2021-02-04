package com.cobox.iot.vulture

import com.cobox.iot.vulture.application.Application
import com.cobox.iot.vulture.application.Configuration
import com.cobox.iot.vulture.auth.Authority
import com.cobox.iot.vulture.auth.AuthorityDatabase
import com.cobox.iot.vulture.auth.AuthorityService
import com.cobox.iot.vulture.data.DatabaseRepo
import com.cobox.iot.vulture.data.IDatabaseRepo
import com.cobox.iot.vulture.iot.Iot
import com.cobox.iot.vulture.iot.IotDatabase
import com.cobox.iot.vulture.iot.IotService
import com.cobox.iot.vulture.nas.Nas
import com.cobox.iot.vulture.nas.NasDatabase
import com.cobox.iot.vulture.nas.NasService
import java.io.Closeable

class VultureApplication(
    override val configuration: Configuration
) : Application, Closeable {

    override val databaseRepo: IDatabaseRepo = DatabaseRepo(
        app = this
    )

    override val authority: Authority = AuthorityService(
        app = this,
        database = databaseRepo.databases["auth"] as AuthorityDatabase
    )

    override val nas: Nas = NasService(
        app = this,
        database = databaseRepo.databases["nas"] as NasDatabase
    )

    override val iot: Iot = IotService(
        app = this,
        database = databaseRepo.databases["iot"] as IotDatabase
    )

    override fun close() {
        (authority as Closeable).close()
        (nas as Closeable).close()
        (iot as Closeable).close()
    }

}