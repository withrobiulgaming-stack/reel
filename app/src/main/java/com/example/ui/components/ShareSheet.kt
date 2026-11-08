package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.ShortlyPrimary
import com.example.ui.theme.ShortlySurfaceDark
import com.example.ui.theme.ShortlySurfaceLight
import com.example.ui.theme.ShortlyTextSecondary

data class ShareOption(
    val titleBn: String,
    val titleEn: String,
    val icon: ImageVector,
    val iconColor: Color,
    val actionType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    videoTitle: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onShareCompleted: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val options = listOf(
        ShareOption("হোয়াটসঅ্যাপ", "WhatsApp", Icons.Default.Share, Color(0xFF25D366), "whatsapp"),
        ShareOption("ফেসবুক", "Facebook", Icons.Default.ThumbUp, Color(0xFF1877F2), "facebook"),
        ShareOption("লিংক কপি", "Copy Link", Icons.Default.ContentCopy, ShortlyPrimary, "copy"),
        ShareOption("ভিডিও সেভ", "Save Video", Icons.Default.Download, Color(0xFFFFB800), "download"),
        ShareOption("কিউআর কোড", "QR Code", Icons.Default.QrCode, Color(0xFF25F4EE), "qr")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ShortlySurfaceDark,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${Strings.share(language)}: $videoTitle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Strings.close(language),
                        tint = ShortlyTextSecondary
                    )
                }
            }

            HorizontalDivider(color = ShortlySurfaceLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(options.size) { idx ->
                    val option = options[idx]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                when (option.actionType) {
                                    "copy" -> {
                                        clipboardManager.setText(AnnotatedString("https://shortly.social/v/$videoTitle"))
                                        Toast.makeText(
                                            context,
                                            if (language == AppLanguage.BANGLA) "লিংক ক্লিপবোর্ডে কপি করা হয়েছে!" else "Link copied to clipboard!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    "download" -> {
                                        Toast.makeText(
                                            context,
                                            if (language == AppLanguage.BANGLA) "ভিডিও ডাউনলোড শুরু হয়েছে..." else "Video download started...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "${option.titleEn} এ শেয়ার করা হচ্ছে...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                onShareCompleted()
                                onDismiss()
                            }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(ShortlySurfaceLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.titleEn,
                                tint = option.iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) option.titleBn else option.titleEn,
                            color = ShortlyTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
