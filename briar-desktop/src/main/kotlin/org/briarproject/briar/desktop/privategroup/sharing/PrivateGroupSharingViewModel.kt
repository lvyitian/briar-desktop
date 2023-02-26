/*
 * Briar Desktop
 * Copyright (C) 2021-2023 The Briar Project
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

package org.briarproject.briar.desktop.privategroup.sharing

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.briarproject.bramble.api.connection.ConnectionRegistry
import org.briarproject.bramble.api.contact.ContactId
import org.briarproject.bramble.api.contact.ContactManager
import org.briarproject.bramble.api.db.Transaction
import org.briarproject.bramble.api.db.TransactionManager
import org.briarproject.bramble.api.event.Event
import org.briarproject.bramble.api.event.EventBus
import org.briarproject.bramble.api.lifecycle.LifecycleManager
import org.briarproject.bramble.api.plugin.event.ContactConnectedEvent
import org.briarproject.bramble.api.plugin.event.ContactDisconnectedEvent
import org.briarproject.briar.api.conversation.ConversationManager
import org.briarproject.briar.api.identity.AuthorManager
import org.briarproject.briar.api.privategroup.GroupMember
import org.briarproject.briar.api.privategroup.PrivateGroupManager
import org.briarproject.briar.api.privategroup.event.GroupInvitationResponseReceivedEvent
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationManager
import org.briarproject.briar.api.sharing.SharingConstants.MAX_INVITATION_TEXT_LENGTH
import org.briarproject.briar.api.sharing.SharingManager.SharingStatus
import org.briarproject.briar.api.sharing.SharingManager.SharingStatus.SHAREABLE
import org.briarproject.briar.api.sharing.SharingManager.SharingStatus.SHARING
import org.briarproject.briar.api.sharing.event.ContactLeftShareableEvent
import org.briarproject.briar.desktop.contact.ContactItem
import org.briarproject.briar.desktop.threadedgroup.sharing.ThreadedGroupSharingViewModel
import org.briarproject.briar.desktop.threading.BriarExecutors
import org.briarproject.briar.desktop.threading.UiExecutor
import org.briarproject.briar.desktop.utils.InternationalizationUtils
import org.briarproject.briar.desktop.utils.StringUtils.takeUtf8
import org.briarproject.briar.desktop.utils.clearAndAddAll
import org.briarproject.briar.desktop.viewmodel.asList
import org.briarproject.briar.desktop.viewmodel.asState
import org.briarproject.briar.desktop.viewmodel.update
import javax.inject.Inject

class PrivateGroupSharingViewModel @Inject constructor(
    private val privateGroupManager: PrivateGroupManager,
    private val privateGroupInvitationManager: GroupInvitationManager,
    contactManager: ContactManager,
    authorManager: AuthorManager,
    conversationManager: ConversationManager,
    private val connectionRegistry: ConnectionRegistry,
    briarExecutors: BriarExecutors,
    lifecycleManager: LifecycleManager,
    db: TransactionManager,
    eventBus: EventBus,
) : ThreadedGroupSharingViewModel(
    contactManager,
    authorManager,
    conversationManager,
    connectionRegistry,
    briarExecutors,
    lifecycleManager,
    db,
    eventBus,
) {

    private val _sharingStatus = mutableStateOf(emptyMap<ContactId, SharingStatus>())
    private val _shareableSelected = mutableStateOf(emptySet<ContactId>())
    private val _sharingMessage = mutableStateOf("")

    private val _members = mutableStateListOf<GroupMember>()
    val members = _members.asList()

    data class ShareableContactItem(val status: SharingStatus, val contactItem: ContactItem)

    val contactList = derivedStateOf {
        _contactList.mapNotNull {
            _sharingStatus.value[it.id]?.let { status ->
                ShareableContactItem(status, it)
            }
        }.sortedWith(
            // first all items that are SHAREABLE (false comes before true)
            // second non-case-sensitive, alphabetical order on displayName
            compareBy(
                { it.status != SHAREABLE },
                { it.contactItem.displayName.lowercase(InternationalizationUtils.locale) }
            )
        )
    }

    val sharingMessage = _sharingMessage.asState()

    val buttonEnabled = derivedStateOf { _shareableSelected.value.isNotEmpty() }

    override fun onInit() {
        super.onInit()
        loadContacts()
    }

    override fun reload() {
        super.reload()
        _shareableSelected.value = emptySet()
        _sharingMessage.value = ""
        runOnDbThreadWithTransaction(true) { txn ->
            val members = privateGroupManager.getMembers(txn, _groupId!!)
            txn.attach {
                _members.clearAndAddAll(members)
            }
        }
    }

    @UiExecutor
    fun isShareableSelected(shareable: ShareableContactItem) =
        _shareableSelected.value.contains(shareable.contactItem.id)

    @UiExecutor
    fun toggleShareable(shareable: ShareableContactItem) =
        if (isShareableSelected(shareable)) _shareableSelected.value -= shareable.contactItem.id
        else _shareableSelected.value += shareable.contactItem.id

    @UiExecutor
    fun setSharingMessage(message: String) {
        _sharingMessage.value = message.takeUtf8(MAX_INVITATION_TEXT_LENGTH)
    }

    // todo: only possible if group creator
    @UiExecutor
    fun shareForum() = runOnDbThreadWithTransaction(false) { txn ->
        val groupId = _groupId ?: return@runOnDbThreadWithTransaction
        val message = _sharingMessage.value.ifEmpty { null }
        _shareableSelected.value.forEach { contactId ->
            // privateGroupInvitationManager.sendInvitation(txn, groupId, contactId, message)
        }
        txn.attach { reload() }
    }

    @UiExecutor
    override fun eventOccurred(e: Event) {
        super.eventOccurred(e)

        val groupId = _groupId ?: return
        when {
            e is GroupInvitationResponseReceivedEvent && e.messageHeader.shareableId == groupId -> {
                if (e.messageHeader.wasAccepted()) {
                    _sharingStatus.value += e.contactId to SHARING
                    val connected = connectionRegistry.isConnected(e.contactId)
                    _sharingInfo.update { addContact(connected) }
                } else {
                    _sharingStatus.value += e.contactId to SHAREABLE
                }
            }

            // todo: update/reload member list on member join and leave(?)
            //  e is GroupMessageAddedEvent && e.groupId == groupId && e.header is JoinMessageHeader

            // todo: those could be moved to GroupSharingViewModel
            e is ContactLeftShareableEvent && e.groupId == groupId -> {
                _sharingStatus.value += e.contactId to SHAREABLE
                val connected = connectionRegistry.isConnected(e.contactId)
                _sharingInfo.update { removeContact(connected) }
            }

            e is ContactConnectedEvent -> {
                if (_sharingStatus.value[e.contactId] == SHARING)
                    _sharingInfo.update { updateContactConnected(true) }
            }

            e is ContactDisconnectedEvent -> {
                if (_sharingStatus.value[e.contactId] == SHARING)
                    _sharingInfo.update { updateContactConnected(false) }
            }
        }
    }

    override fun loadSharingStatus(txn: Transaction) {
        val groupId = _groupId ?: return
        val map = contactManager.getContacts(txn).associate { contact ->
            contact.id to privateGroupInvitationManager.getSharingStatus(txn, contact, groupId)
        }
        txn.attach {
            val sharing = map.filterValues { it == SHARING }.keys
            val online =
                sharing.fold(0) { acc, it -> if (connectionRegistry.isConnected(it)) acc + 1 else acc }
            _sharingStatus.value = map
            _sharingInfo.value = SharingInfo(sharing.size, online)
        }
    }
}
