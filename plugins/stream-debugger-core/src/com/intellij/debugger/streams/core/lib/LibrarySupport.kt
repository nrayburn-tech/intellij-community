// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.debugger.streams.core.lib

import com.intellij.debugger.streams.core.trace.dsl.Dsl

/**
 * @author Vitaliy.Bibaev
 */
interface LibrarySupport {
  fun createHandlerFactory(dsl: Dsl): HandlerFactory
  val interpreterFactory: InterpreterFactory
  val resolverFactory: ResolverFactory
}