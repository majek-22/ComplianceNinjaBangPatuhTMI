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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.LanguageDropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
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

    val sliceTrail = remember { mutableStateListOf<Offset>() }
    var startButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var hasTriggeredStartShift by remember { mutableStateOf(false) }

    fun checkSliceStartShift(p1: Offset, p2: Offset) {
        if (hasTriggeredStartShift) return
        val length = kotlin.math.hypot(p2.x - p1.x, p2.y - p1.y)
        if (length < 30f) return

        val bounds = startButtonBounds ?: return
        if (lineIntersectsRectMenu(p1, p2, bounds)) {
            hasTriggeredStartShift = true
            coroutineScope.launch {
                delay(100)
                onStartShift()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        sliceTrail.clear()
                        sliceTrail.add(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val currentPos = change.position
                        if (sliceTrail.isNotEmpty()) {
                            val lastPos = sliceTrail.last()
                            checkSliceStartShift(lastPos, currentPos)
                        }
                        sliceTrail.add(currentPos)
                        if (sliceTrail.size > 14) {
                            sliceTrail.removeAt(0)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            delay(100)
                            sliceTrail.clear()
                        }
                    },
                    onDragCancel = {
                        sliceTrail.clear()
                    }
                )
            }
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
                            isLandscape = true,
                            currentLanguage = currentLanguage,
                            onPositioned = { startButtonBounds = it }
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
                            currentLanguage = currentLanguage,
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
                    verticalArrangement = Arrangement.spacedBy(0.dp)
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
                        isLandscape = false,
                        currentLanguage = currentLanguage,
                        onPositioned = { startButtonBounds = it }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Side-by-side rules table
                    ComplianceRulesTable(
                        currentLanguage = currentLanguage,
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

        // 5. Slice Trail Overlay (Visual feedback when dragging / slicing)
        if (sliceTrail.size > 1) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(sliceTrail.first().x, sliceTrail.first().y)
                    for (i in 1 until sliceTrail.size) {
                        lineTo(sliceTrail[i].x, sliceTrail[i].y)
                    }
                }
                // Outer vibrant glowing stroke
                drawPath(
                    path = path,
                    color = CoralPrimary.copy(alpha = 0.85f),
                    style = Stroke(
                        width = 12f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                // Inner pure-white energy blade core
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 4.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // 6. Popup Mini Screen: Compliance AI Chatbot
        if (showChatPopup) {
            ComplianceChatbotPopup(
                onDismiss = { showChatPopup = false }
            )
        }

        // 7. Compliance Comic Viewer Dialog (5 Pages, Horizontal Draggable, 80% screen in landscape)
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
    currentLanguage: String,
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
        // 1. MENGHAPUS JARAK BAWAAN KOLOM
        verticalArrangement = Arrangement.spacedBy(0.dp) 
    ) {
        val howToPlayText = when (currentLanguage.lowercase()) {
            "ja" -> "遊び方" 
            "in", "id" -> "CARA BERMAIN" 
            else -> "HOW TO PLAY" 
        }
        val sliceMissionBannerText = when (currentLanguage.lowercase()) {
            "ja" -> stringResource(R.string.menu_slice_mission_banner)
            "in", "id" -> stringResource(R.string.menu_slice_mission_banner)
            else -> stringResource(R.string.menu_slice_mission_banner)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_katana_crossed),
                contentDescription = sliceMissionBannerText,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 6.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "$howToPlayText • $sliceMissionBannerText".uppercase(),
                color = Color(0xFFFFD54F), 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp, 
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xCC000000), 
                        offset = Offset(2f, 4f),
                        blurRadius = 6f
                    )
                )
            )
        }
        // Horizontally Draggable 4-Category Carousel
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
        ) {
            // Scroll 1: SLICE — Violations
            item {
                NinjaMissionScrollCard(
                    headerTitle = stringResource(R.string.menu_slice_violations_header).replace("—", "-"),
                    headerIconRes = R.drawable.ic_crossed_katanas_gold,
                    headerPlaqueColors = listOf(Color(0xFF7D0827), Color(0xFF560219), Color(0xFF7D0827)),
                    bgImageRes = R.drawable.bg_ninja_scroll,
                    items = violationEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(205.dp)
                )
            }

            // Scroll 2: PROTECT — Legitimate
            item {
                NinjaMissionScrollCard(
                    headerTitle = stringResource(R.string.menu_protect_legitimate_header).replace("—", "-"),
                    headerIconRes = R.drawable.ic_laurel_shield,
                    headerPlaqueColors = listOf(Color(0xFF0F2E52), Color(0xFF07192E), Color(0xFF0F2E52)),
                    bgImageRes = R.drawable.bg_ninja_scroll_blue,
                    items = legitimateEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(205.dp)
                )
            }

            // Scroll 3: AVOID — Traps
            item {
                NinjaMissionScrollCard(
                    headerTitle = stringResource(R.string.menu_avoid_legitimate_header).replace("—", "-"),
                    headerIconRes = R.drawable.ic_scroll_hazard,
                    headerPlaqueColors = listOf(Color(0xFF6B180A), Color(0xFF400A03), Color(0xFF6B180A)),
                    bgImageRes = R.drawable.bg_ninja_scroll_crimson,
                    items = trapEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(205.dp)
                )
            }

            // Scroll 4: COLLECT — Bonus
            item {
                NinjaMissionScrollCard(
                    headerTitle = stringResource(R.string.menu_collect_bonus_header).replace("—", "-"),
                    headerIconRes = R.drawable.ic_scroll_bonus_gem,
                    headerPlaqueColors = listOf(Color(0xFF0B3D23), Color(0xFF042011), Color(0xFF0B3D23)),
                    bgImageRes = R.drawable.bg_ninja_scroll_green,
                    items = bonusEntries.map {
                        RuleItemData(
                            iconRes = it.iconRes,
                            name = stringResource(it.nameRes)
                        )
                    },
                    modifier = Modifier.width(205.dp)
                )
            }
        }

        // Drag Indicator Bar & 4 Dot Indicators for all 4 Mission Scrolls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 2. TARIKAN EKSTREM KE ATAS (-30.dp)
                .offset(y = (-25).dp) 
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

            val activeScrollPage = listState.firstVisibleItemIndex.coerceIn(0, 3)

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (pageIndex in 0..2) {
                    val isActive = activeScrollPage == pageIndex
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 7.5.dp else 5.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFFFFD54F) else Color(0x44FFFFFF))
                    )
                }
            }
        }
    }
}

