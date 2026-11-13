package com.grittech.pathfit.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.ui.components.findActivity
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CardHigh
import com.grittech.pathfit.ui.theme.CardHighest
import com.grittech.pathfit.ui.theme.CardRest
import com.grittech.pathfit.ui.theme.Ink
import com.grittech.pathfit.ui.theme.InkDim
import com.grittech.pathfit.ui.theme.InkMuted
import com.grittech.pathfit.ui.theme.Line
import com.grittech.pathfit.ui.theme.Mint
import com.grittech.pathfit.ui.theme.MintDeep
import com.grittech.pathfit.ui.theme.OnMint
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var progress by remember { mutableStateOf(0.18f) }
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 420, easing = LinearEasing),
        label = "splashProgress",
    )
    val pulse = rememberInfiniteTransition(label = "splashPulse")
    val glow by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "glow",
    )
    val scale by pulse.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "scale",
    )
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "dot",
    )

    LaunchedEffect(Unit) {
        val steps = listOf(0.45f, 0.72f, 0.94f)
        for (step in steps) {
            delay(900)
            progress = step
        }
        vm.onSplashFinished()
    }

    LaunchedEffect(state.splashError) {
        if (state.splashError != null) progress = 0.35f
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Canvas),
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(280.dp)
                .alpha(0.18f * glow)
                .background(Brush.radialGradient(listOf(MintDeep.copy(alpha = 0.55f), Color.Transparent)), CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp)
                .size(220.dp)
                .alpha(0.12f)
                .background(Brush.radialGradient(listOf(Color(0xFF7BD0FF).copy(alpha = 0.35f), Color.Transparent)), CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(220.dp)
                .alpha(0.14f)
                .background(Brush.radialGradient(listOf(Mint.copy(alpha = 0.28f), Color.Transparent)), CircleShape),
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(CardHighest.copy(alpha = 0.8f))
                    .border(1.dp, Color(0x4D3B4A41), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Mint.copy(alpha = dotAlpha)))
            }

            Spacer(Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(112.dp)
                            .scale(scale)
                            .alpha(0.35f * glow)
                            .background(Brush.radialGradient(listOf(Mint.copy(alpha = 0.55f), Color.Transparent)), CircleShape),
                    )
                    Box(
                        Modifier
                            .size(80.dp)
                            .shadow(20.dp, RoundedCornerShape(26.dp), ambientColor = Mint.copy(alpha = 0.35f), spotColor = Mint.copy(alpha = 0.45f))
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(CardHighest, CardHigh, Color(0xFF090E1C)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(MintDeep, Mint, Color(0xFF7BD0FF)),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            SplashPulse(40.dp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("FitPath", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Metabolic Health, Made Effortless",
                    color = Mint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Intuitive nutrition and honest metabolic tracking designed for your biology.",
                    color = InkMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 28.dp),
                )
                Spacer(Modifier.height(28.dp))

                if (state.splashError == null) {
                    Box(
                        Modifier
                            .width(128.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(CardHighest),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(animated.coerceIn(0.12f, 1f))
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(Mint, Color(0xFF7BD0FF)))),
                        )
                    }
                } else {
                    Text(state.splashError ?: "", color = Color(0xFFFFB4AB), textAlign = TextAlign.Center, fontSize = 13.sp)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Retry",
                        color = OnMint,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Mint)
                            .clickable {
                                progress = 0.2f
                                vm.onSplashFinished()
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                "ENDOCRINE-INFORMED & SECURE",
                color = InkDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SplashPulse(size: androidx.compose.ui.unit.Dp) {
    Canvas(Modifier.size(size)) {
        val stroke = Stroke(
            width = size.toPx() * 0.09f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val path = Path().apply {
            moveTo(0f, size.toPx() * 0.55f)
            lineTo(size.toPx() * 0.22f, size.toPx() * 0.55f)
            lineTo(size.toPx() * 0.34f, size.toPx() * 0.28f)
            lineTo(size.toPx() * 0.52f, size.toPx() * 0.78f)
            lineTo(size.toPx() * 0.66f, size.toPx() * 0.42f)
            lineTo(size.toPx() * 0.78f, size.toPx() * 0.55f)
            lineTo(size.toPx(), size.toPx() * 0.55f)
        }
        drawPath(path, Color(0xFF002112), style = stroke)
    }
}

@Composable
fun SignInScreen() {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    var showTerms by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .shadow(14.dp, RoundedCornerShape(16.dp), ambientColor = MintDeep.copy(alpha = 0.35f), spotColor = MintDeep.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(16.dp))
                .background(MintDeep.copy(alpha = 0.2f))
                .border(1.dp, Mint.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            SplashPulse(24.dp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Welcome to FitPath",
            color = Ink,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.4).sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Sign in to unlock personalized metabolic baselines, honest food tracking, and mindful nutrition guidance.",
            color = InkMuted,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(Modifier.height(24.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardHigh.copy(alpha = 0.7f))
                .border(1.dp, Color(0x403B4A41), RoundedCornerShape(16.dp))
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Explore, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                Text(
                    "  WHY FITPATH",
                    color = Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x333B4A41)))
            Spacer(Modifier.height(16.dp))
            WhyRow(Icons.Filled.TouchApp, "1-Tap Logging", "Instant everyday home meals capture without kitchen scales.")
            Spacer(Modifier.height(16.dp))
            WhyRow(Icons.Filled.Restaurant, "Honest Calorie Ranges", "Realistic regional estimates for salans, rotis & daily staples.")
            Spacer(Modifier.height(16.dp))
            WhyRow(Icons.Filled.Shield, "Smart Guardrails", "Safe metabolic baselines and tailored daily nutrition targets.")
        }

        Spacer(Modifier.height(24.dp))

        if (state.error != null) {
            Text(state.error ?: "", color = Color(0xFFFFB4AB), fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(18.dp, CircleShape, ambientColor = Color.White.copy(alpha = 0.12f), spotColor = Color.White.copy(alpha = 0.18f))
                .clip(CircleShape)
                .background(Color.White)
                .clickable(enabled = !state.busy && state.agreed) { vm.signIn(activity) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.busy) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color(0xFF1A73E8), strokeWidth = 2.dp)
            } else {
                GoogleMark()
                Spacer(Modifier.width(12.dp))
                Text("Continue with Google", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = Mint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Fast, passwordless Google authentication", color = InkMuted, fontSize = 12.sp)
        }

        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clickable { vm.setAgreed(!state.agreed) },
        ) {
            Box(
                Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (state.agreed) MintDeep else Color.Transparent)
                    .border(1.dp, if (state.agreed) MintDeep else InkDim, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (state.agreed) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Canvas, modifier = Modifier.size(12.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = InkMuted)) { append("I agree to the ") }
                    withStyle(SpanStyle(color = Mint, fontWeight = FontWeight.Medium)) { append("Terms of Service") }
                    withStyle(SpanStyle(color = InkMuted)) { append(" & ") }
                    withStyle(SpanStyle(color = Mint, fontWeight = FontWeight.Medium)) { append("Privacy Policy") }
                    withStyle(SpanStyle(color = InkMuted)) { append(".") }
                },
                fontSize = 12.sp,
                modifier = Modifier.clickable { showTerms = true },
            )
        }
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            confirmButton = { TextButton(onClick = { showTerms = false }) { Text("Close") } },
            title = { Text("Terms & privacy") },
            text = {
                Text("FitPath uses your Google account only to keep your profile, meals, and targets on your uid. Nutrition figures are estimates, not a medical diagnosis.")
            },
        )
    }
}

