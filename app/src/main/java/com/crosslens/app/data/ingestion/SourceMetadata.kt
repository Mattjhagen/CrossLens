package com.crosslens.app.data.ingestion

/**
 * Factual metadata about a news source for display in event-comparison contexts.
 *
 * IMPORTANT: This metadata is for reader context only. It does NOT infer, assign,
 * or display political ideology. Editorial descriptions are shown only when they
 * have documented provenance (self-description or third-party documentation).
 */
data class SourceMetadata(
    /** Publisher's display name (reader-facing) */
    val publisherName: String,

    /** Country or region where publisher is based */
    val country: String,

    /** Primary language of publication */
    val primaryLanguage: String,

    /** ISO language code (e.g., "en", "fr", "ar") */
    val languageCode: String,

    /**
     * Publisher's editorial description or documented ownership information.
     * Only shown when provenance is documented.
     * Examples: "Public broadcaster", "Private newspaper owned by...",
     * "State-funded international news service"
     */
    val editorialDescription: String? = null,

    /**
     * Source of the editorial description (e.g., "Publisher about page",
     * "Reuters Institute Digital News Report 2025", "Wikipedia")
     */
    val descriptionProvenance: String? = null,

    /** Publisher's homepage URL */
    val homepage: String
)

/**
 * Maps source IDs to their display metadata.
 * In production, this would be a database table with audit trail.
 */
object SourceMetadataRegistry {

