package com.example.ui.screens

import android.app.Activity
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.HowToPlayOverlay
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BadgeShape
import com.example.data.ComplianceCategory
import com.example.data.FloatingPopup
import com.example.data.GameItem
import com.example.data.SliceTrailPoint
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundGradientEnd
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CardLegitimateEnd
import com.example.ui.theme.CardLegitimateStart
import com.example.ui.theme.CardViolationEnd
import com.example.ui.theme.CardViolationStart
import com.example.ui.theme.ComboBadgeStyle
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanEnergy
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassWhite05
import com.example.ui.theme.GlassWhite10
import com.example.ui.theme.GlassWhite20
import com.example.ui.theme.GlassWhite40
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.HeaderBorderBottom
import com.example.ui.theme.HeaderGlassBg
import com.example.ui.theme.HudLabelStyle
import com.example.ui.theme.HudScoreDigitsStyle
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.SheetContainerBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TimerDigitsStyle
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.GameViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onOpenGlossary: () -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconPainters = rememberCategoryPainters()
    val sliceTrail = remember { mutableStateListOf<SliceTrailPoint>() }
    // Lingering slices created during freeze bonus mode (remain visible until freeze ends)
    val freezeSlices = remember { mutableStateListOf<FreezeSlash>() }

    // Clear lingering freeze slices automatically when freeze bonus mode expires
    LaunchedEffect(uiState.isFreezeActive) {
        if (!uiState.isFreezeActive) {
            freezeSlices.clear()
        }
    }

    val context = LocalContext.current
    val window = (context as? Activity)?.window

    // Immersive full-screen mode during active gameplay so notifications/bars don't interfere with slicing
    DisposableEffect(window) {
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            }
        } else {
            onDispose { }
        }
    }

    val popupPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }

    LaunchedEffect(uiState.isPaused) {
        if (!uiState.isPaused) {
            var lastFrameNanos = withFrameNanos { it }
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    val dt = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f
                    lastFrameNanos = frameTimeNanos
                    viewModel.updateFrame(dt)

                    val now = System.nanoTime()
                    val threshold = 180_000_000L
                    sliceTrail.removeAll { (now - it.timestampNanos) > threshold }
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shake_anim")
    val shakeFactor by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val shakeOffsetX = (shakeFactor * uiState.screenShakeIntensity * 0.8f).toInt()
    val shakeOffsetY = (shakeFactor * uiState.screenShakeIntensity * 0.5f).toInt()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(shakeOffsetX, shakeOffsetY) }
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(widthPx, heightPx) {
            viewModel.setScreenDimensions(widthPx, heightPx)
        }

        // 1. FULLSCREEN GAMEPLAY BACKGROUND IMAGE (DYNAMIC PER MISSION)
        Image(
            painter = painterResource(id = uiState.selectedLevel.backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // FREEZE BONUS DARK ATMOSPHERE OVERLAY ("screen otomatis terlihat sedikit ada efek gelap")
        val freezeDarkAlpha by animateFloatAsState(
            targetValue = if (uiState.isFreezeActive) 0.58f else 0f,
            animationSpec = tween(350),
            label = "freezeDarkAlpha"
        )
        if (freezeDarkAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF020712).copy(alpha = freezeDarkAlpha * 0.70f),
                                Color(0xFF000308).copy(alpha = freezeDarkAlpha)
                            )
                        )
                    )
            )
        }

        // Safe-Zone Debug Overlay (Feature 3)
        if (uiState.debugOverlayEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val left = viewModel.engine.safeZoneLeft
                val right = viewModel.engine.safeZoneRight
                val top = viewModel.engine.safeZoneTop
                val bottom = viewModel.engine.safeZoneBottom

                drawRect(
                    color = Color(0x1800E5FF),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top)
                )
                drawRect(
                    color = Color(0x8800E5FF),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = 2.5f)
                )
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(0f, top),
                    end = Offset(size.width, top),
                    strokeWidth = 2f
                )
            }
        }

        // 2. ACTIVE FLYING ITEMS: Rendered as discrete Compose composables driven by SnapshotStateList
        for (item in viewModel.engine.activeItems) {
            key(item.id) {
                FlyingItemComposable(
                    item = item,
                    painter = iconPainters[item.category]
                )
            }
        }

        // 3. FOREGROUND INTERACTIVE CANVAS (Slice Trail, Particles, Popups, Pointer Input)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("playfield_canvas")
                .pointerInput(uiState.isPaused) {
                    if (!uiState.isPaused) {
                        var lastX = 0f
                        var lastY = 0f
                        detectDragGestures(
                            onDragStart = { offset ->
                                lastX = offset.x
                                lastY = offset.y
                                val now = System.nanoTime()
                                sliceTrail.clear()
                                sliceTrail.add(SliceTrailPoint(lastX, lastY, now))
                                viewModel.onSliceStart()
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val curX = change.position.x
                                val curY = change.position.y
                                val now = System.nanoTime()

                                sliceTrail.add(SliceTrailPoint(curX, curY, now))
                                viewModel.onSliceSegment(lastX, lastY, curX, curY)

                                // When freeze bonus mode is active, record slice cuts so they stay visible until freeze ends
                                if (uiState.isFreezeActive) {
                                    val dx = curX - lastX
                                    val dy = curY - lastY
                                    if (dx * dx + dy * dy > 4f) {
                                        freezeSlices.add(
                                            FreezeSlash(
                                                x1 = lastX,
                                                y1 = lastY,
                                                x2 = curX,
                                                y2 = curY,
                                                auraColor = if (freezeSlices.size % 2 == 0) Color(0xFF00E5FF) else Color(0xFFFF9800),
                                                midColor = Color(0xFFFFD700)
                                            )
                                        )
                                    }
                                }

                                lastX = curX
                                lastY = curY
                            },
                            onDragEnd = {
                                sliceTrail.clear()
                                viewModel.onSliceEnd()
                            },
                            onDragCancel = {
                                sliceTrail.clear()
                                viewModel.onSliceEnd()
                            }
                        )
                    }
                }
        ) {
            // Draw particle bursts
            val particles = viewModel.engine.particles.toList()
            for (p in particles) {
                drawCircle(
                    color = Color(p.color).copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }

            // Draw lingering freeze bonus slices that stay visible until freeze time finishes
            if (freezeSlices.isNotEmpty()) {
                drawFreezeSlices(freezeSlices)
            }

            // Draw active slice trail (enlarged effect if freeze bonus is active)
            drawSliceTrail(sliceTrail, isFreezeActive = uiState.isFreezeActive)

            // Draw floating popups
            val popups = viewModel.engine.popups.toList()
            for (popup in popups) {
                drawFloatingPopup(popup, popupPaint)
            }
        }

        // 2. TOP HUD BAR (Time and Volume to the left of lives, no pause button, clean flat background)
        TopHudBar(
            levelNumber = uiState.selectedLevel.levelNumber,
            score = uiState.score,
            lives = uiState.lives,
            baseLives = uiState.baseLives,
            shieldBonusLives = uiState.shieldBonusLives,
            comboMultiplier = uiState.comboMultiplier,
            elapsedSeconds = uiState.timeRemaining,
            isAudioMuted = uiState.isAudioMuted,
            onToggleAudioMute = { viewModel.toggleAudioMute() },
            isPaused = uiState.isPaused,
            onTogglePause = { if (uiState.isPaused) viewModel.resumeGame() else viewModel.pauseGame() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Pause overlay dialog
        if (uiState.isPaused && !uiState.shouldShowHowToPlay) {
            AlertDialog(
                onDismissRequest = { viewModel.resumeGame() },
                title = {
                    Text(
                        text = stringResource(R.string.dialog_paused_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = stringResource(R.string.menu_subtitle))
                        OutlinedButton(
                            onClick = { viewModel.showHowToPlay() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.how_to_play_title))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resumeGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Text(stringResource(R.string.dialog_resume))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onReturnToMenu
                    ) {
                        Text(stringResource(R.string.dialog_quit_to_menu))
                    }
                }
            )
        }

        // 3. FREEZE BONUS ACTIVE HUD
        AnimatedVisibility(
            visible = uiState.isFreezeActive,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 70.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xF2081C2E),
                border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isIndonesian = uiState.currentLanguage == "in" || uiState.currentLanguage == "id"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isIndonesian) "❄️ BONUS BEKU! ❄️" else "❄️ FREEZE BONUS! ❄️",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1fs", uiState.freezeTimeRemaining),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    val corruptorBonusText = if (isIndonesian) {
                        "Tebas Corruptor Bebas! ${uiState.freezeBonusHits} Tebasan (+${uiState.freezeBonusHits * 10} Poin)"
                    } else {
                        "Slash Corruptor Freely! ${uiState.freezeBonusHits} Slashes (+${uiState.freezeBonusHits * 10} Points)"
                    }
                    Text(
                        text = corruptorBonusText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0C2C4D))
                    ) {
                        val progress = (uiState.freezeTimeRemaining / 5.0f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFFFFD700))
                                    )
                                )
                        )
                    }
                }
            }
        }

        // 4. FROST & DARK VIGNETTE BORDER WHEN FREEZE BONUS IS ACTIVE
        if (uiState.isFreezeActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x66000511)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            3.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFFFFD700), Color(0xFF00E5FF))
                            )
                        )
                    )
            )
        }

        // 5. SLICE ALERT / FEEDBACK BANNER
        AnimatedVisibility(
            visible = uiState.feedbackMessage != null && !uiState.isFreezeActive,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 76.dp, start = 20.dp, end = 20.dp)
        ) {
            uiState.feedbackMessage?.let { msg ->
                val bannerBorder = if (uiState.feedbackIsPositive) MintSuccess else CrimsonDanger
                val bannerBg = if (uiState.feedbackIsPositive) Color(0xEE143627) else Color(0xEE3F1218)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bannerBg,
                    border = BorderStroke(1.5.dp, bannerBorder),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.feedbackIsPositive) Icons.Default.Favorite else Icons.Default.Warning,
                            contentDescription = null,
                            tint = bannerBorder,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                }
            }
        }

        // 4. FULLSCREEN IMPACT FLASH OVERLAY
        uiState.flashOverlayColor?.let { flashColor ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(flashColor))
            )
        }

        // 5. READY COUNTDOWN OVERLAY (3 Seconds Orange Banner)
        if (uiState.readyCountdown > 0f) {
            val countdownInt = (uiState.readyCountdown.toInt() + 1).coerceIn(1, 3)
            val readyTransition = rememberInfiniteTransition(label = "ready_pulse")
            val readyScale by readyTransition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "ready_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.scale(readyScale)
                ) {
                    Text(
                        text = "Ready...",
                        color = Color(0xFFFF9800), // Orange
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xCC000000),
                                offset = Offset(4f, 4f),
                                blurRadius = 14f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FF9800))
                            .border(2.dp, Color(0xFFFF9800), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$countdownInt",
                            color = Color(0xFFFFB74D),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 6. GAME OVER BANNER (Red)
        if (uiState.isGameOverBannerShowing) {
            val goTransition = rememberInfiniteTransition(label = "game_over_pulse")
            val goScale by goTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "go_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.scale(goScale)
                ) {
                    Text(
                        text = "GAME OVER",
                        color = Color(0xFFFF2A2A), // Bold Red
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF000000),
                                offset = Offset(6f, 6f),
                                blurRadius = 18f
                            )
                        )
                    )
                }
            }
        }

        // 7. HOW TO PLAY INSTRUCTION OVERLAY
        HowToPlayOverlay(
            visible = uiState.shouldShowHowToPlay,
            onDismiss = { viewModel.dismissHowToPlay() },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// =========================================================================
// CANVAS RENDERING HELPERS
// =========================================================================

@Composable
private fun FlyingItemComposable(
    item: GameItem,
    painter: Painter?,
    modifier: Modifier = Modifier
) {
    if (painter == null) return

    val categoryColor = when {
        item.category.isFreezeBonus -> Color(0xFF00E676)
        item.category.isBonus -> Color(0xFF00E676)
        item.category.isTrap -> Color(0xFFFF5252)
        item.isViolation -> Color(0xFFFFC857)
        else -> Color(0xFF64B5F6)
    }

    val discBgColor = when {
        item.category.isFreezeBonus -> Color(0xFF052F1A)
        item.category.isBonus -> Color(0xFF052F1A)
        item.category.isTrap -> Color(0xFF3E0E0E)
        item.isViolation -> Color(0xFF3E2805)
        else -> Color(0xFF0C2C4D)
    }

    if (!item.sliced) {
        val density = LocalDensity.current
        val diameterDp = with(density) { (item.radius * 2f).toDp() }

        Box(
            modifier = modifier
                .offset {
                    IntOffset(
                        (item.x - item.radius).toInt(),
                        (item.y - item.radius).toInt()
                    )
                }
                .size(diameterDp)
                .graphicsLayer {
                    alpha = item.alpha
                }
        ) {
            // Rotating vector token & neon aura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = item.rotation
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.width / 2f

                    // 1. Radiant outer neon aura matching RULES category color
                    val auraColors = if (item.category.isFreezeBonus) {
                        listOf(
                            Color(0xFF00E676).copy(alpha = 0.85f),
                            Color(0xFFFFD700).copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            categoryColor.copy(alpha = 0.75f),
                            categoryColor.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    }

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = auraColors,
                            radius = r * 1.55f,
                            center = Offset(r, r)
                        ),
                        radius = r * 1.55f,
                        center = Offset(r, r)
                    )

                    // 2. Vibrant colored background disc matching RULES (not pitch black!)
                    drawCircle(
                        color = discBgColor,
                        radius = r,
                        center = Offset(r, r)
                    )

                    // Inner soft highlight disc
                    drawCircle(
                        color = categoryColor.copy(alpha = 0.25f),
                        radius = r * 0.88f,
                        center = Offset(r, r)
                    )

                    // 3. Draw original vector icon in full colors centered inside the category disc
                    val iconDrawSize = if (item.category.isFreezeBonus) r * 1.76f else r * 1.52f
                    val iconPadding = (r * 2f - iconDrawSize) / 2f
                    clipPath(
                        path = Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(r - r * 0.90f, r - r * 0.90f, r + r * 0.90f, r + r * 0.90f))
                        }
                    ) {
                        withTransform({
                            translate(left = iconPadding, top = iconPadding)
                        }) {
                            with(painter) {
                                draw(size = Size(iconDrawSize, iconDrawSize))
                            }
                        }
                    }

                    // 4. Accessible, crisp rim border ring matching RULES
                    drawCircle(
                        color = categoryColor,
                        radius = r * 0.95f,
                        center = Offset(r, r),
                        style = Stroke(width = if (item.category.isFreezeBonus) 5f else 3.5f)
                    )
                }
            }

            // Top hit counter badge for freeze bonus corruptor
            if (item.category.isFreezeBonus && item.bonusHits > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-14).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C2C4D))
                        .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ ${item.bonusHits}x (+${item.bonusHits * 10})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700)
                    )
                }
            }

            // High-contrast floating pill label below token matching the Gameplay Rules title
            val iconName = androidx.compose.ui.res.stringResource(item.category.displayNameRes)

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 10.dp)
                    .wrapContentWidth(unbounded = true),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xF2060E1A),
                border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.85f)),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                ) {
                    // Category glow indicator pip
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Text(
                        text = iconName,
                        fontSize = 7.5.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.2.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    } else {
        // Sliced halves rendered on playfield
        Canvas(modifier = modifier.fillMaxSize()) {
            drawSlicedHalves(item, painter)
        }
    }
}

