package org.briarproject.briar.desktop

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.briarproject.bramble.BrambleCoreEagerSingletons
import org.briarproject.briar.BriarCoreEagerSingletons
import java.io.File
import java.io.File.separator
import java.io.IOException
import java.lang.System.getProperty
import java.nio.file.Files.setPosixFilePermissions
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission.*
import java.util.logging.Level.*
import java.util.logging.LogManager

private val DEFAULT_DATA_DIR = getProperty("user.home") + separator + ".briar"

private class Main : CliktCommand(
    name = "briar-desktop",
    help = "Briar Desktop Client"
) {
    private val debug by option("--debug", "-d", help = "Enable printing of debug messages").flag(
        default = false
    )
    private val verbosity by option(
        "--verbose",
        "-v",
        help = "Print verbose log messages"
    ).counted()
    private val dataDir by option(
        "--data-dir",
        help = "The directory where Briar will store its files. Default: $DEFAULT_DATA_DIR",
        metavar = "PATH",
        envvar = "BRIAR_DATA_DIR"
    ).default(DEFAULT_DATA_DIR)

    override fun run() {
        val level = if (debug) ALL else when (verbosity) {
            0 -> WARNING
            1 -> INFO
            else -> ALL
        }

        LogManager.getLogManager().getLogger("").level = level

        val dataDir = getDataDir()
        val app =
            DaggerBriarDesktopApp.builder().desktopModule(
                DesktopModule(dataDir)
            ).build()
        // We need to load the eager singletons directly after making the
        // dependency graphs
        BrambleCoreEagerSingletons.Helper.injectEagerSingletons(app)
        BriarCoreEagerSingletons.Helper.injectEagerSingletons(app)

        app.getUI().startBriar()
        app.getUI().startUI()
    }

    private fun getDataDir(): File {
        val file = File(dataDir)
        if (!file.exists() && !file.mkdirs()) {
            throw IOException("Could not create directory: ${file.absolutePath}")
        } else if (!file.isDirectory) {
            throw IOException("Data dir is not a directory: ${file.absolutePath}")
        }
        val perms = HashSet<PosixFilePermission>()
        perms.add(OWNER_READ)
        perms.add(OWNER_WRITE)
        perms.add(OWNER_EXECUTE)
        setPosixFilePermissions(file.toPath(), perms)
        return file
    }
}

fun main(args: Array<String>) = Main().main(args)