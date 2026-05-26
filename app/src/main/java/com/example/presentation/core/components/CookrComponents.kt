package com.example.presentation.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.remember
import androidx.compose.foundation.LocalIndication

import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.getValue

// Custom Neo-Brutalism Shadow Modifier
fun Modifier.neoShadow(
    color: Color = Color(0xFF1E1E24),
    offsetX: Dp = 5.dp,
    offsetY: Dp = 5.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp)
): Modifier = this.drawBehind {
    val cornerRadius = shape.topStart.toPx(size, this)
    // Draw solid offset background shadow
    drawRoundRect(
        color = color,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
}

@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 3.dp,
    shadowColor: Color? = MaterialTheme.colorScheme.outline,
    shadowOffset: Dp = 6.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "neoCardScale"
    )

    // Elevate with custom draw modifier
    var cardModifier = modifier.scale(scale)
    if (shadowColor != null) {
        cardModifier = cardModifier.neoShadow(color = shadowColor, offsetX = shadowOffset, offsetY = shadowOffset, shape = shape)
    }
    cardModifier = cardModifier
        .border(BorderStroke(borderWidth, borderColor), shape)
        .clip(shape)
        .background(containerColor)

    if (onClick != null) {
        cardModifier = cardModifier.clickable(interactionSource = interactionSource, indication = LocalIndication.current) { onClick() }
    }

    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun NeoButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 3.dp,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    shadowOffset: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "neoButtonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .neoShadow(color = shadowColor, offsetX = shadowOffset, offsetY = shadowOffset, shape = shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .clip(shape)
            .background(containerColor)
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    borderWidth: Dp = 3.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    Box(
        modifier = modifier
            .border(BorderStroke(borderWidth, borderColor), shape)
            .clip(shape)
            .background(containerColor)
            .padding(horizontal = 4.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            shape = shape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WavyTimerIndicator(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = waveColor.copy(alpha = 0.2f),
    waveAmplitude: Float = 10f,
    waveFrequency: Float = 3f
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "wave")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1500,
                easing = androidx.compose.animation.core.LinearEasing
            )
        ),
        label = "phase"
    )
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(targetValue = progress)

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val progressWidth = width * animatedProgress.coerceIn(0f, 1f)

        // Draw background track line
        drawLine(
            color = trackColor,
            start = androidx.compose.ui.geometry.Offset(0f, centerY),
            end = androidx.compose.ui.geometry.Offset(width, centerY),
            strokeWidth = 10.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(0f, centerY)

        if (progressWidth > 0f) {
            val stepsCount = 100
            for (i in 0..stepsCount) {
                val x = (progressWidth * i) / stepsCount
                
                // Add wave offset
                val phase = (x / width) * waveFrequency * 2 * Math.PI + phaseOffset
                // Dampen the wave at the exact head (to not jump up and down detached)
                val dampening = if (i > stepsCount - 5) (stepsCount - i) / 5f else 1f
                val yOffset = Math.sin(phase).toFloat() * waveAmplitude * dampening
                
                path.lineTo(x, centerY + yOffset)
            }

            drawPath(
                path = path,
                color = waveColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 10.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
}
