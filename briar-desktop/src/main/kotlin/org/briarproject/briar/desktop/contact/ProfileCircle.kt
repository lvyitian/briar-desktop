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

package org.briarproject.briar.desktop.contact

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.briarproject.bramble.api.identity.Author
import org.briarproject.bramble.api.identity.AuthorId
import org.briarproject.briar.api.identity.AuthorInfo
import org.briarproject.briar.desktop.attachment.media.AvatarProducer
import org.briarproject.briar.desktop.theme.outline
import org.briarproject.briar.desktop.utils.PreviewUtils.preview

fun main() = preview {
    val bytes = byteArrayOf(
        -110, 58, 34, -54, 79, 0, -92, -65, 2, 10, -7, 53, -121,
        -31, 39, 48, 86, -54, -4, 7, 108, -106, 89, 11, 65, -118,
        13, -51, -96, 38, -91
    )
    ProfileCircle(90.dp, bytes)

    ProfileCircle(90.dp)

    ProfileCircleRss(90.dp)
}

/**
 * Display the avatar for a [ContactItem]. If it has an avatar image, display that, otherwise
 * display an [Identicon] based on the user's author id. Either way the profile image is displayed
 * within a circle.
 *
 * @param size the size of the circle. In order to avoid aliasing effects for Identicon-based profile images,
 *             pass a multiple of 9 here. That helps as the image is based on a 9x9 square grid.
 */
@Composable
fun ProfileCircle(size: Dp, contactItem: ContactItem) {
    ProfileCircle(size, contactItem.authorId, contactItem.authorInfo)
}

/**
 * Display the given [Author]'s avatar, or if it doesn't exist, the [Identicon]
 * as a profile image within a circle.
 *
 * @param size the size of the circle. In order to avoid aliasing effects, pass a multiple
 *             of 9 here. That helps as the image is based on a 9x9 square grid.
 */
@Composable
fun ProfileCircle(size: Dp, authorId: AuthorId, authorInfo: AuthorInfo) {
    val avatar = AvatarProducer(authorInfo)
    avatar?.let { imageBitmapState ->
        ProfileCircle(size, imageBitmapState.value)
    } ?: ProfileCircle(size, authorId.bytes)
}

/**
 * Display an [Identicon] as a profile image within a circle based on a user's author id.
 *
 * @param size the size of the circle. In order to avoid aliasing effects, pass a multiple
 *             of 9 here. That helps as the image is based on a 9x9 square grid.
 */
@Composable
fun ProfileCircle(size: Dp, input: ByteArray) {
    Canvas(
        Modifier.size(size).clip(CircleShape)
            .border(1.dp, MaterialTheme.colors.outline, CircleShape)
    ) {
        Identicon(input, this.size.width, this.size.height).draw(this)
    }
}

/**
 * Display an avatar bitmap as a profile image within a circle.
 *
 * @param size the size of the circle.
 */
@Composable
fun ProfileCircle(size: Dp, avatar: ImageBitmap?) {
    val modifier = Modifier.size(size).clip(CircleShape)
        .border(1.dp, MaterialTheme.colors.outline, CircleShape)
    // If avatar is null, it should still be loading, so show empty circle
    if (avatar == null) Spacer(
        modifier = modifier,
    ) else Image(
        bitmap = avatar,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier,
    )
}

/**
 * Display a placeholder avatar for pending contacts.
 *
 * @param size the size of the circle.
 */
@Composable
fun ProfileCircle(size: Dp, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colors.outline
    Canvas(modifier.size(size).clip(CircleShape).border(2.dp, color, CircleShape)) {
        val size = size.toPx()
        val center = size / 2
        val width = 2.dp.toPx()
        drawLine(color, Offset(center, center * 0.2f), Offset(center, center), width)
        drawLine(color, Offset(center, center), Offset(size * 0.7f, size * 0.7f), width)
    }
}

/**
 * Display an RSS avatar for RSS feeds.
 *
 * @param size the size of the circle.
 */
@Composable
fun ProfileCircleRss(size: Dp) {
    val modifier = Modifier.size(size).clip(CircleShape)
        .border(1.dp, MaterialTheme.colors.outline, CircleShape)
        .background(Color(0xfffc9403))
    Icon(
        imageVector = Icons.Default.RssFeed,
        contentDescription = null,
        tint = Color.White,
        modifier = modifier,
    )
}
