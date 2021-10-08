package org.briarproject.briar.desktop.testdata

import org.briarproject.bramble.api.contact.Contact
import org.briarproject.bramble.api.db.DbException
import org.briarproject.bramble.api.lifecycle.IoExecutor

interface DeterministicTestDataCreator {
    /**
     * Create fake test data on the IoExecutor
     *
     * @param numContacts    Number of contacts to create. Must be >= 1
     * @param numPrivateMsgs Number of private messages to create for each
     * contact.
     * @param avatarPercent  Percentage of contacts
     * that will use a random profile image. Between 0 and 100.
     */
    fun createTestData(numContacts: Int, numPrivateMsgs: Int, avatarPercent: Int)

    @IoExecutor
    @Throws(DbException::class)
    fun addContact(name: String, alias: String?, avatar: Boolean): Contact
}