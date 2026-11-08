package com.example.data.sample

import com.example.data.local.CommentEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PayoutRequestEntity
import com.example.data.local.UserEntity
import com.example.data.local.VideoEntity

object SampleData {
    val currentUser = UserEntity(
        id = "user_me",
        username = "robiul_creator",
        displayName = "Robiul Islam",
        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
        bio = "✨ Digital Creator | বানাই ছোট ছোট সুন্দর মুহূর্ত 🎬 | Dhaka, Bangladesh",
        followersCount = 42800,
        followingCount = 184,
        totalLikes = 385000,
        isVerified = true,
        isCreator = true,
        role = "SUPER_ADMIN"
    )

    val sampleUsers = listOf(
        currentUser,
        UserEntity(
            id = "user_tanvir",
            username = "tanvir_tech",
            displayName = "Tanvir Ahmed",
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400&auto=format&fit=crop&q=80",
            bio = "📱 গ্যাজেট এবং টেক টিপস প্রতিদিন! নতুন প্রযুক্তি সবার আগে জানুন।",
            followersCount = 125000,
            followingCount = 92,
            totalLikes = 890000,
            isVerified = true,
            isCreator = true
        ),
        UserEntity(
            id = "user_anika",
            username = "anika_vibes",
            displayName = "Anika Tabassum",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
            bio = "💃 Dance & Lifestyle | হাসিমুখে বাঁচুন ❤️ | Chittagong",
            followersCount = 98000,
            followingCount = 150,
            totalLikes = 620000,
            isVerified = true,
            isCreator = true
        ),
        UserEntity(
            id = "user_dhakafoodie",
            username = "dhaka_bites",
            displayName = "Dhaka Street Food",
            avatarUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&auto=format&fit=crop&q=80",
            bio = "🍲 পুরান ঢাকার কাচ্চি থেকে গুলশানের স্ট্রিট ফুড! চলো খাই!",
            followersCount = 210000,
            followingCount = 45,
            totalLikes = 1450000,
            isVerified = true,
            isCreator = true
        ),
        UserEntity(
            id = "user_nafis",
            username = "nafis_comedy",
            displayName = "Nafis Fun Corner",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
            bio = "🤣 হাসির খোরাক দিতে এসেছি | প্রতিদিন নতুন ফানি ভিডিও!",
            followersCount = 340000,
            followingCount = 210,
            totalLikes = 2800000,
            isVerified = true,
            isCreator = true
        )
    )

