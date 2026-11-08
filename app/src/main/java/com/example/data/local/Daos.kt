package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortlyDao {
    // Videos
    @Query("SELECT * FROM videos WHERE isApproved = 1 ORDER BY createdAt DESC")
    fun getAllApprovedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE isApproved = 1 AND isReported = 1")
    fun getReportedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    fun getVideosByCreator(creatorId: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET likesCount = likesCount + :delta WHERE id = :videoId")
    suspend fun updateLikesCount(videoId: String, delta: Int)

    @Query("UPDATE videos SET commentsCount = commentsCount + :delta WHERE id = :videoId")
    suspend fun updateCommentsCount(videoId: String, delta: Int)

    @Query("UPDATE videos SET sharesCount = sharesCount + 1 WHERE id = :videoId")
    suspend fun incrementShareCount(videoId: String)

    @Query("UPDATE videos SET bookmarksCount = bookmarksCount + :delta WHERE id = :videoId")
    suspend fun updateBookmarksCount(videoId: String, delta: Int)

    @Query("UPDATE videos SET viewsCount = viewsCount + 1 WHERE id = :videoId")
    suspend fun incrementViewsCount(videoId: String)

    @Query("UPDATE videos SET isApproved = :approved, isReported = :reported WHERE id = :videoId")
    suspend fun setVideoModerationStatus(videoId: String, approved: Boolean, reported: Boolean)

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideo(videoId: String)

    // Users
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY followersCount DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isBanned = :banned WHERE id = :userId")
    suspend fun setUserBanned(userId: String, banned: Boolean)

    @Query("UPDATE users SET isVerified = :verified WHERE id = :userId")
    suspend fun setUserVerified(userId: String, verified: Boolean)

    // Comments
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createdAt ASC")
    fun getCommentsForVideo(videoId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Query("UPDATE comments SET likesCount = likesCount + :delta WHERE id = :commentId")
    suspend fun updateCommentLikes(commentId: String, delta: Int)

    // Likes
    @Query("SELECT EXISTS(SELECT 1 FROM video_likes WHERE videoId = :videoId AND userId = :userId)")
    fun isVideoLiked(videoId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Query("DELETE FROM video_likes WHERE videoId = :videoId AND userId = :userId")
    suspend fun deleteLike(videoId: String, userId: String)

    @Query("SELECT videoId FROM video_likes WHERE userId = :userId")
    fun getLikedVideoIds(userId: String): Flow<List<String>>

    // Bookmarks
    @Query("SELECT EXISTS(SELECT 1 FROM video_bookmarks WHERE videoId = :videoId AND userId = :userId)")
    fun isVideoBookmarked(videoId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM video_bookmarks WHERE videoId = :videoId AND userId = :userId")
    suspend fun deleteBookmark(videoId: String, userId: String)

    @Query("SELECT videoId FROM video_bookmarks WHERE userId = :userId")
    fun getBookmarkedVideoIds(userId: String): Flow<List<String>>

    // Follows
    @Query("SELECT EXISTS(SELECT 1 FROM user_follows WHERE followerId = :followerId AND followedId = :followedId)")
    fun isUserFollowing(followerId: String, followedId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Query("DELETE FROM user_follows WHERE followerId = :followerId AND followedId = :followedId")
    suspend fun deleteFollow(followerId: String, followedId: String)

    @Query("SELECT followedId FROM user_follows WHERE followerId = :userId")
    fun getFollowingIds(userId: String): Flow<List<String>>

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    // Messages
    @Query("SELECT * FROM messages WHERE (senderId = :u1 AND receiverId = :u2) OR (senderId = :u2 AND receiverId = :u1) ORDER BY createdAt ASC")
    fun getMessagesBetween(u1: String, u2: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET reaction = :reaction WHERE id = :messageId")
    suspend fun updateMessageReaction(messageId: String, reaction: String?)

    // Ads
    @Query("SELECT * FROM ads ORDER BY createdAt DESC")
    fun getAllAds(): Flow<List<AdEntity>>

    @Query("SELECT * FROM ads WHERE isActive = 1")
    fun getActiveAds(): Flow<List<AdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAds(ads: List<AdEntity>)

    @Update
    suspend fun updateAd(ad: AdEntity)

    @Query("UPDATE ads SET isActive = :isActive WHERE id = :adId")
    suspend fun setAdActive(adId: String, isActive: Boolean)

    @Query("UPDATE ads SET impressionsCount = impressionsCount + 1 WHERE id = :adId")
    suspend fun incrementAdImpression(adId: String)

    @Query("UPDATE ads SET clicksCount = clicksCount + 1 WHERE id = :adId")
    suspend fun incrementAdClick(adId: String)

    @Query("DELETE FROM ads WHERE id = :adId")
    suspend fun deleteAd(adId: String)

    // Payouts
    @Query("SELECT * FROM payout_requests ORDER BY createdAt DESC")
    fun getAllPayoutRequests(): Flow<List<PayoutRequestEntity>>

    @Query("SELECT * FROM payout_requests WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    fun getPayoutRequestsForCreator(creatorId: String): Flow<List<PayoutRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayoutRequest(request: PayoutRequestEntity)

    @Query("UPDATE payout_requests SET status = :status WHERE id = :id")
    suspend fun updatePayoutStatus(id: String, status: String)

    // Video Interactions (Analytics & Recommendations)
    @Query("SELECT * FROM video_interactions WHERE videoId = :videoId")
    suspend fun getInteraction(videoId: String): VideoInteractionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: VideoInteractionEntity)

    @Query("SELECT * FROM video_interactions")
    suspend fun getAllInteractions(): List<VideoInteractionEntity>

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()
}

