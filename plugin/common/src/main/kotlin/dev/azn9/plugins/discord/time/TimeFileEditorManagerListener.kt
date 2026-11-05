/*
 * Copyright 2017-2020 Aljoscha Grebe
 * Copyright 2023-2024 Axel JOLY (Azn9) <contact@azn9.dev>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.azn9.plugins.discord.time

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import dev.azn9.plugins.discord.data.ActiveEditorTracker

class TimeFileEditorManagerListener : FileEditorManagerListener {
    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        timeService.initializeFile(file)
        ActiveEditorTracker.onFileSelected(source.project, file)
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        ActiveEditorTracker.onFileClosed(source.project, file)
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        ActiveEditorTracker.onFileSelected(event.manager.project, event.newFile)
    }
}
