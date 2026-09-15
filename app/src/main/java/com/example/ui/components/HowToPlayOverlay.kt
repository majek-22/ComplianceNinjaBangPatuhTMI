package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Brief, non-intrusive 'How to Play' tactical directive overlay.
 * Appears once for new users when they first enter the game arena.
 */
@Composable
fun HowToPlayOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(280)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(280, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.94f,
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        val pulseTransition = rememberInfiniteTransition(label = "pulse_play_btn")
        val btnScale by pulseTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "btn_scale"
        )

        // Backdrop: semi-transparent dark tint allowing the game arena to remain softly visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD9040C16))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume clicks */ }
                )
                .testTag("how_to_play_overlay"),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val isCompactHeight = maxHeight < 420.dp
                val cardShape = RoundedCornerShape(22.dp)

                Surface(
                    shape = cardShape,
                    color = Color(0xF20B1728),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x9900E5FF),
                                Color(0x66FFD700),
                                Color(0x9900E5FF)
                            )
                        )
                    ),
                    shadowElevation = 18.dp,
                    modifier = Modifier
                        .widthIn(max = 780.dp)
                        .shadow(24.dp, cardShape, spotColor = Color(0x5500E5FF))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                horizontal = if (isCompactHeight) 18.dp else 24.dp,
                                vertical = if (isCompactHeight) 12.dp else 18.dp
                            )
                            .then(
                                if (isCompactHeight) Modifier.verticalScroll(rememberScrollState())
                                else Modifier
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFF07121E),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = stringResource(R.string.how_to_play_title),
                                        color = Color.White,
                                        fontSize = if (isCompactHeight) 17.sp else 19.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = stringResource(R.string.how_to_play_subtitle),
                                        color = Color(0xFF80DEEA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Quick dismiss (X) button in top-right
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                                    .testTag("how_to_play_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 14.dp))

                        // 2. Tactical Rule Directives (4 concise cards)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RuleCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.how_to_play_card1_title),
                                desc = stringResource(R.string.how_to_play_card1_desc),
                                icon = Icons.Default.Close,
                                accentColor = Color(0xFFFF5252),
                                bgTint = Color(0x1AFF5252),
                                isCompact = isCompactHeight
                            )
                            RuleCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.how_to_play_card2_title),
                                desc = stringResource(R.string.how_to_play_card2_desc),
                                icon = Icons.Default.CheckCircle,
                                accentColor = Color(0xFF00E676),
                                bgTint = Color(0x1A00E676),
                                isCompact = isCompactHeight
                            )
                            RuleCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.how_to_play_card3_title),
                                desc = stringResource(R.string.how_to_play_card3_desc),
                                icon = Icons.Default.Warning,
                                accentColor = Color(0xFFFFAB00),
                                bgTint = Color(0x1AFFAB00),
                                isCompact = isCompactHeight
                            )
                            RuleCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.how_to_play_card4_title),
                                desc = stringResource(R.string.how_to_play_card4_desc),
                                icon = Icons.Default.Shield,
                                accentColor = Color(0xFFFFD700),
                                bgTint = Color(0x1AFFD700),
                                isCompact = isCompactHeight
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 10.dp else 16.dp))

                        // 3. Confirm / Start Shift Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .scale(btnScale)
                                .height(if (isCompactHeight) 42.dp else 46.dp)
                                .widthIn(min = 220.dp, max = 300.dp)
                                .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFFF5252))
                                .testTag("how_to_play_dismiss_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFF3366), Color(0xFFFF9100))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.how_to_play_start_button),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    bgTint: Color,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgTint,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = if (isCompact) 8.dp else 12.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 28.dp else 34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                color = accentColor,
                fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                color = Color(0xDDFFFFFF),
                fontSize = if (isCompact) 9.5.sp else 10.5.sp,
                lineHeight = if (isCompact) 12.sp else 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 3
            )
        }
    }
}