private fun DrawScope.drawSlicedHalves(
    item: GameItem,
    painter: Painter
) {
    val rad = item.sliceAngle * (PI.toFloat() / 180f)
    val dirX = cos(rad)
    val dirY = sin(rad)
    val normX = -sin(rad)
    val normY = cos(rad)

    val half1Center = Offset(
        item.x + item.half1OffsetX,
        item.y + item.half1OffsetY
    )
    drawSlicedHalf(
        center = half1Center,
        rotation = item.rotation + item.halfRotation1,
        radius = item.radius,
        alpha = item.alpha,
        painter = painter,
        category = item.category,
        isFirstHalf = true,
        sliceAngle = item.sliceAngle,
        dirX = dirX,
        dirY = dirY,
        normX = normX,
        normY = normY
    )

    val half2Center = Offset(
        item.x + item.half2OffsetX,
        item.y + item.half2OffsetY
    )
    drawSlicedHalf(
        center = half2Center,
        rotation = item.rotation + item.halfRotation2,
        radius = item.radius,
        alpha = item.alpha,
        painter = painter,
        category = item.category,
        isFirstHalf = false,
        sliceAngle = item.sliceAngle,
        dirX = dirX,
        dirY = dirY,
        normX = normX,
        normY = normY
    )
}

private fun DrawScope.drawSlicedHalf(
    center: Offset,
    rotation: Float,
    radius: Float,
    alpha: Float,
    painter: Painter,
    category: ComplianceCategory,
    isFirstHalf: Boolean,
    sliceAngle: Float,
    dirX: Float,
    dirY: Float,
    normX: Float,
    normY: Float
) {
    if (alpha <= 0f) return

    val clipExtent = radius * 3.5f
    val clipPath = Path().apply {
        if (isFirstHalf) {
            moveTo(center.x - dirX * clipExtent, center.y - dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent, center.y + dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent + normX * clipExtent, center.y + dirY * clipExtent + normY * clipExtent)
            lineTo(center.x - dirX * clipExtent + normX * clipExtent, center.y - dirY * clipExtent + normY * clipExtent)
            close()
        } else {
            moveTo(center.x - dirX * clipExtent, center.y - dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent, center.y + dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent - normX * clipExtent, center.y + dirY * clipExtent - normY * clipExtent)
            lineTo(center.x - dirX * clipExtent - normX * clipExtent, center.y - dirY * clipExtent - normY * clipExtent)
            close()
        }
    }

    val categoryColor = when {
        category.isFreezeBonus -> Color(0xFF00E676)
        category.isBonus -> Color(0xFF00E676)
        category.isTrap -> Color(0xFFFF5252)
        category.isViolation -> Color(0xFFFFC857)
        else -> Color(0xFF64B5F6)
    }

    val discBgColor = when {
        category.isFreezeBonus -> Color(0xFF052F1A)
        category.isBonus -> Color(0xFF052F1A)
        category.isTrap -> Color(0xFF3E0E0E)
        category.isViolation -> Color(0xFF3E2805)
        else -> Color(0xFF0C2C4D)
    }

    clipPath(clipPath) {
        withTransform({
            translate(left = center.x, top = center.y)
            rotate(degrees = rotation)
        }) {
            val r = radius

            // Colored backing disc matching RULES (not pitch black!)
            drawCircle(
                color = discBgColor.copy(alpha = alpha),
                radius = r
            )

            // Inner soft highlight disc
            drawCircle(
                color = categoryColor.copy(alpha = alpha * 0.25f),
                radius = r * 0.88f
            )

            // Draw vector icon in full original color
            val iconDrawSize = r * 1.52f
            val iconPadding = (r * 2f - iconDrawSize) / 2f
            withTransform({
                translate(left = -r + iconPadding, top = -r + iconPadding)
            }) {
                with(painter) {
                    draw(
                        size = Size(iconDrawSize, iconDrawSize),
                        alpha = alpha
                    )
                }
            }

            // High-contrast rim border ring
            drawCircle(
                color = categoryColor.copy(alpha = alpha),
                radius = r * 0.95f,
                style = Stroke(width = 3.5f)
            )
        }
    }

    // Bright laser cut glow along the slice seam
    val cutLength = radius * 1.05f
    drawLine(
        brush = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = alpha * 0.95f),
                Color(category.glowColor).copy(alpha = alpha * 0.75f)
            )
        ),
        start = Offset(center.x - dirX * cutLength, center.y - dirY * cutLength),
        end = Offset(center.x + dirX * cutLength, center.y + dirY * cutLength),
        strokeWidth = 3.5f
    )
}

