package com.example.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.ShortlyPrimary
import com.example.ui.theme.ShortlySurfaceDark
import com.example.ui.theme.ShortlySurfaceLight
import com.example.ui.theme.ShortlyTextSecondary

@Composable
fun EditProfileDialog(
    initialDisplayName: String,
    initialBio: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String) -> Unit
) {
    var displayName by remember { mutableStateOf(initialDisplayName) }
    var bio by remember { mutableStateOf(initialBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ShortlySurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = Strings.editProfile(language),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name", color = ShortlyTextSecondary) },
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
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio", color = ShortlyTextSecondary) },
                    minLines = 3,
                    maxLines = 4,
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
                onClick = { onSave(displayName, bio) },
                colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary)
            ) {
                Text(Strings.confirm(language), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language), color = ShortlyTextSecondary)
            }
        }
    )
}
