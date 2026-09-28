package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cult.fit "Aurora" Active Ambient Background.
 * Inspired by Cult.fit's dynamic glowing ambient light system:
 * - 3 distinct, smoothly rotating energetic orbs:
 *   1. 🌿 Mint Cyan: Top-Left quadrant (14-second orbital cycle)
 *   2. ⚡ Electric Lime: Center-Right quadrant (11-second orbital cycle)
 *   3. 🌊 Cyber Sky Blue: Lower quadrant (16-second orbital cycle)
 * - Soft Gaussian multi-stop atmospheric light dispersion on deep obsidian dark background.
 */
@Composable
fun FloatingBlurBalls(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cult_aurora_orbs")

    // Orb 1: Mint Cyan (Fast 4.5-second rotation)
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_mint"
    )

    // Orb 2: Electric Lime (Fast 3.6-second counter rotation)
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_lime"
    )

    // Orb 3: Cyber Sky Blue (Fast 5.2-second rotation)
    val angle3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_blue"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val rad1 = Math.toRadians(angle1.toDouble())
        val rad2 = Math.toRadians(angle2.toDouble())
        val rad3 = Math.toRadians(angle3.toDouble())

        // ----------------------------------------------------
        // 1. MINT CYAN ORB (Fast Top-Left & Header Orbit)
        // ----------------------------------------------------
        val orb1X = width * 0.32f + (sin(rad1) * 140f).toFloat()
        val orb1Y = height * 0.08f + (cos(rad1) * 100f).toFloat()
        drawAuroraOrb(
            center = Offset(orb1X, orb1Y),
            radius = width * 0.85f,
            color = Color(0x4E2DD4BF) // Cult.fit Mint Teal - Reaches top status bar & header
        )

        // ----------------------------------------------------
        // 2. ELECTRIC LIME ORB (Fast Mid-Right Orbit)
        // ----------------------------------------------------
        val orb2X = width * 0.72f + (cos(rad2) * 135f).toFloat()
        val orb2Y = height * 0.40f + (sin(rad2) * 115f).toFloat()
        drawAuroraOrb(
            center = Offset(orb2X, orb2Y),
            radius = width * 0.78f,
            color = Color(0x2034D399) // Soft Emerald Mint Ambient
        )

        // ----------------------------------------------------
        // 3. CYBER SKY BLUE ORB (Fast Lower-Center Orbit)
        // ----------------------------------------------------
        val orb3X = width * 0.38f + (sin(rad3) * 150f).toFloat()
        val orb3Y = height * 0.75f + (cos(rad3) * 125f).toFloat()
        drawAuroraOrb(
            center = Offset(orb3X, orb3Y),
            radius = width * 0.82f,
            color = Color(0x480EA5E9) // Cult.fit Electric Sky Blue
        )
    }
}

/**
 * Renders smooth Gaussian radial light bloom without harsh edge boundaries.
 */
private fun DrawScope.drawAuroraOrb(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color,
                color.copy(alpha = color.alpha * 0.75f),
                color.copy(alpha = color.alpha * 0.40f),
                color.copy(alpha = color.alpha * 0.12f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}