private data class RuleItemData(val iconRes: Int, val name: String)

/**
 * Authentic Japanese Makimono Wooden Roller Rod (Jikugi):
 * Cylindrical dark mahogany wood rod, gold flanged end caps,
 * center silk braided cord wrap (top roller), and hanging red silk tassels (Fusahimo).
 */
@Composable
private fun NinjaScrollRoller(
    isTop: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Main cylindrical wooden roller rod spanning horizontally
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5B1208),
                            Color(0xFF8E210F),
                            Color(0xFFAA2A14),
                            Color(0xFF6B1408),
                            Color(0xFF380702)
                        )
                    )
                )
                .border(
                    width = 0.8.dp,
                    color = Color(0x77FFA726),
                    shape = RoundedCornerShape(4.dp)
                )
        )

        // Left gold end cap (Jikugi)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(width = 13.dp, height = 15.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF176),
                            Color(0xFFFFD54F),
                            Color(0xFFFFB300),
                            Color(0xFFE65100)
                        )
                    )
                )
                .border(0.6.dp, Color(0xFFFFF9C4), RoundedCornerShape(3.dp))
        )

        // Right gold end cap (Jikugi)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(width = 13.dp, height = 15.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF176),
                            Color(0xFFFFD54F),
                            Color(0xFFFFB300),
                            Color(0xFFE65100)
                        )
                    )
                )
                .border(0.6.dp, Color(0xFFFFF9C4), RoundedCornerShape(3.dp))
        )

        // Left Hanging Red Silk Tassel (Fusahimo)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 2.dp, y = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gold bead ring
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F))
            )
            // Braided red silk cord
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(4.dp)
                    .background(Color(0xFFC62828))
            )
            // Tassel gold neck band
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFFFFB300))
            )
            // Flared red silk tassel skirt
            Box(
                modifier = Modifier
                    .size(width = 7.dp, height = 9.dp)
                    .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFD32F2F), Color(0xFFB71C1C), Color(0xFF7F0000))
                        )
                    )
            )
        }

        // Right Hanging Red Silk Tassel (Fusahimo)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gold bead ring
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F))
            )
            // Braided red silk cord
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(4.dp)
                    .background(Color(0xFFC62828))
            )
            // Tassel gold neck band
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFFFFB300))
            )
            // Flared red silk tassel skirt
            Box(
                modifier = Modifier
                    .size(width = 7.dp, height = 9.dp)
                    .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFD32F2F), Color(0xFFB71C1C), Color(0xFF7F0000))
                        )
                    )
            )
        }

        // Center Red Silk Cord Wrapping with Gold Binding Rings on Top Roller
        if (isTop) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 22.dp, height = 13.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFC62828)),
                contentAlignment = Alignment.Center
            ) {
                // Triple golden binding cord wraps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFFFD54F))
                    )
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFFFD54F))
                    )
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFFFD54F))
                    )
                }
            }
        }
    }
}

