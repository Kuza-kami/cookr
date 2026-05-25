package com.example.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CookrViewModel
import com.example.presentation.core.components.NeoCard

@Composable
fun AnalyticsScreen(viewModel: CookrViewModel) {
    var animationTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationTrigger = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB)) // Clean, modern off-white background as in screenshot
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. HEADER ROW (Matches Left/Right top design elements in screenshot)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Back Arrow inside circular button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E9F0), CircleShape)
                            .clickable { /* Handle Back */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1E280C)
                        )
                    }

                    // Center Screen Title
                    Text(
                        text = "Statistic",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E280C)
                    )

                    // Right More Options inside circular button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E9F0), CircleShape)
                            .clickable { /* Handle Settings/More */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More Options",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1E280C)
                        )
                    }
                }
            }

            // 2. CALORIES MAIN DISPLAY SECTION (Matches modern layout)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Calories",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "1250",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E280C),
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = " Kcal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                            )
                        }
                        
                        Text(
                            text = "Target: 1920 Kcal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E280C).copy(alpha = 0.8f),
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 3. WEEKLY PROGRESS BAR CHART (Mon to Sun - with Wednesday high-fidelity highlight)
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    shadowColor = Color(0xFFE5E9F0),
                    shadowOffset = 2.dp,
                    borderWidth = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Chart display height
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val chartDays = listOf(
                                ChartDayData("Mon", 0.44f, "44%", false),
                                ChartDayData("Tue", 0.34f, "34%", false),
                                ChartDayData("Wed", 1.10f, "110%", true), // Wednesday High-Fidelity Highlight from Screenshot
                                ChartDayData("Thu", 0.47f, "47%", false),
                                ChartDayData("Fri", 0.32f, "32%", false),
                                ChartDayData("Sat", 0.79f, "79%", false),
                                ChartDayData("Sun", 0.24f, "24%", false)
                            )

                            chartDays.forEach { dayData ->
                                StatsBarCol(
                                    dayData = dayData,
                                    animationTrigger = animationTrigger,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. GRID STATISTICS CARDS (2x2 Multi-metric Dashboard as in Screenshot)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card A: Exercise (Run / Dumbbell theme)
                        GridStatCard(
                            title = "Exercise",
                            value = "2.0 hours",
                            icon = Icons.Outlined.FitnessCenter,
                            iconTint = Color(0xFF4CAF50),
                            iconBg = Color(0xFFE8F5E9),
                            modifier = Modifier.weight(1f)
                        ) {
                            // High fidelity exercise mini sparkline
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val heights = listOf(0.4f, 0.7f, 0.9f, 0.6f, 0.3f, 0.5f, 0.8f, 1.0f, 0.6f, 0.4f)
                                heights.forEach { peak ->
                                    val animatedHeight by animateFloatAsState(
                                        targetValue = if (animationTrigger) peak else 0.1f,
                                        animationSpec = tween(1200, easing = FastOutSlowInEasing),
                                        label = "SparkHeight"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(animatedHeight)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF4CAF50))
                                    )
                                }
                            }
                        }

                        // Card B: BPM (Heartbeat pulse theme)
                        GridStatCard(
                            title = "BPM",
                            value = "86 bpm",
                            icon = Icons.Outlined.Favorite,
                            iconTint = Color(0xFFE53935),
                            iconBg = Color(0xFFFFEBEE),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Elegant Bezier Heartbeat Waves render
                            val animationProgress by animateFloatAsState(
                                targetValue = if (animationTrigger) 1f else 0f,
                                animationSpec = tween(2000, easing = LinearEasing),
                                label = "PulseAnimation"
                            )

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                            ) {
                                val width = size.width
                                val height = size.height
                                val path = Path()
                                
                                path.moveTo(0f, height / 2)
                                path.lineTo(width * 0.2f, height / 2)
                                path.lineTo(width * 0.25f, height / 2 - 8.dp.toPx())
                                path.lineTo(width * 0.3f, height / 2 + 6.dp.toPx())
                                path.lineTo(width * 0.35f, height / 2 - 24.dp.toPx() * animationProgress)
                                path.lineTo(width * 0.4f, height / 2 + 10.dp.toPx() * animationProgress)
                                path.lineTo(width * 0.45f, height / 2)
                                path.lineTo(width * 0.65f, height / 2)
                                path.lineTo(width * 0.7f, height / 2 - 12.dp.toPx())
                                path.lineTo(width * 0.75f, height / 2 + 8.dp.toPx())
                                path.lineTo(width * 0.8f, height / 2)
                                path.lineTo(width, height / 2)

                                drawPath(
                                    path = path,
                                    color = Color(0xFFE53935),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card C: Weight (Trending scale theme)
                        GridStatCard(
                            title = "Weight",
                            value = "65.4 kg",
                            icon = Icons.Outlined.Scale,
                            iconTint = Color(0xFFFF9800),
                            iconBg = Color(0xFFFFF3E0),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Weight dynamic ruler progress indicator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val midY = size.height / 2
                                    val width = size.width
                                    // Draw thin calibration scale lines as in high end apps
                                    for (i in 0..12) {
                                        val x = (width / 12) * i
                                        val lineH = if (i % 3 == 0) size.height * 0.7f else size.height * 0.4f
                                        drawLine(
                                            color = Color.Gray.copy(alpha = 0.4f),
                                            start = Offset(x, midY - lineH / 2),
                                            end = Offset(x, midY + lineH / 2),
                                            strokeWidth = 1.5.dp.toPx()
                                        )
                                    }
                                    // Target anchor indicator pointer
                                    drawCircle(
                                        color = Color(0xFFFF9800),
                                        radius = 4.dp.toPx(),
                                        center = Offset(width * 0.6f, midY)
                                    )
                                }
                            }
                        }

                        // Card D: Water Drops Progress tracker
                        GridStatCard(
                            title = "Water",
                            value = "1.8 liters",
                            icon = Icons.Outlined.WaterDrop,
                            iconTint = Color(0xFF2196F3),
                            iconBg = Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        ) {
                            // 5 high-fidelity water droplet indicators
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val filledDrops = 3
                                val scaleWave by animateFloatAsState(
                                    targetValue = if (animationTrigger) 1.15f else 0.8f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = LinearOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "WaterPulse"
                                )

                                for (i in 0 until 5) {
                                    val isFilled = i < filledDrops
                                    val animScale = if (isFilled && i == filledDrops - 1) scaleWave else 1.0f
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .drawBehind {
                                                drawCircle(
                                                    color = if (isFilled) Color(0xFF2196F3) else Color(0xFFE3F2FD),
                                                    radius = (size.minDimension / 2) * animScale
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.WaterDrop,
                                            contentDescription = null,
                                            tint = if (isFilled) Color.White else Color(0xFF2196F3).copy(alpha = 0.5f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ChartDayData(
    val label: String,
    val progress: Float,
    val percentage: String,
    val isHighlighted: Boolean
)

@Composable
fun StatsBarCol(
    dayData: ChartDayData,
    animationTrigger: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) dayData.progress else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "BarProgress"
    )

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Percentage Text label above bar
        Text(
            text = dayData.percentage,
            fontSize = 11.sp,
            fontWeight = if (dayData.isHighlighted) FontWeight.Black else FontWeight.Bold,
            color = if (dayData.isHighlighted) Color(0xFF8CD867) else Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Bar Pill containing track outline + solid fill
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (dayData.isHighlighted) Color(0xFF8CD867).copy(alpha = 0.12f)
                    else Color(0xFFF1F5F9)
                )
                .drawBehind {
                    // Modern diagonal background track patterns as in design screenshot
                    if (!dayData.isHighlighted) {
                        for (offsetY in 0..size.height.toInt() step 12) {
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                start = Offset(0f, offsetY.toFloat()),
                                end = Offset(size.width, offsetY.toFloat() + 6.dp.toPx()),
                                strokeWidth = 1f
                            )
                        }
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedProgress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (dayData.isHighlighted) {
                            // Active glowing orange-to-lime Ghibli forest fire theme gradient
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF9800),
                                    Color(0xFF8CD867)
                                )
                            )
                        } else {
                            androidx.compose.ui.graphics.SolidColor(Color(0xFFC7EFA2)) // Clean pastel calming green track fill as Brush
                        }
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Label day below bar
        Text(
            text = dayData.label,
            fontSize = 12.sp,
            fontWeight = if (dayData.isHighlighted) FontWeight.Black else FontWeight.Bold,
            color = if (dayData.isHighlighted) Color(0xFF1E280C) else Color.Gray
        )
    }
}

@Composable
fun GridStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    NeoCard(
        modifier = modifier,
        containerColor = Color.White,
        shadowColor = Color(0xFFE5E9F0),
        shadowOffset = 2.dp,
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Icon + Label top row alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Center Visualizer component
            content()

            Spacer(modifier = Modifier.height(12.dp))

            // Numeric metrics bold reading at bottom
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E280C)
            )
        }
    }
}
