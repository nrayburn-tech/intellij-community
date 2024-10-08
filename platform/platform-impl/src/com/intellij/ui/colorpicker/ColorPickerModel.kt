/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:ApiStatus.Internal

package com.intellij.ui.colorpicker

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.picker.ColorListener
import org.jetbrains.annotations.ApiStatus
import java.awt.Color

val DEFAULT_PICKER_COLOR: Color = Color(0xFF, 0xFF, 0xFF, 0xFF)

class ColorPickerModel(originalColor: Color = DEFAULT_PICKER_COLOR) {

  private val listeners = mutableSetOf<ColorListener>()
  private val pipetteListeners = mutableSetOf<ColorPipette.Callback>()
  private val instantListeners = mutableSetOf<ColorListener>()
  private var changedByUser = false

  var color: Color = originalColor
    private set

  fun setColor(newColor: Color, source: Any? = null) {
    color = newColor
    Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
    if (source != null) {
      changedByUser = true
    }

    instantListeners.forEach { it.colorChanged(color, source) }
  }

  fun onClose() {
    if (changedByUser) {
      ApplicationManager.getApplication().invokeLater {
        listeners.forEach { it.colorChanged(color, this) }
      }
    }
  }

  fun onCancel() {
  }

  fun applyColorToSource(newColor: Color, source: Any? = null) {
    setColor(newColor, source)
    listeners.forEach { it.colorChanged(color, source) }
  }

  private val hsb: FloatArray = Color.RGBtoHSB(color.red, color.green, color.blue, null)

  val red: Int get() = color.red

  val green: Int get() = color.green

  val blue: Int get() = color.blue

  val alpha: Int get() = color.alpha

  val hex: String get() = Integer.toHexString(color.rgb)

  val hue: Float get() = hsb[0]

  val saturation: Float get() = hsb[1]

  val brightness: Float get() = hsb[2]

  fun addListener(listener: ColorListener): Unit = addListener(listener, true)

  fun addListener(listener: ColorListener, invokeOnEveryColorChange: Boolean) {
    listeners.add(listener)
    if (invokeOnEveryColorChange) {
      instantListeners.add(listener)
    }
  }

  fun removeListener(listener: ColorListener) {
    listeners.remove(listener)
    instantListeners.remove(listener)
  }

  fun addPipetteListener(listener: ColorPipette.Callback): Boolean = pipetteListeners.add(listener)

  fun removePipetteListener(listener: ColorPipette.Callback): Boolean = pipetteListeners.remove(listener)

  fun firePipettePicked(pickedColor: Color): Unit = pipetteListeners.forEach { it.picked(pickedColor) }

  fun firePipetteUpdated(updatedColor: Color): Unit = pipetteListeners.forEach { it.update(updatedColor) }

  fun firePipetteCancelled(): Unit = pipetteListeners.forEach { it.cancel() }
}
