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

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.briarproject.bramble.api.db.TransactionManager
import org.briarproject.bramble.api.event.Event
import org.briarproject.bramble.api.event.EventBus
import org.briarproject.bramble.api.lifecycle.LifecycleManager
import org.briarproject.bramble.api.sync.GroupId
import org.briarproject.bramble.api.sync.event.GroupAddedEvent
import org.briarproject.bramble.api.sync.event.GroupRemovedEvent
import org.briarproject.briar.api.forum.ForumManager
import org.briarproject.briar.client.MessageTreeImpl
import org.briarproject.briar.desktop.threading.BriarExecutors
import org.briarproject.briar.desktop.utils.clearAndAddAll
import org.briarproject.briar.desktop.viewmodel.EventListenerDbViewModel
import org.briarproject.briar.desktop.viewmodel.asState
import javax.inject.Inject

sealed class PostsState
object Loading : PostsState()
class Loaded(private val messageTree: MessageTreeImpl<ThreadItem>) : PostsState() {
    val posts: MutableList<ThreadItem> get() = messageTree.depthFirstOrder()
}

class ForumsViewModel @Inject constructor(
    private val forumManager: ForumManager,
    briarExecutors: BriarExecutors,
    lifecycleManager: LifecycleManager,
    db: TransactionManager,
    eventBus: EventBus,
) : EventListenerDbViewModel(briarExecutors, lifecycleManager, db, eventBus) {

    private val _fullGroupList = mutableStateListOf<ForumsItem>()
    val groupList = derivedStateOf {
        val filter = _filterBy.value
        _fullGroupList.filter { item ->
            item.name.contains(filter, ignoreCase = true)
        }.sortedByDescending { it.timestamp }
    }

    private val _selectedGroupItem = mutableStateOf<GroupItem?>(null)
    val selectedGroupItem: State<GroupItem?> = _selectedGroupItem

    private val _filterBy = mutableStateOf("")
    val filterBy = _filterBy.asState()

    private val _posts = mutableStateOf<PostsState>(Loading)
    val posts: State<PostsState> = _posts

    override fun onInit() {
        super.onInit()
        loadGroups()
    }

    override fun eventOccurred(e: Event) {
        if (e is GroupAddedEvent) {
            if (e.group.clientId == ForumManager.CLIENT_ID) loadGroups()
        } else if (e is GroupRemovedEvent) {
            if (e.group.clientId == ForumManager.CLIENT_ID) {
                loadGroups()
                if (selectedGroupItem.value?.id == e.group.id) _selectedGroupItem.value = null
            }
        }
    }

    fun createForum(name: String) {
        forumManager.addForum(name)
    }

    private fun loadGroups() {
        runOnDbThreadWithTransaction(true) { txn ->
            val list = forumManager.getForums(txn).map { forums ->
                ForumsItem(
                    forums,
                    forumManager.getGroupCount(txn, forums.id),
                )
            }
            txn.attach {
                _fullGroupList.clearAndAddAll(list)
            }
        }
    }

    fun selectGroup(groupItem: GroupItem) {
        _selectedGroupItem.value = groupItem
        loadPosts(groupItem.id)
    }

    fun isSelected(groupId: GroupId) = _selectedGroupItem.value?.id == groupId

    private fun loadPosts(groupId: GroupId) {
        _posts.value = Loading
        runOnDbThreadWithTransaction(true) { txn ->
            val items = forumManager.getPostHeaders(txn, groupId).map { header ->
                ForumPostItem(header, forumManager.getPostText(txn, header.id))
            }
            val tree = MessageTreeImpl<ThreadItem>().apply { add(items) }
            txn.attach {
                _posts.value = Loaded(tree)
            }
        }
    }

    fun setFilterBy(filter: String) {
        _filterBy.value = filter
    }

    fun deleteGroup(groupItem: GroupItem) {
        forumManager.removeForum((groupItem as ForumsItem).forum)
    }
}