    // Using stable public short MP4 vertical demo videos
    val sampleVideos = listOf(
        VideoEntity(
            id = "vid_1",
            title = "কুয়াশা ঘেরা সুন্দরবন ও নদীর সৌন্দর্য",
            caption = "বাংলাদেশ কত সুন্দর! নদীমাতৃক আমাদের এই প্রিয় জন্মভূমি 🇧🇩💚 #BeautifulBangladesh #Sundarbans #Nature #ShortlyViral",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
            durationSeconds = 15,
            creatorId = "user_me",
            creatorName = "Robiul Islam",
            creatorUsername = "robiul_creator",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
            likesCount = 34200,
            commentsCount = 892,
            sharesCount = 1420,
            bookmarksCount = 3100,
            viewsCount = 148000,
            category = "Lifestyle",
            hashtags = "#BeautifulBangladesh, #Nature, #ShortlyViral, #ForYou",
            soundTitle = "মেঘের দেশে - শান্ত লিরিক্যাল সুর",
            soundArtist = "Acoustic Bengal",
            isVerifiedCreator = true,
            isApproved = true,
            createdAt = System.currentTimeMillis() - 3600000 * 2
        ),
        VideoEntity(
            id = "vid_2",
            title = "পুরান ঢাকার স্পেশাল শাহী বিরিয়ানি তৈরির রহস্য!",
            caption = "এত সুস্বাদু কাচ্চি বিরিয়ানি আপনি জীবনে খেয়েছেন কি? 😍 মুখে দিলেই মিলিয়ে যায়! #Foodie #DhakaBites #StreetFood #Kacchi",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80",
            durationSeconds = 24,
            creatorId = "user_dhakafoodie",
            creatorName = "Dhaka Street Food",
            creatorUsername = "dhaka_bites",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&auto=format&fit=crop&q=80",
            likesCount = 82100,
            commentsCount = 2140,
            sharesCount = 4500,
            bookmarksCount = 7600,
            viewsCount = 390000,
            category = "Food",
            hashtags = "#Foodie, #DhakaBites, #StreetFood, #Kacchi",
            soundTitle = "পুরান ঢাকা ফ্লেভার - বিট মিক্স",
            soundArtist = "DJ OldDhaka",
            isVerifiedCreator = true,
            isApproved = true,
            createdAt = System.currentTimeMillis() - 3600000 * 5
        ),
        VideoEntity(
            id = "vid_3",
            title = "যে ৩টি স্মার্টফোন সেটিংস আজই বন্ধ করবেন!",
            caption = "আপনার ফোন কি দ্রুত গরম হচ্ছে বা ব্যাটারি শেষ হচ্ছে? এই ৩টি সিক্রেট ট্রিক দেখে নিন! ⚡📱 #TechTips #AndroidHacks #TanvirTech",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80",
            durationSeconds = 30,
            creatorId = "user_tanvir",
            creatorName = "Tanvir Ahmed",
            creatorUsername = "tanvir_tech",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400&auto=format&fit=crop&q=80",
            likesCount = 45200,
            commentsCount = 1350,
            sharesCount = 6800,
            bookmarksCount = 9200,
            viewsCount = 210000,
            category = "Tech",
            hashtags = "#TechTips, #AndroidHacks, #TanvirTech, #SmartTips",
            soundTitle = "সাইবার ফিউচার সিন্থ - টেক ড্রিম",
            soundArtist = "TechSynth BD",
            isVerifiedCreator = true,
            isApproved = true,
            createdAt = System.currentTimeMillis() - 3600000 * 8
        ),
        VideoEntity(
            id = "vid_4",
            title = "নতুন বাংলা ফোক ড্যান্স রিল ট্রেন্ড 🔥",
            caption = "গানটা শুনে ড্যান্স না করে আর থাকতে পারলাম না! কমেন্টে জানাবেন কেমন হলো 💃✨ #DanceReel #BanglaTrend #AnikaVibes",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1547153760-18fc86324498?w=800&auto=format&fit=crop&q=80",
            durationSeconds = 18,
            creatorId = "user_anika",
            creatorName = "Anika Tabassum",
            creatorUsername = "anika_vibes",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
            likesCount = 112000,
            commentsCount = 3400,
            sharesCount = 8900,
            bookmarksCount = 12400,
            viewsCount = 580000,
            category = "Dance",
            hashtags = "#DanceReel, #BanglaTrend, #AnikaVibes, #ViralDance",
            soundTitle = "সোহাগ চাঁদ বদনী - রিমিক্স ২০২৬",
            soundArtist = "Folk Studio BD",
            isVerifiedCreator = true,
            isApproved = true,
            createdAt = System.currentTimeMillis() - 3600000 * 12
        ),
        VideoEntity(
            id = "vid_5",
            title = "বাঙালি ভাইভা বোর্ডে যা ঘটে থাকে 😂",
            caption = "প্রশ্ন কর্তা যখন বলেন 'নিজের সম্পর্কে কিছু বলুন'... আমি আর আমার জীবন! একদম বাস্তব ঘটনা! 🤣 #Comedy #BanglaFunny #NafisFun",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1527224857830-43a7acc85260?w=800&auto=format&fit=crop&q=80",
            durationSeconds = 22,
            creatorId = "user_nafis",
            creatorName = "Nafis Fun Corner",
            creatorUsername = "nafis_comedy",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
            likesCount = 230000,
            commentsCount = 9800,
            sharesCount = 19400,
            bookmarksCount = 8100,
            viewsCount = 1200000,
            category = "Comedy",
            hashtags = "#Comedy, #BanglaFunny, #NafisFun, #MemeBangladesh",
            soundTitle = "কমেডি হাসির ড্রপ সাউন্ড",
            soundArtist = "Laugh Club BD",
            isVerifiedCreator = true,
            isApproved = true,
            createdAt = System.currentTimeMillis() - 3600000 * 20
        )
    )

