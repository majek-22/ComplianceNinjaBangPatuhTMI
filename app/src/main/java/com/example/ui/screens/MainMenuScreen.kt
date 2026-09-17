package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.LanguageDropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlossaryEntry
import com.example.data.GlossarySection
import com.example.R
import com.example.data.AvatarHelper
import com.example.ui.components.ComplianceChatbotPopup
import com.example.ui.components.ComplianceComicDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.TextPrimary

@Composable
fun MainMenuScreen(
    currentUser: String?,
    userAvatarId: Int = 1,
    highScore: Int,
    currentLanguage: String,
    isAudioMuted: Boolean,
    shouldShowComic: Boolean = false,
    onComicDismissed: () -> Unit = {},
    onStartShift: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenGlossary: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onToggleLanguage: () -> Unit,
    onSelectLanguage: ((String) -> Unit)? = null,
    onToggleAudioMute: () -> Unit,
    onLogout: () -> Unit,
    onPauseMusic: () -> Unit = {},
    onResumeMusic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_button"
    )

    var showChatPopup by remember { mutableStateOf(false) }
    var showComicDialog by remember(shouldShowComic) { mutableStateOf(shouldShowComic) }
    LaunchedEffect(shouldShowComic) {
        if (shouldShowComic) {
            showComicDialog = true
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val chatIconRotation = remember { Animatable(0f) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isLandscape = maxWidth > maxHeight

        // 1. Scenic Main Menu Background Art
        Image(
            painter = painterResource(id = R.drawable.bg_main_menu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = if (isLandscape) 18.dp else 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Officer profile, Rankings, Rules, Language, Mute, Logout
            MainMenuTopBar(
                currentUser = currentUser,
                userAvatarId = userAvatarId,
                currentLanguage = currentLanguage,
                isAudioMuted = isAudioMuted,
                onOpenProfile = onOpenProfile,
                onOpenComic = { showComicDialog = true },
                onOpenLeaderboard = onOpenLeaderboard,
                onOpenGlossary = onOpenGlossary,
                onToggleLanguage = onToggleLanguage,
                onSelectLanguage = onSelectLanguage,
                onToggleAudioMute = onToggleAudioMute,
                onLogout = onLogout
            )

            // Main Body: Responsive Landscape vs Portrait
            if (isLandscape) {
                // Landscape split: Left = Title Logo, Floating Badges, Start Shift & Feature Highlights
                // Right = Side-by-side "SLICE - Violations" vs "AVOID - Legitimate" Cards
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: High Score & Start Shift Pill positioned below the background title
                    Column(
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        // High Score Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0x55000000), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x33FFD54F), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.menu_high_score_label).uppercase(),
                                color = Color(0xFFB0BEC5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%,d", highScore),
                                color = GoldSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Big Glowing Red "Start Shift" Pill Button
                        StartShiftGlowingButton(
                            onClick = onStartShift,
                            pulseScale = pulseScale,
                            isLandscape = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Right Column: Side-by-side SLICE vs AVOID tables
                    Box(
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        ComplianceRulesTable(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Portrait Layout (vertical scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clearance so the artwork title "Bang Patuh Compliance Ninja" in background image is fully visible
                    Spacer(modifier = Modifier.height(160.dp))

                    // High Score Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0x55000000), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x33FFD54F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.menu_high_score_label).uppercase(),
                            color = Color(0xFFB0BEC5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%,d", highScore),
                            color = GoldSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Big Glowing Red "Start Shift" Pill Button
                    StartShiftGlowingButton(
                        onClick = onStartShift,
                        pulseScale = pulseScale,
                        isLandscape = false
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Side-by-side rules table
                    ComplianceRulesTable(
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 4. Unified Floating AI Assistant Pill Button
        val unifiedPillShape = RoundedCornerShape(26.dp)
        val chatInteractionSource = remember { MutableInteractionSource() }
        Surface(
            shape = unifiedPillShape,
            color = Color(0xEE07121E),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF00A3E0),
                        GoldSecondary
                    )
                )
            ),
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 14.dp, bottom = 10.dp)
                .menuScaleAnimation(chatInteractionSource, hoverScale = 1.05f, pressScale = 1.10f)
                .shadow(10.dp, unifiedPillShape, spotColor = Color(0x6600E5FF))
                .clip(unifiedPillShape)
                .clickable(
                    interactionSource = chatInteractionSource,
                    indication = null
                ) {
                    coroutineScope.launch {
                        launch {
                            chatIconRotation.animateTo(
                                targetValue = chatIconRotation.value + 360f,
                                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                            )
                        }
                        delay(220)
                        showChatPopup = true
                    }
                }
                .testTag("menu_ask_me_button")
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 5.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Online indicator dot + "AI Assistant" text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    val aiAssistantLabel = when (currentLanguage.lowercase()) {
                        "ja" -> "AIアシスタント"
                        "in", "id" -> "Asisten AI"
                        else -> stringResource(R.string.menu_ai_assistant)
                    }
                    Text(
                        text = aiAssistantLabel,
                        color = Color(0xFFE0F7FA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }

                // Chatbot circular icon inside the unified pill
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5FF))
                        .border(1.dp, Color(0x9900E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_chatbot),
                        contentDescription = stringResource(R.string.menu_ask_me),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = chatIconRotation.value
                            },
                        contentScale = ContentScale.Crop
                    )

                    // Mini online dot on chatbot icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                            .border(1.dp, Color(0xFF07121E), CircleShape)
                    )
                }
            }
        }

        // 5. Popup Mini Screen: Compliance AI Chatbot
        if (showChatPopup) {
            ComplianceChatbotPopup(
                onDismiss = { showChatPopup = false }
            )
        }

        // 6. Compliance Comic Viewer Dialog (5 Pages, Horizontal Draggable, 80% screen in landscape)
        if (showComicDialog) {
            ComplianceComicDialog(
                onDismiss = {
                    showComicDialog = false
                    onComicDismissed()
                }
            )
        }
    }
}

// =========================================================================
// TOP BAR COMPONENT & BUTTON ANIMATIONS
// =========================================================================

/**
 * Subtle interactive scale-up animation on hover or tap/press for menu buttons.
 */
@Composable
private fun Modifier.menuScaleAnimation(
    interactionSource: MutableInteractionSource,
    hoverScale: Float = 1.08f,
    pressScale: Float = 1.15f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetScale = when {
        isPressed -> pressScale
        isHovered -> hoverScale
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "menu_btn_scale"
    )

    return this.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}

@Composable
private fun MainMenuTopBar(
    currentUser: String?,
    userAvatarId: Int,
    currentLanguage: String,
    isAudioMuted: Boolean,
    onOpenProfile: () -> Unit,
    onOpenComic: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenGlossary: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSelectLanguage: ((String) -> Unit)? = null,
    onToggleAudioMute: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Officer Profile Button with Username (Top-Left)
        val profileInteraction = remember { MutableInteractionSource() }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xDD091522),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0x8800E5FF), Color(0x3300E5FF))
                )
            ),
            shadowElevation = 6.dp,
            modifier = Modifier.menuScaleAnimation(profileInteraction, hoverScale = 1.04f, pressScale = 1.08f)
        ) {
            Row(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = profileInteraction,
                        indication = null,
                        onClick = onOpenProfile
                    )
                    .padding(start = 5.dp, end = 12.dp)
                    .testTag("menu_profile_btn"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val avatarRes = AvatarHelper.getAvatarRes(userAvatarId, currentUser ?: "")
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = stringResource(R.string.profile_title),
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.2.dp, Color(0xFF00E5FF), CircleShape)
                )
                Text(
                    text = currentUser?.ifBlank { "Player" } ?: "Player",
                    color = Color(0xFFE0F7FA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2. High-Tech Action HUD Modules (Right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // MODULE 1: Game Features Dock (Story Comic, Leaderboard, Rules)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xDD091522),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0x5500E5FF), Color(0x3364B5F6), Color(0x44FFD54F))
                    )
                ),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val comicInteraction = remember { MutableInteractionSource() }
                    // Story / Comic Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .menuScaleAnimation(comicInteraction, hoverScale = 1.12f, pressScale = 1.20f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = comicInteraction,
                                indication = null,
                                onClick = onOpenComic
                            )
                            .testTag("menu_comic_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = stringResource(R.string.menu_comic_button),
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Tactical Vertical Hairline Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color(0x26FFFFFF))
                    )

                    val leaderboardInteraction = remember { MutableInteractionSource() }
                    // Leaderboard Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .menuScaleAnimation(leaderboardInteraction, hoverScale = 1.12f, pressScale = 1.20f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = leaderboardInteraction,
                                indication = null,
                                onClick = onOpenLeaderboard
                            )
                            .testTag("menu_leaderboard_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = stringResource(R.string.menu_leaderboard),
                            tint = GoldSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Tactical Vertical Hairline Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color(0x26FFFFFF))
                    )

                    val glossaryInteraction = remember { MutableInteractionSource() }
                    // Rules / Glossary Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .menuScaleAnimation(glossaryInteraction, hoverScale = 1.12f, pressScale = 1.20f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = glossaryInteraction,
                                indication = null,
                                onClick = onOpenGlossary
                            )
                            .testTag("menu_glossary_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = stringResource(R.string.menu_glossary),
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // MODULE 2: System Settings & Logout Dock (Language, Audio, Exit)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xDD091522),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0x33FFFFFF), Color(0x44FF5252))
                    )
                ),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val langInteraction = remember { MutableInteractionSource() }
                    var showLanguageMenu by remember { mutableStateOf(false) }
                    // Language Switcher
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .menuScaleAnimation(langInteraction, hoverScale = 1.10f, pressScale = 1.18f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = langInteraction,
                                indication = null,
                                onClick = {
                                    showLanguageMenu = true
                                    onToggleLanguage()
                                }
                            )
                            .padding(horizontal = 7.dp)
                            .testTag("menu_lang_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(
                            targetState = currentLanguage,
                            animationSpec = tween(250),
                            label = "language_toggle_crossfade"
                        ) { lang ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "🌐",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = when (lang.lowercase()) {
                                        "ja" -> "JA"
                                        "in", "id" -> "ID"
                                        else -> "EN"
                                    },
                                    color = GoldSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        LanguageDropdownMenu(
                            expanded = showLanguageMenu,
                            currentLanguage = currentLanguage,
                            onDismissRequest = { showLanguageMenu = false },
                            onLanguageSelected = { selectedLang ->
                                showLanguageMenu = false
                                if (onSelectLanguage != null) {
                                    onSelectLanguage(selectedLang)
                                }
                            }
                        )
                    }

                    // Tactical Vertical Hairline Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color(0x26FFFFFF))
                    )

                    val audioInteraction = remember { MutableInteractionSource() }
                    // Audio Mute Toggle Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .menuScaleAnimation(audioInteraction, hoverScale = 1.12f, pressScale = 1.20f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = audioInteraction,
                                indication = null,
                                onClick = onToggleAudioMute
                            )
                            .testTag("menu_audio_mute_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Audio Mute",
                            tint = if (isAudioMuted) Color(0xFFFF5252) else Color(0xFF00E676),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Tactical Vertical Hairline Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color(0x26FFFFFF))
                    )

                    val logoutInteraction = remember { MutableInteractionSource() }
                    // Logout / Exit Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .menuScaleAnimation(logoutInteraction, hoverScale = 1.12f, pressScale = 1.20f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = logoutInteraction,
                                indication = null,
                                onClick = onLogout
                            )
                            .testTag("menu_logout_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.auth_logout),
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// =========================================================================
// 4-CATEGORY DRAGGABLE DIRECTIVES COMPARISON TABLE (SLICE, AVOID, PROTECT, COLLECT)
// =========================================================================

@Composable
private fun ComplianceRulesTable(
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val violationEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.VIOLATIONS } }
    val trapEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.TRAPS } }
    val legitimateEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.LEGITIMATE } }
    val bonusEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.BONUS } }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Horizontally Draggable 4-Category Carousel
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // Scroll 1: SLICE — Violations (Tebas Pelanggaran)
            item {
                NinjaMissionScrollCard(
                    headerTitle = "⚔️ " + stringResource(R.string.menu_slice_violations_header),
                    headerColor = Color(0xFFFFC857),
                    containerBorder = Color(0x66FFC857),
                    discBgColor = Color(0xFF3E2805),
                    items = violationEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(188.dp)
                )
            }

            // Scroll 2: PROTECT — Legitimate (Lindungi Dokumen Sah)
            item {
                NinjaMissionScrollCard(
                    headerTitle = "🛡️ " + stringResource(R.string.menu_protect_legitimate_header),
                    headerColor = Color(0xFF64B5F6),
                    containerBorder = Color(0x6664B5F6),
                    discBgColor = Color(0xFF0C2C4D),
                    items = legitimateEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(188.dp)
                )
            }

            // Scroll 3: AVOID — Traps (Hindari Jebakan)
            item {
                NinjaMissionScrollCard(
                    headerTitle = "⚠️ " + stringResource(R.string.menu_avoid_legitimate_header),
                    headerColor = Color(0xFFFF5252),
                    containerBorder = Color(0x66FF5252),
                    discBgColor = Color(0xFF3E0E0E),
                    items = trapEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(188.dp)
                )
            }

            // Scroll 4: COLLECT — Bonus (Kumpulkan Bonus)
            item {
                NinjaMissionScrollCard(
                    headerTitle = "💎 " + stringResource(R.string.menu_collect_bonus_header),
                    headerColor = Color(0xFF00E676),
                    containerBorder = Color(0x6600E676),
                    discBgColor = Color(0xFF052F1A),
                    items = bonusEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(188.dp)
                )
            }
        }

        // Drag Indicator Bar & Strictly 2 Dot Indicators (..) for single drag navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.menu_drag_rules_hint),
                color = Color(0xFF90CAF9),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Strictly 2 dots (..) for single left/right drag
            val isSecondPage = listState.firstVisibleItemIndex >= 1

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (!isSecondPage) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (!isSecondPage) Color(0xFFFFD54F) else Color(0x44FFFFFF))
                )
                Box(
                    modifier = Modifier
                        .size(if (isSecondPage) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (isSecondPage) Color(0xFFFFD54F) else Color(0x44FFFFFF))
                )
            }
        }
    }
}

