package ru.cian.rustore.publish.utils

import java.lang.Exception
import org.gradle.api.logging.Logger

private const val LOG_TAG = "Rustore Publishing API"

class RustoreLogger(
    private val logger: Logger?
) {

    fun v(message: String) {
        println("$LOG_TAG: $message")
    }

    fun e(exception: Exception) {
        exception.printStackTrace()
    }

    fun i(message: String) {
        val line = "INFO, $LOG_TAG: $message"
        if (logger != null) {
            logger.info(line)
        } else {
            println(line)
        }
    }
}
