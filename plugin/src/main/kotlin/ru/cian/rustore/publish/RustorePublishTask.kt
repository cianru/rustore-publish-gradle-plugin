package ru.cian.rustore.publish

import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import ru.cian.rustore.publish.service.RustoreBuildFormat
import ru.cian.rustore.publish.service.mock.MockServerWrapperImpl
import ru.cian.rustore.publish.service.mock.MockServerWrapperStub
import ru.cian.rustore.publish.service.RustoreServiceImpl
import ru.cian.rustore.publish.service.mock.MockServerWrapper
import ru.cian.rustore.publish.utils.BuildFileProvider
import ru.cian.rustore.publish.utils.ConfigProvider
import ru.cian.rustore.publish.utils.DATETIME_FORMAT_ISO8601
import ru.cian.rustore.publish.utils.FileWrapper
import ru.cian.rustore.publish.utils.Logger
import ru.cian.rustore.publish.utils.RELEASE_DATE_TIME_FORMAT
import ru.cian.rustore.publish.utils.signature.MockSignatureTools
import ru.cian.rustore.publish.utils.signature.SignatureTools
import ru.cian.rustore.publish.utils.signature.SignatureToolsImpl
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@DisableCachingByDefault
open class RustorePublishTask
@Inject constructor(
    private val variant: ApplicationVariant
) : DefaultTask() {

    init {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        description = "Upload and publish application build file " +
            "to RuStore for ${variant.name} buildType"
    }

    private val logger by lazy { Logger(project) }

    @get:Internal
    @set:Option(
        option = "credentialsPath",
        description = "File path with AppGallery credentials params ('key_id' and 'client_secret')"
    )
    var credentialsPath: String? = null

    @get:Internal
    @set:Option(
        option = "keyId",
        description = "'keyId' param from AppGallery credentials. " +
            "The key more priority than value from 'credentialsPath'"
    )
    var keyId: String? = null

    @get:Internal
    @set:Option(
        option = "clientSecret",
        description = "'client_secret' param from AppGallery credentials. " +
            "The key more priority than value from 'credentialsPath'"
    )
    var clientSecret: String? = null

    @get:Internal
    @set:Option(
        option = "buildFormat",
        description = "'apk' or 'aab' for corresponding build format. " +
            "See https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/upload-aab " +
            "how to prepare project for loading of aab files."
    )
    var buildFormat: BuildFormat? = null

    @get:Internal
    @set:Option(
        option = "requestTimeout",
        description = "The time in seconds to wait for the publication to complete. " +
            "Increase it if you build is a large. Default value is 300 seconds."
    )
    var requestTimeout: String? = null

    @Suppress("MaxLineLength")
    @get:Internal
    @set:Option(
        option = "mobileServicesType",
        description = "Type of mobile services used in application. Available values: [\"Unknown\", \"HMS\"]. " +
            "For more details see param `servicesType` in documentation " +
            "https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/apk-file-upload/file-upload-apk/"
    )
    var mobileServicesType: MobileServicesType? = null

    @get:Internal
    @set:Option(
        option = "buildFile",
        description = "Path to build file. 'null' means use standard path for 'apk' and 'aab' files."
    )
    var buildFile: String? = null

    @get:Internal
    @set:Option(
        option = "releaseTime",
        description = "Release time in UTC format. The format is $RELEASE_DATE_TIME_FORMAT."
    )
    var releaseTime: String? = null

    @get:Internal
    @set:Option(
        option = "releasePhasePercent",
        description = "Percentage of target users of release by phase. The integer or decimal value from 0 to 100."
    )
    var releasePhasePercent: String? = null

    @SuppressWarnings("MaxLineLength")
    @get:Internal
    @set:Option(
        option = "releaseNotes",
        description = "Release Notes. Format: 'ru-RU:<releaseNotes_FilePath_1>'. "
    )
    var releaseNotes: String? = null

    @get:Internal
    @set:Option(
        option = "publishType",
        description = "How to publish build file. Available values: ['manual', 'instantly']."
    )
    var publishType: PublishType? = null

    @SuppressWarnings("MaxLineLength")
    @get:Internal
    @set:Option(
        option = "seoTags",
        description = "List of release SEO tags from ru.cian.rustore.publish.SeoTag. " +
            "Number of tags should not be greater than 5. " +
            "For more details see documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list"
    )
    var seoTags: String? = null

    @get:Internal
    @set:Option(option = "apiStub", description = "Use RestAPI stub instead of real RestAPI requests")
    var apiStub: Boolean? = false

    @Suppress("LongMethod")
    @TaskAction
    fun action() {

        val rustorePublishExtension = project.extensions
            .findByName(RustorePublishExtension.MAIN_EXTENSION_NAME) as? RustorePublishExtension
            ?: throw IllegalArgumentException(
                "Plugin extension '${RustorePublishExtension.MAIN_EXTENSION_NAME}' " +
                    "is not available at build.gradle of the application module"
            )

        val buildTypeName = variant.name
        val extension = rustorePublishExtension.instances.find { it.name.equals(buildTypeName, ignoreCase = true) }
            ?: throw IllegalArgumentException(
                "Plugin extension '${RustorePublishExtension.MAIN_EXTENSION_NAME}' " +
                    "instance with name '$buildTypeName' is not available"
            )

        val cli = RustorePublishCli(
            publishType = publishType,
            credentialsPath = credentialsPath,
            keyId = keyId,
            clientSecret = clientSecret,
            requestTimeout = requestTimeout,
            mobileServicesType = mobileServicesType,
            buildFormat = buildFormat,
            buildFile = buildFile,
            releaseTime = releaseTime,
            releasePhasePercent = releasePhasePercent,
            releaseNotes = releaseNotes,
            seoTags = seoTags?.split(",")?.map { SeoTag.valueOf(it.trim()) },
            apiStub = apiStub,
        )

        logger.i("extension=$extension")
        logger.i("cli=$cli")

        logger.v("1/6. Prepare input config")
        val buildFileProvider = BuildFileProvider(variant = variant, logger = logger)
        val config = ConfigProvider(
            extension = extension,
            cli = cli,
            buildFileProvider = buildFileProvider,
            releaseNotesFileProvider = FileWrapper(),
            applicationId = variant.applicationId.get(),
        ).getConfig()
        logger.i("config=$config")

        val artifactFormat = when (config.artifactFormat) {
            BuildFormat.APK -> RustoreBuildFormat.APK
            BuildFormat.AAB -> RustoreBuildFormat.AAB
        }
        val mockServerWrapper = getMockServerWrapper(config, artifactFormat)
        mockServerWrapper.start()

        val rustoreService = RustoreServiceImpl(
            logger = logger,
            baseEntryPoint = mockServerWrapper.getBaseUrl(),
            requestTimeout = config.requestTimeout,
        )

        logger.v("Found build file: `${config.artifactFile.name}`")

        logger.v("2/6. Create signature")
        val datetimeFormatPattern = DateTimeFormatter.ofPattern(DATETIME_FORMAT_ISO8601)
        val timestamp = ZonedDateTime.now().format(datetimeFormatPattern)
        val salt = "${config.credentials.keyId}$timestamp"
        val signatureTools: SignatureTools = if (apiStub != true) SignatureToolsImpl() else MockSignatureTools()
        val signature = signatureTools.signData(salt, config.credentials.clientSecret)

        logger.v("3/6. Get Access Token")

        val token = rustoreService.getToken(
            keyId = config.credentials.keyId,
            timestamp = timestamp,
            signature = signature,
        )

        logger.v("4/6. Create App Draft")
        val appVersionId = rustoreService.createDraft(
            token = token,
            applicationId = config.applicationId,
            whatsNew = config.releaseNotes?.first()?.newFeatures ?: "",
            publishType = config.publishType.name,
            seoTags = config.seoTags.take(5).map { it.id }
        )

        logger.v("5/6. Upload build file '${config.artifactFile}'")
        rustoreService.uploadApkBuildFile(
            token = token,
            applicationId = config.applicationId,
            mobileServicesType = config.mobileServicesType.value,
            versionId = appVersionId,
            artifactFormat = artifactFormat,
            buildFile = config.artifactFile
        )

        logger.v("6/6. Submit publication")
        val summitResult = rustoreService.submit(
            token = token,
            applicationId = config.applicationId,
            versionId = appVersionId,
            priorityUpdate = 5,
        )

        if (summitResult) {
            logger.v("Upload and submit build file - Successfully Done!")
        } else {
            logger.v("Upload and submit build file - Failed!")
        }

        mockServerWrapper.shutdown()
    }

    private fun getMockServerWrapper(
        config: PluginConfig,
        artifactFormat: RustoreBuildFormat
    ): MockServerWrapper {
        return if (apiStub == true) {
            MockServerWrapperImpl(
                logger = logger,
                applicationId = config.applicationId,
                requestFormArgument = artifactFormat.fileExtension,
            )
        } else {
            MockServerWrapperStub()
        }
    }

    internal enum class ReleaseType(val type: Int) {
        FULL(type = 1),
        PHASE(type = 3)
    }

    companion object {

        const val TASK_NAME = "publishRustore"
    }
}
