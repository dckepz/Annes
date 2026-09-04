package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RegisterUserRequest
import com.example.data.models.SavedLoginInfo
import com.example.data.models.User
import com.example.ui.components.AnnesGeometricLogo
import com.example.ui.components.AnnesLogoVariant
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    savedLoginInfo: SavedLoginInfo?,
    registeredUsers: List<User>,
    onLoginClick: (String, String, Boolean, (Boolean, String?) -> Unit) -> Unit,
    onBiometricLoginClick: ((Boolean, String?) -> Unit) -> Unit,
    onRegisterUserClick: (RegisterUserRequest, (Boolean, String?) -> Unit) -> Unit,
    onConfigureServerClick: () -> Unit,
    onToggleThemeClick: () -> Unit,
    isDarkTheme: Boolean,
    isLoading: Boolean,
    currentServerUrl: String
) {
    // 0: Admin Portal, 1: Staff Portal
    var selectedRoleIndex by remember {
        mutableStateOf(if (savedLoginInfo?.userRole?.lowercase() == "staff") 1 else 0)
    }

    // Staff Sub-tab: 0 = Staff Sign In, 1 = Create Staff Account
    var staffSubTab by remember { mutableStateOf(0) }

    // Admin Credentials
    var adminIdentifier by remember {
        mutableStateOf(if (savedLoginInfo?.userRole?.lowercase() != "staff") savedLoginInfo?.email ?: "" else "")
    }
    var adminPassword by remember {
        mutableStateOf(if (savedLoginInfo?.userRole?.lowercase() != "staff") savedLoginInfo?.password ?: "" else "")
    }
    var adminPasswordVisible by remember { mutableStateOf(false) }

    // Staff Sign In Credentials
    var staffPhone by remember {
        mutableStateOf(if (savedLoginInfo?.userRole?.lowercase() == "staff") savedLoginInfo?.email ?: "" else "")
    }
    var staffPin by remember {
        mutableStateOf(if (savedLoginInfo?.userRole?.lowercase() == "staff") savedLoginInfo?.password ?: "" else "")
    }
    var staffPinVisible by remember { mutableStateOf(false) }

    // Staff Registration Credentials (Phone + Numeric PIN only)
    var regStaffPhone by remember { mutableStateOf("") }
    var regStaffPin by remember { mutableStateOf("") }
    var regStaffPinConfirm by remember { mutableStateOf("") }
    var regStaffPinVisible by remember { mutableStateOf(false) }

    var rememberMe by remember {
        mutableStateOf(savedLoginInfo?.rememberMe ?: true)
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showBiometricPrompt by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val bgGradient = if (isDarkTheme) {
        Brush.verticalGradient(listOf(Color(0xFF20160B), AppBlack, Color(0xFF0F101A)))
    } else {
        Brush.verticalGradient(listOf(LightHeaderHero, LightBg, Color(0xFFF0EBE0)))
    }

    val cardBg = if (isDarkTheme) SurfaceCard else LightCard
    val elevatedCardBg = if (isDarkTheme) SurfaceCardElevated else LightCardElevated
    val cardBorder = if (isDarkTheme) SurfaceCardBorder else LightCardBorder
    val textColor = if (isDarkTheme) TextPrimary else LightTextPrimary
    val textMutedColor = if (isDarkTheme) TextMuted else LightTextMuted
    val textSecColor = if (isDarkTheme) TextSecondary else LightTextSecondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Theme switch button at top-right
        IconButton(
            onClick = onToggleThemeClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color(0x33FFB300) else Color(0x22D4AF37))
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = "Toggle Theme",
                tint = PrimaryGold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Boutique Monogram Logo
            AnnesGeometricLogo(
                size = 85.dp,
                animated = true,
                variant = AnnesLogoVariant.White,
                showSubtitle = true,
                subtitleText = "HAUTE COUTURE & LUXURY ATELIER"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // PRIMARY ROLE SELECTOR: ADMIN VS STAFF (Strict Separation)
            // ========================================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                color = elevatedCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Admin Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedRoleIndex = 0
                                errorMessage = null
                                successMessage = null
                            }
                            .testTag("tab_role_admin"),
                        color = if (selectedRoleIndex == 0) PrimaryGold else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (selectedRoleIndex == 0) AppBlack else textSecColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Portal",
                                color = if (selectedRoleIndex == 0) AppBlack else textSecColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Staff Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedRoleIndex = 1
                                errorMessage = null
                                successMessage = null
                            }
                            .testTag("tab_role_staff"),
                        color = if (selectedRoleIndex == 1) FashionCoral else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Badge,
                                contentDescription = null,
                                tint = if (selectedRoleIndex == 1) Color.White else textSecColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Staff Portal",
                                color = if (selectedRoleIndex == 1) Color.White else textSecColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            if (selectedRoleIndex == 0) PrimaryGold else FashionCoral,
                            cardBorder
                        )
                    ),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedRoleIndex == 0) {
                        // ========================================================
                        // ADMIN ACCESS (PREDETERMINED ACCOUNT — NO SIGN UP)
                        // ========================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = PrimaryGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Administrator Access",
                                style = MaterialTheme.typography.titleLarge,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Sign in to access boutique inventory, POS sales, and analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 18.dp)
                        )

                        // Admin Phone Number or Username Input
                        OutlinedTextField(
                            value = adminIdentifier,
                            onValueChange = {
                                adminIdentifier = it
                                errorMessage = null
                            },
                            label = { Text("Phone Number or Email") },
                            placeholder = { Text("07XXXXXXXX") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Phone, contentDescription = null, tint = PrimaryGold)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGold,
                                unfocusedBorderColor = cardBorder,
                                focusedLabelColor = PrimaryGold,
                                cursorColor = PrimaryGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_admin_phone")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Admin Password Input
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = {
                                adminPassword = it
                                errorMessage = null
                            },
                            label = { Text("Admin Password") },
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryGold)
                            },
                            trailingIcon = {
                                IconButton(onClick = { adminPasswordVisible = !adminPasswordVisible }) {
                                    Icon(
                                        imageVector = if (adminPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null,
                                        tint = textMutedColor
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (adminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    onLoginClick(adminIdentifier, adminPassword, rememberMe) { success, err ->
                                        if (!success) errorMessage = err
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGold,
                                unfocusedBorderColor = cardBorder,
                                focusedLabelColor = PrimaryGold,
                                cursorColor = PrimaryGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_admin_password")
                        )

                        // Remember Me Checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryGold,
                                    checkmarkColor = AppBlack
                                ),
                                modifier = Modifier.testTag("checkbox_remember_me")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Keep me signed in (Remember Me)",
                                color = textSecColor,
                                fontSize = 13.sp
                            )
                        }

                        // Error feedback
                        if (!errorMessage.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = StatusDangerBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = errorMessage ?: "", color = StatusDanger, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Admin Login Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                onLoginClick(adminIdentifier, adminPassword, rememberMe) { success, err ->
                                    if (!success) errorMessage = err
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_admin_login"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGold,
                                contentColor = AppBlack
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = AppBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log In as Administrator",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Biometric Login Button
                        OutlinedButton(
                            onClick = {
                                showBiometricPrompt = true
                                onBiometricLoginClick { success, err ->
                                    showBiometricPrompt = false
                                    if (!success) errorMessage = err
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_biometric_login"),
                            shape = RoundedCornerShape(14.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(DarkGold, PrimaryGold))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDarkTheme) PrimaryGold else DarkGold
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = "Biometric Login",
                                tint = PrimaryGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Biometric Quick Sign In",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                    } else {
                        // ========================================================
                        // STAFF PORTAL (SIGN IN OR FAST MINIMAL SELF-REGISTRATION)
                        // ========================================================

                        // Sub-Tab Switcher: Staff Sign In vs Create Staff Account
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                            color = elevatedCardBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(3.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            staffSubTab = 0
                                            errorMessage = null
                                            successMessage = null
                                        }
                                        .testTag("tab_staff_signin"),
                                    color = if (staffSubTab == 0) FashionCoral else Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Staff Sign In",
                                            color = if (staffSubTab == 0) Color.White else textSecColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            staffSubTab = 1
                                            errorMessage = null
                                            successMessage = null
                                        }
                                        .testTag("tab_staff_signup"),
                                    color = if (staffSubTab == 1) FashionCoral else Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Create Staff Account",
                                            color = if (staffSubTab == 1) Color.White else textSecColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (staffSubTab == 0) {
                            // Staff Sign In (Phone + Numeric PIN)
                            Text(
                                text = "Boutique Sales Floor Access",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enter registered phone number and numeric PIN",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecColor,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            // Phone Number Field
                            OutlinedTextField(
                                value = staffPhone,
                                onValueChange = {
                                    staffPhone = it
                                    errorMessage = null
                                },
                                label = { Text("Phone Number") },
                                placeholder = { Text("0712345678") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = FashionCoral)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FashionCoral,
                                    unfocusedBorderColor = cardBorder,
                                    focusedLabelColor = FashionCoral,
                                    cursorColor = FashionCoral
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_staff_phone")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Numeric Password / PIN Field
                            OutlinedTextField(
                                value = staffPin,
                                onValueChange = {
                                    staffPin = it
                                    errorMessage = null
                                },
                                label = { Text("Numeric Password (PIN)") },
                                placeholder = { Text("••••") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Pin, contentDescription = null, tint = FashionCoral)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { staffPinVisible = !staffPinVisible }) {
                                        Icon(
                                            imageVector = if (staffPinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = null,
                                            tint = textMutedColor
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (staffPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onLoginClick(staffPhone, staffPin, rememberMe) { success, err ->
                                            if (!success) errorMessage = err
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FashionCoral,
                                    unfocusedBorderColor = cardBorder,
                                    focusedLabelColor = FashionCoral,
                                    cursorColor = FashionCoral
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_staff_password")
                            )

                            // Remember Me
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = FashionCoral,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Remember this device",
                                    color = textSecColor,
                                    fontSize = 13.sp
                                )
                            }

                            if (!errorMessage.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusDangerBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = errorMessage ?: "", color = StatusDanger, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Staff Sign In Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onLoginClick(staffPhone, staffPin, rememberMe) { success, err ->
                                        if (!success) errorMessage = err
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_staff_login"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FashionCoral,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign In as Staff", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                        } else {
                            // ========================================================
                            // CREATE STAFF ACCOUNT (PHONE + NUMERIC PIN ONLY)
                            // ========================================================
                            Text(
                                text = "New Floor Staff Registration",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Quick setup with phone number and numeric PIN only",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecColor,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            // Phone Number
                            OutlinedTextField(
                                value = regStaffPhone,
                                onValueChange = {
                                    regStaffPhone = it
                                    errorMessage = null
                                },
                                label = { Text("Phone Number *") },
                                placeholder = { Text("0712345678") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = FashionCoral)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FashionCoral,
                                    unfocusedBorderColor = cardBorder,
                                    focusedLabelColor = FashionCoral,
                                    cursorColor = FashionCoral
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_staff_reg_phone")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Numeric Password (PIN)
                            OutlinedTextField(
                                value = regStaffPin,
                                onValueChange = {
                                    regStaffPin = it
                                    errorMessage = null
                                },
                                label = { Text("Numeric Password (PIN) *") },
                                placeholder = { Text("4 to 6 digits e.g. 1234") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Pin, contentDescription = null, tint = FashionCoral)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regStaffPinVisible = !regStaffPinVisible }) {
                                        Icon(
                                            imageVector = if (regStaffPinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = null,
                                            tint = textMutedColor
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (regStaffPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FashionCoral,
                                    unfocusedBorderColor = cardBorder,
                                    focusedLabelColor = FashionCoral,
                                    cursorColor = FashionCoral
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_staff_reg_pin")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Confirm Numeric Password (PIN)
                            OutlinedTextField(
                                value = regStaffPinConfirm,
                                onValueChange = {
                                    regStaffPinConfirm = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Numeric Password *") },
                                placeholder = { Text("Re-enter numeric PIN") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = FashionCoral)
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FashionCoral,
                                    unfocusedBorderColor = cardBorder,
                                    focusedLabelColor = FashionCoral,
                                    cursorColor = FashionCoral
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_staff_reg_pin_confirm")
                            )

                            if (!errorMessage.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusDangerBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = errorMessage ?: "", color = StatusDanger, fontSize = 12.sp)
                                    }
                                }
                            }

                            if (!successMessage.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = FashionEmerald.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FashionEmerald, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = successMessage ?: "", color = FashionEmerald, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Register Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    val cleanPhone = regStaffPhone.trim()
                                    if (cleanPhone.isBlank() || regStaffPin.isBlank()) {
                                        errorMessage = "Please enter both phone number and numeric password"
                                        return@Button
                                    }
                                    if (regStaffPin != regStaffPinConfirm) {
                                        errorMessage = "Numeric passwords do not match"
                                        return@Button
                                    }
                                    if (!regStaffPin.all { it.isDigit() }) {
                                        errorMessage = "Password must be numeric (digits only)"
                                        return@Button
                                    }

                                    val phoneDigits = cleanPhone.replace(Regex("[^0-9]"), "")
                                    val req = RegisterUserRequest(
                                        username = "staff_$phoneDigits",
                                        email = "staff_$phoneDigits@annesfashion.com",
                                        password = regStaffPin,
                                        role = "staff",
                                        firstName = "Floor Staff",
                                        lastName = phoneDigits.takeLast(4).ifBlank { "POS" },
                                        phone = cleanPhone
                                    )

                                    onRegisterUserClick(req) { success, err ->
                                        if (success) {
                                            successMessage = "Account created! You can now sign in with your phone & PIN."
                                            staffPhone = cleanPhone
                                            staffPin = regStaffPin
                                            staffSubTab = 0
                                        } else {
                                            errorMessage = err
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_register_staff"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FashionCoral,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Staff Account", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Discrete Boutique Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Checkroom,
                    contentDescription = null,
                    tint = PrimaryGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Anne's Fashion Line • Nairobi Atelier & Cloud POS",
                    color = textMutedColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
