package ru.cian.rustore.publish

import groovy.lang.Closure
import org.gradle.api.Project

open class RustorePublishExtension(
    project: Project
) {

    val instances = project.container(RustorePublishExtensionConfig::class.java) { name ->
        RustorePublishExtensionConfig(name, project)
    }

    companion object {

        const val MAIN_EXTENSION_NAME = "rustorePublish"
    }
}

/**
 * For required property use GradleProperty class instance.
 * For example:
 *  var param by GradleProperty(project, String::class.java)
 */
class RustorePublishExtensionConfig(
    val name: String,
    val project: Project
) {

    /**
     * (Required)
     * Path to json file with RuStore credentials params (`key_id` and `client_secret`).
     * How to get credentials see [[RU] Rustore API Getting Started](https://www.rustore.ru/help/work-with-rustore-api/api-authorization-process/).
     * Plugin credential json example:
     * {
     *   "key_id": "<KEY_ID>",
     *   "client_secret": "<CLIENT_SECRET>"
     * }
     *
     * Type: String (Optional)
     * Default value: `null` (but plugin wait that you provide credentials by CLI params)
     * CLI: `--credentialsPath`
     */
    var credentialsPath: String? = null

    /**
     * (Optional)
     * CLI: `--publishType`
     * ----| 'instantly' – the application will be published immediately after the review process is completed.
     * ----| 'manual' – the application must be published manually by the developer after ther review process is completed.
     * Gradle Extension DSL, available values:
     * ----| ru.cian.rustore.publish.PublishType.INSTANTLY
     * ----| ru.cian.rustore.publish.PublishType.MANUAL
     * Default value: `instantly`
     */
    var publishType = PublishType.INSTANTLY

    /**
     * (Optional)
     * The time in seconds to wait for the publication to complete. Increase it if you build is large.
     * Type: Long (Optional)
     * Default value: `300` // (5min)
     * CLI: `--requestTimeout`
     */
    var requestTimeout: Long? = null

    /**
     * (Optional)
     * Type of mobile services used in application.
     * For more details see param `servicesType` in documentation:
     * https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/apk-file-upload/file-upload-apk/
     * CLI: `--mobileServicesType`
     * ----| 'Unknown'
     * ----| 'HMS'
     * Gradle Extension DSL, available values:
     * ----| ru.cian.rustore.publish.MobileServicesType.UNKNOWN
     * ----| ru.cian.rustore.publish.MobileServicesType.HMS
     * Default value: `Unknown`
     */
    var mobileServicesType: MobileServicesType = MobileServicesType.UNKNOWN

    /**
     * (Required)
     * Build file format.
     * See https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/upload-aab how to prepare project for loading of aab files.
     * Type: String (Optional)
     * CLI: `--buildFormat`, available values:
     * ----| 'apk'
     * ----| 'aab'
     * Gradle Extension DSL, available values:
     * ----| ru.cian.rustore.publish.BuildFormat.APK
     * ----| ru.cian.rustore.publish.BuildFormat.AAB
     * Default value: `apk`
     */
    var buildFormat: BuildFormat = BuildFormat.APK

    /**
     * (Optional)
     * Path to build file if you would like to change default path. "null" means use standard path for "apk" and "aab" files.
     * Type: String (Optional)
     * Default value: `null`
     * CLI: `--buildFile`
     */
    var buildFile: String? = null

    /**
     * (Optional)
     * Release Notes settings. For mote info see ReleaseNote param desc.
     * Type: List<ReleaseNote> (Optional)
     * Default value: `null`
     * CLI: (see ReleaseNotes param desc.)
     */
    var releaseNotes: List<ReleaseNote>? = null

    var releasePhase: ReleasePhaseExtension? = null

    /**
     * (Optional)
     * List of available SEO tags for RuStore app listing.
     * For more details see documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list
     * Number of tags should not be greater than 5.
     * Default value: []
     * CLI: `--seoTags`. For example: `--seoTags=LIFESTYLE,ROMANTIC`
     * Gradle Extension DSL, available values from ru.cian.rustore.publish.SeoTag
     */
    var seoTags: List<SeoTag> = emptyList()

    /**
     * (Required)
     * See description in https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version
     * Type: String
     * Default value: `null` (but plugin wait that you provide credentials by CLI params)
     * CLI: `--minAndroidVersion`
     */
    var minAndroidVersion: String = "8"

    /**
     * (Required)
     * Information about Developer.
     * See description in https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version
     * Type: ru.cian.rustore.publish.DeveloperContacts (Required)
     * Default value: `null`
     * CLI: (see DeveloperContacts param desc.)
     */
    var developerContacts: DeveloperContacts? = null

    init {
        require(name.isNotBlank()) {
            "Name must not be blank nor empty"
        }
    }

    fun releasePhase(closure: Closure<ReleasePhaseExtension>): ReleasePhaseExtension {
        releasePhase = ReleasePhaseExtension()
        project.configure(releasePhase!!, closure)
        return releasePhase!!
    }

    override fun toString(): String {
        return "RustorePublishExtensionConfig(" +
            "name='$name', " +
            "credentialsPath='$credentialsPath', " +
            "publishType='$publishType', " +
            "requestTimeout='$requestTimeout', " +
            "mobileServicesType='$mobileServicesType', " +
            "seoTags='$seoTags', " +
            "buildFormat='$buildFormat', " +
            "buildFile='$buildFile', " +
            "releasePhase='$releasePhase', " +
            "releaseNotes='$releaseNotes', " +
            "minAndroidVersion='$minAndroidVersion', " +
            "developerContacts='$developerContacts'" +
            ")"
    }
}

