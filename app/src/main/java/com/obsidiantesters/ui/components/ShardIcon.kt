package com.obsidiantesters.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidiantesters.ui.theme.ShardGold

/** Small geometric crystal/shard icon rendered via Canvas. */
@Composable
fun ShardIcon(modifier: Modifier = Modifier, size: Dp = 18.dp, color: Color = ShardGold) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.65f, h)
            lineTo(w * 0.35f, h)
            lineTo(w * 0.15f, h * 0.35f)
            close()
        }
        drawPath(path, color)
        // Highlight facet
        val facet = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.55f)
            close()
        }
        drawPath(facet, color.copy(alpha = 0.5f))
    }
}

/** Shard balance display: gold crystal icon + count. */
@Composable
fun ShardBalance(balance: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ShardIcon()
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$balance",
            color = ShardGold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