    val sampleComments = listOf(
        CommentEntity(
            id = "comm_1",
            videoId = "vid_1",
            userId = "user_anika",
            userName = "Anika Tabassum",
            userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
            content = "অসাধারণ দৃশ্য ভাইয়া! আমাদের দেশের নদীগুলো সত্যিই জাদুকরী 💚",
            likesCount = 42,
            createdAt = System.currentTimeMillis() - 1800000
        ),
        CommentEntity(
            id = "comm_2",
            videoId = "vid_1",
            userId = "user_tanvir",
            userName = "Tanvir Ahmed",
            userAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400&auto=format&fit=crop&q=80",
            content = "কোন ক্যামেরা দিয়ে শ্যুট করেছেন? কালার গ্রেডিং দারুণ হয়েছে!",
            likesCount = 18,
            createdAt = System.currentTimeMillis() - 1200000
        ),
        CommentEntity(
            id = "comm_3",
            videoId = "vid_1",
            userId = "user_me",
            userName = "Robiul Islam",
            userAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
            content = "ধন্যবাদ তানভীর ভাই! 4K 60fps মোডে ফোন দিয়েই ক্যাপচার করা।",
            likesCount = 25,
            parentCommentId = "comm_2",
            createdAt = System.currentTimeMillis() - 600000
        )
    )

    val sampleNotifications = listOf(
        NotificationEntity(
            id = "notif_1",
            userId = "user_me",
            type = "LIKE",
            title = "নতুন পছন্দ",
            message = "Anika Tabassum আপনার 'কুয়াশা ঘেরা সুন্দরবন' ভিডিওটি পছন্দ করেছেন।",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
            relatedVideoId = "vid_1",
            createdAt = System.currentTimeMillis() - 300000
        ),
        NotificationEntity(
            id = "notif_2",
            userId = "user_me",
            type = "FOLLOW",
            title = "নতুন অনুসারী",
            message = "Tanvir Ahmed আপনাকে ফলো করা শুরু করেছেন।",
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400&auto=format&fit=crop&q=80",
            createdAt = System.currentTimeMillis() - 1800000
        ),
        NotificationEntity(
            id = "notif_3",
            userId = "user_me",
            type = "EARNING",
            title = "ক্রিয়েটর রিওয়ার্ড যোগ হয়েছে",
            message = "আপনার ভিডিওর ১০,০০০ ভিউ মাইলস্টোনে ৳৫৫০ আপনার ক্রিয়েটর ব্যালেন্সে জমা হয়েছে!",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
            createdAt = System.currentTimeMillis() - 86400000
        )
    )

    val samplePayouts = listOf(
        PayoutRequestEntity(
            id = "pay_1",
            creatorId = "user_me",
            creatorName = "Robiul Islam",
            amountBdt = 12500.0,
            method = "bKash",
            accountInfo = "017XXXXXXXX (Personal)",
            status = "APPROVED",
            createdAt = System.currentTimeMillis() - 86400000 * 3
        ),
        PayoutRequestEntity(
            id = "pay_2",
            creatorId = "user_me",
            creatorName = "Robiul Islam",
            amountBdt = 8200.0,
            method = "Nagad",
            accountInfo = "018XXXXXXXX (Personal)",
            status = "PENDING",
            createdAt = System.currentTimeMillis() - 86400000
        )
    )

    val trendingCategories = listOf(
        "For You",
        "Comedy",
        "Music",
        "Dance",
        "Food",
        "Tech",
        "Lifestyle",
        "Gaming",
        "News"
    )

    val trendingHashtags = listOf(
        Pair("#BeautifulBangladesh", "1.8M"),
        Pair("#ShortlyViral", "2.4M"),
        Pair("#DhakaBites", "940K"),
        Pair("#BanglaComedy", "3.2M"),
        Pair("#TechTips", "620K"),
        Pair("#DanceReel", "1.1M")
    )

    val sampleSounds = listOf(
        "মেঘের দেশে - শান্ত লিরিক্যাল সুর",
        "পুরান ঢাকা ফ্লেভার - বিট মিক্স",
        "সাইবার ফিউচার সিন্থ - টেক ড্রিম",
        "সোহাগ চাঁদ বদনী - রিমিক্স ২০২৬",
        "কমেডি হাসির ড্রপ সাউন্ড",
        "লো-ফাই ট্রাভেল ভাইবস"
    )

