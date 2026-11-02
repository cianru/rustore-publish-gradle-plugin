package ru.cian.rustore.publish.utils

const val DATETIME_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX"

/**
 * Default timeouts of requests to Rustore API. Each timeout limits a pause between data packets, not a total request time.
 * Read timeout is long because Rustore API processes build file before response.
 */
const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 30L
const val DEFAULT_READ_TIMEOUT_SECONDS = 300L // 5min
const val DEFAULT_WRITE_TIMEOUT_SECONDS = 60L // 1min

/**
 * Rustore Public API domain with certificate issued by international CA.
 */
const val RUSTORE_API_BASE_URL = "https://public-api.rustore.ru"

/**
 * Rustore Public API domain with certificate issued by Russian Trusted Root CA (Ministry of Digital Development).
 * See https://www.rustore.ru/developer/blog/rustore-obnovlyaet-sertifikaty-i-domeny-chto-nuzhno-sdelat-razrabotchikam
 */
const val RUSTORE_API_BASE_URL_RUSSIAN_CA = "https://public-api-m.rustore.ru"
