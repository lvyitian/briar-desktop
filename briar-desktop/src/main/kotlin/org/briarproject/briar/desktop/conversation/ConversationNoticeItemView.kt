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

package org.briarproject.briar.desktop.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import org.briarproject.bramble.api.sync.GroupId
import org.briarproject.bramble.api.sync.MessageId
import org.briarproject.briar.desktop.theme.BriarTheme
import org.briarproject.briar.desktop.theme.noticeIn
import org.briarproject.briar.desktop.theme.noticeOut
import org.briarproject.briar.desktop.theme.privateMessageDate
import org.briarproject.briar.desktop.theme.textPrimary
import org.briarproject.briar.desktop.theme.textSecondary
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import org.briarproject.briar.desktop.utils.PreviewUtils.preview
import org.briarproject.briar.desktop.utils.appendAfterColon
import org.briarproject.briar.desktop.utils.appendCommaSeparated
import java.time.Instant

@Suppress("HardCodedStringLiteral")
fun main() = preview(
    "notice" to "Text of notice message.",
    "text" to "This is a long long long message that spans over several lines.\n\nIt ends here.",
    "time" to Instant.now().toEpochMilli(),
    "isIncoming" to false,
    "isRead" to false,
    "isSent" to false,
    "isSeen" to true,
) {
    BriarTheme {
        ConversationNoticeItemView(
            ConversationNoticeItem(
                notice = getStringParameter("notice"),
                text = getStringParameter("text"),
                id = MessageId(getRandomIdPersistent()),
                groupId = GroupId(getRandomIdPersistent()),
                time = getLongParameter("time"),
                autoDeleteTimer = 0,
                isIncoming = getBooleanParameter("isIncoming"),
                isRead = getBooleanParameter("isRead"),
                isSent = getBooleanParameter("isSent"),
                isSeen = getBooleanParameter("isSeen"),
            )
        )
    }
}

/**
 * Composable for private messages containing a notice.
 */
@Composable
fun ConversationNoticeItemView(
    m: ConversationNoticeItem,
    onDelete: (MessageId) -> Unit = {},
) {
    val textColor = if (m.isIncoming) MaterialTheme.colors.textPrimary else Color.White
    val noticeBackground = if (m.isIncoming) MaterialTheme.colors.noticeIn else MaterialTheme.colors.noticeOut
    val noticeColor = if (m.isIncoming) MaterialTheme.colors.textSecondary else MaterialTheme.colors.privateMessageDate
    val text = m.text
    ConversationItemView(
        item = m,
        onDelete = onDelete,
        conversationItemDescription = buildString {
            append(m.notice)
            if (text != null) {
                appendCommaSeparated(i18n("access.conversation.notice.additional_message"))
                appendAfterColon(text)
            }
        }
    ) {
        Column(Modifier.width(IntrinsicSize.Max)) {
            if (text != null) {
                SelectionContainer {
                    Text(
                        text,
                        style = MaterialTheme.typography.body1,
                        color = textColor,
                        modifier = Modifier.padding(12.dp, 8.dp).align(Start)
                    )
                }
            }
            Column(
                Modifier.fillMaxWidth().background(noticeBackground).padding(12.dp, 8.dp)
            ) {
                Text(
                    text = m.notice,
                    style = MaterialTheme.typography.body2,
                    fontStyle = FontStyle.Italic,
                    color = noticeColor,
                    modifier = Modifier.align(Start).padding(bottom = 8.dp)
                )
                ConversationItemStatusView(m)
            }
        }
    }
}