    private val metadata = mapOf(
        // Live RSS sources
        "bbc-news-rss" to SourceMetadata(
            publisherName = "BBC News",
            country = "United Kingdom",
            primaryLanguage = "English",
            languageCode = "en-GB",
            editorialDescription = "British public service broadcaster",
            descriptionProvenance = "BBC Royal Charter",
            homepage = "https://www.bbc.co.uk/news"
        ),

        "aljazeera-rss" to SourceMetadata(
            publisherName = "Al Jazeera",
            country = "Qatar",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "State-funded international news service",
            descriptionProvenance = "Al Jazeera corporate profile",
            homepage = "https://www.aljazeera.com"
        ),

        "dw-rss" to SourceMetadata(
            publisherName = "Deutsche Welle",
            country = "Germany",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "German public international broadcaster",
            descriptionProvenance = "Deutsche Welle Act",
            homepage = "https://www.dw.com"
        ),

        "france24-rss" to SourceMetadata(
            publisherName = "France 24",
            country = "France",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "French public international news channel",
            descriptionProvenance = "France 24 corporate profile",
            homepage = "https://www.france24.com"
        ),

        "guardian-rss" to SourceMetadata(
            publisherName = "The Guardian",
            country = "United Kingdom",
            primaryLanguage = "English",
            languageCode = "en-GB",
            editorialDescription = "British newspaper owned by Scott Trust",
            descriptionProvenance = "Guardian corporate structure documentation",
            homepage = "https://www.theguardian.com"
        ),

        "spiegel-rss" to SourceMetadata(
            publisherName = "Der Spiegel International",
            country = "Germany",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "German news magazine",
            descriptionProvenance = "Der Spiegel corporate profile",
            homepage = "https://www.spiegel.de/international"
        ),

        "swissinfo-rss" to SourceMetadata(
            publisherName = "swissinfo.ch",
            country = "Switzerland",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "Swiss public broadcaster SRG SSR's international service",
            descriptionProvenance = "swissinfo.ch about page",
            homepage = "https://www.swissinfo.ch"
        ),

        "abc-es-rss" to SourceMetadata(
            publisherName = "ABC",
            country = "Spain",
            primaryLanguage = "Spanish",
            languageCode = "es",
            editorialDescription = "Spanish daily newspaper",
            descriptionProvenance = "ABC corporate profile",
            homepage = "https://www.abc.es"
        ),

        "nytimes-rss" to SourceMetadata(
            publisherName = "The New York Times",
            country = "United States",
            primaryLanguage = "English",
            languageCode = "en-US",
            editorialDescription = "American newspaper owned by The New York Times Company",
            descriptionProvenance = "NYT corporate structure",
            homepage = "https://www.nytimes.com"
        ),

        "cbc-rss" to SourceMetadata(
            publisherName = "CBC News",
            country = "Canada",
            primaryLanguage = "English",
            languageCode = "en-CA",
            editorialDescription = "Canadian public broadcaster",
            descriptionProvenance = "CBC/Radio-Canada Act",
            homepage = "https://www.cbc.ca"
        ),

        "abc-au-rss" to SourceMetadata(
            publisherName = "ABC News (Australia)",
            country = "Australia",
            primaryLanguage = "English",
            languageCode = "en-AU",
            editorialDescription = "Australian public broadcaster",
            descriptionProvenance = "ABC Charter",
            homepage = "https://www.abc.net.au/news"
        ),

        "japantimes-rss" to SourceMetadata(
            publisherName = "The Japan Times",
            country = "Japan",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "English-language daily newspaper",
            descriptionProvenance = "Japan Times corporate profile",
            homepage = "https://www.japantimes.co.jp"
        ),

        "hindu-rss" to SourceMetadata(
            publisherName = "The Hindu",
            country = "India",
            primaryLanguage = "English",
            languageCode = "en-IN",
            editorialDescription = "Indian daily newspaper",
            descriptionProvenance = "The Hindu corporate profile",
            homepage = "https://www.thehindu.com"
        ),

        "channelnewsasia-rss" to SourceMetadata(
            publisherName = "Channel NewsAsia",
            country = "Singapore",
            primaryLanguage = "English",
            languageCode = "en-SG",
            editorialDescription = "Singaporean news channel owned by Mediacorp",
            descriptionProvenance = "CNA corporate structure",
            homepage = "https://www.channelnewsasia.com"
        ),

        "asahi-rss" to SourceMetadata(
            publisherName = "朝日新聞 (Asahi Shimbun)",
            country = "Japan",
            primaryLanguage = "Japanese",
            languageCode = "ja",
            editorialDescription = "Japanese daily newspaper",
            descriptionProvenance = "Asahi Shimbun corporate profile",
            homepage = "https://www.asahi.com"
        ),

        // Batch 1 Expansion - Additional international sources
        "irishtimes-rss" to SourceMetadata(
            publisherName = "The Irish Times",
            country = "Ireland",
            primaryLanguage = "English",
            languageCode = "en-IE",
            editorialDescription = "Irish newspaper owned by Irish Times Trust",
            descriptionProvenance = "Irish Times Trust documentation",
            homepage = "https://www.irishtimes.com"
        ),

        "washingtonpost-rss" to SourceMetadata(
            publisherName = "The Washington Post",
            country = "United States",
            primaryLanguage = "English",
            languageCode = "en-US",
            editorialDescription = "American newspaper owned by Nash Holdings (Jeff Bezos)",
            descriptionProvenance = "Washington Post ownership documentation",
            homepage = "https://www.washingtonpost.com"
        ),

        "timesofindia-rss" to SourceMetadata(
            publisherName = "The Times of India",
            country = "India",
            primaryLanguage = "English",
            languageCode = "en-IN",
            editorialDescription = "Indian newspaper owned by Bennett, Coleman & Co.",
            descriptionProvenance = "Times of India corporate profile",
            homepage = "https://timesofindia.indiatimes.com"
        ),

        "straitstimes-rss" to SourceMetadata(
            publisherName = "The Straits Times",
            country = "Singapore",
            primaryLanguage = "English",
            languageCode = "en-SG",
            editorialDescription = "Singaporean newspaper owned by SPH Media",
            descriptionProvenance = "SPH Media corporate structure",
            homepage = "https://www.straitstimes.com"
        ),

        "koreaherald-rss" to SourceMetadata(
            publisherName = "The Korea Herald",
            country = "South Korea",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "English-language South Korean newspaper",
            descriptionProvenance = "Korea Herald corporate profile",
            homepage = "http://www.koreaherald.com"
        ),

        "arabnews-rss" to SourceMetadata(
            publisherName = "Arab News",
            country = "Saudi Arabia",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "English-language daily owned by Saudi Research and Marketing Group",
            descriptionProvenance = "Arab News corporate structure",
            homepage = "https://www.arabnews.com"
        ),

        "scmp-rss" to SourceMetadata(
            publisherName = "South China Morning Post",
            country = "Hong Kong SAR",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "Hong Kong newspaper owned by Alibaba Group",
            descriptionProvenance = "SCMP ownership documentation",
            homepage = "https://www.scmp.com"
        ),

        "lemonde-rss" to SourceMetadata(
            publisherName = "Le Monde",
            country = "France",
            primaryLanguage = "French",
            languageCode = "fr",
            editorialDescription = "French daily newspaper owned by Le Monde Group",
            descriptionProvenance = "Le Monde corporate structure",
            homepage = "https://www.lemonde.fr"
        ),

        // Demo sources (must be labeled as fictional)
        "bbc-demo" to SourceMetadata(
            publisherName = "BBC News (Demo)",
            country = "Demo",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "DEMO: Fictional test content",
            descriptionProvenance = "CrossLens test fixture",
            homepage = "https://bbc.example"
        ),

        "lemonde-demo" to SourceMetadata(
            publisherName = "Le Monde (Demo)",
            country = "Demo",
            primaryLanguage = "French",
            languageCode = "fr",
            editorialDescription = "DEMO: Fictional test content",
            descriptionProvenance = "CrossLens test fixture",
            homepage = "https://lemonde.example"
        ),

        "guardian-demo" to SourceMetadata(
            publisherName = "The Guardian (Demo)",
            country = "Demo",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "DEMO: Fictional test content",
            descriptionProvenance = "CrossLens test fixture",
            homepage = "https://guardian.example"
        ),

        "reuters-demo" to SourceMetadata(
            publisherName = "Reuters (Demo)",
            country = "Demo",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "DEMO: Fictional test content",
            descriptionProvenance = "CrossLens test fixture",
            homepage = "https://reuters.example"
        ),

        "ft-demo" to SourceMetadata(
            publisherName = "Financial Times (Demo)",
            country = "Demo",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "DEMO: Fictional test content",
            descriptionProvenance = "CrossLens test fixture",
            homepage = "https://ft.example"
        ),

        "wapo-demo" to SourceMetadata(
            publisherName = "Washington Post (Demo)",
            country = "Demo",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "DEMO: Fictional test content",
            descriptionProvenance = "CrossLens test fixture",
            homepage = "https://washingtonpost.example"
        ),

        "aljazeera-demo" to SourceMetadata(
            publisherName = "Al Jazeera (Demo)",
            country = "Demo",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "DEMO: Fictional test content",
            descriptionProvenance = "CrossLens test fixture",
            homepage = "https://aljazeera.example"
        ),

        // Short-key aliases for mock data compatibility
        "bbc" to SourceMetadata(
            publisherName = "BBC News",
            country = "United Kingdom",
            primaryLanguage = "English",
            languageCode = "en-GB",
            editorialDescription = "British public service broadcaster",
            descriptionProvenance = "BBC Royal Charter",
            homepage = "https://www.bbc.co.uk/news"
        ),

        "nyt" to SourceMetadata(
            publisherName = "The New York Times",
            country = "United States",
            primaryLanguage = "English",
            languageCode = "en-US",
            editorialDescription = "American newspaper owned by The New York Times Company",
            descriptionProvenance = "NYT corporate structure",
            homepage = "https://www.nytimes.com"
        ),

        "lemonde" to SourceMetadata(
            publisherName = "Le Monde",
            country = "France",
            primaryLanguage = "French",
            languageCode = "fr",
            editorialDescription = "French daily newspaper owned by Le Monde Group",
            descriptionProvenance = "Le Monde corporate structure",
            homepage = "https://www.lemonde.fr"
        ),

        "aljazeera" to SourceMetadata(
            publisherName = "Al Jazeera",
            country = "Qatar",
            primaryLanguage = "English",
            languageCode = "en",
            editorialDescription = "State-funded international news service",
            descriptionProvenance = "Al Jazeera corporate profile",
            homepage = "https://www.aljazeera.com"
        ),

        "globe" to SourceMetadata(
            publisherName = "The Globe and Mail",
            country = "Canada",
            primaryLanguage = "English",
            languageCode = "en-CA",
            editorialDescription = "Canadian national newspaper",
            descriptionProvenance = "Globe and Mail corporate profile",
            homepage = "https://www.theglobeandmail.com"
        ),

        "yomiuri" to SourceMetadata(
            publisherName = "読売新聞 (Yomiuri Shimbun)",
            country = "Japan",
            primaryLanguage = "Japanese",
            languageCode = "ja",
            editorialDescription = "Japanese daily newspaper",
            descriptionProvenance = "Yomiuri Shimbun corporate profile",
            homepage = "https://www.yomiuri.co.jp"
        )
    )

    // Mutable registry for testing - only written to by test methods
    private val testMetadata = mutableMapOf<String, SourceMetadata>()

    fun getMetadata(sourceId: String): SourceMetadata? =
        testMetadata[sourceId] ?: metadata[sourceId]

    fun getAllMetadata(): List<Pair<String, SourceMetadata>> = metadata.toList()

    /**
     * TEST ONLY: Register metadata for testing.
     * This allows tests to inject mock metadata without modifying production registry.
     */
    @androidx.annotation.VisibleForTesting
    fun registerForTesting(sourceId: String, metadata: SourceMetadata) {
        testMetadata[sourceId] = metadata
    }

    /**
     * TEST ONLY: Clear all test registrations.
     * Must be called in test teardown to prevent test pollution.
     */
    @androidx.annotation.VisibleForTesting
    fun clearTestMetadata() {
        testMetadata.clear()
    }
}
