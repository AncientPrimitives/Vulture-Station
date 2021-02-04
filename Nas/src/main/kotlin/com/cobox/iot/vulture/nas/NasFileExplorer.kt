package com.cobox.iot.vulture.nas

import com.cobox.iot.vulture.application.Application

class NasFileExplorer(
    private val app: Application
) {

    fun findNasRootFolder(): String = ""

    fun makeOwnerFolderName(ownerName: String, ownerId: Int): String =
        "[$ownerId][$ownerName].nas"

    fun createNasOwnerFolder(ownerName: String, ownerId: Int) {

    }

}