private fun DrawScope.drawSliceTrail(
    points: List<SliceTrailPoint>,
    isFreezeActive: Boolean = false
) {
    if (points.size < 2) return

    val now = System.nanoTime()
    val maxAgeNanos = if (isFreezeActive) 350_000_000L else 220_000_000L

    // In freeze mode, slice effect is larger with glowing aura
    val outerWidth = if (isFreezeActive) 36f else 22f
    val midWidth = if (isFreezeActive) 18f else 10f
    val coreWidth = if (isFreezeActive) 8f else 4.5f

    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]

        val age = (now - p1.timestampNanos).coerceAtLeast(0L)
        val progress = 1.0f - (age.toFloat() / maxAgeNanos).coerceIn(0f, 1f)
        if (progress <= 0f) continue

        val auraColor = if (isFreezeActive) Color(0xFF00E5FF) else Color(0xFFFF3B30)
        val midColor = if (isFreezeActive) Color(0xFFFFD54F) else Color(0xFFFF9800)

        // 1. Fiery red / electric cyan outer bloom aura
        drawLine(
            color = auraColor.copy(alpha = (if (isFreezeActive) 0.65f else 0.45f) * progress),
            start = Offset(p1.x, p1.y),
            end = Offset(p2.x, p2.y),
            strokeWidth = outerWidth * progress,
            cap = StrokeCap.Round
        )

        // 2. Vibrant orange-gold mid streak
        drawLine(
            color = midColor.copy(alpha = 0.90f * progress),
            start = Offset(p1.x, p1.y),
            end = Offset(p2.x, p2.y),
            strokeWidth = midWidth * progress,
            cap = StrokeCap.Round
        )

        // 3. Razor-sharp blazing white core
        drawLine(
            color = Color.White.copy(alpha = 1.0f * progress),
            start = Offset(p1.x, p1.y),
            end = Offset(p2.x, p2.y),
            strokeWidth = coreWidth * progress,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Data class representing persistent slice cuts etched during the freeze bonus corruptor round.
 */
private data class FreezeSlash(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val auraColor: Color = Color(0xFF00E5FF),
    val midColor: Color = Color(0xFFFFD700)
)

/**
 * Renders lingering freeze bonus slice cuts across the screen that stay visible until the freeze mode timer finishes.
 */
private fun DrawScope.drawFreezeSlices(slices: List<FreezeSlash>) {
    for (slice in slices) {
        // 1. Large electric cyan / fiery neon outer aura
        drawLine(
            color = slice.auraColor.copy(alpha = 0.60f),
            start = Offset(slice.x1, slice.y1),
            end = Offset(slice.x2, slice.y2),
            strokeWidth = 36f,
            cap = StrokeCap.Round
        )

        // 2. Vibrant golden-orange mid laser slash
        drawLine(
            color = slice.midColor.copy(alpha = 0.88f),
            start = Offset(slice.x1, slice.y1),
            end = Offset(slice.x2, slice.y2),
            strokeWidth = 18f,
            cap = StrokeCap.Round
        )

        // 3. Piercing blazing white blade core
        drawLine(
            color = Color.White,
            start = Offset(slice.x1, slice.y1),
            end = Offset(slice.x2, slice.y2),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawFloatingPopup(popup: FloatingPopup, paint: Paint) {
    if (popup.alpha <= 0f) return

    paint.color = Color(popup.color).copy(alpha = popup.alpha).hashCode()
    paint.textSize = 28f * popup.scale

    drawContext.canvas.nativeCanvas.drawText(
        popup.text,
        popup.x,
        popup.y,
        paint
    )
}

// =========================================================================
// TOP HUD COMPOSABLE
// =========================================================================

@Composable
private fun TopHudBar(
    levelNumber: Int,
    score: Int,
    lives: Int,
    baseLives: Int,
    shieldBonusLives: Int,
    comboMultiplier: Int,
    elapsedSeconds: Float,
    isAudioMuted: Boolean,
    onToggleAudioMute: () -> Unit,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    // High-contrast floating HUD bar matching Screen 2
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Close (X) button & Mute toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Exit / Close button (X) inside dark glass circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(1.2.dp, Color(0x55FFFFFF), CircleShape)
                        .clickable(onClick = onTogglePause)
                        .testTag("game_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Exit / Pause",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Volume Mute Toggle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(
                            1.2.dp,
                            if (isAudioMuted) Color(0x88FF5252) else Color(0x884FCB8F),
                            CircleShape
                        )
                        .clickable(onClick = onToggleAudioMute)
                        .testTag("hud_mute_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAudioMuted) androidx.compose.material.icons.Icons.Default.VolumeOff else androidx.compose.material.icons.Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = if (isAudioMuted) Color(0xFFFF5252) else MintSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Center: Big Score, 3 Glowing Hearts & Combo Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Score
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x55000000),
                    border = BorderStroke(1.dp, Color(0x33FFD54F))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%,d", score),
                            style = TextStyle(
                                color = GoldSecondary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                shadow = Shadow(
                                    color = Color(0xCC000000),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 6f
                                )
                            ),
                            modifier = Modifier.testTag("game_score_display")
                        )
                    }
                }

                // Hearts display: 3 base red hearts + 1 extra blue shield heart (max 4)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("game_lives_container")
                ) {
                    for (i in 1..3) {
                        val isHeartActive = i <= baseLives
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isHeartActive) Color(0x33FF3B30) else Color(0x22000000)
                                )
                                .border(
                                    1.2.dp,
                                    if (isHeartActive) Color(0xFFFF3B30) else Color(0x44FFFFFF),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isHeartActive) androidx.compose.material.icons.Icons.Default.Favorite else androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                                contentDescription = "Life $i",
                                tint = if (isHeartActive) Color(0xFFFF3B30) else Color(0x44FFFFFF),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // 4th Bonus Shield Heart (Blue): granted when player collects "Compliance Shield" while having 3 full red lives (max 4)
                    AnimatedVisibility(
                        visible = shieldBonusLives > 0,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        val blueHeartColor = Color(0xFF00E5FF)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0x3300E5FF))
                                .border(
                                    1.2.dp,
                                    blueHeartColor,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                                contentDescription = "Compliance Shield Bonus Life",
                                tint = blueHeartColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Combo Badge (Golden pill x3)
                if (comboMultiplier > 1) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x66000000),
                        border = BorderStroke(1.2.dp, GoldSecondary)
                    ) {
                        Text(
                            text = "x$comboMultiplier",
                            style = TextStyle(
                                color = GoldSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("game_combo_badge")
                        )
                    }
                }
            }

            // Right: Digital Red Timer Capsule & Pause button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Digital Red Timer
                val secondsInt = elapsedSeconds.toInt().coerceAtLeast(0)
                val minutes = secondsInt / 60
                val secs = secondsInt % 60
                val timeString = String.format("%02d:%02d", minutes, secs)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x66180508),
                    border = BorderStroke(1.2.dp, Color(0x88FF3B30))
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = timeString,
                            style = TextStyle(
                                color = Color(0xFFFF3B30),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                shadow = Shadow(
                                    color = Color(0xFFFF3B30),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 8f
                                )
                            ),
                            modifier = Modifier.testTag("game_timer_display")
                        )
                    }
                }

                // Pause toggle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(1.2.dp, Color(0x8864B5F6), CircleShape)
                        .clickable(onClick = onTogglePause)
                        .testTag("hud_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaused) androidx.compose.material.icons.Icons.Default.PlayArrow else androidx.compose.material.icons.Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color(0xFF90CAF9),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// CATEGORY PAINTERS CACHE
// =========================================================================

@Composable
private fun rememberCategoryPainters(): Map<ComplianceCategory, Painter> {
    val painters = mutableMapOf<ComplianceCategory, Painter>()
    for (category in ComplianceCategory.entries) {
        painters[category] = painterResource(id = category.iconRes)
    }
    return painters
}
