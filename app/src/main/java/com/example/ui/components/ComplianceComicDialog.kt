package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import kotlinx.coroutines.launch

/**
 * The 5 comic pages uploaded by the user:
 * comic_page_1.png through comic_page_5.png in res/drawable/
 */
val COMIC_PAGES = listOf(
    R.drawable.comic_page_1,
    R.drawable.comic_page_2,
    R.drawable.comic_page_3,
    R.drawable.comic_page_4,
    R.drawable.comic_page_5
)

/**
 * Transparent Comic Viewer:
 * - Tidak dibuat kotak, transparan dengan background game terlihat di belakangnya
 * - Posisi center dan agak ke atas (ideal landscape balance)
 * - Di sudut kanan atas ada tombol 'x' kecil yang rapi
 * - Di bawah hanya ada pages 5 berupa .....
 * - Bisa di-drag / swipe ke kiri dan kanan
 */
@Composable
fun ComplianceComicDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { COMIC_PAGES.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Transparent scrim allowing game background to be visible
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0x88000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            val isLandscape = maxWidth > maxHeight
            // Around 80% of screen in landscape, balanced and positioned slightly higher up
            val maxAvailableWidth = if (isLandscape) maxWidth * 0.92f else maxWidth * 0.96f
            val maxAvailableHeight = if (isLandscape) (maxHeight - 24.dp) * 0.90f else (maxHeight - 40.dp) * 0.75f

            val aspect = 16f / 9f
            val comicWidth: Dp
            val comicHeight: Dp
            if (maxAvailableWidth / aspect <= maxAvailableHeight) {
                comicWidth = maxAvailableWidth
                comicHeight = maxAvailableWidth / aspect
            } else {
                comicHeight = maxAvailableHeight
                comicWidth = maxAvailableHeight * aspect
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = BiasAlignment(
                    horizontalBias = 0f,
                    verticalBias = if (isLandscape) -0.25f else -0.10f
                )
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = false
                        ) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Exact 16:9 Comic Image Container with small Top-Right 'X' Close Button
                    Box(
                        modifier = Modifier
                            .size(comicWidth, comicHeight)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        // Comic Horizontal Pager (draggable left and right)
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("comic_horizontal_pager")
                        ) { pageIndex ->
                            Image(
                                painter = painterResource(id = COMIC_PAGES[pageIndex]),
                                contentDescription = "Comic Page ${pageIndex + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("comic_page_image_$pageIndex"),
                                contentScale = ContentScale.FillBounds
                            )
                        }

                        // Compact 'X' button in top-right corner of the comic
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xB3000000))
                                .border(1.dp, Color(0x66FFFFFF), CircleShape)
                                .clickable(onClick = onDismiss)
                                .testTag("comic_close_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.comic_close),
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Di bawah hanya ada pages 5 berupa .....
                    Row(
                        modifier = Modifier
                            .testTag("comic_page_dots"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(COMIC_PAGES.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF00E5FF)
                                        else Color.White.copy(alpha = 0.35f)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
