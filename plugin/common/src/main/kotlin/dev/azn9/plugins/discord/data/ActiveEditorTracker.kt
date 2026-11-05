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

package dev.azn9.plugins.discord.data

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import java.lang.ref.WeakReference

/**
 * Remembers the most recently selected project/file as reported by FileEditorManagerListener events.
 *
 * In Gateway / remote development (split mode), the IDE process running the plugin may have no
 * focused frame, so `IdeFocusManager.lastFocusedFrame` and `FileEditorManager.selectedEditor`
 * can return null even though the user has a file open in the client. Selection events still
 * fire, so they are used as a fallback.
 */
object ActiveEditorTracker {
    private val LAST_SELECTED_FILE = Key.create<VirtualFile>("discord.last.selected.file")

    @Volatile
    private var lastProject: WeakReference<Project>? = null

    fun onFileSelected(project: Project, file: VirtualFile?) {
        if (project.isDisposed || project.isDefault) return

        lastProject = WeakReference(project)
        project.putUserData(LAST_SELECTED_FILE, file)
    }

    fun onFileClosed(project: Project, file: VirtualFile) {
        if (project.isDisposed) return

        if (project.getUserData(LAST_SELECTED_FILE) == file) {
            project.putUserData(LAST_SELECTED_FILE, null)
        }
    }

    fun lastSelectedFile(project: Project): VirtualFile? =
        project.getUserData(LAST_SELECTED_FILE)?.takeIf { it.isValid }

    /**
     * Best-effort current project: the focused frame's project if there is one, otherwise the project
     * that last had an editor selection, otherwise the first open non-default project.
     */
    fun resolveProject(focusedProject: Project?): Project? {
        focusedProject?.takeIf { it.isUsable() }?.let { return it }
        lastProject?.get()?.takeIf { it.isUsable() }?.let { return it }

        return ProjectManager.getInstance().openProjects.firstOrNull { it.isUsable() }
    }

    private fun Project.isUsable() = !isDisposed && !isDefault && isOpen
}
