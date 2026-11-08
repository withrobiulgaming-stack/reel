package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val caption: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val durationSeconds: Int,
    val creatorId: String,
    val creatorName: String,
    val creatorUsername: String,
    val creatorAvatarUrl: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val bookmarksCount: Int = 0,
    val viewsCount: Int = 0,
    val category: String, // Comedy, Music, Dance, Tech, Lifestyle, Gaming, Food
    val hashtags: String, // comma or space separated
    val soundTitle: String,
    val soundArtist: String,
    val isVerifiedCreator: Boolean = false,
    val isApproved: Boolean = true,
    val isReported: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val totalLikes: Int = 0,
    val isVerified: Boolean = false,
    val isCreator: Boolean = true,
    val isBanned: Boolean = false,
    val role: String = "USER" // USER, MODERATOR, ADMIN, SUPER_ADMIN
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val videoId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String,
    val content: String,
    val likesCount: Int = 0,
    val parentCommentId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_likes", primaryKeys = ["videoId", "userId"])
data class LikeEntity(
    val videoId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_bookmarks", primaryKeys = ["videoId", "userId"])
data class BookmarkEntity(
    val videoId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_follows", primaryKeys = ["followerId", "followedId"])
data class FollowEntity(
    val followerId: String,
    val followedId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: String, // LIKE, COMMENT, FOLLOW, EARNING, SYSTEM
    val title: String,
    val message: String,
    val avatarUrl: String,
    val relatedVideoId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val messageType: String = "TEXT", // TEXT, IMAGE, VOICE, LIKE_STICKER
    val mediaUrl: String? = null,
    val voiceDurationSec: Int = 0,
    val reaction: String? = null, // e.g. ❤️, 😆, 😮, 😢, 😠, 👍
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ads")
data class AdEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val brandName: String,
    val brandLogoUrl: String,
    val mediaUrl: String, // Banner or video thumbnail
    val description: String,
    val ctaText: String = "Install Now", // e.g. "Shop Now", "Learn More", "Order Food"
    val targetUrl: String = "https://shortly.social",
    val placement: String = "BOTH", // IN_FEED, POST_VIDEO, BOTH
    val isActive: Boolean = true,
    val impressionsCount: Int = 0,
    val clicksCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payout_requests")
data class PayoutRequestEntity(
    @PrimaryKey
    val id: String,
    val creatorId: String,
    val creatorName: String,
    val amountBdt: Double,
    val method: String, // bKash, Nagad, Bank
    val accountInfo: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_interactions")
data class VideoInteractionEntity(
    @PrimaryKey
    val videoId: String,
    val watchTimeMs: Long = 0L,
    val completionCount: Int = 0,
    val rewatchCount: Int = 0,
    val notInterested: Boolean = false
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val id: String,
    val videoId: String,
    val title: String,
    val caption: String,
    val thumbnailUrl: String,
    val creatorName: String,
    val creatorUsername: String,
    val viewsCount: Int,
    val watchedAt: Long = System.currentTimeMillis()
)


