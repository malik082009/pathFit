package com.grittech.pathfit.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grittech.pathfit.ui.AppViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CardRest
import com.grittech.pathfit.ui.theme.Ink
import com.grittech.pathfit.ui.theme.InkMuted
import com.grittech.pathfit.ui.theme.Line
import com.grittech.pathfit.ui.theme.Mint
import com.grittech.pathfit.ui.theme.MintDeep
import com.grittech.pathfit.ui.theme.OnMint

@Composable
fun PulseMark(modifier: Modifier = Modifier, large: Boolean = false) {
    val size = if (large) 84.dp else 36.dp
    Box(
        modifier = modifier
            .size(size)
            .shadow(if (large) 24.dp else 8.dp, RoundedCornerShape(if (large) 22.dp else 12.dp), ambientColor = Mint, spotColor = Mint)
            .clip(RoundedCornerShape(if (large) 22.dp else 12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF7CFFC4), Color(0xFF3DDCFF)))),
        contentAlignment = Alignment.Center,
    ) {
        PulseGlyph(if (large) 36.dp else 18.dp)
    }
}

@Composable
fun PulseGlyph(size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = size.toPx() * 0.08f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        )
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, size.toPx() * 0.55f)
            lineTo(size.toPx() * 0.28f, size.toPx() * 0.55f)
            lineTo(size.toPx() * 0.40f, size.toPx() * 0.28f)
            lineTo(size.toPx() * 0.58f, size.toPx() * 0.78f)
            lineTo(size.toPx() * 0.72f, size.toPx() * 0.48f)
            lineTo(size.toPx(), size.toPx() * 0.48f)
        }
        drawPath(path, Color(0xFF06281C), style = stroke)
    }
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showArrow: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(18.dp, CircleShape, ambientColor = Mint.copy(alpha = 0.35f), spotColor = Mint.copy(alpha = 0.45f))
            .clip(CircleShape)
            .background(if (enabled) MintDeep else MintDeep.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = OnMint,
            fontWeight = FontWeight.Bold,
            fontSize = if (label.length > 28) 13.sp else 15.sp,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (showArrow) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = OnMint)
        }
    }
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .border(1.dp, Line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Ink, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StepHeader(kicker: String, title: String, body: String, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(kicker, color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF252A39)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Mint),
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(title, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = InkMuted, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun FitField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailing: String? = null,
) {
    Column {
        Text(label, color = InkMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = InkMuted)
            Spacer(Modifier.width(10.dp))
            Text(value.ifBlank { "Choose" }, color = if (value.isBlank()) InkMuted else Ink, modifier = Modifier.weight(1f))
            if (trailing != null) Text(trailing, color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ChoiceCard(
    title: String,
    body: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color(0xFF10261C) else CardRest)
            .border(1.dp, if (selected) Mint.copy(alpha = 0.7f) else Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) Mint.copy(alpha = 0.16f) else Canvas),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) Mint else InkMuted)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold)
            Text(body, color = InkMuted, fontSize = 12.sp)
        }
        if (selected) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                Text("✓", color = OnMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BackLink(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = InkMuted, fontSize = 14.sp)
    }
}

fun Context.findActivity(): Activity {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    error("No activity")
}

@Composable
fun rememberFitViewModel(): AppViewModel {
    val activity = androidx.compose.ui.platform.LocalContext.current.findActivity() as ComponentActivity
    return viewModel(viewModelStoreOwner = activity)
}

@Composable
fun ScreenColumn(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Canvas)
            .padding(horizontal = 16.dp),
        content = content,
    )
}
