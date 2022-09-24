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

import dagger.Module
import dagger.Provides
import org.briarproject.bramble.api.FeatureFlags
import org.briarproject.bramble.api.plugin.PluginConfig
import org.briarproject.bramble.api.plugin.TransportId
import org.briarproject.bramble.api.plugin.duplex.DuplexPluginFactory
import org.briarproject.bramble.api.plugin.simplex.SimplexPluginFactory
import org.briarproject.bramble.plugin.tcp.TestLanTcpPluginFactory
import org.briarproject.bramble.plugin.tor.UnixTorPluginFactory
import org.briarproject.bramble.util.OsUtils
import org.briarproject.briar.api.test.TestAvatarCreator
import org.briarproject.briar.desktop.testdata.DeterministicTestDataCreator
import org.briarproject.briar.desktop.testdata.DeterministicTestDataCreatorImpl
import org.briarproject.briar.desktop.testdata.TestAvatarCreatorImpl
import java.util.Collections
import javax.inject.Singleton

@Module(
    includes = [
        DesktopCoreModule::class,
    ]
)
internal class DesktopTestModule {
    @Provides
    internal fun provideFeatureFlags(desktopFeatureFlags: DesktopFeatureFlags) = object : FeatureFlags {
        override fun shouldEnableImageAttachments() = true
        override fun shouldEnableProfilePictures() = true
        override fun shouldEnableDisappearingMessages() = false
        override fun shouldEnablePrivateGroupsInCore() = desktopFeatureFlags.shouldEnablePrivateGroups()
        override fun shouldEnableForumsInCore() = desktopFeatureFlags.shouldEnableForums()
        override fun shouldEnableBlogsInCore() = desktopFeatureFlags.shouldEnableBlogs()
        override fun shouldEnableMailbox() = false
    }

    @Provides
    @Singleton
    internal fun provideDesktopFeatureFlags() = object : DesktopFeatureFlags {
        override fun shouldEnablePrivateGroups() = false
        override fun shouldEnableForums() = true
        override fun shouldEnableBlogs() = false
        override fun shouldEnableTransportSettings() = false
    }

    @Provides
    @Singleton
    internal fun provideTestAvatarCreator(testAvatarCreator: TestAvatarCreatorImpl): TestAvatarCreator =
        testAvatarCreator

    @Provides
    @Singleton
    internal fun provideDeterministicTestDataCreator(testDataCreator: DeterministicTestDataCreatorImpl): DeterministicTestDataCreator =
        testDataCreator

    @Provides
    internal fun providePluginConfig(tor: UnixTorPluginFactory, lan: TestLanTcpPluginFactory): PluginConfig {
        val duplex: List<DuplexPluginFactory> =
            if (OsUtils.isLinux() || OsUtils.isMac()) listOf(tor, lan) else listOf(lan)
        return object : PluginConfig {
            override fun getDuplexFactories(): Collection<DuplexPluginFactory> = duplex
            override fun getSimplexFactories(): Collection<SimplexPluginFactory> = Collections.emptyList()
            override fun shouldPoll(): Boolean = true
            override fun getTransportPreferences(): Map<TransportId, List<TransportId>> = emptyMap()
        }
    }
}