open class ReleasePhaseExtension {

    var percent: Double? = null

    constructor()

    constructor(percent: Double?) {
        this.percent = percent
    }

    override fun toString(): String {
        return "ReleasePhaseConfig(" +
            "percent='$percent'" +
            ")"
    }
}

open class ReleaseNote {

    lateinit var lang: String
    lateinit var filePath: String

    constructor()

    constructor(lang: String, filePath: String) {
        this.lang = lang
        this.filePath = filePath
    }

    override fun toString(): String {
        return "ReleaseNote(" +
            "lang='$lang', " +
            "filePath='$filePath'" +
            ")"
    }
}

open class DeveloperContacts {

    lateinit var email: String
    var website: String? = null
    var vkCommunity: String? = null

    constructor()

    constructor(email: String, website: String?, vkCommunity: String?) {
        this.email = email
        this.website = website
        this.vkCommunity = vkCommunity
    }

    override fun toString(): String {
        return "DeveloperContacts(" +
            "email='$email', " +
            "website='$website', " +
            "vkCommunity='$vkCommunity'" +
            ")"
    }
}

enum class BuildFormat(val fileExtension: String) {
    APK("apk"),
    AAB("aab"),
}

enum class MobileServicesType(val value: String) {
    HMS("HMS"),
    UNKNOWN("Unknown"),
}

enum class PublishType {
    /**
     * Manual publication. After review you should publish it manually;
     */
    MANUAL,

    /**
     * Automatically publish on all users after reviewing store approve;
     */
    INSTANTLY,

    /**
     * Delayed publication. You should set publishDateTime;
     */
//    DELAYED, // FIXME: Implement delayed publication after adding of `publishDateTime` API param;
}

/**
 * List of available SEO tags for RuStore app listing.
 * For more details see documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list
 */
