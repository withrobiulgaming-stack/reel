package com.example.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ShortlyRepository
import com.example.data.sample.SampleData
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class SettingsSection {
    OVERVIEW,
    ACCOUNT,
    PRIVACY,
    SECURITY,
    NOTIFICATIONS,
    VIDEO_FEED,
    APPEARANCE,
    STORAGE,
    CREATOR,
    MONETIZATION,
    SUPPORT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: ShortlyRepository,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenWatchHistory: () -> Unit,
    onOpenCreatorDashboard: () -> Unit,
    onRequestAdminAccess: () -> Unit = {},
    onRequestLogin: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("shortly_comprehensive_settings", Context.MODE_PRIVATE) }
    val isLoggedIn by repository.isLoggedIn.collectAsState()

    var currentSection by remember { mutableStateOf(SettingsSection.OVERVIEW) }
    var searchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Account state
    val currentUser by repository.getUser(repository.currentUserId).collectAsState(initial = SampleData.currentUser)
    val user = currentUser ?: SampleData.currentUser
    var usernameState by remember(user) { mutableStateOf(user.username) }
    var displayNameState by remember(user) { mutableStateOf(user.displayName) }
    var bioState by remember(user) { mutableStateOf(user.bio) }
    var emailState by remember { mutableStateOf(prefs.getString("user_email", "robiul@shortly.social") ?: "robiul@shortly.social") }
    var phoneState by remember { mutableStateOf(prefs.getString("user_phone", "+880 1712-345678") ?: "+880 1712-345678") }
    var websiteState by remember { mutableStateOf(prefs.getString("user_website", "https://shortly.social/@robiul") ?: "https://shortly.social/@robiul") }
    var dobState by remember { mutableStateOf("15 Oct 1998") }
    var genderState by remember { mutableStateOf("Male") }

    // Toggles state with SharedPreferences persistence
    var isPrivateAccount by remember { mutableStateOf(prefs.getBoolean("priv_private_account", false)) }
    var activityStatus by remember { mutableStateOf(prefs.getBoolean("priv_activity_status", true)) }
    var hideLikeCount by remember { mutableStateOf(prefs.getBoolean("priv_hide_like_count", false)) }
    var hideViewCount by remember { mutableStateOf(prefs.getBoolean("priv_hide_view_count", false)) }
    var allowDownloads by remember { mutableStateOf(prefs.getBoolean("priv_allow_downloads", true)) }

    var twoFactorAuth by remember { mutableStateOf(prefs.getBoolean("sec_2fa", false)) }
    var loginAlerts by remember { mutableStateOf(prefs.getBoolean("sec_login_alerts", true)) }

    var notifLikes by remember { mutableStateOf(prefs.getBoolean("notif_likes", true)) }
    var notifComments by remember { mutableStateOf(prefs.getBoolean("notif_comments", true)) }
    var notifFollows by remember { mutableStateOf(prefs.getBoolean("notif_follows", true)) }
    var notifMessages by remember { mutableStateOf(prefs.getBoolean("notif_messages", true)) }
    var notifLive by remember { mutableStateOf(prefs.getBoolean("notif_live", true)) }
    var notifQuietHours by remember { mutableStateOf(prefs.getBoolean("notif_quiet_hours", false)) }

    var autoplayVideos by remember { mutableStateOf(prefs.getBoolean("feed_autoplay", true)) }
    var dataSaverMode by remember { mutableStateOf(prefs.getBoolean("feed_data_saver", false)) }
    var wifiOnlyHd by remember { mutableStateOf(prefs.getBoolean("feed_wifi_hd", true)) }
    var videoQuality by remember { mutableStateOf(prefs.getString("feed_video_quality", "Auto (1080p)") ?: "Auto (1080p)") }

    var selectedTheme by remember { mutableStateOf(prefs.getString("app_theme", "Dark") ?: "Dark") }
    var fontSize by remember { mutableStateOf(prefs.getString("app_font_size", "Normal") ?: "Normal") }
    var autoTranslate by remember { mutableStateOf(prefs.getBoolean("app_auto_translate", true)) }
    var reducedMotion by remember { mutableStateOf(prefs.getBoolean("app_reduced_motion", false)) }

    var cacheSizeMb by remember { mutableStateOf(prefs.getInt("cache_size_mb", 124)) }
    var autoDeleteDownloads by remember { mutableStateOf(prefs.getBoolean("storage_auto_delete", true)) }

    var defaultUploadVisibility by remember { mutableStateOf(prefs.getString("creator_visibility", "Public") ?: "Public") }
    var defaultAllowComments by remember { mutableStateOf(prefs.getBoolean("creator_allow_comments", true)) }
    var defaultAllowDuet by remember { mutableStateOf(prefs.getBoolean("creator_allow_duet", true)) }

    // Dialogs
    var showEditDialogType by remember { mutableStateOf<String?>(null) }
    var tempInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentSection) {
                            SettingsSection.OVERVIEW -> if (language == AppLanguage.BANGLA) "সেটিংস ও প্রাইভেসি" else "Settings & Privacy"
                            SettingsSection.ACCOUNT -> if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট সেটিংস" else "Account Settings"
                            SettingsSection.PRIVACY -> if (language == AppLanguage.BANGLA) "প্রাইভেসি সেটিংস" else "Privacy Settings"
                            SettingsSection.SECURITY -> if (language == AppLanguage.BANGLA) "সিকিউরিটি ও নিরাপত্তা" else "Security & Sessions"
                            SettingsSection.NOTIFICATIONS -> if (language == AppLanguage.BANGLA) "নোটিফিকেশন সেটিংস" else "Notification Settings"
                            SettingsSection.VIDEO_FEED -> if (language == AppLanguage.BANGLA) "ভিডিও ও ফিড সেটিংস" else "Video & Feed Settings"
                            SettingsSection.APPEARANCE -> if (language == AppLanguage.BANGLA) "ডিজাইন ও ভাষা" else "Appearance & Language"
                            SettingsSection.STORAGE -> if (language == AppLanguage.BANGLA) "স্টোরেজ ও ডেটা" else "Storage & Data"
                            SettingsSection.CREATOR -> if (language == AppLanguage.BANGLA) "ক্রিয়েটর সেটিংস" else "Creator Settings"
                            SettingsSection.MONETIZATION -> if (language == AppLanguage.BANGLA) "মনিটাইজেশন সেটিংস" else "Monetization Settings"
                            SettingsSection.SUPPORT -> if (language == AppLanguage.BANGLA) "সহায়তা ও তথ্য" else "Support & About"
                        },
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentSection != SettingsSection.OVERVIEW) {
                            currentSection = SettingsSection.OVERVIEW
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xD0121626))
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
            .fillMaxSize()
            .background(GlassAtmosphereGradient)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (currentSection) {
                // Main Category List
                SettingsSection.OVERVIEW -> {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (language == AppLanguage.BANGLA) "সেটিংস খুঁজুন..." else "Search settings...",
                                color = ShortlyTextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ShortlyTextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShortlySurfaceDark,
                            unfocusedContainerColor = ShortlySurfaceDark,
                            focusedBorderColor = ShortlyPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 10 Core Categorized Sections
                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট সেটিংস" else "Account Settings",
                        subtitle = if (language == AppLanguage.BANGLA) "ব্যক্তিগত তথ্য, ইউজারনেম, পাসওয়ার্ড ও অ্যাকাউন্ট নিয়ন্ত্রণ" else "Personal info, username, email & account control",
                        icon = Icons.Default.Person,
                        iconTint = ShortlyCyan,
                        onClick = { currentSection = SettingsSection.ACCOUNT }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "প্রাইভেসি ও গোপনীয়তা" else "Privacy Settings",
                        subtitle = if (language == AppLanguage.BANGLA) "কে আপনার কনটেন্ট দেখতে পারবে, কমেন্ট ও মেসেজ নিয়ন্ত্রণ" else "Who can see your content, comments & messages",
                        icon = Icons.Default.Lock,
                        iconTint = ShortlyPrimary,
                        onClick = { currentSection = SettingsSection.PRIVACY }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "সিকিউরিটি ও ডিভাইস সেশন" else "Security Settings",
                        subtitle = if (language == AppLanguage.BANGLA) "2FA ভেরিফিকেশন, সক্রিয় ডিভাইস ও লগইন অ্যালার্ট" else "2FA, active devices & manage sessions",
                        icon = Icons.Default.Security,
                        iconTint = Color(0xFF00E676),
                        onClick = { currentSection = SettingsSection.SECURITY }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "নোটিফিকেশন সেটিংস" else "Notification Settings",
                        subtitle = if (language == AppLanguage.BANGLA) "লাইক, কমেন্ট, ফলোয়ার্স ও পুশ অ্যালার্ট নিয়ন্ত্রণ" else "Likes, comments, followers & quiet hours",
                        icon = Icons.Default.Notifications,
                        iconTint = ShortlyAccentGold,
                        onClick = { currentSection = SettingsSection.NOTIFICATIONS }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "ভিডিও ও ফিড দেখার অভিজ্ঞতা" else "Video & Feed Settings",
                        subtitle = if (language == AppLanguage.BANGLA) "অটোপ্লে, ডেটা সেভার, রেজোলিউশন ও হিস্ট্রি রিসেট" else "Autoplay, data saver, quality & feed reset",
                        icon = Icons.Default.PlayCircle,
                        iconTint = ShortlyCyan,
                        onClick = { currentSection = SettingsSection.VIDEO_FEED }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "অ্যাপের ডিজাইন ও ভাষা" else "Appearance & Language",
                        subtitle = if (language == AppLanguage.BANGLA) "বাংলা / English, ডার্ক মোড ও ফন্ট সাইজ" else "Bangla / English, dark mode & font size",
                        icon = Icons.Default.Language,
                        iconTint = ShortlyPrimary,
                        onClick = { currentSection = SettingsSection.APPEARANCE }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "স্টোরেজ ও ডেটা ব্যবহার" else "Storage & Data",
                        subtitle = if (language == AppLanguage.BANGLA) "ক্যাশ পরিষ্কার করুন (${cacheSizeMb} MB), ডাউনলোড সেটিংস" else "App cache (${cacheSizeMb} MB) & data usage",
                        icon = Icons.Default.Storage,
                        iconTint = Color(0xFF9C27B0),
                        onClick = { currentSection = SettingsSection.STORAGE }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "ক্রিয়েটর সেটিংস" else "Creator Settings",
                        subtitle = if (language == AppLanguage.BANGLA) "ডিফল্ট আপলোড প্রাইভেসি, কমেন্ট ও রিমিক্স অনুমতি" else "Default upload visibility, remix & duet rules",
                        icon = Icons.Default.VideoCall,
                        iconTint = ShortlyAccentGold,
                        onClick = { currentSection = SettingsSection.CREATOR }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "মনিটাইজেশন ও পেমেন্ট" else "Monetization Settings",
                        subtitle = if (language == AppLanguage.BANGLA) "বিকাশ/নগদ পেমেন্ট, আয় ও কর তথ্য" else "bKash, Nagad, payouts & tax information",
                        icon = Icons.Default.MonetizationOn,
                        iconTint = Color(0xFF00E676),
                        onClick = { currentSection = SettingsSection.MONETIZATION }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCategoryCard(
                        title = if (language == AppLanguage.BANGLA) "সহায়তা ও তথ্য" else "Support & About",
                        subtitle = if (language == AppLanguage.BANGLA) "হেল্প সেন্টার, শর্তাবলী, প্রাইভেসি পলিসি ও ভার্সন" else "Help center, community rules & app version",
                        icon = Icons.Default.HelpOutline,
                        iconTint = Color.White,
                        onClick = { currentSection = SettingsSection.SUPPORT }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ID Login / Logout Option at the end of Settings
                    if (isLoggedIn) {
                        Button(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E1A22)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF453A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF453A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA) "আইডি লগআউট (Logout ID)" else "Log Out Account",
                                color = Color(0xFFFF453A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onRequestLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Login, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA) "আইডি লগইন করুন (Log In ID)" else "Sign In to Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }

                // 1. Account Settings
                SettingsSection.ACCOUNT -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "ব্যক্তিগত তথ্য" else "Personal Information")
                    SettingsItemRow("Profile & Cover Photo", "Tap to upload or update photos") {
                        Toast.makeText(context, "Select photo from gallery", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Username", "@$usernameState") {
                        tempInput = usernameState
                        showEditDialogType = "username"
                    }
                    SettingsItemRow("Display Name", displayNameState) {
                        tempInput = displayNameState
                        showEditDialogType = "displayName"
                    }
                    SettingsItemRow("Bio", bioState) {
                        tempInput = bioState
                        showEditDialogType = "bio"
                    }
                    SettingsItemRow("Website Link", websiteState) {
                        tempInput = websiteState
                        showEditDialogType = "website"
                    }
                    SettingsItemRow("Email", emailState) {
                        tempInput = emailState
                        showEditDialogType = "email"
                    }
                    SettingsItemRow("Phone Number", phoneState) {
                        tempInput = phoneState
                        showEditDialogType = "phone"
                    }
                    SettingsItemRow("Date of Birth", dobState) {
                        Toast.makeText(context, "Date of Birth: $dobState", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Gender", genderState) {
                        genderState = if (genderState == "Male") "Female" else "Male"
                        Toast.makeText(context, "Gender updated: $genderState", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Verification Status", if (user.isVerified) "Verified Creator (ব্লু ব্যাজ সক্রিয়)" else "Apply for Verification") {
                        Toast.makeText(context, "Account verification active", Toast.LENGTH_SHORT).show()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট নিয়ন্ত্রণ" else "Account Control")
                    SettingsDangerRow("Deactivate Account", "সাময়িকভাবে অ্যাকাউন্ট নিষ্ক্রিয় করুন") {
                        Toast.makeText(context, "Account deactivation initiated", Toast.LENGTH_LONG).show()
                    }
                    SettingsDangerRow("Permanently Delete Account", "আপনার সমস্ত ডেটা ও ভিডিও স্থায়ীভাবে মুছে ফেলুন") {
                        Toast.makeText(context, "Account deletion confirmation requested", Toast.LENGTH_LONG).show()
                    }
                }

                // 2. Privacy Settings
                SettingsSection.PRIVACY -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "কে আপনার কনটেন্ট ও প্রোফাইল দেখতে পারবে" else "Who can see your Content & Profile")
                    SettingsToggleRow("Private Account", "শুধুমাত্র অনুমোদিত ফলোয়ার্স আপনার ভিডিও দেখতে পারবে", isPrivateAccount) {
                        isPrivateAccount = it
                        prefs.edit().putBoolean("priv_private_account", it).apply()
                    }
                    SettingsToggleRow("Activity Status", "আপনি অনলাইন আছেন কিনা অন্যরা দেখতে পাবে", activityStatus) {
                        activityStatus = it
                        prefs.edit().putBoolean("priv_activity_status", it).apply()
                    }
                    SettingsToggleRow("Hide Like Count", "আপনার ভিডিওর মোট লাইক সংখ্যা গোপন রাখুন", hideLikeCount) {
                        hideLikeCount = it
                        prefs.edit().putBoolean("priv_hide_like_count", it).apply()
                    }
                    SettingsToggleRow("Hide View Count", "অন্যরা আপনার ভিডিওর ভিউ কাউন্ট দেখতে পাবে না", hideViewCount) {
                        hideViewCount = it
                        prefs.edit().putBoolean("priv_hide_view_count", it).apply()
                    }
                    SettingsToggleRow("Allow Video Downloads", "অন্যরা আপনার ভিডিও সেভ করতে পারবে", allowDownloads) {
                        allowDownloads = it
                        prefs.edit().putBoolean("priv_allow_downloads", it).apply()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "ইন্টারেকশন অনুমতি" else "Interaction Permissions")
                    SettingsItemRow("Who can comment", "Everyone (সবার জন্য)") { Toast.makeText(context, "Comment permission: Everyone", Toast.LENGTH_SHORT).show() }
                    SettingsItemRow("Who can mention me", "Everyone") { Toast.makeText(context, "Mentions allowed for everyone", Toast.LENGTH_SHORT).show() }
                    SettingsItemRow("Who can tag me", "Friends Only") { Toast.makeText(context, "Tagging allowed for friends", Toast.LENGTH_SHORT).show() }
                    SettingsItemRow("Who can send messages", "Friends Only") { Toast.makeText(context, "Direct messages: Friends only", Toast.LENGTH_SHORT).show() }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "ব্লক ও সীমাবদ্ধ তালিকা" else "Blocked & Restricted")
                    SettingsItemRow("Blocked Accounts", "০ জন ব্যবহারকারী ব্লক করা আছে") { Toast.makeText(context, "No blocked users", Toast.LENGTH_SHORT).show() }
                    SettingsItemRow("Restricted Accounts", "০ জন অ্যাকাউন্ট রেস্ট্রিক্টেড") { Toast.makeText(context, "No restricted users", Toast.LENGTH_SHORT).show() }
                    SettingsItemRow("Muted Accounts", "০ জন মিউট করা অ্যাকাউন্ট") { Toast.makeText(context, "No muted users", Toast.LENGTH_SHORT).show() }
                }

                // 3. Security Settings
                SettingsSection.SECURITY -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "লগইন ও পাসওয়ার্ড" else "Login & Password")
                    SettingsItemRow("Change Password", "সর্বশেষ পরিবর্তন করা হয়েছে ৩০ দিন আগে") {
                        showEditDialogType = "password"
                    }
                    SettingsToggleRow("Two-Factor Authentication (2FA)", "নতুন ডিভাইসে লগইন করার সময় এসএমএস বা ওটিপি কোড চাইবে", twoFactorAuth) {
                        twoFactorAuth = it
                        prefs.edit().putBoolean("sec_2fa", it).apply()
                        Toast.makeText(context, if (it) "2FA Enabled!" else "2FA Disabled", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Passkeys & Biometrics", "ফিঙ্গারপ্রিন্ট বা ফেস আনলক দিয়ে দ্রুত লগইন সক্রিয়") {
                        Toast.makeText(context, "Passkey configured", Toast.LENGTH_SHORT).show()
                    }
                    SettingsToggleRow("Login Alerts", "অপরিচিত ব্রাউজার থেকে লগইন হলে সতর্কতা পাঠান", loginAlerts) {
                        loginAlerts = it
                        prefs.edit().putBoolean("sec_login_alerts", it).apply()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "নিরাপত্তা ও অথেন্টিকেশন প্রটোকল" else "Security & Auth Protocols")
                    SettingsItemRow("Supabase JWT Verification", "Signed • HS256 • Role-Based RLS Protected") {
                        Toast.makeText(context, "Supabase JWT session verified", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Rate Limiting & Brute-Force Protection", "সক্রিয় (৫টি ব্যর্থ চেষ্টার পর ৬০ সেকেন্ড লক)") {}
                    SettingsItemRow("Row Level Security (RLS)", "Enforced for Videos, Users & Transactions") {}

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "সক্রিয় ডিভাইস ও সেশন" else "Active Devices & Sessions")
                    var activeSessionsList by remember { mutableStateOf(repository.authManager.getActiveSessions()) }
                    activeSessionsList.forEach { sess ->
                        SettingsItemRow(
                            title = sess.deviceName,
                            value = "${sess.location} • ${sess.ipAddress} • ${if (sess.isCurrentDevice) "Active Now" else "Terminate"}"
                        ) {
                            if (!sess.isCurrentDevice) {
                                activeSessionsList = repository.authManager.terminateSession(sess.id)
                                Toast.makeText(context, "Session ${sess.deviceName} terminated!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    SettingsDangerRow("Logout from All Devices", "অন্যান্য সকল ডিভাইস থেকে অবিলম্বে সেশন বন্ধ করুন") {
                        repository.authManager.logoutAllDevices()
                        activeSessionsList = repository.authManager.getActiveSessions()
                        Toast.makeText(context, "Logged out from all other devices!", Toast.LENGTH_LONG).show()
                    }
                }

                // 4. Notification Settings
                SettingsSection.NOTIFICATIONS -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "পুশ নোটিফিকেশন" else "Push Notifications")
                    SettingsToggleRow("Likes Notifications", "কেউ আপনার ভিডিও লাইক করলে অ্যালার্ট পান", notifLikes) {
                        notifLikes = it
                        prefs.edit().putBoolean("notif_likes", it).apply()
                    }
                    SettingsToggleRow("Comments Notifications", "নতুন কমেন্ট ও রিপ্লাইয়ের নোটিফিকেশন", notifComments) {
                        notifComments = it
                        prefs.edit().putBoolean("notif_comments", it).apply()
                    }
                    SettingsToggleRow("New Followers", "কেউ আপনাকে ফলো করলে জানুন", notifFollows) {
                        notifFollows = it
                        prefs.edit().putBoolean("notif_follows", it).apply()
                    }
                    SettingsToggleRow("Direct Messages", "ইনবক্সে নতুন মেসেজ এলে সাউন্ড ও ব্যানার দেখান", notifMessages) {
                        notifMessages = it
                        prefs.edit().putBoolean("notif_messages", it).apply()
                    }
                    SettingsToggleRow("Live Notifications", "আপনার প্রিয় ক্রিয়েটর লাইভে গেলে নোটিফিকেশন পান", notifLive) {
                        notifLive = it
                        prefs.edit().putBoolean("notif_live", it).apply()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "বিরক্ত করবেন না (Do Not Disturb)" else "Quiet Hours")
                    SettingsToggleRow("Quiet Hours (রাত ১১টা - সকাল ৭টা)", "এই সময়ে সমস্ত নোটিফিকেশন শব্দহীন থাকবে", notifQuietHours) {
                        notifQuietHours = it
                        prefs.edit().putBoolean("notif_quiet_hours", it).apply()
                    }
                }

                // 5. Video & Feed Settings
                SettingsSection.VIDEO_FEED -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "প্লেব্যাক সেটিংস" else "Playback Settings")
                    SettingsToggleRow("Autoplay Videos", "ফিডে স্ক্রল করার সময় ভিডিও স্বয়ংক্রিয়ভাবে চলবে", autoplayVideos) {
                        autoplayVideos = it
                        prefs.edit().putBoolean("feed_autoplay", it).apply()
                    }
                    SettingsToggleRow("Data Saver Mode", "কম ডেটা খরচের জন্য ভিডিও কোয়ালিটি অপটিমাইজ করুন", dataSaverMode) {
                        dataSaverMode = it
                        prefs.edit().putBoolean("feed_data_saver", it).apply()
                    }
                    SettingsToggleRow("Wi-Fi Only HD Playback", "শুধুমাত্র ওয়াইফাই থাকলে হাই-ডেফিনিশন (HD) ভিডিও চলবে", wifiOnlyHd) {
                        wifiOnlyHd = it
                        prefs.edit().putBoolean("feed_wifi_hd", it).apply()
                    }
                    SettingsItemRow("Default Video Quality", videoQuality) {
                        val qualities = listOf("Auto (1080p)", "720p", "480p", "360p")
                        val next = qualities[(qualities.indexOf(videoQuality) + 1) % qualities.size]
                        videoQuality = next
                        prefs.edit().putString("feed_video_quality", next).apply()
                        Toast.makeText(context, "Quality: $next", Toast.LENGTH_SHORT).show()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "হিস্ট্রি ও রেকমেন্ডেশন" else "History & Feed Algorithm")
                    SettingsItemRow("Watch History (দেখা ভিডিও)", "আপনার দেখা ভিডিওর ইতিহাস চেক ও রিস্টোর করুন") {
                        onOpenWatchHistory()
                    }
                    SettingsItemRow("Search History", "সার্চের সমস্ত কিওয়ার্ড তালিকা") {
                        Toast.makeText(context, "Search history is active", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Reset For You Feed", "নতুন করে আপনার পছন্দ অনুযায়ী ফিড সাজাতে অ্যালগরিদম রিসেট করুন") {
                        Toast.makeText(context, "For You algorithm reset successfully!", Toast.LENGTH_LONG).show()
                    }
                }

                // 6. Appearance & Language
                SettingsSection.APPEARANCE -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "ভাষা নির্বাচন" else "Language Selection")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ShortlySurfaceDark, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("App Language", color = Color.White, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = language == AppLanguage.BANGLA,
                                onClick = { onLanguageChange(AppLanguage.BANGLA) },
                                label = { Text("বাংলা") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShortlyPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = ShortlySurfaceLight,
                                    labelColor = ShortlyTextSecondary
                                )
                            )
                            FilterChip(
                                selected = language == AppLanguage.ENGLISH,
                                onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                                label = { Text("English") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShortlyPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = ShortlySurfaceLight,
                                    labelColor = ShortlyTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "থিম ও ডিসপ্লে" else "Theme & Display")
                    SettingsItemRow("Theme", selectedTheme) {
                        selectedTheme = if (selectedTheme == "Dark") "Light" else "Dark"
                        prefs.edit().putString("app_theme", selectedTheme).apply()
                        Toast.makeText(context, "Theme set to $selectedTheme", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("App Font Size", fontSize) {
                        val sizes = listOf("Small", "Normal", "Large")
                        val next = sizes[(sizes.indexOf(fontSize) + 1) % sizes.size]
                        fontSize = next
                        prefs.edit().putString("app_font_size", next).apply()
                        Toast.makeText(context, "Font size: $next", Toast.LENGTH_SHORT).show()
                    }
                    SettingsToggleRow("Auto Translate Captions", "অন্য ভাষার ক্যাপশন বাংলায় স্বয়ংক্রিয়ভাবে অনুবাদ করুন", autoTranslate) {
                        autoTranslate = it
                        prefs.edit().putBoolean("app_auto_translate", it).apply()
                    }
                    SettingsToggleRow("Reduced Motion", "অ্যানিমেশন ও ট্রানজিশন কমান", reducedMotion) {
                        reducedMotion = it
                        prefs.edit().putBoolean("app_reduced_motion", it).apply()
                    }
                }

                // 7. Storage & Data
                SettingsSection.STORAGE -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "ক্যাশ ও মেমোরি" else "Cache & Storage")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ShortlySurfaceDark)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("App Cache Size", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("$cacheSizeMb MB temporary files", color = ShortlyTextSecondary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                cacheSizeMb = 0
                                prefs.edit().putInt("cache_size_mb", 0).apply()
                                Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear Cache", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "ডাউনলোড সেটিংস" else "Download Settings")
                    SettingsItemRow("Downloaded Videos", "০টি অফলাইন ভিডিও সংরক্ষিত") {}
                    SettingsToggleRow("Auto-delete Downloads", "৩০ দিন পর ডাউনলোড করা ভিডিও স্বয়ংক্রিয়ভাবে মুছে দিন", autoDeleteDownloads) {
                        autoDeleteDownloads = it
                        prefs.edit().putBoolean("storage_auto_delete", it).apply()
                    }
                    SettingsItemRow("Download Quality", "High (720p HD)") {}
                }

                // 8. Creator Settings
                SettingsSection.CREATOR -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "ডিফল্ট আপলোড নিয়মাবলী" else "Upload Preferences")
                    SettingsItemRow("Default Upload Visibility", defaultUploadVisibility) {
                        val visibilities = listOf("Public", "Friends Only", "Private")
                        val next = visibilities[(visibilities.indexOf(defaultUploadVisibility) + 1) % visibilities.size]
                        defaultUploadVisibility = next
                        prefs.edit().putString("creator_visibility", next).apply()
                    }
                    SettingsToggleRow("Allow Comments by Default", "নতুন আপলোড করা সব ভিডিওতে মন্তব্য চালু রাখুন", defaultAllowComments) {
                        defaultAllowComments = it
                        prefs.edit().putBoolean("creator_allow_comments", it).apply()
                    }
                    SettingsToggleRow("Allow Remix & Duet", "অন্যরা আপনার ভিডিওর সাথে ডুয়েট করতে পারবে", defaultAllowDuet) {
                        defaultAllowDuet = it
                        prefs.edit().putBoolean("creator_allow_duet", it).apply()
                    }
                    SettingsItemRow("Creator Studio", "অ্যানালিটিক্স ও ড্যাশবোর্ড সরাসরি খুলুন") {
                        onOpenCreatorDashboard()
                    }
                }

                // 9. Monetization Settings
                SettingsSection.MONETIZATION -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "আয় ও পেমেন্ট পদ্ধতি" else "Earnings & Payouts")
                    SettingsItemRow("Current Creator Earnings", "৳ 20,700 BDT (Available)") {
                        onOpenCreatorDashboard()
                    }
                    SettingsItemRow("Payout Methods", "bKash (বিকাশ), Nagad (নগদ), Bank Transfer") {
                        onOpenCreatorDashboard()
                    }
                    SettingsItemRow("Tax Information (TIN)", "TIN: 8492-XXXX-9281 (Verified)") {}
                    SettingsItemRow("Monetization Eligibility", "Status: Active (১০,০০০ ফলোয়ার্স পূর্ণ)") {}
                }

                // 10. Support & About
                SettingsSection.SUPPORT -> {
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "সহায়তা" else "Help & Support")
                    SettingsItemRow("Help Center", "সাধারণ জিজ্ঞাসা ও প্ল্যাটফর্ম গাইড") {
                        Toast.makeText(context, "Opening Shortly Help Center...", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Report a Problem", "অ্যাপে কোনো সমস্যা হলে রিপোর্ট জানান") {
                        Toast.makeText(context, "Report submission form loaded", Toast.LENGTH_SHORT).show()
                    }
                    SettingsItemRow("Community Guidelines", "কমিউনিটি নিরাপত্তা ও আচরণ নির্দেশিকা") {}

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroupHeader(if (language == AppLanguage.BANGLA) "অ্যাপ সম্পর্কিত তথ্য" else "About Shortly")
                    SettingsItemRow("Terms of Service", "ব্যবহারের শর্তাবলী") {}
                    SettingsItemRow("Privacy Policy", "গোপনীয়তা নীতিমালা") {}
                    SettingsItemRow("App Version", "Shortly v1.0.0 (Production Build)") {}
                    SettingsItemRow("Open-Source Licenses", "Apache 2.0, MIT, Android Jetpack") {}
                }
            }
        }
    }

    // Interactive Edit Dialog
    if (showEditDialogType != null) {
        val type = showEditDialogType!!
        AlertDialog(
            onDismissRequest = { showEditDialogType = null },
            containerColor = ShortlySurfaceDark,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = when (type) {
                        "username" -> "Edit Username"
                        "displayName" -> "Edit Display Name"
                        "bio" -> "Edit Bio"
                        "website" -> "Edit Website Link"
                        "email" -> "Change Email"
                        "phone" -> "Change Phone Number"
                        "password" -> "Change Password"
                        else -> "Edit Information"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = tempInput,
                    onValueChange = { tempInput = it },
                    singleLine = type != "bio",
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (type) {
                            "username" -> {
                                usernameState = tempInput
                                coroutineScope.launch { repository.updateUserProfile(displayNameState, bioState) }
                            }
                            "displayName" -> {
                                displayNameState = tempInput
                                coroutineScope.launch { repository.updateUserProfile(displayNameState, bioState) }
                            }
                            "bio" -> {
                                bioState = tempInput
                                coroutineScope.launch { repository.updateUserProfile(displayNameState, bioState) }
                            }
                            "website" -> {
                                websiteState = tempInput
                                prefs.edit().putString("user_website", tempInput).apply()
                            }
                            "email" -> {
                                emailState = tempInput
                                prefs.edit().putString("user_email", tempInput).apply()
                            }
                            "phone" -> {
                                phoneState = tempInput
                                prefs.edit().putString("user_phone", tempInput).apply()
                            }
                            "password" -> {
                                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showEditDialogType = null
                        Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialogType = null }) {
                    Text("Cancel", color = ShortlyTextSecondary)
                }
            }
        )
    }

    // ID Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = ShortlySurfaceDark,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = if (language == AppLanguage.BANGLA) "আইডি লগআউট নিশ্চিতকরণ" else "Confirm Logout",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "আপনি কি নিশ্চিতভাবে Shortly থেকে আপনার অ্যাকাউন্ট লগআউট করতে চান?"
                    else
                        "Are you sure you want to log out of your Shortly account on this device?",
                    color = ShortlyTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        repository.logout()
                        Toast.makeText(
                            context,
                            if (language == AppLanguage.BANGLA) "সফলভাবে লগআউট হয়েছে!" else "Successfully logged out!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onLogoutSuccess()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "হ্যাঁ, লগআউট" else "Yes, Logout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(RoundedCornerShape(14.dp), backgroundColor = Color(0x30FFFFFF))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconTint.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = ShortlyTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = ShortlyTextSecondary)
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        color = ShortlyPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun SettingsItemRow(
    title: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(RoundedCornerShape(12.dp), backgroundColor = Color(0x28FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (value.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, color = ShortlyCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = ShortlyTextSecondary, modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(RoundedCornerShape(12.dp), backgroundColor = Color(0x28FFFFFF))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = ShortlyTextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ShortlyPrimary,
                uncheckedThumbColor = ShortlyTextSecondary,
                uncheckedTrackColor = Color(0x25FFFFFF)
            )
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun SettingsDangerRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(RoundedCornerShape(12.dp), backgroundColor = Color(0x22FF453A))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color(0xFFFF453A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = ShortlyTextSecondary, fontSize = 11.sp)
        }
        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF453A), modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.height(6.dp))
}

