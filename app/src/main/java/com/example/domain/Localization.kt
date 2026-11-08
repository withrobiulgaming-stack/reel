package com.example.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val label: String) {
    BANGLA("bn", "বাংলা"),
    ENGLISH("en", "English")
}

object Strings {
    // Bottom Navigation
    fun home(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "হোম" else "Home"
    fun explore(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এক্সপ্লোর" else "Explore"
    fun create(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "তৈরি করুন" else "Create"
    fun inbox(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ইনবক্স" else "Inbox"
    fun profile(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রোফাইল" else "Profile"

    // Feeds
    fun forYou(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার জন্য" else "For You"
    fun following(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুসরণ" else "Following"
    fun trending(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "জনপ্রিয়" else "Trending"
    fun latest(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাম্প্রতিক" else "Latest"

    // Feed interaction
    fun follow(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুসরণ" else "Follow"
    fun followingState(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুসরণ করছেন" else "Following"
    fun unfollow(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আনফলো" else "Unfollow"
    fun likes(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পছন্দ" else "Likes"
    fun comments(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মন্তব্যসমূহ" else "Comments"
    fun share(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শেয়ার" else "Share"
    fun save(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষণ" else "Save"
    fun saved(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষিত" else "Saved"
    fun originalSound(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অরিজিনাল সাউন্ড" else "Original Sound"
    fun writeComment(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "একটি মন্তব্য লিখুন..." else "Write a comment..."
    fun post(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পোস্ট" else "Post"
    fun reply(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উত্তর দিন" else "Reply"
    fun replies(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উত্তর" else "Replies"
    fun noCommentsYet(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এখনও কোনো মন্তব্য নেই। প্রথম মন্তব্যটি করুন!" else "No comments yet. Be the first to comment!"

    // Upload & Studio
    fun uploadVideo(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিও আপলোড করুন" else "Upload Video"
    fun recordVideo(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিও রেকর্ড" else "Record Video"
    fun selectFromGallery(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গ্যালারি থেকে নিন" else "Choose from Gallery"
    fun videoCaption(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিও ক্যাপশন লিখুন..." else "Write video caption..."
    fun selectCategory(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্যাটাগরি নির্বাচন করুন" else "Select Category"
    fun selectMusic(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মিউজিক / ব্যাকগ্রাউন্ড সাউন্ড" else "Music / Sound Track"
    fun selectPrivacy(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গোপনীয়তা সেটিংস" else "Privacy Settings"
    fun publicPrivacy(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পাবলিক (সবার জন্য)" else "Public (Everyone)"
    fun friendsPrivacy(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুধুমাত্র বন্ধুরা" else "Friends Only"
    fun privatePrivacy(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রাইভেট (শুধুমাত্র আমি)" else "Private (Only Me)"
    fun publishVideo(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিও প্রকাশ করুন" else "Publish Video"
    fun uploadProgress(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপলোড ও প্রসেসিং হচ্ছে..." else "Uploading & Processing..."
    fun stepR2(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "১/৪ ক্লাউড স্টোরেজে আপলোড..." else "1/4 Uploading to Object Storage..."
    fun stepFFmpeg(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "২/৪ FFmpeg ট্রান্সকোডিং ও রেজোলিউশন..." else "2/4 FFmpeg Transcoding Resolutions..."
    fun stepHLS(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "৩/৪ HLS মাল্টি-রেট স্ট্রিমিং জেনারেশন..." else "3/4 Generating HLS Adaptive Streams..."
    fun stepCDN(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "৪/৪ CDN ক্যাশিং এবং সফল প্রকাশ!" else "4/4 CDN Caching & Published Successfully!"
    fun uploadSuccess(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিও সফলভাবে আপলোড এবং পাবলিশ হয়েছে!" else "Video uploaded and published successfully!"

    // Explore & Search
    fun searchHint(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিও, ক্রিয়েটর বা হ্যাশট্যাগ খুঁজুন..." else "Search videos, creators, or hashtags..."
    fun trendingHashtags(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "জনপ্রিয় হ্যাশট্যাগ" else "Trending Hashtags"
    fun topCreators(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সেরা ক্রিয়েটরবৃন্দ" else "Top Creators"
    fun exploreCategories(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্যাটাগরি সমূহ" else "Categories"
    fun views(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিউ" else "views"

    // Profile
    fun followers(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুসারী" else "Followers"
    fun followingCount(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুসরণ" else "Following"
    fun editProfile(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রোফাইল সম্পাদনা" else "Edit Profile"
    fun creatorStudio(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্রিয়েটর স্টুডিও" else "Creator Studio"
    fun adminPanel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাডমিন প্যানেল" else "Admin Panel"
    fun myVideos(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আমার ভিডিও" else "My Videos"
    fun likedVideos(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পছন্দের" else "Liked"
    fun savedVideos(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষিত" else "Saved"
    fun language(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাষা / Language" else "Language / ভাষা"
    fun settings(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সেটিংস" else "Settings"
    fun logout(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লগআউট" else "Logout"
    fun verifiedBadge(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভেরিফায়েড ক্রিয়েটর" else "Verified Creator"

    // Inbox & Messages
    fun notifications(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নোটিফিকেশন" else "Notifications"
    fun directMessages(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডিরেক্ট মেসেজ" else "Messages"
    fun typeMessage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বার্তা লিখুন..." else "Type a message..."
    fun send(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পাঠান" else "Send"
    fun online(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনলাইন" else "Online"

    // Creator Dashboard
    fun creatorDashboardTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্রিয়েটর অ্যানালিটিক্স ও ড্যাশবোর্ড" else "Creator Analytics & Dashboard"
    fun totalViews(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোট ভিডিও ভিউ" else "Total Video Views"
    fun watchTimeHours(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোট ওয়াচ টাইম (ঘণ্টা)" else "Watch Time (Hours)"
    fun engagementRate(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এনগেজমেন্ট রেট" else "Engagement Rate"
    fun estimatedEarnings(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আনুমানিক আয় (টাকা)" else "Estimated Earnings (BDT)"
    fun monetizationStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মনিটাইজেশন স্ট্যাটাস" else "Monetization Status"
    fun eligibleForMonetization(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মনিটাইজেশন সক্রিয় (Active)" else "Monetization Active"
    fun withdrawFunds(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "টাকা উত্তোলন করুন" else "Withdraw Earnings"
    fun selectPayoutMethod(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পেমেন্ট মেথড নির্বাচন করুন" else "Select Payout Method"
    fun accountOrMobileNumber(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিকাশ/নগদ বা ব্যাংক একাউন্ট নম্বর" else "bKash / Nagad / Account Number"
    fun amountBdt(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "টাকার পরিমাণ (৳)" else "Amount in BDT (৳)"
    fun submitWithdrawal(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উত্তোলন রিকোয়েস্ট পাঠান" else "Submit Payout Request"
    fun withdrawalSubmitted(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার উত্তোলনের আবেদনটি অ্যাডমিন অনুমোদনের জন্য গৃহীত হয়েছে!" else "Withdrawal submitted for admin review!"
    fun recentTransactions(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাম্প্রতিক লেনদেন" else "Recent Transactions"

    // Admin Panel
    fun adminDashboardTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাডমিন ও কনটেন্ট মডারেশন" else "Admin & Content Moderation"
    fun rbacRole(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাডমিন রোল:" else "RBAC Role:"
    fun totalPlatformUsers(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্ল্যাটফর্মের মোট ইউজার" else "Total Users"
    fun totalPlatformVideos(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোট পাবলিশড ভিডিও" else "Total Videos"
    fun pendingReports(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিপোর্ট ও রিভিউ কিউ" else "Reports & Review Queue"
    fun platformRevenue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্ল্যাটফর্ম রেভিনিউ (৳)" else "Platform Revenue (BDT)"
    fun videoModerationQueue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিও মডারেশন কিউ" else "Video Moderation Queue"
    fun approve(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুমোদন করুন" else "Approve"
    fun reject(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাতিল করুন" else "Reject"
    fun banUser(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ইউজার ব্যান করুন" else "Ban User"
    fun approvePayout(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "টাকা পাঠানো সম্পন্ন (Approve)" else "Approve Payout"
    fun payoutQueue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্রিয়েটর পেমেন্ট রিকোয়েস্ট" else "Creator Payout Requests"

    // Auth & Login Prompts
    fun login(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লগইন করুন" else "Log In"
    fun loginRequiredTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লগইন প্রয়োজন" else "Login Required"
    fun loginRequiredDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভিডিওতে লাইক দেওয়া, মন্তব্য করা ও সেভ করতে Shortly তে লগইন করুন।" else "Please log in to Shortly to like, comment, save videos, and follow creators."
    fun continueWithGoogle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "Google দিয়ে লগইন" else "Continue with Google"
    fun continueWithPhoneEmail(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোবাইল নম্বর বা ইমেইল দিয়ে প্রবেশ" else "Continue with Phone or Email"
    fun quickDemoLogin(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এক ক্লিকে ডেমো লগইন" else "Quick Demo Login"
    fun guestModeTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গেস্ট মোড" else "Guest Mode"
    fun guestModeDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনি গেস্ট হিসেবে ভিডিও দেখছেন। সম্পূর্ণ অভিজ্ঞতার জন্য লগইন করুন।" else "You are watching as a guest. Log in for full features."
    fun guestUser(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গেস্ট ইউজার" else "Guest User"
    fun notLoggedIn(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লগইন করা নেই" else "Not logged in"

    // Watch History
    fun watchHistory(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দেখা ভিডিও / ওয়াচ হিস্ট্রি" else "Watch History"
    fun clearHistory(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "হিস্ট্রি মুছুন" else "Clear History"
    fun noWatchHistory(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এখনও কোনো ভিডিও দেখা হয়নি।" else "No watch history recorded yet."
    fun historyCleared(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ওয়াচ হিস্ট্রি মুছে ফেলা হয়েছে!" else "Watch history cleared!"

    // General Dialogs / Messages
    fun confirm(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিশ্চিত করুন" else "Confirm"
    fun cancel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel"
    fun close(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বন্ধ করুন" else "Close"
    fun error(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ত্রুটি" else "Error"
    fun success(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সফল হয়েছে" else "Success"
}

class LocalizationManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shortly_prefs", Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(
        if (prefs.getString("app_lang", "bn") == "en") AppLanguage.ENGLISH else AppLanguage.BANGLA
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_lang", language.code).apply()
        _currentLanguage.value = language
    }

    companion object {
        @Volatile
        private var instance: LocalizationManager? = null

        fun getInstance(context: Context): LocalizationManager {
            return instance ?: synchronized(this) {
                instance ?: LocalizationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
