package com.github.linuxchina.dotenvx

import com.github.linuxchina.dotenvx.intentions.EncryptEnvValueIntention
import com.intellij.openapi.util.IconLoader

val LOCKER_ICON = IconLoader.getIcon("icons/locker.svg", EncryptEnvValueIntention::class.java)
val VARIABLE_ICON = IconLoader.getIcon("icons/key-value.svg", EncryptEnvValueIntention::class.java)
val KEY_ICON = IconLoader.getIcon("icons/key.svg", EncryptEnvValueIntention::class.java)
