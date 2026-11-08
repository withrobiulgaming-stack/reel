package com.example.ui.screens.creator

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PayoutRequestEntity
import com.example.data.repository.ShortlyRepository
import com.example.data.sample.SampleData
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorDashboardScreen(
    repository: ShortlyRepository,
    language: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val myVideos by repository.getVideosByCreator(repository.currentUserId).collectAsState(initial = emptyList())
    val payouts by repository.getCreatorPayouts().collectAsState(initial = emptyList())

    // Metrics computation
    val totalViews = remember(myVideos) { myVideos.sumOf { it.viewsCount.toLong() } }
    val totalLikes = remember(myVideos) { myVideos.sumOf { it.likesCount.toLong() } }
    val totalComments = remember(myVideos) { myVideos.sumOf { it.commentsCount.toLong() } }
    val estimatedWatchHours = remember(totalViews) { (totalViews * 18 / 3600.0).toInt() }
    val engagementRate = remember(totalViews, totalLikes, totalComments) {
        if (totalViews > 0) String.format("%.1f%%", ((totalLikes + totalComments).toDouble() / totalViews.toDouble()) * 100) else "8.4%"
    }
    // Estimated Earnings at ৳120 per 10,000 views
    val estimatedEarningsBdt = remember(totalViews) { (totalViews * 0.012).toInt() }

    var showPayoutDialog by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf("bKash") }
    var accountInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.creatorDashboardTitle(language),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShortlySurfaceDark
                )
            )
        },
        containerColor = Color.Black,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Earnings Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2A1B4E), Color(0xFF16152B))
                        )
                    )
                    .border(1.dp, ShortlyAccentGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.estimatedEarnings(language),
                            color = ShortlyTextSecondary,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(ShortlyAccentGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "৳ BDT",
                                color = ShortlyAccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "৳ ${String.format("%,d", estimatedEarningsBdt + 20700)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showPayoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.withdrawFunds(language),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Analytics KPI Grid
            Text(
                text = if (language == AppLanguage.BANGLA) "পারফরম্যান্স সারাংশ" else "Performance Overview",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsKpiCard(
                    title = Strings.totalViews(language),
                    value = String.format("%,d", totalViews),
                    icon = Icons.Default.Visibility,
                    tint = ShortlyCyan,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsKpiCard(
                    title = Strings.watchTimeHours(language),
                    value = "$estimatedWatchHours hrs",
                    icon = Icons.Default.Schedule,
                    tint = ShortlyAccentGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsKpiCard(
                    title = Strings.likes(language),
                    value = String.format("%,d", totalLikes),
                    icon = Icons.Default.Favorite,
                    tint = ShortlyPrimary,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsKpiCard(
                    title = Strings.engagementRate(language),
                    value = engagementRate,
                    icon = Icons.Default.TrendingUp,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Monetization Eligibility Progress
            Text(
                text = Strings.monetizationStatus(language),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShortlySurfaceDark, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.eligibleForMonetization(language),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "আপনার অ্যাকাউন্ট ১০,০০০ অনুসারী এবং ১০০,০০০ ভিউ মাইলফলক পূরণ করেছে। প্রতি বিজ্ঞাপন ও ভিউয়ের জন্য রেভিনিউ শেয়ারিং সক্রিয় রয়েছে।"
                    else
                        "Your account has completed the 10,000 followers and 100,000 views threshold. In-stream ad revenue sharing is active.",
                    color = ShortlyTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions / Withdrawals
            Text(
                text = Strings.recentTransactions(language),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (payouts.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো লেনদেন রেকর্ড নেই" else "No transaction records",
                        color = ShortlyTextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    payouts.forEach { payout ->
                        PayoutRow(payout = payout, language = language)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Payout Request Dialog (bKash / Nagad / Bank)
    if (showPayoutDialog) {
        AlertDialog(
            onDismissRequest = { showPayoutDialog = false },
            containerColor = ShortlySurfaceDark,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = Strings.withdrawFunds(language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = Strings.selectPayoutMethod(language),
                        color = ShortlyTextSecondary,
                        fontSize = 12.sp
                    )

                    // Method Selector Chips (bKash, Nagad, Bank)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("bKash" to BkashPink, "Nagad" to NagadOrange, "Bank" to ShortlyCyan).forEach { (method, brandColor) ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = brandColor,
                                    selectedLabelColor = Color.White,
                                    containerColor = ShortlySurfaceLight,
                                    labelColor = Color.White
                                ),
                                border = null
                            )
                        }
                    }

                    OutlinedTextField(
                        value = accountInput,
                        onValueChange = { accountInput = it },
                        label = { Text(Strings.accountOrMobileNumber(language), color = ShortlyTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShortlySurfaceLight,
                            unfocusedContainerColor = ShortlySurfaceLight,
                            focusedBorderColor = ShortlyPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text(Strings.amountBdt(language), color = ShortlyTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShortlySurfaceLight,
                            unfocusedContainerColor = ShortlySurfaceLight,
                            focusedBorderColor = ShortlyPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 1000.0
                        coroutineScope.launch {
                            repository.requestPayout(
                                amountBdt = amount,
                                method = selectedMethod,
                                accountInfo = accountInput.ifBlank { "017XXXXXXXX" }
                            )
                        }
                        showPayoutDialog = false
                        Toast.makeText(context, Strings.withdrawalSubmitted(language), Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary)
                ) {
                    Text(Strings.submitWithdrawal(language), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayoutDialog = false }) {
                    Text(Strings.cancel(language), color = ShortlyTextSecondary)
                }
            }
        )
    }
}

@Composable
fun AnalyticsKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(ShortlySurfaceDark, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = ShortlyTextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PayoutRow(
    payout: PayoutRequestEntity,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val statusColor = when (payout.status) {
        "APPROVED" -> Color(0xFF00E676)
        "REJECTED" -> Color(0xFFFF453A)
        else -> ShortlyAccentGold
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ShortlySurfaceDark, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${payout.method} • ${payout.accountInfo}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeFormat.format(Date(payout.createdAt)),
                color = ShortlyTextSecondary,
                fontSize = 10.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "৳ ${String.format("%,.0f", payout.amountBdt)}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = payout.status,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
