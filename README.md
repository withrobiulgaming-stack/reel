# SHORTLY - Short Video Social Media Platform (TikTok / Instagram Reels Architecture)

**Shortly** is a production-focused Short Video Social Media Platform built with modern Android (Kotlin + Jetpack Compose + Room + Hardware Video Player), bilingual localization (Default: বাংলা Bangla, English), an asynchronous multi-resolution video transcoding pipeline, a 5-signal recommendation engine, creator analytics with bKash/Nagad monetization, and full administrative moderation.

---

## 1. Core Platform Architecture

### Mobile Client (Android Jetpack Compose)
- **Engine**: Kotlin, Jetpack Compose (M3), Coroutines, Flow.
- **Video Playback**: Android `MediaPlayer` with `TextureView` hardware acceleration, infinite vertical swipe (`VerticalPager`), double-tap to like with heart bursting animation, pause/play on single tap, volume toggle, and seamless buffering states.
- **Feeds**:
  1. **For You (আপনার জন্য)**: Personalized by Recommendation Engine.
  2. **Following (অনুসরণ)**: Chronological & engagement feed from followed creators.
  3. **Trending (জনপ্রিয়)**: High-velocity viral videos ranked by views and shares.
  4. **Latest (সাম্প্রতিক)**: Real-time latest uploads.

### Bilingual Localization Engine
- **Default Language**: **বাংলা (Bangla)** across all navigation, buttons, forms, creator studio, admin moderation, notifications, and monetization.
- **English Toggle**: Instant on-the-fly language switching from profile settings, persisted via `SharedPreferences`.

### Database Architecture
- **Local Persistence**: **Room Database** (`ShortlyDatabase.kt`) with relational entities:
  - `videos` (id, title, caption, videoUrl, thumbnailUrl, durationSeconds, creatorId, stats, category, hashtags, soundTitle, status)
  - `users` (id, username, displayName, avatarUrl, bio, followers, verified, role)
  - `comments` (id, videoId, userId, content, nested replies parentId, likes)
  - `video_likes` & `video_bookmarks` & `user_follows`
  - `notifications` (likes, follows, earnings, system alerts)
  - `messages` (1-to-1 real-time direct messages)
  - `payout_requests` (bKash / Nagad / Bank transfers)
  - `video_interactions` (watch time, completion count, rewatch signals)

---

## 2. Recommendation Engine Formula

Shortly implements an asynchronous normalized scoring formula:

$$\text{Score} = 0.30 \times \text{WatchTime} + 0.25 \times \text{Engagement} + 0.20 \times \text{Completion} + 0.15 \times \text{Shares} + 0.10 \times \text{Recency}$$

- **Personalization Boost**: Category affinity ($\times 1.25$), followed creators ($\times 1.20$), and new creator discovery ($\times 1.15$).
- **Seen Suppression**: Previously watched items scaled by $0.40$ to ensure content freshness and diversity.
- **Negative Feedback**: Instant exclusion of items flagged as "Not Interested".

---

## 3. Video Transcoding & HLS Streaming Pipeline

1. **Signed Session**: Client requests upload session.
2. **Object Storage**: Direct upload to Cloudflare R2 bucket.
3. **FFmpeg Transcoder**: Background worker generates 360p, 480p, 720p, 1080p outputs.
4. **HLS Generation**: Transcoder builds adaptive `.m3u8` playlists and `.ts` chunk segments.
5. **CDN Distribution**: Assets distributed globally through Cloudflare Edge Cache.

---

## 4. Monetization & Creator Rewards

- **Revenue Metric**: Estimated earnings at standard rate per 10,000 views.
- **Local Payment Gateways**: Full payout request workflow with idempotent submission for:
  - **bKash (বিকাশ)**
  - **Nagad (নগদ)**
  - **Bank Transfer**
- **Admin Approval Queue**: Finance and Super Admin roles can review, approve, or reject withdrawal requests with audit logs.

---

## 5. Security & RBAC Roles

- **SUPER_ADMIN**: Complete access to user bans, video removal, role assignment, and financial approvals.
- **MODERATOR**: Content review and flagged video actions.
- **FINANCE**: Review payout requests and transaction reconciliation.
- **ANALYST**: Read-only access to platform DAU, MAU, and watch-time telemetry.
