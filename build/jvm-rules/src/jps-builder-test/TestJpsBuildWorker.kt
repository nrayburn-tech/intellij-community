// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("TestOnlyProblems")

package org.jetbrains.bazel.jvm.jps.test

import org.apache.arrow.memory.RootAllocator
import org.jetbrains.bazel.jvm.TestModules
import org.jetbrains.bazel.jvm.collectSources
import org.jetbrains.bazel.jvm.configureOpenTelemetry
import org.jetbrains.bazel.jvm.getTestWorkerPaths
import org.jetbrains.bazel.jvm.jps.buildUsingJps
import org.jetbrains.bazel.jvm.jps.configureGlobalJps
import org.jetbrains.bazel.jvm.kotlin.JvmBuilderFlags
import org.jetbrains.bazel.jvm.kotlin.parseArgs
import org.jetbrains.bazel.jvm.performTestInvocation
import org.jetbrains.bazel.jvm.span
import org.jetbrains.jps.incremental.dependencies.DependencyAnalyzer
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi

internal object TestJpsBuildWorker {
  @OptIn(ExperimentalPathApi::class)
  @JvmStatic
  fun main(startupArgs: Array<String>) {
    val testPaths = getTestWorkerPaths()
    val baseDir = testPaths.baseDir

    val testModule = TestModules.LANG_IMPL
    val sources = testModule.sourcePaths.flatMap { collectSources(sourceDirPath = it, paths = testPaths) }
    require(sources.isNotEmpty())
    val testParams = testModule.getParams(baseDir)

    performTestInvocation { out, coroutineScope ->
      // IDEA console is bad and outdated, write to file and use modern tooling to view logs
      val logFile = testPaths.userHomeDir.resolve("kotlin-worker/log.jsonl")
      Files.createDirectories(logFile.parent)
      val openTelemetryAndOnClose = configureOpenTelemetry(Files.newOutputStream(logFile), "test-builder")
      val tracer = openTelemetryAndOnClose.first.getTracer("test-builder")
      configureGlobalJps(tracer, coroutineScope)

      val args = parseArgs(testParams.lines().toTypedArray())

      println("out jar: " + baseDir.resolve(args.mandatorySingle(JvmBuilderFlags.OUT)).normalize())

      val messageDigest = MessageDigest.getInstance("SHA-512")
      val exitCode = RootAllocator(Long.MAX_VALUE).use { allocator ->
        tracer.span("build") { span ->
          buildUsingJps(
            forceIncremental = true,
            baseDir = baseDir,
            args = args,
            out = out,
            sources = sources,
            dependencyFileToDigest = args.optionalList(JvmBuilderFlags.CP).associate {
              val file = baseDir.resolve(it).normalize()
              val digest = messageDigest.digest(Files.readAllBytes(file))
              messageDigest.reset()
              file to digest
            },
            sourceFileToDigest = sources.associate {
              val file = baseDir.resolve(it).normalize()
              val digest = messageDigest.digest(Files.readAllBytes(file))
              messageDigest.reset()
              file to digest
            },
            isDebugEnabled = true,
            allocator = allocator,
            parentSpan = span,
            tracer = tracer,
            cachePrefix = "test-builder-",
            dependencyAnalyzer = DependencyAnalyzer(coroutineScope),
          )
        }
      }

      openTelemetryAndOnClose.second()

      exitCode
    }
  }
}