    val sampleAds = listOf(
        com.example.data.local.AdEntity(
            id = "ad_daraz_11",
            title = "দারাজ ১১.১১ মেগা সেল! সর্বোচ্চ ৮০% পর্যন্ত ছাড় 🔥",
            brandName = "Daraz Bangladesh",
            brandLogoUrl = "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=200&auto=format&fit=crop&q=80",
            mediaUrl = "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1080&auto=format&fit=crop&q=80",
            description = "অনলাইন শপিংয়ের সবচেয়ে বড় ধামাকা অফার শুরু হয়ে গেছে! লাখ টাকার ভাউচার ও ফ্রি ডেলিভারি নিন এখনই।",
            ctaText = "Shop Now 🛍️",
            targetUrl = "https://www.daraz.com.bd",
            placement = "BOTH",
            isActive = true,
            impressionsCount = 14250,
            clicksCount = 1840
        ),
        com.example.data.local.AdEntity(
            id = "ad_pathao_food",
            title = "পাঠাও ফুডে হট ডিলে ৩০% পর্যন্ত ফ্ল্যাট ক্যাশব্যাক! 🍔🍕",
            brandName = "Pathao Food",
            brandLogoUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200&auto=format&fit=crop&q=80",
            mediaUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=1080&auto=format&fit=crop&q=80",
            description = "ক্ষুধা লেগেছে? বিরিয়ানি, পিজ্জা বা ডেজার্ট—অর্ডার করুন আর ৩০ মিনিটে গরম খাবার পৌঁছে যাবে ঘরে।",
            ctaText = "Order Food 🛵",
            targetUrl = "https://pathao.com/food",
            placement = "IN_FEED",
            isActive = true,
            impressionsCount = 9800,
            clicksCount = 1120
        ),
        com.example.data.local.AdEntity(
            id = "ad_bkash_cashback",
            title = "বিকাশ পেমেন্টে স্পেশাল ক্যাশব্যাক ও বোনাস রিওয়ার্ড! 💳",
            brandName = "bKash Limited",
            brandLogoUrl = "https://images.unsplash.com/photo-1556742049-0a67c5574f73?w=200&auto=format&fit=crop&q=80",
            mediaUrl = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=1080&auto=format&fit=crop&q=80",
            description = "মোবাইল রিচার্জ, সেন্ড মানি বা শপিংয়ে বিকাশ করলেই আকর্ষণীয় ক্যাশব্যাক আপনার ব্যালেন্সে।",
            ctaText = "Claim Offer 🎁",
            targetUrl = "https://www.bkash.com",
            placement = "POST_VIDEO",
            isActive = true,
            impressionsCount = 18600,
            clicksCount = 2340
        ),
        com.example.data.local.AdEntity(
            id = "ad_gp_5g",
            title = "গ্রামীণফোন সুপারফাস্ট ৫জি - গেমিং ও আল্ট্রা এইচডি স্ট্রিমিং ⚡",
            brandName = "Grameenphone",
            brandLogoUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200&auto=format&fit=crop&q=80",
            mediaUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1080&auto=format&fit=crop&q=80",
            description = "দেশের ১ নম্বর নেটওয়ার্কে উপভোগ করুন হাই স্পিড ইন্টারনেট প্যাক এবং আনলিমিটেড বিনোদন।",
            ctaText = "Activate 5G 🚀",
            targetUrl = "https://www.grameenphone.com",
            placement = "BOTH",
            isActive = true,
            impressionsCount = 22100,
            clicksCount = 3100
        )
    )

    val sampleInitialMessages = listOf(
        com.example.data.local.MessageEntity(
            id = "msg_init_1",
            senderId = "user_tanvir",
            receiverId = "user_me",
            text = "আসসালামু আলাইকুম রবিউল ভাই! কেমন আছেন? আপনার নতুন সুন্দরবনের ভিডিওটা দারুণ হয়েছে!",
            messageType = "TEXT",
            reaction = "❤️",
            isRead = true,
            createdAt = System.currentTimeMillis() - 3600000 * 3
        ),
        com.example.data.local.MessageEntity(
            id = "msg_init_2",
            senderId = "user_me",
            receiverId = "user_tanvir",
            text = "ওয়ালাইকুম আসসালাম তানভীর ভাই! অনেক ধন্যবাদ। আপনি কোন ক্যামেরা দিয়ে ড্রোন শটগুলো নেন?",
            messageType = "TEXT",
            reaction = "👍",
            isRead = true,
            createdAt = System.currentTimeMillis() - 3600000 * 2
        ),
        com.example.data.local.MessageEntity(
            id = "msg_init_3",
            senderId = "user_tanvir",
            receiverId = "user_me",
            text = "আমি সাধারণত DJI Mini Pro আর 4K 60fps এ শ্যুট করি। কাল বিকেলে সময় থাকলে একটু কথা বলবেন?",
            messageType = "TEXT",
            reaction = null,
            isRead = true,
            createdAt = System.currentTimeMillis() - 1800000
        )
    )
}