@Composable
private fun WhyRow(icon: ImageVector, title: String, body: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardHighest)
                .border(1.dp, Color(0x333B4A41), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Mint, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(body, color = InkMuted, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun GoogleMark() {
    Canvas(Modifier.size(20.dp)) {
        val r = size.minDimension / 2f
        val stroke = Stroke(width = r * 0.34f, cap = StrokeCap.Butt)
        val c = Offset(r, r)
        drawArc(Color(0xFF4285F4), -40f, 100f, false, style = stroke, topLeft = Offset(r * 0.18f, r * 0.18f), size = androidx.compose.ui.geometry.Size(r * 1.64f, r * 1.64f))
        drawArc(Color(0xFF34A853), 60f, 90f, false, style = stroke, topLeft = Offset(r * 0.18f, r * 0.18f), size = androidx.compose.ui.geometry.Size(r * 1.64f, r * 1.64f))
        drawArc(Color(0xFFFBBC05), 150f, 70f, false, style = stroke, topLeft = Offset(r * 0.18f, r * 0.18f), size = androidx.compose.ui.geometry.Size(r * 1.64f, r * 1.64f))
        drawArc(Color(0xFFEA4335), 220f, 80f, false, style = stroke, topLeft = Offset(r * 0.18f, r * 0.18f), size = androidx.compose.ui.geometry.Size(r * 1.64f, r * 1.64f))
        drawRect(
            Color(0xFF4285F4),
            topLeft = Offset(c.x - 1f, c.y - r * 0.17f),
            size = androidx.compose.ui.geometry.Size(r * 0.95f, r * 0.34f),
        )
    }
}
