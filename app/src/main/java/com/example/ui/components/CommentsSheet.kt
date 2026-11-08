package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.CommentEntity
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.ShortlyPrimary
import com.example.ui.theme.ShortlySurfaceDark
import com.example.ui.theme.ShortlySurfaceLight
import com.example.ui.theme.ShortlyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    comments: List<CommentEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSendComment: (text: String, parentId: String?) -> Unit,
    onLikeComment: (commentId: String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<CommentEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ShortlySurfaceDark,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${Strings.comments(language)} (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Strings.close(language),
                        tint = ShortlyTextSecondary
                    )
                }
            }

            HorizontalDivider(color = ShortlySurfaceLight, thickness = 1.dp)

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.noCommentsYet(language),
                        color = ShortlyTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filter root comments and display replies nested
                    val rootComments = comments.filter { it.parentCommentId == null }
                    val repliesMap = comments.filter { it.parentCommentId != null }.groupBy { it.parentCommentId }

                    items(rootComments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            language = language,
                            onReply = { replyingTo = comment },
                            onLike = { onLikeComment(comment.id) }
                        )

                        // Render nested replies
                        val replies = repliesMap[comment.id] ?: emptyList()
                        for (reply in replies) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 42.dp, top = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                CommentItem(
                                    comment = reply,
                                    language = language,
                                    isReply = true,
                                    onReply = { replyingTo = comment },
                                    onLike = { onLikeComment(reply.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Replying Banner
            if (replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ShortlySurfaceLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${Strings.reply(language)}: @${replyingTo?.userName}",
                        color = ShortlyPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { replyingTo = null },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = ShortlyTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (replyingTo != null) "${Strings.reply(language)}..." else Strings.writeComment(language),
                            color = ShortlyTextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendComment(inputText.trim(), replyingTo?.id)
                            inputText = ""
                            replyingTo = null
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .background(
                            if (inputText.isNotBlank()) ShortlyPrimary else ShortlySurfaceLight,
                            CircleShape
                        )
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = Strings.post(language),
                        tint = if (inputText.isNotBlank()) Color.White else ShortlyTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentEntity,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    isReply: Boolean = false,
    onReply: () -> Unit,
    onLike: () -> Unit
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(comment.userAvatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(if (isReply) 28.dp else 36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.userName,
                color = ShortlyTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.content,
                color = Color.White,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Strings.reply(language),
                    color = ShortlyTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onReply() }
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                isLiked = !isLiked
                onLike()
            }
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isLiked) ShortlyPrimary else ShortlyTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${comment.likesCount + if (isLiked) 1 else 0}",
                color = ShortlyTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
