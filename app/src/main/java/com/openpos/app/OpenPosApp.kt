package com.openpos.app

import android.app.Application
import com.openpos.app.data.AppDatabase
import com.openpos.app.data.BackupManager
import com.openpos.app.data.repository.PosRepository

/**
 * Simple hand-rolled DI container — no account/session state of any kind.
 */
class OpenPosApp : Application() {
    lateinit var repository: PosRepository
        private set
    lateinit var backupManager: BackupManager
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = PosRepository(db)
        backupManager = BackupManager(repository)
    }
}
