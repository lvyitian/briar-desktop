package org.briarproject.briar.compose

import org.briarproject.bramble.api.crypto.KeyStrengthener
import org.briarproject.bramble.api.db.DatabaseConfig
import java.io.File

internal class SwingDatabaseConfig(private val dbDir: File, private val keyDir: File) :
    DatabaseConfig {

    override fun getDatabaseDirectory() = dbDir

    override fun getDatabaseKeyDirectory() = keyDir

    override fun getKeyStrengthener(): KeyStrengthener? = null
}
