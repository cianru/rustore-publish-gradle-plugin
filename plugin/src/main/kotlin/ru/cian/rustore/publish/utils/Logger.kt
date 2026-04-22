package ru.cian.rustore.publish.utils

private const val LOG_TAG = "Rustore Publishing API"

class Logger(
    private val gradleLogger: org.gradle.api.logging.Logger
) {

    fun v(message: String) {
        println("$LOG_TAG: $message")
    }

    fun e(exception: Exception) {
        exception.printStackTrace()
    }

    fun i(message: String) {
        gradleLogger.info("INFO, $LOG_TAG: $message")
    }
}
