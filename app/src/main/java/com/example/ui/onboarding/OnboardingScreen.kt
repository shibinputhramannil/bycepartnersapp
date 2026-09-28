package com.example.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LiquidGlassButton
import com.example.ui.theme.ByceGreen
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DarkNavyDepth
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val imageRes: Int,
    val headline: String,
    val body: String,
    val primaryButtonText: String,
    val pageIndex: Int
)

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    val pages = remember {
        listOf(
            OnboardingPageData(
                imageRes = R.drawable.img_gym_interior,
                headline = "Bring your gym into the network.",
                body = "Connect your gym to Byce and welcome eligible members.",
                primaryButtonText = "Next",
                pageIndex = 0
            ),
            OnboardingPageData(
                imageRes = R.drawable.img_gym_manager,
                headline = "Run your gym from your phone.",
                body = "Manage check-ins, members and daily operations in one place.",
                primaryButtonText = "Next",
                pageIndex = 1
            ),
            OnboardingPageData(
                imageRes = R.drawable.img_gym_reception,
                headline = "Check in members in seconds.",
                body = "Verify access quickly and keep your gym moving.",
                primaryButtonText = "Get started",
                pageIndex = 2
            )
        )
    }

    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkNavy,
                        DarkNavyDepth,
                        DarkNavy
                    )
                )
            )
            .testTag("onboarding_screen_container")
    ) {
        // Soft atmospheric ambient depth lighting behind photography and glass
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x18222021),
                            Color(0x082DD4BF),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding + 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --------------------------------------------------
            // HORIZONTAL PAGER WITH FULL-BLEED TOP & SIDE PHOTOGRAPHY
            // --------------------------------------------------
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("onboarding_horizontal_pager")
            ) { pageIndex ->
                val page = pages[pageIndex]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Photo container touching top, left, and right edges of the phone
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    ) {
                        // Realistic Gym Photograph
                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = page.headline,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Dark gradient vignette for photographic contrast & smooth bottom transition
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x30000000),
                                            Color.Transparent,
                                            Color(0xC0222021),
                                            DarkNavy
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Typography: Headline + Body (Sentence case only, clean & spacious)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = page.headline,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            lineHeight = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = page.body,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // --------------------------------------------------
            // BOTTOM: PROGRESS DOTS + LIQUID GLASS CTA
            // --------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Dots Indicator: ● ○ ○
                Row(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .testTag("progress_dots_indicator"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width = if (isSelected) 24.dp else 8.dp
                        val color = if (isSelected) ByceGreen else Color(0x38FFFFFF)

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                // Primary CTA Button (Translucent Liquid Glass)
                val currentButtonText = if (pagerState.currentPage == 2) "Get started" else "Next"
                LiquidGlassButton(
                    text = currentButtonText,
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onGetStartedClick()
                        }
                    },
                    testTag = "primary_next_button"
                )
            }
        }
    }
}
