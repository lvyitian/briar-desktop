/*
 * Briar Desktop
 * Copyright (C) 2021-2022 The Briar Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.briarproject.briar.desktop.viewmodel

import mu.KotlinLogging
import org.briarproject.bramble.api.db.DatabaseExecutor
import org.briarproject.bramble.api.db.DbCallable
import org.briarproject.bramble.api.db.Transaction
import org.briarproject.bramble.api.db.TransactionManager
import org.briarproject.bramble.api.lifecycle.LifecycleManager
import org.briarproject.briar.desktop.threading.BriarExecutors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class DbViewModel(
    private val briarExecutors: BriarExecutors,
    private val lifecycleManager: LifecycleManager,
    private val db: TransactionManager
) : ViewModel {

    companion object {
        private val LOG = KotlinLogging.logger {}
    }

    /**
     * Waits for the DB to open and runs the given [task] on the [DatabaseExecutor].
     * To avoid inconsistent state between the database and the UI
     * whenever the UI should react to a successful transaction,
     * strongly consider using [runOnDbThreadWithTransaction] instead.
     */
    protected fun runOnDbThread(task: () -> Unit) = briarExecutors.onDbThread(task)

    /**
     * Waits for the DB to open and runs the given [task] on the [DatabaseExecutor],
     * providing a [Transaction], that may be [readOnly] or not, to the task.
     * Updates to the UI that depend on a successful transaction in the database
     * should be attached to the transaction via [Transaction.attach].
     */
    protected fun runOnDbThreadWithTransaction(
        readOnly: Boolean,
        task: (Transaction) -> Unit
    ) = briarExecutors.onDbThreadWithTransaction(readOnly, task)

    protected suspend fun <T> runOnDbThread(
        readOnly: Boolean,
        task: (Transaction) -> T
    ) = suspendCoroutine<T> { cont ->
        briarExecutors.onDbThread {
            val t = db.transactionWithResult(readOnly, DbCallable { txn -> task(txn) })
            cont.resume(t)
        }
    }
}
