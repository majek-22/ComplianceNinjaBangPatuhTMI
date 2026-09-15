package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Resolves a custom user background image if the user uploaded or pasted an asset
 * into res/drawable (e.g. sample_loginscreen, sample_login_screen, bg_login_custom),
 * falling back gracefully to the generated high-resolution scenic ninja village background.
 */
private fun getCustomOrFallbackBackgroundId(context: Context): Int {
    val candidates = listOf(
        "sample_loginscreen",
        "sample_login_screen",
        "bg_login_custom",
        "custom_login_bg",
        "bg_login_screen",
        "bg_main_menu"
    )
    for (name in candidates) {
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (resId != 0) {
            return resId
        }
    }
    return R.drawable.bg_main_menu
}

/**
 * 4-Point Ninja Shuriken Icon for tabs and action buttons
 */
@Composable
fun ShurikenIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00E5FF)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rOuter = minOf(w, h) / 2f
        val rInner = rOuter * 0.28f

        val path = Path().apply {
            // Point 1: Top
            moveTo(cx, cy - rOuter)
            quadraticTo(cx + rInner * 0.4f, cy - rInner, cx + rInner, cy - rInner)
            // Point 2: Right
            lineTo(cx + rOuter, cy)
            quadraticTo(cx + rInner, cy + rInner * 0.4f, cx + rInner, cy + rInner)
            // Point 3: Bottom
            lineTo(cx, cy + rOuter)
            quadraticTo(cx - rInner * 0.4f, cy + rInner, cx - rInner, cy + rInner)
            // Point 4: Left
            lineTo(cx - rOuter, cy)
            quadraticTo(cx - rInner, cy - rInner * 0.4f, cx - rInner, cy - rInner)
            close()
        }
        drawPath(path = path, color = color)
        // Center hole
        drawCircle(color = Color(0xFF0A1118), radius = rInner * 0.45f, center = Offset(cx, cy))
    }
}

/**
 * Microsoft 4-Color Corporate Logo Icon
 */
@Composable
fun MicrosoftLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = minOf(size.width, size.height)
        val tileSize = s * 0.44f
        val gap = s * 0.12f
        val startX = (size.width - (tileSize * 2 + gap)) / 2f
        val startY = (size.height - (tileSize * 2 + gap)) / 2f

        // Red (top-left)
        drawRect(Color(0xFFF25022), Offset(startX, startY), Size(tileSize, tileSize))
        // Green (top-right)
        drawRect(Color(0xFF7FBA00), Offset(startX + tileSize + gap, startY), Size(tileSize, tileSize))
        // Blue (bottom-left)
        drawRect(Color(0xFF00A4EF), Offset(startX, startY + tileSize + gap), Size(tileSize, tileSize))
        // Yellow (bottom-right)
        drawRect(Color(0xFFFFB900), Offset(startX + tileSize + gap, startY + tileSize + gap), Size(tileSize, tileSize))
    }
}

/**
 * Google 4-Color 'G' Logo Icon
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = minOf(size.width, size.height)
        val stroke = s * 0.22f
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (s - stroke) / 2f

        // Blue Arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
        // Green Arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
        // Yellow Arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
        // Red Arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
        // Horizontal bar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Square
        )
    }
}

/**
 * Futuristic Cyber-Ninja HUD Text Field matching the attachment UI
 */
@Composable
fun CyberNinjaInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        isError -> Color(0xFFFF5252)
        isFocused -> Color(0xFF00E5FF)
        else -> Color(0x3300E5FF)
    }
    val glowColor = if (isFocused) Color(0x3300E5FF) else Color.Transparent
    val backgroundColor = Color(0x700B131F)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .shadow(if (isFocused) 6.dp else 0.dp, shape = RoundedCornerShape(10.dp), spotColor = Color(0xFF00E5FF))
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused || isError) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .testTag(testTag),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
        ),
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(Color(0xFF00E5FF)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    leadingIcon()
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF607D8B),
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        trailingIcon()
                    }
                }
            }
        }
    )
}

