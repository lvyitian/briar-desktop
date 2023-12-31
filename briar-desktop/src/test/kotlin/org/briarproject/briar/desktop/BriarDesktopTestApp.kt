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

package org.briarproject.briar.desktop

import dagger.Component
import org.briarproject.bramble.BrambleCoreEagerSingletons
import org.briarproject.bramble.BrambleCoreModule
import org.briarproject.bramble.api.account.AccountManager
import org.briarproject.bramble.api.connection.ConnectionRegistry
import org.briarproject.bramble.api.contact.ContactManager
import org.briarproject.bramble.api.crypto.CryptoComponent
import org.briarproject.bramble.api.event.EventBus
import org.briarproject.bramble.api.identity.IdentityManager
import org.briarproject.bramble.api.lifecycle.IoExecutor
import org.briarproject.bramble.api.lifecycle.LifecycleManager
import org.briarproject.bramble.api.lifecycle.ShutdownManager
import org.briarproject.bramble.api.properties.TransportPropertyManager
import org.briarproject.briar.BriarCoreEagerSingletons
import org.briarproject.briar.BriarCoreModule
import org.briarproject.briar.api.forum.ForumManager
import org.briarproject.briar.api.forum.ForumSharingManager
import org.briarproject.briar.api.introduction.IntroductionManager
import org.briarproject.briar.api.privategroup.GroupMessageFactory
import org.briarproject.briar.api.privategroup.PrivateGroupFactory
import org.briarproject.briar.api.privategroup.PrivateGroupManager
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationFactory
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationManager
import org.briarproject.briar.api.test.TestDataCreator
import org.briarproject.briar.desktop.testdata.DeterministicTestDataCreator
import org.briarproject.briar.desktop.threading.BriarExecutors
import org.briarproject.briar.desktop.ui.BriarUi
import java.util.concurrent.Executor
import javax.inject.Singleton

@Component(
    modules = [
        BrambleCoreModule::class,
        BriarCoreModule::class,
        DesktopTestModule::class,
    ]
)
@Singleton
internal interface BriarDesktopTestApp : BrambleCoreEagerSingletons, BriarCoreEagerSingletons {

    fun getBriarUi(): BriarUi

    fun getEventBus(): EventBus

    fun getIdentityManager(): IdentityManager

    fun getIntroductionManager(): IntroductionManager

    fun getContactManager(): ContactManager

    fun getLifecycleManager(): LifecycleManager

    fun getShutdownManager(): ShutdownManager

    fun getAccountManager(): AccountManager

    fun getTestDataCreator(): TestDataCreator

    fun getDeterministicTestDataCreator(): DeterministicTestDataCreator

    fun getConnectionRegistry(): ConnectionRegistry

    @IoExecutor
    fun getIoExecutor(): Executor

    fun getTransportPropertyManager(): TransportPropertyManager

    fun getCryptoComponent(): CryptoComponent

    fun getBriarExecutors(): BriarExecutors

    fun getForumManager(): ForumManager

    fun getForumSharingManager(): ForumSharingManager

    fun getPrivateGroupManager(): PrivateGroupManager

    fun getPrivateGroupFactory(): PrivateGroupFactory

    fun getGroupMessageFactory(): GroupMessageFactory

    fun getGroupInvitationManager(): GroupInvitationManager

    fun getGroupInvitationFactory(): GroupInvitationFactory
}
