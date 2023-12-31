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

package org.briarproject.briar.desktop.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.briarproject.bramble.api.contact.ContactId
import org.briarproject.bramble.api.contact.PendingContactId
import org.briarproject.bramble.api.contact.PendingContactState
import org.briarproject.bramble.api.identity.AuthorId
import org.briarproject.briar.desktop.contact.add.remote.PendingContactItem
import org.briarproject.briar.desktop.contact.add.remote.PendingContactItemView
import org.briarproject.briar.desktop.theme.surfaceVariant
import org.briarproject.briar.desktop.ui.Constants.COLUMN_WIDTH
import org.briarproject.briar.desktop.ui.Constants.HEADER_SIZE
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.SearchTextField
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import org.briarproject.briar.desktop.utils.PreviewUtils.preview
import org.briarproject.briar.desktop.utils.getRandomAuthorInfo
import java.time.Instant

@Suppress("HardCodedStringLiteral")
fun main() = preview {
    val list = remember {
        listOf(
            ContactItem(
                id = ContactId(0),
                authorId = AuthorId(getRandomId()),
                authorInfo = getRandomAuthorInfo(),
                name = "Maria",
                alias = "Mary",
                isConnected = true,
                isEmpty = false,
                unread = 2,
                timestamp = Instant.now().toEpochMilli(),
            ),
            PendingContactItem(
                id = PendingContactId(getRandomId()),
                alias = "Thomas",
                timestamp = (Instant.now().minusSeconds(300)).toEpochMilli(),
                state = PendingContactState.ADDING_CONTACT,
            ),
            ContactItem(
                id = ContactId(1),
                authorId = AuthorId(getRandomId()),
                authorInfo = getRandomAuthorInfo(),
                name = "Anna",
                alias = null,
                isConnected = false,
                isEmpty = true,
                unread = 0,
                timestamp = (Instant.now().minusSeconds(300)).toEpochMilli(),
            ),
        )
    }

    val (selected, setSelected) = remember { mutableStateOf<ContactListItem?>(null) }
    val (filterBy, setFilterBy) = remember { mutableStateOf("") }

    val filteredList = remember(filterBy) {
        list.filter {
            it.displayName.contains(filterBy, ignoreCase = true)
        }.sortedByDescending { it.timestamp }
    }

    ContactList(
        contactList = filteredList,
        isSelected = { selected == it },
        selectContact = setSelected,
        removePendingContact = {},
        filterBy = filterBy,
        setFilterBy = setFilterBy,
        onContactAdd = {},
    )
}

@Composable
fun ContactList(
    contactList: List<ContactListItem>,
    isSelected: (ContactListItem) -> Boolean,
    selectContact: (ContactListItem) -> Unit,
    removePendingContact: (PendingContactItem) -> Unit,
    filterBy: String,
    setFilterBy: (String) -> Unit,
    onContactAdd: () -> Unit,
) = Column(
    modifier = Modifier.fillMaxHeight().width(COLUMN_WIDTH).background(MaterialTheme.colors.surfaceVariant),
) {
    Column(
        // Align height to top bar on the right (incl. divider)
        modifier = Modifier.fillMaxWidth().height(HEADER_SIZE + 1.dp),
    ) {
        SearchTextField(
            placeholder = i18n("contacts.search.title"),
            icon = Icons.Filled.PersonAdd,
            searchValue = filterBy,
            addButtonDescription = i18n("access.contacts.add"),
            onValueChange = setFilterBy,
            onAddButtonClicked = onContactAdd,
        )
    }

    VerticallyScrollableArea(modifier = Modifier.fillMaxSize()) { scrollState ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .semantics {
                    contentDescription = i18n("access.contact.list")
                }
                .selectableGroup()
        ) {
            items(
                items = contactList,
                key = { item -> item.uniqueId },
                contentType = { item -> item::class }
            ) { item ->
                ListItemView(
                    onSelect = { selectContact(item) },
                    selected = isSelected(item),
                    // let divider start at horizontal position of text
                    dividerOffsetFromStart = (16 + 36 + 12).dp,
                ) {
                    val modifier = Modifier
                        .heightIn(min = HEADER_SIZE)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        // makes sure that ConnectionIndicator and Delete button are aligned with AddContact button
                        .padding(start = 16.dp, end = 4.dp)

                    when (item) {
                        is ContactItem -> {
                            ContactItemView(
                                contactItem = item,
                                modifier = modifier
                            )
                        }

                        is PendingContactItem -> {
                            PendingContactItemView(
                                contactItem = item,
                                onRemove = {
                                    removePendingContact(item)
                                },
                                modifier = modifier
                            )
                        }
                    }
                }
            }
        }
    }
}
