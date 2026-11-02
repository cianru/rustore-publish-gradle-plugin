# Rustore API domain migration

Rustore [moves](https://www.rustore.ru/developer/blog/rustore-obnovlyaet-sertifikaty-i-domeny-chto-nuzhno-sdelat-razrabotchikam)
Public API from `https://public-api.rustore.ru` to `https://public-api-m.rustore.ru` and recommends switching
to the new domain after 2026-09-17. The new domain uses certificate issued by Russian Trusted Root CA
(Ministry of Digital Development), which is absent in the default JVM truststore. Both domains still work in parallel.
The plugin uses the new domain by default and trusts Russian Trusted Root CA bundled into the plugin.

Default settings are equal to:

```kotlin
networkSetting {
    apiBaseUrl = "https://public-api-m.rustore.ru"
    useBundledRussianTrustedRootCa = true
}
```

There are other ways to trust the certificate:

1. Install Russian Trusted Root CA from https://www.gosuslugi.ru/crt into the truststore of JVM that runs Gradle
   (e.g. `keytool -importcert -cacerts -alias russian_trusted_root_ca -file russian_trusted_root_ca.cer`)
   or pass a custom truststore with `-Djavax.net.ssl.trustStore` in `org.gradle.jvmargs`.
   The bundled certificate can be disabled by `useBundledRussianTrustedRootCa = false`
   (CLI: `--no-useBundledRussianTrustedRootCa`).
2. Set `networkSetting.trustedCertificatePath` to the certificate file managed by you.

The bundled certificate and `trustedCertificatePath` are added only to plugin requests to Rustore API in addition
to JVM default CAs, JVM global settings are not changed. If TLS connection fails, the plugin throws `RustoreTlsException`
with the list of ways to fix it.

While the old domain works, you can switch back to it:

```kotlin
networkSetting {
    apiBaseUrl = "https://public-api.rustore.ru"
}
```
