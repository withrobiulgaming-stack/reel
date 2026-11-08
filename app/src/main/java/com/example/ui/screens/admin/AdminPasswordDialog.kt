package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.AppLanguage
import com.example.ui.theme.*

@Composable
fun AdminPasswordDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF0131728)),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0x60FFFFFF), Color(0x15FFFFFF))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Glassy Icon Aura
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(ShortlyPrimary.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, ShortlyPrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = ShortlyPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = if (language == AppLanguage.BANGLA) "অ্যাডমিন ভেরিফিকেশন" else "Admin Access Portal",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "অ্যাডমিন প্যানেলে প্রবেশ করতে সুরক্ষিত মাস্টার পাসওয়ার্ড লিখুন:"
                    else
                        "Enter the secure master password to access Admin Moderation Panel:",
                    color = ShortlyTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "অ্যাডমিন পাসওয়ার্ড লিখুন" else "Enter Admin Password",
                            color = ShortlyTextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ShortlyPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = ShortlyTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x35FFFFFF),
                        unfocusedContainerColor = Color(0x20FFFFFF),
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFFF453A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x25FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ShortlyTextSecondary)
                    ) {
                        Text(text = if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (passwordInput.trim() == "robiul1000") {
                                onSuccess()
                            } else {
                                errorMessage = if (language == AppLanguage.BANGLA)
                                    "ভুল অ্যাডমিন পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন।"
                                else
                                    "Incorrect Admin Password! Access Denied."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "প্রবেশ করুন" else "Unlock Portal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