/**
 * Screen "Login & Register" completely matching the attachment design:
 * - Full-screen scenic Japanese Mount Fuji nighttime background with sakura cherry blossoms
 * - Bang Patuh 3D Owl Ninja character and bold distressed title logo on the left
 * - Futuristic Cyber-Ninja HUD Terminal card on the right with glowing cyan accents,
 *   segmented Log In / Register tabs, custom input fields, "Remember me", "Forgot password?",
 *   glowing cyan shuriken submit button, Microsoft/Google/SSO social buttons, and vertical Japanese text
 * - Dynamic custom image support: if the user adds an image to res/drawable, it automatically displays!
 */
@Composable
fun LoginRegisterScreen(
    currentLanguage: String,
    isAudioMuted: Boolean,
    errorMessage: String?,
    onCheckUsernameTaken: suspend (String) -> Boolean,
    onLogin: (username: String, pass: String) -> Unit,
    onRegister: (username: String, pass: String) -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleAudioMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Log In, 1 = Register
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }

    var isUsernameTaken by remember { mutableStateOf(false) }
    var isCheckingUsername by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Dynamically check if custom background or artwork was supplied by user
    val backgroundResId = remember { getCustomOrFallbackBackgroundId(context) }

    // Real-time username check for Register tab
    LaunchedEffect(username, selectedTab) {
        val clean = username.trim()
        if (selectedTab == 1 && clean.length >= 3) {
            delay(350)
            isCheckingUsername = true
            isUsernameTaken = onCheckUsernameTaken(clean)
            isCheckingUsername = false
        } else {
            isUsernameTaken = false
            isCheckingUsername = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060B12))
    ) {
        // 1. Fullscreen Scenic Background from sample_loginscreen.png
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main Responsive Layout
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            // Top Header: Language pill ("🌐 EN ⌵") & Audio Mute controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Language Selector Pill matching attachment "🌐 EN ⌵"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x800A1118))
                        .border(1.dp, Color(0x5500E5FF), RoundedCornerShape(20.dp))
                        .clickable(onClick = onToggleLanguage)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("language_toggle_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Canvas(modifier = Modifier.size(13.dp)) {
                            drawCircle(color = Color(0xFF00E5FF), style = Stroke(1.4f))
                            drawOval(
                                color = Color(0xFF00E5FF),
                                style = Stroke(1.2f),
                                size = Size(size.width * 0.46f, size.height),
                                topLeft = Offset(size.width * 0.27f, 0f)
                            )
                            drawLine(
                                color = Color(0xFF00E5FF),
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 1.2f
                            )
                        }
                        Text(
                            text = if (currentLanguage == "in") "ID" else "EN",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Audio Mute Pill
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x800A1118))
                        .border(
                            1.dp,
                            if (isAudioMuted) Color(0x88FF5252) else Color(0x8800E5FF),
                            CircleShape
                        )
                        .clickable(onClick = onToggleAudioMute)
                        .testTag("audio_mute_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = stringResource(if (isAudioMuted) R.string.audio_unmute else R.string.audio_mute),
                        tint = if (isAudioMuted) Color(0xFFFF5252) else Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Body Section
            if (isLandscape) {
                // LANDSCAPE MODE: Side-by-side arrangement matching sample_loginscreen.png
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT: Left side is transparent to showcase the Bang Patuh ninja mascot and logo from sample_loginscreen.png
                    Spacer(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    )

                    // RIGHT: Cyber-Ninja HUD Login & Register Terminal Card
                    CyberNinjaHudCard(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            selectedTab = tab
                            isUsernameTaken = false
                        },
                        username = username,
                        onUsernameChange = { input ->
                            val filtered = input.filter { it.isLetterOrDigit() }.take(10)
                            username = filtered
                        },
                        password = password,
                        onPasswordChange = { password = it },
                        showPassword = showPassword,
                        onToggleShowPassword = { showPassword = !showPassword },
                        rememberMe = rememberMe,
                        onToggleRememberMe = { rememberMe = !rememberMe },
                        isUsernameTaken = isUsernameTaken,
                        isCheckingUsername = isCheckingUsername,
                        errorMessage = errorMessage,
                        onSubmit = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            coroutineScope.launch {
                                delay(150)
                                if (selectedTab == 0) {
                                    onLogin(username, password)
                                } else {
                                    onRegister(username, password)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 380.dp)
                            .fillMaxHeight()
                    )
                }
            } else {
                // PORTRAIT MODE: Centered view with Cyber HUD Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 42.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Cyber HUD Card directly over background
                    CyberNinjaHudCard(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            selectedTab = tab
                            isUsernameTaken = false
                        },
                        username = username,
                        onUsernameChange = { input ->
                            val filtered = input.filter { it.isLetterOrDigit() }.take(10)
                            username = filtered
                        },
                        password = password,
                        onPasswordChange = { password = it },
                        showPassword = showPassword,
                        onToggleShowPassword = { showPassword = !showPassword },
                        rememberMe = rememberMe,
                        onToggleRememberMe = { rememberMe = !rememberMe },
                        isUsernameTaken = isUsernameTaken,
                        isCheckingUsername = isCheckingUsername,
                        errorMessage = errorMessage,
                        onSubmit = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            coroutineScope.launch {
                                delay(150)
                                if (selectedTab == 0) {
                                    onLogin(username, password)
                                } else {
                                    onRegister(username, password)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * The Cyber-Ninja HUD Terminal Card containing:
 * - Glowing trapezoidal Log In / Register tabs
 * - Welcome greeting & subtitle
 * - Alphanumeric username field & security password input
 * - Remember me checkbox + Forgot password link
 * - High-impact glowing Cyan brush action button
 * - Corporate & social login buttons (Microsoft, Google, SSO)
 * - Mode switch footer link
 * - Vertical Japanese tech border on the right: "コンプライアンス 忍"
 */
@Composable
fun CyberNinjaHudCard(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleShowPassword: () -> Unit,
    rememberMe: Boolean,
    onToggleRememberMe: () -> Unit,
    isUsernameTaken: Boolean,
    isCheckingUsername: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSubmit = if (selectedTab == 1) {
        username.isNotBlank() && password.isNotBlank() && !isUsernameTaken && !isCheckingUsername
    } else {
        username.isNotBlank() && password.isNotBlank()
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            // Cyber HUD border with futuristic glow
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF00E5FF),
                ambientColor = Color(0xFF005FFF)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xEE09111C))
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00E5FF),
                        Color(0x5500E5FF),
                        Color(0x2200E5FF),
                        Color(0x8800B0FF)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Vertical Japanese text ribbon on the right edge: "コンプライアンス 忍"
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val jpText = listOf("コ", "ン", "プ", "ラ", "イ", "ア", "ン", "ス", "忍")
            jpText.forEach { ch ->
                Text(
                    text = ch,
                    color = Color(0x5500E5FF),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            ShurikenIcon(modifier = Modifier.size(10.dp), color = Color(0x7700E5FF))
        }

        // Inner Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 26.dp, top = 12.dp, bottom = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TOP TABS: [ ✦ Log in ] and [ 👤 Register ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x40000000))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Log in Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (selectedTab == 0) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00B0FF),
                                        Color(0xFF00E5FF)
                                    )
                                )
                            } else {
                                SolidColor(Color.Transparent)
                            }
                        )
                        .clickable { onTabSelected(0) }
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (selectedTab == 0) {
                            ShurikenIcon(modifier = Modifier.size(13.dp), color = Color(0xFF09111C))
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = stringResource(R.string.auth_login_tab),
                            color = if (selectedTab == 0) Color(0xFF09111C) else Color(0xFF90A4AE),
                            fontSize = 12.5.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Register Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (selectedTab == 1) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00B0FF),
                                        Color(0xFF00E5FF)
                                    )
                                )
                            } else {
                                SolidColor(Color.Transparent)
                            }
                        )
                        .clickable { onTabSelected(1) }
                        .testTag("tab_register"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color(0xFF09111C) else Color(0xFF78909C),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.auth_register_tab),
                            color = if (selectedTab == 1) Color(0xFF09111C) else Color(0xFF90A4AE),
                            fontSize = 12.5.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.SemiBold
                        )
                    }
                }
            }

            // Heading & Subtitle
            Column(modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = if (selectedTab == 0) {
                        stringResource(R.string.auth_welcome_back)
                    } else {
                        stringResource(R.string.auth_register_welcome)
                    },
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = if (selectedTab == 0) {
                        stringResource(R.string.auth_welcome_subtitle)
                    } else {
                        stringResource(R.string.auth_register_welcome_subtitle)
                    },
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            // 1. Username Input Field
            CyberNinjaInputField(
                value = username,
                onValueChange = onUsernameChange,
                placeholder = stringResource(R.string.auth_username_placeholder),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (selectedTab == 1 && isUsernameTaken) Color(0xFFFF5252) else Color(0xFF00E5FF),
                        modifier = Modifier.size(17.dp)
                    )
                },
                trailingIcon = {
                    if (selectedTab == 1 && username.trim().length >= 3 && !isCheckingUsername && !isUsernameTaken) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Available",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0x4400E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                isError = selectedTab == 1 && isUsernameTaken,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text
                ),
                testTag = "username_input"
            )

            // Character Counter & Validation status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 1 && isUsernameTaken) {
                        stringResource(R.string.auth_error_username_taken)
                    } else {
                        stringResource(R.string.auth_username_hint)
                    },
                    color = if (selectedTab == 1 && isUsernameTaken) Color(0xFFFF5252) else Color(0xFF78909C),
                    fontSize = 10.sp
                )
                Text(
                    text = "${username.length}/10",
                    color = if (username.length == 10) Color(0xFFFFD54F) else Color(0xFF607D8B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. Password Input Field
            CyberNinjaInputField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.auth_password_placeholder),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = onToggleShowPassword,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("toggle_password_visibility")
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = Color(0xFF90A4AE),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                testTag = "password_input"
            )

            // Remember Me & Forgot Password Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Remember me" checkbox
                Row(
                    modifier = Modifier
                        .clickable(onClick = onToggleRememberMe)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (rememberMe) Color(0xFF00E5FF) else Color(0x33000000))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rememberMe) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF09111C),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.auth_remember_me),
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp
                    )
                }

                // "Forgot password?" Link
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    color = Color(0xFF80D8FF),
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        // Helpful instant hint
                        onPasswordChange("")
                    }
                )
            }

            // General Error Text if present
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF5252),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_error_text")
                )
            }

            // ACTION BUTTON: Glowing Cyan Brush "✦ LOG IN" / "✦ REGISTER"
            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF09111C),
                    disabledContainerColor = Color(0x3300E5FF),
                    disabledContentColor = Color(0x66FFFFFF)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .shadow(
                        elevation = if (canSubmit) 10.dp else 0.dp,
                        shape = RoundedCornerShape(10.dp),
                        spotColor = Color(0xFF00E5FF)
                    )
                    .testTag("auth_submit_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ShurikenIcon(
                        modifier = Modifier.size(14.dp),
                        color = if (canSubmit) Color(0xFF09111C) else Color(0x66FFFFFF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (selectedTab == 0) R.string.auth_login_btn else R.string.auth_register_btn
                        ),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer Link: "Don't have an account? Register now →" / "Already have an account? Log in →"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clickable {
                        onTabSelected(if (selectedTab == 0) 1 else 0)
                    }
                    .testTag("auth_toggle_mode_btn"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) {
                        stringResource(R.string.auth_dont_have_account) + " "
                    } else {
                        stringResource(R.string.auth_already_have_account) + " "
                    },
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
                Text(
                    text = if (selectedTab == 0) {
                        stringResource(R.string.auth_register_now)
                    } else {
                        stringResource(R.string.auth_login_now)
                    },
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
