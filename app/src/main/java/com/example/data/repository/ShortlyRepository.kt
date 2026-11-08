package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.*
import com.example.data.sample.SampleData
import com.example.domain.RecommendationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ShortlyRepository(
    context: Context,
    private val dao: ShortlyDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val recommendationEngine = RecommendationEngine()
    val currentUserId = SampleData.currentUser.id

    val authManager = com.example.data.auth.AuthManager(context)
    val isLoggedIn: StateFlow<Boolean> = authManager.isLoggedIn

    init {
        scope.launch {
            seedDatabaseIfEmpty()
        }
    }

    fun login(quickUser: UserEntity = SampleData.currentUser) {
        authManager.loginWithEmail("robiul@shortly.app", "password123")
    }

    fun logout() {
        authManager.logout()
    }

    private suspend fun seedDatabaseIfEmpty() {
        val existingVideos = dao.getAllApprovedVideos().first()
        if (existingVideos.isEmpty()) {
            dao.insertUsers(SampleData.sampleUsers)
            dao.insertVideos(SampleData.sampleVideos)
            dao.insertComments(SampleData.sampleComments)
            for (p in SampleData.samplePayouts) {
                dao.insertPayoutRequest(p)
            }
            for (n in SampleData.sampleNotifications) {
                dao.insertNotification(n)
            }
            dao.insertAds(SampleData.sampleAds)
            for (m in SampleData.sampleInitialMessages) {
                dao.insertMessage(m)
            }
            // Add initial follow for test
            dao.insertFollow(FollowEntity(currentUserId, "user_tanvir"))
        } else {
            // Also ensure ads are present if newly added to db
            val existingAds = dao.getAllAds().first()
            if (existingAds.isEmpty()) {
                dao.insertAds(SampleData.sampleAds)
            }
        }
    }

    // Video streams
    fun getApprovedVideos(): Flow<List<VideoEntity>> = dao.getAllApprovedVideos()
    fun getReportedVideos(): Flow<List<VideoEntity>> = dao.getReportedVideos()
    fun getVideosByCreator(creatorId: String): Flow<List<VideoEntity>> = dao.getVideosByCreator(creatorId)

    // User & Profile
    fun getUser(userId: String): Flow<UserEntity?> = dao.getUserFlow(userId)
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun updateUserProfile(displayName: String, bio: String) {
        val user = dao.getUserById(currentUserId) ?: return
        dao.updateUser(user.copy(displayName = displayName, bio = bio))
    }

    // Interactions (Guarded by login state in UI)
    fun isVideoLiked(videoId: String): Flow<Boolean> = dao.isVideoLiked(videoId, currentUserId)
    fun isVideoBookmarked(videoId: String): Flow<Boolean> = dao.isVideoBookmarked(videoId, currentUserId)
    fun isFollowingCreator(creatorId: String): Flow<Boolean> = dao.isUserFollowing(currentUserId, creatorId)
    fun getLikedVideoIds(): Flow<List<String>> = dao.getLikedVideoIds(currentUserId)
    fun getBookmarkedVideoIds(): Flow<List<String>> = dao.getBookmarkedVideoIds(currentUserId)
    fun getFollowingIds(): Flow<List<String>> = dao.getFollowingIds(currentUserId)

    suspend fun toggleLike(videoId: String) {
        val isLiked = dao.isVideoLiked(videoId, currentUserId).first()
        if (isLiked) {
            dao.deleteLike(videoId, currentUserId)
            dao.updateLikesCount(videoId, -1)
        } else {
            dao.insertLike(LikeEntity(videoId, currentUserId))
            dao.updateLikesCount(videoId, 1)

            // Notify creator
            val video = dao.getVideoById(videoId)
            if (video != null && video.creatorId != currentUserId) {
                dao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = video.creatorId,
                        type = "LIKE",
                        title = "নতুন পছন্দ",
                        message = "আপনার ভিডিও '${video.title}' পছন্দ করেছেন।",
                        avatarUrl = SampleData.currentUser.avatarUrl,
                        relatedVideoId = videoId
                    )
                )
            }
        }
    }

    suspend fun toggleBookmark(videoId: String) {
        val isBookmarked = dao.isVideoBookmarked(videoId, currentUserId).first()
        if (isBookmarked) {
            dao.deleteBookmark(videoId, currentUserId)
            dao.updateBookmarksCount(videoId, -1)
        } else {
            dao.insertBookmark(BookmarkEntity(videoId, currentUserId))
            dao.updateBookmarksCount(videoId, 1)
        }
    }

    suspend fun toggleFollow(creatorId: String) {
        if (creatorId == currentUserId) return
        val isFollowing = dao.isUserFollowing(currentUserId, creatorId).first()
        if (isFollowing) {
            dao.deleteFollow(currentUserId, creatorId)
        } else {
            dao.insertFollow(FollowEntity(currentUserId, creatorId))
            // Notification
            dao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = creatorId,
                    type = "FOLLOW",
                    title = "নতুন ফলোয়ার",
                    message = "${SampleData.currentUser.displayName} আপনাকে অনুসরণ করা শুরু করেছেন।",
                    avatarUrl = SampleData.currentUser.avatarUrl
                )
            )
        }
    }

    suspend fun recordShare(videoId: String) {
        dao.incrementShareCount(videoId)
    }

    // View Tracking & Watch History
    suspend fun recordView(video: VideoEntity, watchTimeMs: Long, completed: Boolean) {
        dao.incrementViewsCount(video.id)
        val existing = dao.getInteraction(video.id) ?: VideoInteractionEntity(video.id)
        dao.insertInteraction(
            existing.copy(
                watchTimeMs = existing.watchTimeMs + watchTimeMs,
                completionCount = if (completed) existing.completionCount + 1 else existing.completionCount
            )
        )

        // Save into user's watch history
        dao.insertWatchHistory(
            WatchHistoryEntity(
                id = "hist_${video.id}_${System.currentTimeMillis()}",
                videoId = video.id,
                title = video.title,
                caption = video.caption,
                thumbnailUrl = video.thumbnailUrl,
                creatorName = video.creatorName,
                creatorUsername = video.creatorUsername,
                viewsCount = video.viewsCount + 1,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = dao.getWatchHistory()

    suspend fun clearWatchHistory() {
        dao.clearWatchHistory()
    }

    // Comments
    fun getCommentsForVideo(videoId: String): Flow<List<CommentEntity>> = dao.getCommentsForVideo(videoId)

    suspend fun postComment(videoId: String, text: String, parentId: String? = null) {
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            videoId = videoId,
            userId = currentUserId,
            userName = SampleData.currentUser.displayName,
            userAvatarUrl = SampleData.currentUser.avatarUrl,
            content = text,
            parentCommentId = parentId
        )
        dao.insertComment(comment)
        dao.updateCommentsCount(videoId, 1)

        val video = dao.getVideoById(videoId)
        if (video != null && video.creatorId != currentUserId) {
            dao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = video.creatorId,
                    type = "COMMENT",
                    title = "নতুন মন্তব্য",
                    message = "${SampleData.currentUser.displayName}: \"$text\"",
                    avatarUrl = SampleData.currentUser.avatarUrl,
                    relatedVideoId = videoId
                )
            )
        }
    }

    suspend fun likeComment(commentId: String) {
        dao.updateCommentLikes(commentId, 1)
    }

    // Video Upload Pipeline
    suspend fun uploadVideo(
        title: String,
        caption: String,
        category: String,
        hashtags: String,
        soundTitle: String,
        videoUriOrUrl: String,
        thumbnailUrl: String,
        durationSeconds: Int = 15
    ): VideoEntity {
        val newVideo = VideoEntity(
            id = "vid_" + UUID.randomUUID().toString().take(8),
            title = title,
            caption = caption,
            videoUrl = videoUriOrUrl,
            thumbnailUrl = thumbnailUrl,
            durationSeconds = durationSeconds,
            creatorId = currentUserId,
            creatorName = SampleData.currentUser.displayName,
            creatorUsername = SampleData.currentUser.username,
            creatorAvatarUrl = SampleData.currentUser.avatarUrl,
            category = category,
            hashtags = hashtags,
            soundTitle = soundTitle,
            soundArtist = SampleData.currentUser.displayName,
            isVerifiedCreator = true,
            isApproved = true,
            createdAt = System.currentTimeMillis()
        )
        dao.insertVideo(newVideo)
        return newVideo
    }

    // Direct Messages & Notifications
    fun getNotifications(): Flow<List<NotificationEntity>> = dao.getNotificationsForUser(currentUserId)
    suspend fun markNotificationsRead() = dao.markAllNotificationsRead(currentUserId)

    fun getChatMessages(otherUserId: String): Flow<List<MessageEntity>> = dao.getMessagesBetween(currentUserId, otherUserId)

    suspend fun sendMessage(
        otherUserId: String,
        text: String,
        messageType: String = "TEXT",
        mediaUrl: String? = null,
        voiceDurationSec: Int = 0
    ) {
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = currentUserId,
            receiverId = otherUserId,
            text = text,
            messageType = messageType,
            mediaUrl = mediaUrl,
            voiceDurationSec = voiceDurationSec,
            reaction = null,
            createdAt = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
    }

    suspend fun receiveMessage(
        fromUserId: String,
        text: String,
        messageType: String = "TEXT"
    ) {
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = fromUserId,
            receiverId = currentUserId,
            text = text,
            messageType = messageType,
            reaction = null,
            createdAt = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
    }

    suspend fun reactToMessage(messageId: String, reaction: String?) {
        dao.updateMessageReaction(messageId, reaction)
    }

    // Ads Management
    fun getAllAds(): Flow<List<AdEntity>> = dao.getAllAds()
    fun getActiveAds(): Flow<List<AdEntity>> = dao.getActiveAds()

    suspend fun createAd(
        title: String,
        brandName: String,
        brandLogoUrl: String,
        mediaUrl: String,
        description: String,
        ctaText: String,
        targetUrl: String,
        placement: String = "BOTH"
    ): AdEntity {
        val newAd = AdEntity(
            id = "ad_" + UUID.randomUUID().toString().take(8),
            title = title,
            brandName = brandName,
            brandLogoUrl = brandLogoUrl,
            mediaUrl = mediaUrl,
            description = description,
            ctaText = ctaText,
            targetUrl = targetUrl,
            placement = placement,
            isActive = true,
            impressionsCount = 0,
            clicksCount = 0,
            createdAt = System.currentTimeMillis()
        )
        dao.insertAd(newAd)
        return newAd
    }

    suspend fun toggleAdActive(adId: String, isActive: Boolean) {
        dao.setAdActive(adId, isActive)
    }

    suspend fun deleteAd(adId: String) {
        dao.deleteAd(adId)
    }

    suspend fun recordAdImpression(adId: String) {
        dao.incrementAdImpression(adId)
    }

    suspend fun recordAdClick(adId: String) {
        dao.incrementAdClick(adId)
    }

    // Creator Earnings & Payout Requests (bKash / Nagad / Bank)
    fun getCreatorPayouts(): Flow<List<PayoutRequestEntity>> = dao.getPayoutRequestsForCreator(currentUserId)
    fun getAllPayouts(): Flow<List<PayoutRequestEntity>> = dao.getAllPayoutRequests()

    suspend fun requestPayout(amountBdt: Double, method: String, accountInfo: String) {
        val request = PayoutRequestEntity(
            id = "pay_" + UUID.randomUUID().toString().take(8),
            creatorId = currentUserId,
            creatorName = SampleData.currentUser.displayName,
            amountBdt = amountBdt,
            method = method,
            accountInfo = accountInfo,
            status = "PENDING"
        )
        dao.insertPayoutRequest(request)
    }

    suspend fun updatePayoutStatus(id: String, status: String) {
        dao.updatePayoutStatus(id, status)
    }

    // Admin & Moderation
    suspend fun moderateVideo(videoId: String, approve: Boolean, report: Boolean) {
        dao.setVideoModerationStatus(videoId, approve, report)
    }

    suspend fun setUserBanned(userId: String, banned: Boolean) {
        dao.setUserBanned(userId, banned)
    }

    suspend fun setUserVerified(userId: String, verified: Boolean) {
        dao.setUserVerified(userId, verified)
    }

    suspend fun updateUserRole(userId: String, role: String) {
        val user = dao.getUserById(userId) ?: return
        dao.updateUser(user.copy(role = role))
    }

    suspend fun getAllInteractions(): Map<String, VideoInteractionEntity> {
        return dao.getAllInteractions().associateBy { it.videoId }
    }
}
