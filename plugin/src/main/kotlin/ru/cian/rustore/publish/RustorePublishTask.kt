package ru.cian.rustore.publish

import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import ru.cian.rustore.publish.service.MockRustoreService
import ru.cian.rustore.publish.service.RustoreService
import ru.cian.rustore.publish.service.RustoreServiceImpl
import ru.cian.rustore.publish.utils.BuildFileProvider
import ru.cian.rustore.publish.utils.ConfigProvider
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
        option = "deployType",
        description = "How to deploy build: 'publish' to all users or create 'draft' " +
            "without publishing or 'upload-only' without draft creation"
    )
    var deployType: DeployType? = null

    @get:Internal
    @set:Option(
        option = "credentialsPath",
        description = "File path with AppGallery credentials params ('company_id' and 'client_secret')"
    )
    var credentialsPath: String? = null

    @get:Internal
    @set:Option(
        option = "companyId",
        description = "'company_id' param from AppGallery credentials. " +
            "The key more priority than value from 'credentialsPath'"
    )
    var companyId: String? = null

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
        description = "'apk' or 'aab' for corresponding build format"
    )
    var buildFormat: BuildFormat? = null

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
    @set:Option(option = "apiStub", description = "Use RestAPI stub instead of real RestAPI requests")
    var apiStub: Boolean? = false

    @Suppress("LongMethod")
    @TaskAction
    fun action() {

        val rustoreService: RustoreService = if (apiStub == true) MockRustoreService() else RustoreServiceImpl(logger)
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

        val cli = InputPluginCliParam(
            deployType = deployType,
            credentialsPath = credentialsPath,
            companyId = companyId,
            clientSecret = clientSecret,
            mobileServicesType = mobileServicesType,
            buildFormat = buildFormat,
            buildFile = buildFile,
            releaseTime = releaseTime,
            releasePhasePercent = releasePhasePercent,
            releaseNotes = releaseNotes,
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

        logger.v("Found build file: `${config.artifactFile.name}`")

        logger.v("2/6. Create signature")
        val datetimeFormatPattern = DateTimeFormatter.ofPattern(DATETIME_FORMAT_ISO8601)
        val timestamp = ZonedDateTime.now().format(datetimeFormatPattern)
        val salt = "${config.credentials.companyId}$timestamp"
        val signatureTools: SignatureTools = if (apiStub != true) SignatureToolsImpl() else MockSignatureTools()
        val signature = signatureTools.signData(salt, config.credentials.clientSecret)

        logger.v("3/6. Get Access Token")
        val token = rustoreService.getToken(
            companyId = config.credentials.companyId,
            timestamp = timestamp,
            signature = signature,
        )

        logger.v("4/6. Create App Draft")
        val appVersionId = rustoreService.createDraft(
            token = token,
            applicationId = config.applicationId,
            whatsNew = config.releaseNotes?.first()?.newFeatures ?: "",
        )

        logger.v("5/6. Upload build file '${config.artifactFile}'")
        rustoreService.uploadBuildFile(
            token = token,
            applicationId = config.applicationId,
            mobileServicesType = config.mobileServicesType.value,
            versionId = appVersionId,
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
    }

    internal enum class ReleaseType(val type: Int) {
        FULL(type = 1),
        PHASE(type = 3)
    }

    companion object {
        const val TASK_NAME = "publishRustore"
        private const val DATETIME_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX"
    }
}