/**
 * Authentic Ninja Mission Scroll Card (Makimono):
 * Replicates the authentic Japanese scroll style:
 * - Cylindrical mahogany wooden rollers with gold end knobs & hanging red silk tassels
 * - Golden antique parchment paper (Honshi)
 * - Crimson & gold brocade mounting ribbons on left/right vertical borders
 * - Ornate royal lacquer plaque with golden border, corner rivets, and icon
 * - Large authentic 3D PNG icons on soft parchment strips with crisp sumi ink typography
 */
@Composable
private fun NinjaMissionScrollCard(
    headerTitle: String,
    headerIconRes: Int,
    headerPlaqueColors: List<Color>,
    items: List<RuleItemData>,
    modifier: Modifier = Modifier,
    bgImageRes: Int? = null
) {
    if (bgImageRes != null) {
        // Render scroll using uploaded bg_ninja_scroll with dynamic title, icons, and names overlaid on parchment
        BoxWithConstraints(
            modifier = modifier,
            contentAlignment = Alignment.TopCenter
        ) {
            val cardWidth = maxWidth
            val cardHeight = maxWidth * (1402f / 1122f)

            // Scaled font and icon sizing based on card width (referenced to standard 240dp)
            val scaleFactor = (cardWidth.value / 240f).coerceIn(0.7f, 1.8f)
            val titleFontSize = (10.5f * scaleFactor).sp
            val titleIconSize = (14f * scaleFactor).dp
            val itemFontSize = (9.5f * scaleFactor).sp
            val itemIconSize = (22f * scaleFactor).dp

            Box(
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
            ) {
                // Authentic Makimono background illustration
                Image(
                    painter = painterResource(id = bgImageRes),
                    contentDescription = headerTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                // 1. Lacquer Plaque Header Title: positioned precisely inside the plaque (13.0% to 21.5% of total height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = cardHeight * 0.130f,
                            start = cardWidth * 0.165f,
                            end = cardWidth * 0.165f
                        )
                        .height(cardHeight * 0.085f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = headerIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(titleIconSize),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width((4.5f * scaleFactor).dp))

                        Text(
                            text = headerTitle,
                            color = Color.White,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.4.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 2. Parchment Items Column: positioned strictly within parchment body (24.0% to 85.0% of total height)
                // Arranged from the top with consistent compact row height and item spacing across all scrolls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = cardHeight * 0.245f,
                            start = cardWidth * 0.155f,
                            end = cardWidth * 0.155f
                        )
                        .height(cardHeight * 0.605f),
                    verticalArrangement = Arrangement.spacedBy((5f * scaleFactor).dp, Alignment.Top)
                ) {
                    for (item in items) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape((5f * scaleFactor).dp))
                                .background(Color(0x35FFF8EA))
                                .border((0.6f * scaleFactor).dp, Color(0x38B08953), RoundedCornerShape((5f * scaleFactor).dp))
                                .padding(
                                    horizontal = (6f * scaleFactor).dp,
                                    vertical = (2f * scaleFactor).dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Violation / Item Icon
                            Image(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.name,
                                modifier = Modifier.size(itemIconSize),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.width((6.5f * scaleFactor).dp))

                            // Item Name in Sumi Ink
                            Text(
                                text = item.name,
                                color = Color(0xFF1F160E),
                                fontSize = itemFontSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Top Wooden Scroll Roller
        NinjaScrollRoller(isTop = true)

        // Unrolled Parchment Body (Honshi)
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .offset(y = (-13).dp),
            shape = RoundedCornerShape(2.dp),
            color = Color(0xFFFFF8EA),
            border = BorderStroke(1.dp, Color(0x66B08953)),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF8EB),
                                Color(0xFFFEEDD6),
                                Color(0xFFF9DEC0),
                                Color(0xFFF3CFA0),
                                Color(0xFFE9BF84)
                            )
                        )
                    )
            ) {
                // Red & Gold Brocade Mounting Edge Ribbons on Left and Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .matchParentSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Brocade Ribbon + Gold Trim
                    Row(modifier = Modifier.fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6F0913), Color(0xFF8E111C), Color(0xFFA81C2A))
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(Color(0xFFFFD54F))
                        )
                    }

                    // Right Gold Trim + Brocade Ribbon
                    Row(modifier = Modifier.fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(Color(0xFFFFD54F))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFA81C2A), Color(0xFF8E111C), Color(0xFF6F0913))
                                    )
                                )
                        )
                    }
                }

                // Inner Scroll Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Ornate Header Plaque (Royal Lacquer Plaque with Golden Rim & Corner Rivets)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.verticalGradient(headerPlaqueColors))
                            .border(
                                BorderStroke(
                                    2.dp,
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFE082), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFE082))
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        // 4 Golden Corner Rivets / Studs
                        // Top-Start
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE082))
                                .border(0.5.dp, Color(0xFF5D4037), CircleShape)
                        )
                        // Top-End
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE082))
                                .border(0.5.dp, Color(0xFF5D4037), CircleShape)
                        )
                        // Bottom-Start
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE082))
                                .border(0.5.dp, Color(0xFF5D4037), CircleShape)
                        )
                        // Bottom-End
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE082))
                                .border(0.5.dp, Color(0xFF5D4037), CircleShape)
                        )

                        // Header Plaque Content: Icon + Title
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = headerIconRes),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = headerTitle,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Target entries listed on parchment (exact replica of user's image)
                    for (item in items) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x80FFF8EA))
                                .border(0.8.dp, Color(0x66DEBA8C), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Large 3D Icon displayed directly on parchment
                            Image(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.name,
                                modifier = Modifier.size(38.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Mission Target Name in Sumi Ink
                            Text(
                                text = item.name,
                                color = Color(0xFF1F160E),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Bottom Wooden Scroll Roller
        NinjaScrollRoller(
            isTop = false,
            modifier = Modifier.offset(y = (-13).dp)
        )
        }
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
    currentLanguage: String = "en",
    onPositioned: ((Rect) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Kita HANYA mengatur lebarnya saja, tinggi akan mengikuti rasio gambar asli
    val buttonWidth = if (isLandscape) 234.dp else 260.dp

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

    val buttonDrawable = when (currentLanguage.lowercase()) {
        "ja" -> R.drawable.btn_start_shift_jp
        "in", "id" -> R.drawable.btn_start_shift_id
        else -> R.drawable.btn_start_shift_eg
    }

    val startShiftLabel = when (currentLanguage.lowercase()) {
        "ja" -> "シフト開始"
        "in", "id" -> "MULAI SHIFT"
        else -> "START SHIFT"
    }

    // Tampilkan gambar SECARA LANGSUNG tanpa komponen Box pembungkus!
    Image(
        painter = painterResource(id = buttonDrawable),
        contentDescription = startShiftLabel,
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                onPositioned?.invoke(coordinates.boundsInRoot())
            }
            .scale(pulseScale * interactiveScale)
            .width(buttonWidth)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("start_shift_button"),
        contentScale = ContentScale.Fit // Memastikan lekukan gambar asli tidak rusak
    )
}

private fun lineIntersectsRectMenu(p1: Offset, p2: Offset, rect: Rect): Boolean {
    if (rect.contains(p1) || rect.contains(p2)) return true
    val minX = minOf(p1.x, p2.x)
    val maxX = maxOf(p1.x, p2.x)
    val minY = minOf(p1.y, p2.y)
    val maxY = maxOf(p1.y, p2.y)

    if (maxX < rect.left || minX > rect.right || maxY < rect.top || minY > rect.bottom) {
        return false
    }

    return lineIntersectsLineMenu(p1, p2, Offset(rect.left, rect.top), Offset(rect.right, rect.top)) ||
            lineIntersectsLineMenu(p1, p2, Offset(rect.left, rect.bottom), Offset(rect.right, rect.bottom)) ||
            lineIntersectsLineMenu(p1, p2, Offset(rect.left, rect.top), Offset(rect.left, rect.bottom)) ||
            lineIntersectsLineMenu(p1, p2, Offset(rect.right, rect.top), Offset(rect.right, rect.bottom))
}

private fun lineIntersectsLineMenu(a1: Offset, a2: Offset, b1: Offset, b2: Offset): Boolean {
    val d = (a2.x - a1.x) * (b2.y - b1.y) - (a2.y - a1.y) * (b2.x - b1.x)
    if (d == 0f) return false
    val u = ((b1.x - a1.x) * (b2.y - b1.y) - (b1.y - a1.y) * (b2.x - b1.x)) / d
    val v = ((b1.x - a1.x) * (a2.y - a1.y) - (b1.y - a1.y) * (a2.x - a1.x)) / d
    return (u in 0f..1f) && (v in 0f..1f)
}



