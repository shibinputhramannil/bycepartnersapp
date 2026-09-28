package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ByceGreen
import com.example.ui.theme.ByceGreenGlow
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenGlow
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.GlassSurfaceMedium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite

/**
 * Custom Liquid Glass Container Composable.
 * Features translucent multi-layer glass backdrop with smooth multi-stop gradient,
 * soft depth shadow, and customizable rounded corners with no distracting borders.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color? = null,
    backgroundBrush: Brush? = null,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val effectiveBackgroundBrush = backgroundBrush ?: if (backgroundColor == null) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x22FFFFFF),
                Color(0x1BFFFFFF),
                Color(0x15FFFFFF),
                Color(0x10FFFFFF),
                Color(0x0CFFFFFF)
            )
        )
    } else null

    val effectiveBorderBrush = borderBrush ?: if (borderColor == null) {
        Brush.linearGradient(
            colors = listOf(
                GlassBorderSpecular,
                GlassBorderLight,
                Color(0x15FFFFFF)
            )
        )
    } else null

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color(0x80000000)
            )
            .clip(shape)
            .then(
                if (effectiveBackgroundBrush != null) {
                    Modifier.background(effectiveBackgroundBrush)
                } else if (backgroundColor != null) {
                    Modifier.background(backgroundColor)
                } else Modifier
            )
            .then(
                if (borderWidth > 0.dp) {
                    if (effectiveBorderBrush != null) {
                        Modifier.border(borderWidth, effectiveBorderBrush, shape)
                    } else if (borderColor != null) {
                        Modifier.border(borderWidth, borderColor, shape)
                    } else Modifier
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * Slide 1: Floating Liquid Glass Pill ("Byce partner")
 */
@Composable
fun LiquidGlassPill(
    text: String,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.testTag("liquid_glass_pill"),
        shape = RoundedCornerShape(50.dp),
        backgroundColor = Color(0x3BFFFFFF),
        borderColor = Color(0x4DFFFFFF),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Subtle Byce green indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(ByceGreen, CircleShape)
                    .shadow(4.dp, CircleShape, spotColor = ByceGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = TextWhite,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Slide 2: Floating Liquid Glass Panel ("18 check-ins", "342 members")
 */
@Composable
fun LiquidGlassStatPanel(
    checkIns: String = "18 check-ins",
    members: String = "342 members",
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.testTag("liquid_glass_stat_panel"),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x45222021),
        borderColor = Color(0x35FFFFFF),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(ByceGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = checkIns,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextWhite
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Today's activity",
                    fontSize = 11.sp,
                    color = TextSubtle
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(Color(0x33FFFFFF))
            )

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = members,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Active network",
                    fontSize = 11.sp,
                    color = TextSubtle
                )
            }
        }
    }
}

/**
 * Slide 3: Floating Liquid Glass Confirmation Panel ("Access approved", "Byce membership")
 */
@Composable
fun LiquidGlassApprovalPanel(
    title: String = "Access approved",
    subtitle: String = "Byce membership",
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.testTag("liquid_glass_approval_panel"),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0x4D222021),
        borderColor = Color(0x40FFFFFF),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // White checkmark inside circular glass
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x28FFFFFF))
                    .border(1.dp, Color(0x40FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checkmark",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Subtle Byce Green verified indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(ByceGreen, CircleShape)
            )
        }
    }
}

/**
 * Translucent Liquid Glass Primary Button.
 * Does NOT look like a solid green button.
 * Translucent glass finish with subtle specular highlight and refined touch response.
 */
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "primary_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(150),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .defaultMinSize(minHeight = 42.dp)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .shadow(
                elevation = if (isPressed) 3.dp else 8.dp,
                shape = RoundedCornerShape(23.dp),
                ambientColor = Color.Black,
                spotColor = ByceGreenGlow
            )
            .clip(RoundedCornerShape(23.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x3BFFFFFF),
                        Color(0x20FFFFFF),
                        Color(0x18FFFFFF)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBorderSpecular,
                        GlassBorderSpecular,
                        GlassBorderLight,
                        Color(0x20FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(23.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Specular highlight at top of liquid glass button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x80FFFFFF),
                            ByceGreen.copy(alpha = 0.5f),
                            Color(0x80FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextWhite,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Liquid Glass Segmented Switch / Control (Log in | Sign up or Owner | Manager).
 * Selected tab: Soft white/grey liquid glass with subtle Byce green (#A6CE39) indicator.
 * Unselected tab: Transparent dark glass.
 */
@Composable
fun LiquidGlassSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "liquid_glass_segmented_control"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .testTag(testTag)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(21.dp),
                ambientColor = Color.Black,
                spotColor = Color(0x60000000)
            )
            .clip(RoundedCornerShape(21.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x30222021),
                        Color(0x20161415)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x35FFFFFF),
                        Color(0x12FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(21.dp)
            )
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, optionText ->
                val isSelected = selectedIndex == index

                val tabScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.98f,
                    animationSpec = tween(200),
                    label = "tab_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x38FFFFFF),
                                                Color(0x20FFFFFF)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0x60FFFFFF),
                                                Color(0x30FFFFFF)
                                            )
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .clickable { onOptionSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = optionText,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) TextWhite else TextMuted
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = CircleShape,
                                        ambientColor = NeonGreen,
                                        spotColor = NeonGreen
                                    )
                                    .background(NeonGreen, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Large Rounded Liquid Glass Input Field with optional trailing visibility toggle icon & error text.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordToggleClick: (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    errorText: String? = null,
    testTag: String = "liquid_glass_text_field"
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag(testTag)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x20FFFFFF),
                            Color(0x12FFFFFF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = if (errorText != null) {
                            listOf(Color(0xFF882222), Color(0xFFFF5252))
                        } else {
                            listOf(
                                GlassBorderSpecular,
                                GlassBorderLight,
                                Color(0x15FFFFFF)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = Color(0x60FFFFFF),
                            fontSize = 15.sp
                        )
                    }

                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        visualTransformation = if (isPassword && !isPasswordVisible) {
                            androidx.compose.ui.text.input.PasswordVisualTransformation()
                        } else {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    trailingIcon()
                } else if (isPassword && onPasswordToggleClick != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onPasswordToggleClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPasswordVisible) "Hide" else "Show",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = Color(0xFFFF6B6B),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Secondary Neutral Glass Button (e.g. Continue with Google).
 * Neutral translucent glass treatment as requested.
 */
@Composable
fun LiquidGlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    testTag: String = "liquid_glass_secondary_button"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x1EFFFFFF),
                        Color(0x10FFFFFF)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x28FFFFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextWhite
            )
        }
    }
}

/**
 * Minimalist Vector Google "G" Icon Composable
 */
@Composable
fun GoogleIcon() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(0x20FFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextWhite
        )
    }
}

