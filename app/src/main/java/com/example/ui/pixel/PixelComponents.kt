package com.example.ui.pixel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.GbaCardFrameDark
import com.example.ui.theme.RetroGold
import com.example.ui.theme.RetroRed
import com.example.ui.theme.RetroWindowBevel
import com.example.ui.theme.RetroWindowBorder
import com.example.ui.theme.RetroWindowHighlight

/**
 * Custom 16-bit stepped pixel shape that enforces hard 90-degree stair-step corners
 * matching classic GBA / MOTHER 3 cartridge windows without modern rounded anti-aliasing.
 */
class PixelStepShape(private val step: Dp = 4.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = with(density) { step.toPx() }
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(s, 0f)
            lineTo(w - s, 0f)
            lineTo(w - s, s)
            lineTo(w, s)
            lineTo(w, h - s)
            lineTo(w - s, h - s)
            lineTo(w - s, h)
            lineTo(s, h)
            lineTo(s, h - s)
            lineTo(0f, h - s)
            lineTo(0f, s)
            lineTo(s, s)
            close()
        }
        return Outline.Generic(path)
    }
}

private val ditherBrushCache = java.util.concurrent.ConcurrentHashMap<Triple<Int, Int, Int>, ShaderBrush>()

private fun getDitherBrush(colorA: Color, colorB: Color, tileSizePx: Int): ShaderBrush {
    val s = tileSizePx.coerceIn(2, 32)
    val key = Triple(colorA.toArgb(), colorB.toArgb(), s)
    return ditherBrushCache.getOrPut(key) {
        val bmpSize = s * 2
        val bmp = android.graphics.Bitmap.createBitmap(bmpSize, bmpSize, android.graphics.Bitmap.Config.ARGB_8888)
        val argbA = colorA.toArgb()
        val argbB = colorB.toArgb()
        val pixels = IntArray(bmpSize * bmpSize)
        for (y in 0 until bmpSize) {
            val isRowA = (y / s) % 2 == 0
            for (x in 0 until bmpSize) {
                val isColA = (x / s) % 2 == 0
                pixels[y * bmpSize + x] = if (isRowA == isColA) argbA else argbB
            }
        }
        bmp.setPixels(pixels, 0, bmpSize, 0, 0, bmpSize, bmpSize)
        val imageBitmap = bmp.asImageBitmap()
        val shader = ImageShader(imageBitmap, TileMode.Repeated, TileMode.Repeated)
        ShaderBrush(shader)
    }
}

/**
 * GBA Checkerboard Dithering Pattern (Ultra-fast GPU-accelerated repeated shader)
 * Eliminates tens of thousands of CPU drawRect calls and prevents SurfaceSyncGroup ANR / timeout.
 */
fun Modifier.checkerboardDithering(
    tileDp: Dp = 3.dp,
    colorA: Color,
    colorB: Color
): Modifier = this.drawBehind {
    val tileSizePx = tileDp.toPx().toInt().coerceAtLeast(1)
    val brush = getDitherBrush(colorA, colorB, tileSizePx)
    drawRect(brush = brush, size = size)
}

/**
 * Custom draw modifier rendering MOTHER 3 layered 3D window bevels with crisp pixel borders.
 */
fun Modifier.pixelBevel(
    borderWidth: Dp = 3.dp,
    outerColor: Color = GbaCardFrameDark,
    highlightColor: Color = RetroWindowHighlight,
    shadowColor: Color = RetroWindowBevel,
    backgroundColor: Color = Color.Transparent
): Modifier = this.drawBehind {
    val b = borderWidth.toPx()
    val w = size.width
    val h = size.height

    if (backgroundColor != Color.Transparent) {
        drawRect(color = backgroundColor, size = size)
    }

    // Outer dark frame
    drawRect(color = outerColor, topLeft = Offset(0f, 0f), size = Size(w, b))
    drawRect(color = outerColor, topLeft = Offset(0f, h - b), size = Size(w, b))
    drawRect(color = outerColor, topLeft = Offset(0f, 0f), size = Size(b, h))
    drawRect(color = outerColor, topLeft = Offset(w - b, 0f), size = Size(b, h))

    // Inner highlight (Top & Left)
    drawRect(color = highlightColor, topLeft = Offset(b, b), size = Size(w - 2 * b, b))
    drawRect(color = highlightColor, topLeft = Offset(b, b), size = Size(b, h - 2 * b))

    // Inner shadow (Bottom & Right)
    drawRect(color = shadowColor, topLeft = Offset(b, h - 2 * b), size = Size(w - 2 * b, b))
    drawRect(color = shadowColor, topLeft = Offset(w - 2 * b, b), size = Size(b, h - 2 * b))
}

@Composable
fun PixelWindow(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF7F3E8),
    borderWidth: Dp = 3.dp,
    step: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(PixelStepShape(step))
            .background(backgroundColor)
            .pixelBevel(
                borderWidth = borderWidth,
                outerColor = GbaCardFrameDark,
                highlightColor = RetroWindowHighlight,
                shadowColor = RetroWindowBevel
            )
    ) {
        content()
    }
}

@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = RetroRed,
    textColor: Color = Color.White,
    step: Dp = 4.dp,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val yOffset = if (isPressed) 2.dp else 0.dp

    Box(
        modifier = modifier
            .offset { IntOffset(0, yOffset.roundToPx()) }
            .clip(PixelStepShape(step))
            .background(if (enabled) backgroundColor else Color.Gray)
            .pixelBevel(
                borderWidth = 2.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = if (isPressed) RetroWindowBevel else RetroWindowHighlight,
                shadowColor = if (isPressed) RetroWindowHighlight else Color(0xFF1E140D)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun PixelBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(PixelStepShape(2.dp))
            .background(backgroundColor)
            .pixelBevel(
                borderWidth = 1.5.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = Color(0x66FFFFFF),
                shadowColor = Color(0x66000000)
            )
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = FusionPixelFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PixelRatingStars(
    rating: Int,
    maxRating: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            val isGold = i <= rating
            // Draw authentic 16-bit chunky pixel star
            Canvas(modifier = Modifier.size(10.dp).padding(1.dp)) {
                val c = if (isGold) RetroGold else Color(0xFF555555)
                // Draw 5-point retro star with hard pixel steps
                val w = size.width
                val h = size.height
                val p = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    lineTo(w * 0.65f, h * 0.35f)
                    lineTo(w, h * 0.38f)
                    lineTo(w * 0.73f, h * 0.65f)
                    lineTo(w * 0.82f, h)
                    lineTo(w * 0.5f, h * 0.78f)
                    lineTo(w * 0.18f, h)
                    lineTo(w * 0.27f, h * 0.65f)
                    lineTo(0f, h * 0.38f)
                    lineTo(w * 0.35f, h * 0.35f)
                    close()
                }
                drawPath(path = p, color = c)
                drawPath(path = p, color = GbaCardFrameDark, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
            }
        }
    }
}