private data class RuleItemData(val iconRes: Int, val name: String)

/**
 * Top/Bottom wooden roller bar with polished mahogany wood grain and golden brass end knobs (Jikugi)
 */
@Composable
private fun NinjaScrollRoller(
    isTop: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(13.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main wooden rod spanning horizontally
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(9.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6D4C41),
                            Color(0xFF8D6E63),
                            Color(0xFF4E342E),
                            Color(0xFF27150E)
                        )
                    )
                )
                .border(
                    width = 0.5.dp,
                    color = Color(0x88FFA726),
                    shape = RoundedCornerShape(3.dp)
                )
        )

        // Left gold knob (Jikugi)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 9.dp, height = 13.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFE082),
                            Color(0xFFFFB300),
                            Color(0xFFE65100),
                            Color(0xFFBF360C)
                        )
                    )
                )
                .border(0.5.dp, Color(0xFFFFF9C4), RoundedCornerShape(3.dp))
        )

        // Right gold knob (Jikugi)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 9.dp, height = 13.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFE082),
                            Color(0xFFFFB300),
                            Color(0xFFE65100),
                            Color(0xFFBF360C)
                        )
                    )
                )
                .border(0.5.dp, Color(0xFFFFF9C4), RoundedCornerShape(3.dp))
        )

        // Center red silk cord knot on top roller
        if (isTop) {
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 7.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFD50000))
                    .border(0.5.dp, Color(0xFFFFD54F), RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * Authentic Ninja Mission Scroll Card (Makimono):
 * Features top and bottom wooden scroll roller rods with gold end caps,
 * aged rice parchment texture, brocade border ribbons, and traditional mission directives.
 */
@Composable
private fun NinjaMissionScrollCard(
    headerTitle: String,
    headerColor: Color,
    containerBorder: Color,
    discBgColor: Color,
    items: List<RuleItemData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Wooden Scroll Roller
        NinjaScrollRoller(isTop = true, accentColor = headerColor)

        // Unrolled Parchment Body (Honshi)
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f),
            shape = RoundedCornerShape(2.dp),
            color = Color(0xFF1A110B),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        headerColor.copy(alpha = 0.65f),
                        Color(0xFF5D4037),
                        headerColor.copy(alpha = 0.65f)
                    )
                )
            ),
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF261912),
                                Color(0xFF1D120B),
                                Color(0xFF150C07)
                            )
                        )
                    )
            ) {
                // Traditional Japanese brocade fabric borders along edges
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(180.dp)
                            .background(headerColor.copy(alpha = 0.40f))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(180.dp)
                            .background(headerColor.copy(alpha = 0.40f))
                    )
                }

                // Inner parchment mission content
                Column(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mission Placard Header (Scroll Seal Banner)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        headerColor.copy(alpha = 0.28f),
                                        Color(0x33000000),
                                        headerColor.copy(alpha = 0.28f)
                                    )
                                )
                            )
                            .border(1.dp, headerColor.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                            .padding(vertical = 3.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = headerTitle,
                            color = headerColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Target entries listed on mission parchment
                    for (item in items) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x44000000))
                                .border(0.5.dp, Color(0x338D6E63), RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            // Target medallion token disc
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(discBgColor)
                                    .border(1.dp, headerColor.copy(alpha = 0.65f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.name,
                                    modifier = Modifier.size(20.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Mission Target Name
                            Text(
                                text = item.name,
                                color = Color(0xFFF0EBE5),
                                fontSize = 9.8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Bottom Wooden Scroll Roller
        NinjaScrollRoller(isTop = false, accentColor = headerColor)
    }
}

// =========================================================================
// GLOWING "START SHIFT" PILL BUTTON
// =========================================================================

@Composable
private fun StartShiftGlowingButton(
    onClick: () -> Unit,
    pulseScale: Float,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(26.dp)
    val gradientBrush = Brush.horizontalGradient(
        listOf(
            Color(0xFFFF5252),
            Color(0xFFE53935),
            Color(0xFFD32F2F)
        )
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetInteractiveScale = when {
        isPressed -> 1.07f
        isHovered -> 1.04f
        else -> 1.0f
    }

    val interactiveScale by animateFloatAsState(
        targetValue = targetInteractiveScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "start_button_interactive_scale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(pulseScale * interactiveScale)
            .width(if (isLandscape) 220.dp else 260.dp)
            .height(50.dp)
            .shadow(16.dp, buttonShape, spotColor = Color(0xFFFF5252))
            .pointerInput(onClick) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDrag = { _, _ -> }
                )
            }
            .testTag("start_shift_button"),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush, buttonShape)
                .border(1.5.dp, Color(0xFFFFCDD2).copy(alpha = 0.85f), buttonShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Samurai Katana Sword slice icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_katana_slice),
                    contentDescription = "Katana Slice",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.menu_start_shift),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x88000000),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}



