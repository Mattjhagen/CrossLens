package com.crosslens.app.data.recommendation

import com.crosslens.app.core.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class PersonalizedRecommendationEngineTest {

    private lateinit var engine: PersonalizedRecommendationEngine
    private lateinit var sampleStories: List<Story>

    @Before
    fun setup() {
        engine = PersonalizedRecommendationEngine()

        // Create sample stories with different topics and regions
        sampleStories = listOf(
            createStory(
                id = "tech1",
                title = "AI Breakthrough in Seattle",
                topicIds = listOf("tech"),
                countries = listOf("US")
            ),
            createStory(
                id = "climate1",
                title = "Climate Summit in Paris",
                topicIds = listOf("climate"),
                countries = listOf("FR")
            ),
            createStory(
                id = "politics1",
                title = "Election Updates from London",
                topicIds = listOf("politics"),
                countries = listOf("GB")
            ),
            createStory(
                id = "tech2",
                title = "Startup Funding Round in Toronto",
                topicIds = listOf("tech", "business"),
                countries = listOf("CA")
            ),
            createStory(
                id = "health1",
                title = "Health Policy Reform",
                topicIds = listOf("health", "politics"),
                countries = listOf("US")
            )
        )
    }

    @Test
    fun `eligibility requires at least one MORE preference or location`() {
        // Not eligible with no preferences or location
        assertFalse(engine.isEligible(emptyList(), null))

        // Eligible with one MORE preference
        val morePrefs = listOf(createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC))
        assertTrue(engine.isEligible(morePrefs, null))

        // Not eligible with only LESS preferences
        val lessPrefs = listOf(createPreference("politics", PreferenceType.LESS, DimensionType.TOPIC))
        assertFalse(engine.isEligible(lessPrefs, null))

        // Eligible with location
        assertTrue(engine.isEligible(emptyList(), "seattle_wa_us"))
    }

    @Test
    fun `generateRecommendations returns empty when not eligible`() {
        val result = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = emptyList(),
            demoLocalLocation = null
        )

        assertFalse(result.isEligible)
        assertTrue(result.recommendations.isEmpty())
    }

    @Test
    fun `generateRecommendations ranks by topic preference`() {
        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC)
        )

        val result = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        assertTrue(result.isEligible)
        assertTrue(result.recommendations.isNotEmpty())

        // First recommendation should be a tech story
        val topRecommendation = result.recommendations.first()
        assertTrue(topRecommendation.story.topicIds.contains("tech"))
        assertTrue(topRecommendation.explanation.contains("Technology"))
        assertTrue(topRecommendation.score > 0)
    }

    @Test
    fun `generateRecommendations excludes LESS preferences`() {
        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC),
            createPreference("politics", PreferenceType.LESS, DimensionType.TOPIC)
        )

        val result = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        // Should not include any politics stories
        result.recommendations.forEach { rec ->
            assertFalse(rec.story.topicIds.contains("politics"))
        }
    }

    @Test
    fun `generateRecommendations excludes LESS region preferences`() {
        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC),
            createPreference("GB", PreferenceType.LESS, DimensionType.REGION)
        )

        val result = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        // Should not include any GB stories
        result.recommendations.forEach { rec ->
            assertFalse(rec.story.eventCountryCodes.contains("GB"))
        }
    }

    @Test
    fun `generateRecommendations ranks by region preference`() {
        val preferences = listOf(
            createPreference("FR", PreferenceType.MORE, DimensionType.REGION)
        )

        val result = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        assertTrue(result.isEligible)
        assertTrue(result.recommendations.isNotEmpty())

        // Should include FR story with appropriate explanation
        val frRecommendation = result.recommendations.find { it.story.eventCountryCodes.contains("FR") }
        assertNotNull(frRecommendation)
        assertTrue(frRecommendation!!.explanation.contains("FR coverage"))
    }

    @Test
    fun `generateRecommendations prioritizes local location`() {
        val preferences = listOf(
            createPreference("climate", PreferenceType.MORE, DimensionType.TOPIC)
        )

        // Create a story that matches Seattle location
        val localStory = createStory(
            id = "local1",
            title = "Tech Development in Seattle",
            summary = "Seattle-based company announces expansion",
            topicIds = listOf("tech"),
            countries = listOf("US")
        )

        val storiesWithLocal = sampleStories + localStory

        val result = engine.generateRecommendations(
            allStories = storiesWithLocal,
            preferences = preferences,
            demoLocalLocation = "seattle_wa_us"
        )

        assertTrue(result.isEligible)

        // Local stories should get higher scores (15 points) than topic matches (10 points)
        val localRec = result.recommendations.find { it.story.id == "local1" }
        if (localRec != null) {
            assertTrue(localRec.explanation.contains("Seattle local-news selection"))
            assertTrue(localRec.score >= 15)
        }
    }

    @Test
    fun `generateRecommendations limits to 5 recommendations`() {
        // Create many matching stories
        val manyStories = (1..20).map { i ->
            createStory(
                id = "story$i",
                title = "Tech Story $i",
                topicIds = listOf("tech"),
                countries = listOf("US")
            )
        }

        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC)
        )

        val result = engine.generateRecommendations(
            allStories = manyStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        assertTrue(result.recommendations.size <= 5)
    }

    @Test
    fun `generateRecommendations is deterministic with same inputs`() {
        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC),
            createPreference("climate", PreferenceType.MORE, DimensionType.TOPIC)
        )

        val result1 = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        val result2 = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        // Same order, same scores
        assertEquals(result1.recommendations.size, result2.recommendations.size)
        result1.recommendations.zip(result2.recommendations).forEach { (rec1, rec2) ->
            assertEquals(rec1.story.id, rec2.story.id)
            assertEquals(rec1.score, rec2.score)
            assertEquals(rec1.explanation, rec2.explanation)
        }
    }

    @Test
    fun `generateRecommendations combines multiple topic matches`() {
        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC),
            createPreference("business", PreferenceType.MORE, DimensionType.TOPIC)
        )

        val result = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        // Story with both tech and business should score higher
        val multiTopicStory = result.recommendations.find { it.story.id == "tech2" }
        assertNotNull(multiTopicStory)
        // Should get 20 points (10 per topic match)
        assertEquals(20, multiTopicStory!!.score)
    }

    @Test
    fun `all recommendations have clear explanations`() {
        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC),
            createPreference("US", PreferenceType.MORE, DimensionType.REGION)
        )

        val result = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        // Every recommendation must have a non-empty explanation
        result.recommendations.forEach { rec ->
            assertFalse(rec.explanation.isEmpty())
            assertFalse(rec.explanation == "Recommended for you") // Should have specific reason
        }
    }

    @Test
    fun `refresh preserves recommendations with same preferences`() {
        val preferences = listOf(
            createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC)
        )

        val result1 = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        // Simulate refresh - same stories, same preferences
        val result2 = engine.generateRecommendations(
            allStories = sampleStories,
            preferences = preferences,
            demoLocalLocation = null
        )

        // Results should be identical
        assertEquals(result1.recommendations.size, result2.recommendations.size)
        assertEquals(result1.recommendations.map { it.story.id }, result2.recommendations.map { it.story.id })
    }

    @Test
    fun `removing MORE preference makes user ineligible if no location`() {
        // Start with preference
        val withPref = listOf(createPreference("tech", PreferenceType.MORE, DimensionType.TOPIC))
        assertTrue(engine.isEligible(withPref, null))

        // Remove preference
        assertFalse(engine.isEligible(emptyList(), null))
    }

    @Test
    fun `location alone makes user eligible`() {
        assertTrue(engine.isEligible(emptyList(), "seattle_wa_us"))
        assertTrue(engine.isEligible(emptyList(), "paris_idf_fr"))
        assertTrue(engine.isEligible(emptyList(), "london_eng_gb"))
        assertTrue(engine.isEligible(emptyList(), "toronto_on_ca"))
    }

    // Helper functions
    private fun createStory(
        id: String,
        title: String,
        summary: String = "Summary for $title",
        topicIds: List<String> = emptyList(),
        countries: List<String> = emptyList()
    ): Story {
        return Story(
            id = id,
            title = title,
            summary = summary,
            eventTime = null,
            updatedTime = Instant.now(),
            topicIds = topicIds,
            eventCountryCodes = countries,
            articleIds = listOf("art1", "art2", "art3"),
            claimIds = emptyList(),
            lensGapAssessment = LensGapAssessment(
                storyId = id,
                status = LensGapStatus.AVAILABLE,
                score = 3,
                components = listOf("Framing", "Emphasis"),
                sampleSourceIds = listOf("src1", "src2"),
                sampleArticleIds = listOf("art1", "art2"),
                coverageWindow = "48h",
                confidence = "Demo",
                limitations = "Mock data",
                methodVersion = "v1.0",
                generatedTime = Instant.now(),
                isDemo = true
            )
        )
    }

    private fun createPreference(
        value: String,
        type: PreferenceType,
        dimension: DimensionType
    ): PersonalRelevancePreference {
        return PersonalRelevancePreference(
            id = "${type.name}_${dimension.name}_$value",
            preferenceType = type,
            dimensionType = dimension,
            dimensionValue = value,
            createdTime = Instant.now()
        )
    }
}
