// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.notification;

import com.intellij.internal.statistic.collectors.fus.actions.persistence.ActionIdProvider;
import com.intellij.openapi.actionSystem.ActionWithDelegate;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.intellij.openapi.util.NlsContexts.NotificationContent;

/**
 * @see Notification#addAction(AnAction)
 */
public abstract class NotificationAction extends DumbAwareAction {
  @SuppressWarnings("DialogTitleCapitalization")
  public NotificationAction(@Nullable @NotificationContent String text) {
    super(text);
  }

  public NotificationAction(@NotNull Supplier<@NotificationContent String> dynamicText) {
    super(dynamicText);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, Notification.get(e));
  }

  public abstract void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification);

  public static @NotNull NotificationAction create(
    @NotNull @NotificationContent String text,
    @NotNull BiConsumer<? super AnActionEvent, ? super Notification> action
  ) {
    return new Simple(text, action, false, action, null);
  }

  public static @NotNull NotificationAction create(
    @NotNull @NotificationContent String text,
    @NotNull String actionId,
    @NotNull BiConsumer<? super AnActionEvent, ? super Notification> action
  ) {
    return new Simple(text, action, false, action, actionId);
  }

  public static @NotNull NotificationAction create(
    @NotNull Supplier<@NotificationContent String> dynamicText,
    @NotNull BiConsumer<? super AnActionEvent, ? super Notification> action
  ) {
    return new Simple(dynamicText, action, false, action, null);
  }

  public static @NotNull NotificationAction createExpiring(
    @NotNull @NotificationContent String text,
    @NotNull BiConsumer<? super AnActionEvent, ? super Notification> action
  ) {
    return new Simple(text, action, true, action, null);
  }

  public static @NotNull NotificationAction createSimple(@NotNull @NotificationContent String text,
                                                         @NotNull @NonNls String actionId,
                                                         @NotNull Runnable action) {
    return new Simple(text, (event, notification) -> action.run(), false, action, actionId);
  }

  public static @NotNull NotificationAction createSimple(@NotNull @NotificationContent String text, @NotNull Runnable action) {
    return new Simple(text, (event, notification) -> action.run(), false, action, null);
  }

  public static @NotNull NotificationAction createSimple(@NotNull Supplier<@NotificationContent String> dynamicText, @NotNull Runnable action) {
    return new Simple(dynamicText, (event, notification) -> action.run(), false, action, null);
  }

  public static @NotNull NotificationAction createSimpleExpiring(@NotNull @NotificationContent String text, @NotNull Runnable action) {
    return new Simple(text, (event, notification) -> action.run(), true, action, null);
  }

  public static @NotNull NotificationAction createSimpleExpiring(
    @NotNull @NotificationContent String text,
    @NotNull String actionId,
    @NotNull Runnable action
  ) {
    return new Simple(text, (event, notification) -> action.run(), true, action, actionId);
  }

  @ApiStatus.Internal
  public static final class Simple extends NotificationAction implements ActionWithDelegate<Object>, ActionIdProvider {
    private final BiConsumer<? super AnActionEvent, ? super Notification> myAction;
    private final boolean myExpire;
    private final Object myActionInstance;  // for FUS
    private final @Nullable String myActionId;  // for FUS

    private Simple(
      @NotificationContent String text,
      BiConsumer<? super AnActionEvent, ? super Notification> action,
      boolean expire,
      Object actionInstance,
      @Nullable String actionId
    ) {
      super(text);
      myAction = action;
      myExpire = expire;
      myActionInstance = actionInstance;
      myActionId = actionId;
    }

    private Simple(
      Supplier<@NotificationContent String> dynamicText,
      BiConsumer<? super AnActionEvent, ? super Notification> action,
      boolean expire,
      Object actionInstance,
      @Nullable String actionId
    ) {
      super(dynamicText);
      myAction = action;
      myExpire = expire;
      myActionInstance = actionInstance;
      myActionId = actionId;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
      if (myExpire) {
        notification.expire();
      }
      myAction.accept(e, notification);
    }

    @Override
    public @NotNull Object getDelegate() {
      return myActionInstance;
    }

    @Override
    public @NotNull String getId() {
      if (myActionId != null) {
        return myActionId;
      }
      if (myActionInstance instanceof ActionIdProvider actionIdProvider) {
        var providerId = actionIdProvider.getId();
        if (providerId != null) {
          return providerId;
        }
      }
      return getDelegate().getClass().getName();
    }
  }
}
