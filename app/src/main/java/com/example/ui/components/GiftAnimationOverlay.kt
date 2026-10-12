package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GiftItem
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ActiveGiftAnimation(
    val id: String = UUID.randomUUID().toString(),
    val gift: GiftItem,
    val senderName: String,
    val receiverName: String,
    val coinValue: Int = gift.coinCost
)

private data class OverlayParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val emoji: String? = null,
    val rotationSpeed: Float
)

@Composable
fun ScreenWideGiftAnimationOverlay(
    activeGift: ActiveGiftAnimation?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeGift == null) return

    val animProgress = remember(activeGift.id) { Animatable(0f) }

    LaunchedEffect(activeGift.id) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
        onDismiss()
    }

    val progress = animProgress.value

    // Alpha fade in quickly and fade out at the end
    val overlayAlpha = when {
        progress < 0.1f -> progress / 0.1f
        progress > 0.85f -> (1f - progress) / 0.15f
        else -> 1f
    }

    // Scale pop-in of center gift with spring-like bounce
    val giftScale = when {
        progress < 0.15f -> (progress / 0.15f) * 1.35f
        progress < 0.28f -> 1.35f - ((progress - 0.15f) / 0.13f) * 0.35f
        progress > 0.85f -> 1f + ((progress - 0.85f) / 0.15f) * 0.4f
        else -> 1f + sin(progress * 15f) * 0.05f // gentle floating pulse
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rays")
    val rayAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "rayRotation"
    )

    // Pre-calculate random particles once per gift animation
    val particles = remember(activeGift.id) {
        val colors = listOf(NeonPink, NeonAmber, NeonCyan, Color(0xFFFFD700), Color(0xFFFF5252), Color(0xFFE040FB))
        val emojis = listOf("✨", "🌟", "🎉", "💎", "⭐", "🪙", "💖")
        List(40) { i ->
            OverlayParticle(
                angle = (i * (360f / 40f) + Random.nextFloat() * 15f) * (PI.toFloat() / 180f),
                speed = 220f + Random.nextFloat() * 450f,
                size = 6f + Random.nextFloat() * 10f,
                color = colors[Random.nextInt(colors.size)],
                emoji = if (i % 2 == 0) emojis[Random.nextInt(emojis.size)] else null,
                rotationSpeed = Random.nextFloat() * 360f
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .alpha(overlayAlpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
            .testTag("screen_wide_gift_overlay"),
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val center = Offset(width / 2f, height / 2f)

        // 1. Full-screen dark translucent backdrop with radial vignette glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x992E1065),
                            Color(0xCC090A1A),
                            Color(0xF0030712)
                        ),
                        center = center,
                        radius = (width.coerceAtLeast(height)) * 0.85f
                    )
                )
        )

        // 2. Rotating radiant sunburst light rays & shockwave expanding rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Expanding Shockwave rings
            val ringProgress = (progress * 2.2f).coerceAtMost(1f)
            val ringRadius = ringProgress * (size.minDimension * 0.85f)
            val ringAlpha = (1f - ringProgress).coerceIn(0f, 1f) * 0.7f
            if (ringRadius > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.Transparent, NeonCyan.copy(alpha = ringAlpha), NeonPink.copy(alpha = ringAlpha * 0.5f)),
                        center = center,
                        radius = ringRadius
                    ),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 6.dp.toPx())
                )
            }

            // Rotating Sunburst rays
            rotate(rayAngle, pivot = center) {
                val numRays = 16
                val rayLength = size.maxDimension
                for (i in 0 until numRays) {
                    val angleRad = (i * (360f / numRays)) * (PI.toFloat() / 180f)
                    val rayColor = if (i % 2 == 0) NeonAmber.copy(alpha = 0.18f) else NeonPink.copy(alpha = 0.15f)
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        val spread = 0.08f
                        lineTo(
                            center.x + cos(angleRad - spread) * rayLength,
                            center.y + sin(angleRad - spread) * rayLength
                        )
                        lineTo(
                            center.x + cos(angleRad + spread) * rayLength,
                            center.y + sin(angleRad + spread) * rayLength
                        )
                        close()
                    }
                    drawPath(path, color = rayColor)
                }
            }

            // Confetti and sparkle shower
            particles.forEach { particle ->
                val dist = particle.speed * progress
                val gravityDrop = 380f * (progress * progress)
                val px = center.x + cos(particle.angle) * dist
                val py = center.y + sin(particle.angle) * dist + gravityDrop
                val particleAlpha = (1f - (progress * 0.9f)).coerceIn(0f, 1f)

                if (px in -50f..(size.width + 50f) && py in -50f..(size.height + 50f)) {
                    if (particle.emoji == null) {
                        drawCircle(
                            color = particle.color.copy(alpha = particleAlpha),
                            radius = particle.size,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        // 3. Central Mega Gift Presentation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Pulsing Neon Aura behind the Gift
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(giftScale)
            ) {
                // Outer glowing gradient ring
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFF007A).copy(alpha = 0.45f),
                                    Color(0xFF7928CA).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Shimmering inner badge
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E1035),
                                    Color(0xFF2E1A47)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    NeonAmber,
                                    NeonPink,
                                    NeonCyan,
                                    NeonAmber
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeGift.gift.iconEmoji,
                        fontSize = 72.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Grand Celebration Title & Sender Card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF007A),
                                Color(0xFF7928CA),
                                Color(0xFF00F0FF)
                            )
                        )
                    )
                    .padding(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xF00D0B24))
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE GIFT UNLEASHED!",
                            color = NeonAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "✨", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sender -> Receiver Announcement
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activeGift.senderName,
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = " sent ",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                        Text(
                            text = activeGift.gift.name,
                            color = NeonPink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = activeGift.gift.iconEmoji, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "to ${activeGift.receiverName}",
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gift Boost Value pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF22173F))
                            .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${activeGift.coinValue} coins hype boost!",
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
