package com.example.domain

import com.example.data.local.VideoEntity
import com.example.data.local.VideoInteractionEntity
import kotlin.math.exp
import kotlin.math.max

data class RankedVideo(
    val video: VideoEntity,
    val recommendationScore: Double,
    val rankReason: String
)

class RecommendationEngine {
    // Configurable weights as per architectural specifications
    private val weightWatchTime = 0.30
    private val weightEngagement = 0.25
    private val weightCompletion = 0.20
    private val weightShares = 0.15
    private val weightRecency = 0.10

    /**
     * Ranks videos for the For You personalized feed.
     * Computes normalized scores using interaction telemetry, user preferences, and freshness.
     */
    fun rankPersonalizedFeed(
        videos: List<VideoEntity>,
        interactions: Map<String, VideoInteractionEntity>,
        likedVideoIds: Set<String>,
        followedCreatorIds: Set<String>,
        preferredCategories: Set<String> = emptySet(),
        seenVideoIds: Set<String> = emptySet()
    ): List<VideoEntity> {
        if (videos.isEmpty()) return emptyList()

        // Filter out "not interested" videos
        val validVideos = videos.filter { video ->
            val interaction = interactions[video.id]
            interaction?.notInterested != true
        }

        val currentTime = System.currentTimeMillis()
        val maxViews = max(1, validVideos.maxOfOrNull { it.viewsCount } ?: 1)
        val maxLikes = max(1, validVideos.maxOfOrNull { it.likesCount } ?: 1)
        val maxComments = max(1, validVideos.maxOfOrNull { it.commentsCount } ?: 1)
        val maxShares = max(1, validVideos.maxOfOrNull { it.sharesCount } ?: 1)

        val rankedList = validVideos.map { video ->
            val interaction = interactions[video.id]

            // 1. Normalized Watch Time (0.0 to 1.0)
            val expectedDurationMs = (video.durationSeconds * 1000).toLong()
            val watchTime = interaction?.watchTimeMs ?: 0L
            val normWatchTime = if (expectedDurationMs > 0) {
                (watchTime.toDouble() / expectedDurationMs.toDouble()).coerceIn(0.0, 1.5) / 1.5
            } else 0.5

            // 2. Normalized Engagement (Likes + Comments + Saved)
            val likeRatio = (video.likesCount.toDouble() / maxLikes).coerceIn(0.0, 1.0)
            val commentRatio = (video.commentsCount.toDouble() / maxComments).coerceIn(0.0, 1.0)
            val userLikedBonus = if (likedVideoIds.contains(video.id)) 0.3 else 0.0
            val normEngagement = ((likeRatio * 0.6 + commentRatio * 0.4) + userLikedBonus).coerceIn(0.0, 1.0)

            // 3. Completion & Rewatch Rate
            val completionCount = interaction?.completionCount ?: 0
            val rewatchCount = interaction?.rewatchCount ?: 0
            val normCompletion = ((completionCount * 0.7 + rewatchCount * 0.3) / 2.0).coerceIn(0.0, 1.0)

            // 4. Shares (strong viral signal)
            val normShares = (video.sharesCount.toDouble() / maxShares).coerceIn(0.0, 1.0)

            // 5. Recency (exponential decay over 7 days)
            val ageHours = max(0L, (currentTime - video.createdAt) / 3600000)
            val normRecency = exp(-ageHours.toDouble() / 48.0).coerceIn(0.0, 1.0)

            // Base ranking formula
            var score = (weightWatchTime * normWatchTime) +
                    (weightEngagement * normEngagement) +
                    (weightCompletion * normCompletion) +
                    (weightShares * normShares) +
                    (weightRecency * normRecency)

            // Personalization boost: Preferred categories
            if (preferredCategories.contains(video.category)) {
                score *= 1.25
            }

            // Followed creator boost
            if (followedCreatorIds.contains(video.creatorId)) {
                score *= 1.20
            }

            // New creator discovery boost (fresh creators with < 100k views get a lift)
            if (video.viewsCount < 100000) {
                score *= 1.15
            }

            // Content diversity / seen video suppression
            if (seenVideoIds.contains(video.id)) {
                score *= 0.40 // deprioritize already viewed videos
            }

            RankedVideo(
                video = video,
                recommendationScore = score,
                rankReason = if (score > 0.8) "Trending Match" else "Discovery"
            )
        }

        // Return sorted by highest recommendation score
        return rankedList.sortedByDescending { it.recommendationScore }.map { it.video }
    }

    /**
     * Trending ranking prioritizing pure engagement velocity and total shares.
     */
    fun rankTrendingFeed(videos: List<VideoEntity>): List<VideoEntity> {
        return videos.sortedByDescending {
            it.viewsCount * 0.4 + it.likesCount * 0.35 + it.sharesCount * 0.25
        }
    }

    /**
     * Latest feed by creation timestamp.
     */
    fun rankLatestFeed(videos: List<VideoEntity>): List<VideoEntity> {
        return videos.sortedByDescending { it.createdAt }
    }

    /**
     * Following feed filtered by followed creators.
     */
    fun rankFollowingFeed(videos: List<VideoEntity>, followedCreatorIds: Set<String>): List<VideoEntity> {
        val filtered = videos.filter { followedCreatorIds.contains(it.creatorId) }
        return if (filtered.isNotEmpty()) {
            filtered.sortedByDescending { it.createdAt }
        } else {
            // If user doesn't follow anyone yet, show creators with high follow recommendations
            videos.sortedByDescending { it.likesCount }
        }
    }
}
