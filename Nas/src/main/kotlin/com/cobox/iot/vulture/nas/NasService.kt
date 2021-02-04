package com.cobox.iot.vulture.nas

import com.cobox.iot.vulture.application.Application
import com.cobox.iot.vulture.msgbus.Eventable
import io.vertx.core.eventbus.EventBus
import java.io.Closeable
import java.io.File

private enum class CreateFolderResult {
    Okay, HasExisted, ExistsButNotADirectory,
    CannotWrite, CannotCreate
}

class NasService(
    private val app: Application,
    private val database: NasDatabase
) : Nas, Closeable, Eventable {

    init {
        ensureNasRootFolder()
    }

    protected fun ensureNasRootFolder() {
        val creatingPath = File(app.configuration.nas.root).canonicalPath
        createFolder(creatingPath) { result ->
            when (result) {
                CreateFolderResult.Okay ->
                    println("[NAS] create NAS folder '$creatingPath' success")
                CreateFolderResult.HasExisted ->
                    println("[NAS] NAS folder '$creatingPath' exists, no need to create")
                CreateFolderResult.ExistsButNotADirectory ->
                    println("[NAS] NAS folder '$creatingPath' isn't a directory, " +
                            "please remove the file and try to launch Vulture again")
                CreateFolderResult.CannotWrite ->
                    println("[NAS] NAS folder '$creatingPath' cannot be read, " +
                            "please override the reading access permission and try to launch Vulture again")
                CreateFolderResult.CannotCreate ->
                    println("[NAS] create NAS folder '$creatingPath' failed")
            }
        }
    }

    override fun onRegisterAddress(bus: EventBus) {
        TODO("Not yet implemented")
    }

    override fun createBusinessFor(username: String): Boolean {
        val creatingPath = File("${app.configuration.nas.root}/$username.nas").canonicalPath
        return createFolder(creatingPath) { result ->
            when (result) {
                CreateFolderResult.Okay ->
                    println("[NAS] create NAS folder '$creatingPath' success")
                CreateFolderResult.HasExisted ->
                    println("[NAS] NAS folder '$creatingPath' exists, no need to create")
                CreateFolderResult.ExistsButNotADirectory ->
                    println("[NAS] NAS folder '$creatingPath' isn't a directory, " +
                            "please remove the file and try to launch Vulture again")
                CreateFolderResult.CannotWrite ->
                    println("[NAS] NAS folder '$creatingPath' cannot be read, " +
                            "please override the reading access permission and try to launch Vulture again")
                CreateFolderResult.CannotCreate ->
                    println("[NAS] create NAS folder '$creatingPath' failed")
            }
        }
    }

    override fun close() {
        println("[NAS] shutdown Nas service")
    }

    private fun createFolder(
        folderPath: String,
        onResult: ((CreateFolderResult) -> Unit)
    ): Boolean {
        File(folderPath).let { folder ->
            if (!folder.exists()) {
                folder.mkdirs().also { isSuccess ->
                    onResult(if (isSuccess) CreateFolderResult.Okay else CreateFolderResult.CannotCreate)
                    return isSuccess
                }
            } else if (!folder.isDirectory) {
                onResult(CreateFolderResult.ExistsButNotADirectory)
                return false
            } else if (!folder.canWrite()) {
                onResult(CreateFolderResult.CannotWrite)
                return false
            } else {
                onResult(CreateFolderResult.Okay)
                return true
            }
        }
    }

}