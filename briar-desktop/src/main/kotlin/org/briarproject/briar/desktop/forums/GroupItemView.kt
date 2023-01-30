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

package org.briarproject.briar.desktop.forums

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.briarproject.bramble.api.sync.GroupId
import org.briarproject.briar.desktop.ui.NumberBadge
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18nF
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18nP
import org.briarproject.briar.desktop.utils.PreviewUtils.preview
import org.briarproject.briar.desktop.utils.TimeUtils.getFormattedTimestamp
import org.briarproject.briar.desktop.utils.appendCommaSeparated
import org.briarproject.briar.desktop.utils.buildBlankAnnotatedString
import java.time.Instant

@Suppress("HardCodedStringLiteral")
fun main() = preview(
    "name" to "This is a test forum! This is a test forum! This is a test forum! This is a test forum!",
    "msgCount" to 42,
    "unread" to 23,
    "timestamp" to Instant.now().toEpochMilli(),
) {
    val item = object : GroupItem {
        override val id: GroupId = GroupId(getRandomIdPersistent())
        override val name: String = getStringParameter("name")
        override val msgCount: Int = getIntParameter("msgCount")
        override val unread: Int = getIntParameter("unread")
        override val timestamp: Long = getLongParameter("timestamp")
    }
    GroupItemView(item)
}

@Composable
fun GroupItemView(
    groupItem: GroupItem,
    modifier: Modifier = Modifier,
) = Row(
    horizontalArrangement = spacedBy(12.dp),
    verticalAlignment = CenterVertically,
    modifier = modifier
        // allows content to be bottom-aligned
        .height(IntrinsicSize.Min)
        .semantics {
            text = getDescription(groupItem)
        },
) {
    Box(
        modifier = Modifier.align(Top).padding(vertical = 6.dp), // todo: or 8? same with contact!
    ) {
        GroupCircle(groupItem)
        NumberBadge(
            num = groupItem.unread,
            modifier = Modifier.align(TopEnd).offset(6.dp, (-6).dp)
        )
    }
    GroupItemViewInfo(groupItem)
}

private fun getDescription(item: GroupItem) = buildBlankAnnotatedString {
    append(item.name)
    if (item.unread > 0) appendCommaSeparated(i18nP("access.forums.unread_count", item.unread))
    if (item.msgCount == 0) appendCommaSeparated(i18n("group.card.no_posts"))
    else appendCommaSeparated(
        i18nF(
            "access.forums.last_post_timestamp",
            getFormattedTimestamp(item.timestamp)
        )
    )
}

@Composable
private fun GroupItemViewInfo(groupItem: GroupItem) = Column(
    horizontalAlignment = Start,
) {
    Spacer(Modifier.weight(1f, fill = true))
    Text(
        text = groupItem.name,
        style = MaterialTheme.typography.body1,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.heightIn(min = 4.dp).weight(1f, fill = true))
    Row(
        horizontalArrangement = SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (groupItem.msgCount > 0) i18nP("group.card.posts", groupItem.msgCount)
            else i18nP("group.card.no_posts", groupItem.msgCount),
            style = MaterialTheme.typography.caption
        )
        if (groupItem.msgCount > 0) {
            Text(
                text = getFormattedTimestamp(groupItem.timestamp),
                style = MaterialTheme.typography.caption
            )
        }
    }
}
