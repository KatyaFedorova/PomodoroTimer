package org.timer.main.domain

import kotlinx.datetime.*

fun generateID() = Clock.System.now().toEpochMilliseconds().toString()
