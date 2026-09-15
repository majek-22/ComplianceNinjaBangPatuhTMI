package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.GlossaryEntry
import com.example.data.GlossarySection
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MintSuccess

private enum class RulesTabFilter {
    ALL,
    VIOLATIONS,
    LEGITIMATE,
    TRAPS,
    BONUS
}

@Composable
fun IconGlossaryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedEntry by remember { mutableStateOf<GlossaryEntry?>(null) }
    var currentFilter by remember { mutableStateOf(RulesTabFilter.ALL) }

    val filteredEntries = remember(currentFilter) {
        when (currentFilter) {
            RulesTabFilter.ALL -> GlossaryEntry.ALL_ENTRIES
            RulesTabFilter.VIOLATIONS -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.VIOLATIONS }
            RulesTabFilter.LEGITIMATE -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.LEGITIMATE }
            RulesTabFilter.TRAPS -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.TRAPS }
            RulesTabFilter.BONUS -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.BONUS }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09131F),
                        Color(0xFF0F1E32),
                        Color(0xFF070D16)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x4464B5F6), CircleShape)
                            .testTag("glossary_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.glossary_title),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = stringResource(R.string.glossary_subtitle),
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp
                        )
                    }
                }

                // Total Badges Counter Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x22FFC857),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${GlossaryEntry.ALL_ENTRIES.size} Items",
                        color = GoldSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick Combat Directives Banner (Aturan Inti)
            CombatDirectivesBanner()

            Spacer(modifier = Modifier.height(12.dp))

            // Game Style Filter Tabs
            RulesFilterTabs(
                currentFilter = currentFilter,
                onFilterSelected = { currentFilter = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Items List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEntries, key = { it.category.name }) { entry ->
                    GlossaryCard(
                        entry = entry,
                        onSelect = { selectedEntry = entry }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Icon Detail Popup Dialog (Rich Tactical Dossier)
        selectedEntry?.let { entry ->
            IconDetailDialog(
                entry = entry,
                onDismiss = { selectedEntry = null }
            )
        }
    }
}

/**
 * High-impact 4-way visual summary of the core gameplay rules
 */
@Composable
private fun CombatDirectivesBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x3364B5F6), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0E1A2C)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DirectiveItem(
                    title = stringResource(R.string.rules_directive_slice_title),
                    subtitle = stringResource(R.string.rules_directive_slice_sub),
                    color = Color(0xFFFFC857),
                    iconEmoji = "⚔️",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                DirectiveItem(
                    title = stringResource(R.string.rules_directive_protect_title),
                    subtitle = stringResource(R.string.rules_directive_protect_sub),
                    color = Color(0xFF64B5F6),
                    iconEmoji = "🛡️",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DirectiveItem(
                    title = stringResource(R.string.rules_directive_trap_title),
                    subtitle = stringResource(R.string.rules_directive_trap_sub),
                    color = Color(0xFFFF5252),
                    iconEmoji = "⚠️",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                DirectiveItem(
                    title = stringResource(R.string.rules_directive_bonus_title),
                    subtitle = stringResource(R.string.rules_directive_bonus_sub),
                    color = Color(0xFF00E676),
                    iconEmoji = "💎",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DirectiveItem(
    title: String,
    subtitle: String,
    color: Color,
    iconEmoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = iconEmoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFB0BEC5),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Filter tabs: All, Violations, Legitimate, Traps, Bonus
 */
@Composable
private fun RulesFilterTabs(
    currentFilter: RulesTabFilter,
    onFilterSelected: (RulesTabFilter) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RulesTabFilter.entries.forEach { filter ->
            val isSelected = currentFilter == filter
            val labelRes = when (filter) {
                RulesTabFilter.ALL -> R.string.rules_tab_all
                RulesTabFilter.VIOLATIONS -> R.string.rules_tab_violations
                RulesTabFilter.LEGITIMATE -> R.string.rules_tab_legitimate
                RulesTabFilter.TRAPS -> R.string.rules_tab_traps
                RulesTabFilter.BONUS -> R.string.rules_tab_bonus
            }
            val count = when (filter) {
                RulesTabFilter.ALL -> GlossaryEntry.ALL_ENTRIES.size
                RulesTabFilter.VIOLATIONS -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.VIOLATIONS }
                RulesTabFilter.LEGITIMATE -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.LEGITIMATE }
                RulesTabFilter.TRAPS -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.TRAPS }
                RulesTabFilter.BONUS -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.BONUS }
            }
            val accentColor = when (filter) {
                RulesTabFilter.ALL -> Color(0xFF90CAF9)
                RulesTabFilter.VIOLATIONS -> Color(0xFFFFC857)
                RulesTabFilter.LEGITIMATE -> Color(0xFF64B5F6)
                RulesTabFilter.TRAPS -> Color(0xFFFF5252)
                RulesTabFilter.BONUS -> Color(0xFF00E676)
            }

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0x22132238),
                label = "tab_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) accentColor else Color(0x22FFFFFF),
                label = "tab_border"
            )

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onFilterSelected(filter) }
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                color = bgColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(labelRes),
                        color = if (isSelected) accentColor else Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) accentColor else Color(0x33FFFFFF)
                    ) {
                        Text(
                            text = "$count",
                            color = if (isSelected) Color(0xFF09131F) else Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Rule Card with un-tinted 3D full-color graphic badge
 */
@Composable
private fun GlossaryCard(
    entry: GlossaryEntry,
    onSelect: () -> Unit
) {
    val (badgeColor, actionLabel, pointText) = when (entry.section) {
        GlossarySection.VIOLATIONS -> Triple(Color(0xFFFFC857), "⚔️ TEBAS", "+10 PTS × Kombo")
        GlossarySection.LEGITIMATE -> Triple(Color(0xFF64B5F6), "🛡️ LINDUNGI", "-1 Nyawa jika tertebas")
        GlossarySection.TRAPS -> Triple(Color(0xFFFF5252), "⚠️ JEBAKAN", "-10 Pts jika tertebas")
        GlossarySection.BONUS -> Triple(Color(0xFF00E676), "💎 BONUS", "+1 Nyawa & +25 Pts")
    }

    val discBgColor = when (entry.section) {
        GlossarySection.VIOLATIONS -> Color(0xFF3E2805)
        GlossarySection.LEGITIMATE -> Color(0xFF0C2C4D)
        GlossarySection.TRAPS -> Color(0xFF3E0E0E)
        GlossarySection.BONUS -> Color(0xFF052F1A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .testTag("glossary_item_${entry.category.name}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0E1B2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Un-tinted 3D Badge container with matching gameplay disc background
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(discBgColor)
                    .border(1.5.dp, badgeColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // High-resolution full-color illustration - NO TINT!
                Image(
                    painter = painterResource(id = entry.category.iconRes),
                    contentDescription = stringResource(entry.category.displayNameRes),
                    modifier = Modifier.size(46.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(entry.category.displayNameRes),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Action Directive Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.20f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = actionLabel,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Points & Consequence
                Text(
                    text = pointText,
                    color = badgeColor.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Real-world explanation
                Text(
                    text = stringResource(entry.explanationRes),
                    color = Color(0xFFCFD8DC),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.rules_tap_details_hint),
                tint = badgeColor.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Rich Tactical Investigation Dossier Dialog
 */
@Composable
private fun IconDetailDialog(
    entry: GlossaryEntry,
    onDismiss: () -> Unit
) {
    val (badgeColor, actionDirective, actionDesc) = when (entry.section) {
        GlossarySection.VIOLATIONS -> Triple(
            Color(0xFFFFC857),
            "⚔️ WAJIB DITEBAS!",
            "Segera tebas sebelum jatuh melewati batas layar. Menebas memberikan poin dan menaikkan kombo."
        )
        GlossarySection.LEGITIMATE -> Triple(
            Color(0xFF64B5F6),
            "🛡️ LINDUNGI! JANGAN DITEBAS!",
            "Biarkan dokumen sah jatuh bebas. Menebas dokumen sah akan merusak kepatuhan dan mengurangi 1 NYAWA!"
        )
        GlossarySection.TRAPS -> Triple(
            Color(0xFFFF5252),
            "⚠️ JEBAKAN! HINDARI DITEBAS!",
            "Ini adalah umpan/hoaks yang belum terbukti. Menebasnya akan dikenai PENALTI -10 POIN dan memutus kombo."
        )
        GlossarySection.BONUS -> Triple(
            Color(0xFF00E676),
            "💎 PERISAI EMAS! SEGERA AMBIL!",
            "Tebas perisai emas langka ini untuk memulihkan +1 NYAWA (maksimal 4) dan memperoleh skor bonus +25 poin!"
        )
    }

    val discBgColor = when (entry.section) {
        GlossarySection.VIOLATIONS -> Color(0xFF3E2805)
        GlossarySection.LEGITIMATE -> Color(0xFF0C2C4D)
        GlossarySection.TRAPS -> Color(0xFF3E0E0E)
        GlossarySection.BONUS -> Color(0xFF052F1A)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, badgeColor, RoundedCornerShape(24.dp))
                .testTag("glossary_detail_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1626)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large 3D Icon with Radiant Disc Background
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(discBgColor)
                        .border(2.dp, badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = entry.category.iconRes),
                        contentDescription = stringResource(entry.category.displayNameRes),
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item Name
                Text(
                    text = stringResource(entry.category.displayNameRes),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor)
                ) {
                    Text(
                        text = actionDirective,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Context Box (Compliance Meaning & In-Game Action)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x33FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ATURAN ARENA GAME:",
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = actionDesc,
                            color = Color(0xFFECEFF1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "KONTEKS TATA KELOLA:",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = stringResource(entry.explanationRes),
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ninja Bang Patuh Pro Tip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x22FFC857), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mascot_owl_transparent),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.rules_ninja_tip_label),
                            color = GoldSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = when (entry.section) {
                                GlossarySection.VIOLATIONS -> "Fokus tebas saat berada di area aman. Jangan biarkan lolos ke bawah!"
                                GlossarySection.LEGITIMATE -> "Perhatikan cap resmi atau warna biru. Jangan panik menggesek layar!"
                                GlossarySection.TRAPS -> "Item jebakan sering muncul berdekatan dengan pelanggaran. Teliti sebelum menebas!"
                                GlossarySection.BONUS -> "Perisai emas sangat berharga. Utamakan menebasnya untuk memperpanjang shift Anda!"
                            },
                            color = Color(0xFFFFF8E1),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("glossary_dialog_dismiss_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = badgeColor,
                        contentColor = Color(0xFF09131F)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.glossary_dialog_got_it),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
