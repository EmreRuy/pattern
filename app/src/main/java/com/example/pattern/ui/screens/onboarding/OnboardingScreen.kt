package com.example.pattern.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.R
import com.example.pattern.ui.components.HabitProgressCard
import com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen.HabitDetailsUi
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage.Identity,
        OnboardingPage.Mastery,
        OnboardingPage.Discipline
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        // Subtle ambient gradient for premium feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 2000f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                // Snap settings for "snappier" feeling
                pageSpacing = 16.dp
            ) { position ->
                OnboardingPagerItem(page = pages[position], isVisible = pagerState.currentPage == position)
            }

            // Bottom Navigation
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Modern elongated indicators
                Row(
                    Modifier.height(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        
                        val width by animateDpAsState(
                            targetValue = if (pagerState.currentPage == iteration) 40.dp else 12.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "indicatorWidth"
                        )

                        Box(
                            modifier = Modifier
                                .width(width)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch { 
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1,
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) 
                            }
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 4.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < pages.size - 1) 
                            stringResource(R.string.onboarding_btn_next).uppercase() 
                        else 
                            stringResource(R.string.onboarding_btn_start).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // Top Skip Button - Minimalist
        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_btn_skip),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun OnboardingPagerItem(page: OnboardingPage, isVisible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual Area with entry animation
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            this@Column.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = BackOut)),
                exit = fadeOut(tween(300))
            ) {
                when (page) {
                    OnboardingPage.Identity -> IdentityVisual()
                    OnboardingPage.Mastery -> MasteryVisual()
                    OnboardingPage.Discipline -> DisciplineVisual()
                }
            }
        }

        // Text Area - Taking inspiration from HabitProgressCard labels
        Column(
            modifier = Modifier.weight(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(page.headline),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 40.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(page.support),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun IdentityVisual() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .size(180.dp)
            .scale(scale),
        shape = RoundedCornerShape(56.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        //Patterns Logo will be here maybe I Don't know yet
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint =  Color(0xFF246A4B)
            )
        }
    }
}

@Composable
fun MasteryVisual() {
    val mockHabit = remember {
        HabitDetailsUi(
            id = 0,
            name = "Meditation",
            icon = null,
            accentColor = Color(0xFF246A4B),
            currentStreak = 12,
            totalCompletions = 45,
            goal = "10 mins",
            frequency = "Daily",
            createdOn = "2023-01-01",
            createdAtLocalDate = LocalDate.now(),
            totalXP = 1250
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .scale(0.9f)
    ) {
        HabitProgressCard(
            habit = mockHabit,
            accentColor = Color(0xFF246A4B)
        )
    }
}

@Composable
fun DisciplineVisual() {
    Surface(
        modifier = Modifier.size(240.dp, 100.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color(0xFF246A4B)
            ) {
                Icon(
                    Icons.Rounded.NightsStay,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(120.dp, 14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp, 10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                )
            }
        }
    }
}

sealed class OnboardingPage(
    val headline: Int,
    val support: Int
) {
    data object Identity : OnboardingPage(R.string.onboarding_1_headline, R.string.onboarding_1_support)
    data object Mastery : OnboardingPage(R.string.onboarding_2_headline, R.string.onboarding_2_support)
    data object Discipline : OnboardingPage(R.string.onboarding_3_headline, R.string.onboarding_3_support)
}

val BackOut = Easing { fraction ->
    val s = 1.70158f
    val f = fraction - 1.0f
    f * f * ((s + 1.0f) * f + s) + 1.0f
}
