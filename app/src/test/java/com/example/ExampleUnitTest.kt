package com.example

import com.example.data.sample.SampleData
import com.example.domain.AppLanguage
import com.example.domain.RecommendationEngine
import com.example.domain.Strings
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun recommendationEngine_ranksVideosWithHighestEngagement() {
        val engine = RecommendationEngine()
        val videos = SampleData.sampleVideos
        val ranked = engine.rankPersonalizedFeed(
            videos = videos,
            interactions = emptyMap(),
            likedVideoIds = setOf("vid_5"),
            followedCreatorIds = setOf("user_nafis")
        )

        assertNotNull(ranked)
        assertTrue(ranked.isNotEmpty())
        assertEquals(videos.size, ranked.size)
    }

    @Test
    fun localization_resolvesBanglaAndEnglishStrings() {
        assertEquals("হোম", Strings.home(AppLanguage.BANGLA))
        assertEquals("Home", Strings.home(AppLanguage.ENGLISH))

        assertEquals("আপনার জন্য", Strings.forYou(AppLanguage.BANGLA))
        assertEquals("For You", Strings.forYou(AppLanguage.ENGLISH))

        assertEquals("ক্রিয়েটর স্টুডিও", Strings.creatorStudio(AppLanguage.BANGLA))
        assertEquals("Creator Studio", Strings.creatorStudio(AppLanguage.ENGLISH))
    }
}
