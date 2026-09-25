package org.timer.main.timer

import kotlinx.browser.document
import kotlinx.datetime.*
import kotlinx.datetime.format.*
import org.w3c.dom.Worker

actual class DefaultCountDownTimer actual constructor(
    actual var totalMillis: Long,
    actual val onTick: (Long) -> Unit,
    actual val onFinish: () -> Unit,
    actual val isRunning: (Boolean) -> Unit,
) : CountDownTimer {
    actual var currentMillis: Long = totalMillis
    private val initalTotalMillis = totalMillis
    init {
        isRunning.invoke(false)
    }

    private var worker: Worker? = null
    private var startMoment: Long = 0

    actual override fun startTimer() {
        isRunning.invoke(true)
        startMoment = Clock.System.now().toEpochMilliseconds()
        worker = Worker(
            "data:text/javascript," +
                    "onmessage = function (e) {\n" +
                    "    let count = setInterval(function () {\n" +
                    "        postMessage(null);\n" +
                    "    }, 1000);\n" +
                    "}"
        )
        worker?.postMessage(null)
        worker?.onmessage = { onTick() }
    }

    private fun onTick() {
        val timePass = ((Clock.System.now().toEpochMilliseconds() - startMoment).toDouble() / 1000).toLong() * 1000
        currentMillis = totalMillis - timePass

        if (currentMillis <= 0) {
            document.title = 0L.formatToMMSS()
            onTick.invoke(0)
            onFinish.invoke()
            isRunning.invoke(false)
            worker?.terminate()
        } else {
            document.title = currentMillis.formatToMMSS()
            onTick.invoke(currentMillis)
        }
    }

    actual override fun pauseTimer() {
        isRunning.invoke(false)
        totalMillis = currentMillis
        worker?.terminate()
    }

    actual override fun resetTimer(){
        isRunning.invoke(false)
        totalMillis = initalTotalMillis
        currentMillis = initalTotalMillis
        document.title = initalTotalMillis.formatToMMSS()
        onTick.invoke(initalTotalMillis)
        worker?.terminate()
    }

    actual override fun isFinished() : Boolean{
        return currentMillis <= 0L
    }

    private fun Long.formatToMMSS(): String {
        val time = LocalTime.fromMillisecondOfDay(this.toInt())
        return if (time.hour >= 1) {
            time.format(LocalTime.Format {
                hour()
                char(':')
                minute()
                char(':')
                second()
            })
        } else {
            time.format(LocalTime.Format {
                minute()
                char(':')
                second()
            })
        }
    }

    actual override fun getCurrentTimeMillis(): Long = currentMillis

}