enum class SeoTag(val id: Int) {
    X4(1),
    AUTOMOBILES(2),
    GAMBLING(3),
    AMERICAN_FOOTBALL(4),
    ANIME(5),
    APOCALYPSE(6),
    ARKANOID(7),
    BILLIARDS(8),
    BLACKJACK(9),
    BOWLING(10),
    MILITARY_GAMES(11),
    EASTERN_FANTASY(12),
    HEROIC_SHOOTERS(13),
    HYPER_CASUAL_GAME(14),
    RACING_SIMULATORS(15),
    URBAN_FANTASY(16),
    CITY_BUILDING(17),
    HOUSING_AND_HOME(18),
    ZOMBIES(19),
    SURVIVAL_HORROR_GAMES(20),
    ROGUELIKE_GENRE(21),
    TOWER_DEFENSE_GENRE(22),
    SURVIVAL_GAMES(23),
    INTELLECTUAL_GAMES(24),
    INTERACTIVE_STORIES(25),
    KARTING(26),
    CARD_BATTLES(27),
    PUZZLE_QUESTS(28),
    CYBERPUNK(29),
    COOPERATIVE_MULTIPLAYER(30),
    SPACE(31),
    DICE(32),
    TIC_TAC_TOE(33),
    CROSSWORDS(34),
    LOGIC_GAME(35),
    MAGIC(36),
    MYTHOLOGY(37),
    MMORPG(38),
    MULTIPLAYER_GAMES(39),
    SCIENCE(40),
    SCIENCE_FICTION(41),
    NINJA(42),
    EDUCATIONAL(43),
    SINGLE_PLAYER(44),
    OFFLINE(45),
    PARKOUR(46),
    SOLITAIRE(47),
    SPIDER_SOLITAIRE(48),
    MAHJONG_SOLITAIRE(49),
    SANDBOXES(50),
    PIXEL_GRAPHICS(51),
    PIRATE(52),
    PLATFORMERS(53),
    ESCAPE_SEARCH(54),
    OBJECT_SEARCH(55),
    DIFFERENCE_SEARCH(56),
    PAIR_SEARCH(57),
    WORD_SEARCH(58),
    MATCH_SEARCH(59),
    POKER(60),
    POP_IT(61),
    TURN_BASED_RPG(62),
    MEMORY_DEVELOPMENT(63),
    RUNNERS(64),
    REALISM(65),
    DRAWING(66),
    ACTION_RPG(67),
    IDLE_RPG(68),
    ROMANTIC(69),
    FISHING(70),
    LIFE_SIMULATOR(71),
    ITEM_MATCHING(72),
    COMPETITIVE(73),
    BATTLES(74),
    MEDIEVAL_FANTASY(75),
    STEAMPUNK(76),
    ARCHERY(77),
    BALL_SHOOTING(78),
    CONSTRUCTION(79),
    SUDOKU(80),
    SUPERHEROES(81),
    TACTICAL_GAMES(82),
    TACTICAL_SHOOTERS(83),
    MATCH_3(84),
    FIGHTING(85),
    FARM(86),
    FOOTBALL(87),
    FANTASY(88),
    HOCKEY(89),
    CHESS(90),
    CHECKERS(91),
    ECONOMIC_STRATEGY(92),
    EXTREME_RIDING(93),
    IDLE_GAME(94),
    MOBA(95),
    SHMUP(96),
    BEAUTY(97),
    HOROSCOPES(98),
    LANGUAGE_LEARNING(99),
    INVESTMENTS(100),
    COSMETICS(101),
    PERSONAL_FINANCE(102),
    MEDITATION(103),
    WALLPAPERS(104),
    JOB_SEARCH(105),
    WEIGHT_LOSS(106),
    LOYALTY_PROGRAMS(107),
    RADIO(108),
    SELF_IMPROVEMENT(109),
    SLEEP(110),
    TRAINING(111),
    MOVIES(112),
    PHOTO_EDITORS(113),
    HUMOR(114),
    WORK(115),
    LIFESTYLE(116),
    SOCIAL(117),
    COMMUNICATION(118),
    MEDICINE(119),
    PHOTOGRAPHY(120),
    MOTHERHOOD_AND_CHILDHOOD(121),
    DATING(122),
    WEATHER(123),
    COLORING(124),
    AUDIOBOOKS(125),
    RUNNING(126),
    COMICS(127),
    RESTAURANTS(128),
    EMOJI(129),
    HOME_AUTOMATION(130),
    NEWS_AGGREGATORS(131),
    CINEMA_TICKETS_REVIEWS_AND_POSTERS(132),
    BLOGS(133),
    NOTEBOOKS(134),
    WEB_BROWSERS(135),
    VIDEO_CALLS(136),
    VIDEO_PLAYERS(137),
    HOTELS_AND_VACATION_RENTALS(138),
    GRAMMAR(139),
    CHILDREN_LITERATURE(140),
    INTERIOR_DESIGN(141),
    FOOD_DELIVERY(142),
    PRESCHOOL_EDUCATION(143),
    AUDIO_RECORDING(144),
    CALL_RECORDING(145),
    YOGA(146),
    CALENDAR(147),
    CALCULATORS(148),
    KEYBOARD(149),
    PERSONAL_ASSISTANTS(150),
    MOBILE_PAYMENTS(151),
    PRIMARY_EDUCATION(152),
    MESSAGING(153),
    DRIVING_LEARNING(154),
    PUBLIC_TRANSPORT(155),
    CAR_BUYING(156),
    VIDEO_STREAMING(157),
    TRAVEL_GUIDES(158),
    VIDEO_EDITING(159),
    RECIPES(160),
    VIDEO_DOWNLOADING(161),
    EVENTS(162),
    INSURANCE(163),
    TELEVISION(164),
    FITNESS_TRACKERS(165),
    CHILD_CARE(166),
    FLASHLIGHTS(167),
    CLOCKS_ALARM_AND_TIMER(168),
    EMAIL(169),
    ENCYCLOPEDIAS(170),
    MATHEMATICS(171),
    FASHION(172)
}
