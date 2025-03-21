// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.vcs.changes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Manages asynchronous file status updating for files under VCS.
 *
 * @author max
 */
public abstract class VcsDirtyScopeManager {
  public static @NotNull VcsDirtyScopeManager getInstance(@NotNull Project project) {
    return project.getService(VcsDirtyScopeManager.class);
  }

  /**
   * Requests an asynchronous file status update for all files in the project.
   */
  public abstract void markEverythingDirty();

  /**
   * Requests an asynchronous file status update for the specified virtual file.
   *
   * @param file the file for which the status update is requested.
   */
  public abstract void fileDirty(@NotNull VirtualFile file);

  /**
   * Requests an asynchronous file status update for the specified file path.
   *
   * @param file the file path for which the status update is requested.
   */
  public abstract void fileDirty(@NotNull FilePath file);

  /**
   * Requests an asynchronous file status update for all files under the specified directory.
   *
   * @param dir the directory for which the file status update is requested.
   */
  public abstract void dirDirtyRecursively(@NotNull VirtualFile dir);

  public abstract void dirDirtyRecursively(@NotNull FilePath path);

  /**
   * Requests an asynchronous file status update for all files under the specified root.
   *
   * @param root the root for which the file status update is requested.
   *             
   * @see com.intellij.openapi.vcs.ProjectLevelVcsManager#getVcsRootFor(VirtualFile)
   */
  public abstract void rootDirty(@NotNull VirtualFile root);

  public abstract @NotNull Collection<FilePath> whatFilesDirty(@NotNull Collection<? extends FilePath> files);

  /**
   * Requests an asynchronous file status update for all files specified and under the specified directories
   */
  public abstract void filePathsDirty(final @Nullable Collection<? extends FilePath> filesDirty, final @Nullable Collection<? extends FilePath> dirsRecursivelyDirty);

  /**
   * Requests an asynchronous file status update for all files specified and under the specified directories
   */
  public abstract void filesDirty(final @Nullable Collection<? extends VirtualFile> filesDirty, final @Nullable Collection<? extends VirtualFile> dirsRecursivelyDirty);
}
