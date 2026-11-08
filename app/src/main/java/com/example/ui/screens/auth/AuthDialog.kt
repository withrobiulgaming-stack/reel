package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthResult
import com.example.data.repository.ShortlyRepository
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class AuthMode {
    LOGIN,
    REGISTER,
    PHONE_OTP,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    language: AppLanguage,
    repository: ShortlyRepository,
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val authManager = repository.authManager

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Email / Password Form State
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }

    // Register Form State
    var registerName by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    // Phone OTP Form State
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCountdown by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Forgot Password Form State
    var resetEmailInput by remember { mutableStateOf("") }
    var resetCodeInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var isResetCodeSent by remember { mutableStateOf(false) }

    // Lockout & Error State
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableLongStateOf(0L) }

    // Countdown Timer for OTP
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            otpCountdown = 60
            while (otpCountdown > 0) {
                delay(1000)
                otpCountdown--
            }
            isTimerRunning = false
        }
    }

    // Check Lockout on open
    LaunchedEffect(Unit) {
        val (locked, sec) = authManager.isLockedOut()
        if (locked) lockoutSeconds = sec
    }

    // Countdown for Lockout
    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            while (lockoutSeconds > 0) {
                delay(1000)
                lockoutSeconds--
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xF2121626),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0x60FFFFFF))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (authMode == AuthMode.FORGOT_PASSWORD) {
                    IconButton(onClick = { authMode = AuthMode.LOGIN; errorMessage = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }

                Text(
                    text = when (authMode) {
                        AuthMode.LOGIN -> if (language == AppLanguage.BANGLA) "লগইন করুন" else "Sign In"
                        AuthMode.REGISTER -> if (language == AppLanguage.BANGLA) "নতুন অ্যাকাউন্ট" else "Create Account"
                        AuthMode.PHONE_OTP -> if (language == AppLanguage.BANGLA) "ফোন ওটিপি লগইন" else "Phone OTP Login"
                        AuthMode.FORGOT_PASSWORD -> if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট পুনরুদ্ধার" else "Account Recovery"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ShortlyTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs (Login / Register / Phone OTP)
            if (authMode != AuthMode.FORGOT_PASSWORD) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ShortlySurfaceLight)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabs = listOf(
                        AuthMode.LOGIN to if (language == AppLanguage.BANGLA) "ইমেইল লগইন" else "Email",
                        AuthMode.REGISTER to if (language == AppLanguage.BANGLA) "রেজিস্টার" else "Register",
                        AuthMode.PHONE_OTP to if (language == AppLanguage.BANGLA) "ফোন ওটিপি" else "Phone OTP"
                    )

                    tabs.forEach { (mode, label) ->
                        val isSelected = authMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ShortlyPrimary else Color.Transparent)
                                .clickable {
                                    authMode = mode
                                    errorMessage = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else ShortlyTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Lockout Warning Banner
            if (lockoutSeconds > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF5A1A1A))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF5252))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA)
                                "অতিরিক্ত ব্যর্থ চেষ্টার কারণে অ্যাকাউন্ট সাময়িক লক। অপেক্ষা করুন: ${lockoutSeconds} সেকেন্ড"
                            else
                                "Account temporarily locked due to failed attempts. Cooldown: ${lockoutSeconds}s",
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 1. Email Login Mode
            if (authMode == AuthMode.LOGIN) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it; errorMessage = null },
                    placeholder = { Text("ইমেইল ঠিকানা (Email address)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ShortlyTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    placeholder = { Text("পাসওয়ার্ড (Password)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ShortlyTextSecondary) },
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberSession = !rememberSession }
                    ) {
                        Checkbox(
                            checked = rememberSession,
                            onCheckedChange = { rememberSession = it },
                            colors = CheckboxDefaults.colors(checkedColor = ShortlyPrimary)
                        )
                        Text(
                            text = if (language == AppLanguage.BANGLA) "সেশন মনে রাখুন" else "Remember session",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = if (language == AppLanguage.BANGLA) "পাসওয়ার্ড ভুলে গেছেন?" else "Forgot password?",
                        color = ShortlyCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                authMode = AuthMode.FORGOT_PASSWORD
                                errorMessage = null
                            }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        when (val result = authManager.loginWithEmail(emailInput, passwordInput, rememberSession)) {
                            is AuthResult.Success -> onLoginSuccess()
                            is AuthResult.Error -> errorMessage = result.message
                            is AuthResult.LockedOut -> lockoutSeconds = result.remainingSeconds
                        }
                    },
                    enabled = lockoutSeconds == 0L,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "প্রবেশ করুন (Sign In)" else "Sign In",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // 2. Register Mode
            if (authMode == AuthMode.REGISTER) {
                OutlinedTextField(
                    value = registerName,
                    onValueChange = { registerName = it; errorMessage = null },
                    placeholder = { Text("আপনার পূর্ণ নাম (Full Name)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ShortlyTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it; errorMessage = null },
                    placeholder = { Text("ইমেইল ঠিকানা (Email)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ShortlyTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    placeholder = { Text("নতুন পাসওয়ার্ড (Password)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ShortlyTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { confirmPasswordInput = it; errorMessage = null },
                    placeholder = { Text("পাসওয়ার্ড নিশ্চিত করুন (Confirm Password)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ShortlyTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceLight,
                        unfocusedContainerColor = ShortlySurfaceLight,
                        focusedBorderColor = ShortlyPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (passwordInput != confirmPasswordInput) {
                            errorMessage = "উভয় পাসওয়ার্ড একই হতে হবে! (Passwords do not match)"
                        } else {
                            when (val result = authManager.registerWithEmail(registerName, emailInput, passwordInput)) {
                                is AuthResult.Success -> onLoginSuccess()
                                is AuthResult.Error -> errorMessage = result.message
                                is AuthResult.LockedOut -> lockoutSeconds = result.remainingSeconds
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট তৈরি করুন (Register)" else "Create Account",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // 3. Phone OTP Login (+880 Bangladesh)
            if (authMode == AuthMode.PHONE_OTP) {
                if (!isOtpSent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShortlySurfaceLight)
                                .padding(horizontal = 14.dp, vertical = 16.dp)
                        ) {
                            Text(text = "🇧🇩 +880", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it; errorMessage = null },
                            placeholder = { Text("17XXXXXXXX (১০ ডিজিট)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ShortlySurfaceLight,
                                unfocusedContainerColor = ShortlySurfaceLight,
                                focusedBorderColor = ShortlyPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val (success, otpOrError) = authManager.sendPhoneOtp(phoneInput)
                            if (success) {
                                isOtpSent = true
                                isTimerRunning = true
                                Toast.makeText(context, " Shortly OTP কোড: $otpOrError", Toast.LENGTH_LONG).show()
                            } else {
                                errorMessage = otpOrError
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "ওটিপি কোড পাঠান (Send OTP)" else "Send OTP Code",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = "মোবাইল নম্বরে পাঠানো ৬-ডিজিটের কোড লিখুন:",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it; errorMessage = null },
                        placeholder = { Text("৬-ডিজিট ওটিপি কোড (e.g. 123456)", color = ShortlyTextSecondary, fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShortlySurfaceLight,
                            unfocusedContainerColor = ShortlySurfaceLight,
                            focusedBorderColor = ShortlyPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTimerRunning) "পুনরায় পাঠান: ${otpCountdown}s" else "কোড পাননি?",
                            color = ShortlyTextSecondary,
                            fontSize = 12.sp
                        )

                        TextButton(
                            onClick = {
                                val (_, otp) = authManager.sendPhoneOtp(phoneInput)
                                isTimerRunning = true
                                Toast.makeText(context, " Shortly নতুন OTP: $otp", Toast.LENGTH_LONG).show()
                            },
                            enabled = !isTimerRunning
                        ) {
                            Text(
                                text = "কোড পুনরায় পাঠান",
                                color = if (!isTimerRunning) ShortlyCyan else ShortlyTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            when (val result = authManager.verifyPhoneOtp(phoneInput, otpInput)) {
                                is AuthResult.Success -> onLoginSuccess()
                                is AuthResult.Error -> errorMessage = result.message
                                is AuthResult.LockedOut -> lockoutSeconds = result.remainingSeconds
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "যাচাই করুন ও প্রবেশ করুন" else "Verify & Sign In",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 4. Forgot Password Mode
            if (authMode == AuthMode.FORGOT_PASSWORD) {
                if (!isResetCodeSent) {
                    Text(
                        text = "আপনার একাউন্টের নিবন্ধিত ইমেইল ঠিকানা লিখুন। পাসওয়ার্ড পরিবর্তনের সিকিউরিটি কোড পাঠানো হবে।",
                        color = ShortlyTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it; errorMessage = null },
                        placeholder = { Text("নিবন্ধিত ইমেইল (Registered Email)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ShortlyTextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShortlySurfaceLight,
                            unfocusedContainerColor = ShortlySurfaceLight,
                            focusedBorderColor = ShortlyPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val (success, codeOrError) = authManager.requestPasswordReset(resetEmailInput)
                            if (success) {
                                isResetCodeSent = true
                                Toast.makeText(context, " Shortly ভেরিফিকেশন কোড: $codeOrError", Toast.LENGTH_LONG).show()
                            } else {
                                errorMessage = codeOrError
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "ভেরিফিকেশন কোড পাঠান", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    OutlinedTextField(
                        value = resetCodeInput,
                        onValueChange = { resetCodeInput = it; errorMessage = null },
                        placeholder = { Text("ইমেইলে প্রাপ্ত ৬-সংখ্যার কোড", color = ShortlyTextSecondary, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShortlySurfaceLight,
                            unfocusedContainerColor = ShortlySurfaceLight,
                            focusedBorderColor = ShortlyPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it; errorMessage = null },
                        placeholder = { Text("নতুন পাসওয়ার্ড লিখুন (New Password)", color = ShortlyTextSecondary, fontSize = 13.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShortlySurfaceLight,
                            unfocusedContainerColor = ShortlySurfaceLight,
                            focusedBorderColor = ShortlyPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val (success, msg) = authManager.confirmPasswordReset(resetEmailInput, resetCodeInput, newPasswordInput)
                            if (success) {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                authMode = AuthMode.LOGIN
                                isResetCodeSent = false
                            } else {
                                errorMessage = msg
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "পাসওয়ার্ড রিসেট সম্পন্ন করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Error Message Display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Social Logins (Google & Apple)
            if (authMode != AuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ShortlySurfaceLight)
                    Text(
                        text = if (language == AppLanguage.BANGLA) " অথবা সামাজিক লগইন " else " or social login ",
                        color = ShortlyTextSecondary,
                        fontSize = 11.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ShortlySurfaceLight)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Google Sign In
                OutlinedButton(
                    onClick = {
                        when (authManager.loginWithGoogle()) {
                            is AuthResult.Success -> onLoginSuccess()
                            else -> {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShortlySurfaceLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = "G  ", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF4285F4))
                    Text(
                        text = Strings.continueWithGoogle(language),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Apple Sign In
                OutlinedButton(
                    onClick = {
                        when (authManager.loginWithApple()) {
                            is AuthResult.Success -> onLoginSuccess()
                            else -> {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShortlySurfaceLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = "  ", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                    Text(
                        text = if (language == AppLanguage.BANGLA) "Apple দিয়ে চালিয়ে যান" else "Continue with Apple",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Guest Continue
            Text(
                text = if (language == AppLanguage.BANGLA) "গেস্ট হিসেবে দেখা চালিয়ে যান" else "Continue as Guest",
                color = ShortlyTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDismiss() }
                    .padding(8.dp)
            )
        }
    }
}
