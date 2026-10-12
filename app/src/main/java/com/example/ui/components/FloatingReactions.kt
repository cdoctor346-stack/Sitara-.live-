package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ReactionParticle(
    val id: Long,
    val emoji: String,
    val startXOffset: Float,
    val swayAmount: Float
)

@Composable
fun FloatingReactionsContainer(
    particles: List<ReactionParticle>,
    onParticleFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        particles.forEach { particle ->
            SingleFloatingReaction(
                particle = particle,
                onFinished = { onParticleFinished(particle.id) }
            )
        }
    }
}

@Composable
private fun SingleFloatingReaction(
    particle: ParticleWrapper,
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(0.4f) }

    LaunchedEffect(particle.id) {
        launch {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 250)
            )
        }
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
        onFinished()
    }

    val currentProgress = progress.value
    val yOffset = (-500 * currentProgress).dp
    val xSway = (kotlin.math.sin(currentProgress * 6.28) * particle.swayAmount + particle.startXOffset).dp
    val alpha = if (currentProgress > 0.7f) (1f - currentProgress) / 0.3f else 1f

    Text(
        text = particle.emoji,
        fontSize = 28.sp,
        modifier = Modifier
            .offset { IntOffset(xSway.roundToPx(), yOffset.roundToPx()) }
            .scale(scale.value)
            .alpha(alpha)
    )
}

typealias ParticleWrapper = ReactionParticle
