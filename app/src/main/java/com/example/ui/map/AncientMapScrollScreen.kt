package com.example.ui.map

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.PathParser
import com.example.R
import com.example.data.DistrictSvgItem
import com.example.data.HKSvgMapData
import com.example.data.HongKongDistricts
import com.example.data.RestaurantCardEntity
import com.example.ui.pixel.PixelButton
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.pixelBevel
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.GbaCardFrameDark
import com.example.ui.theme.GbaHeaderBg
import com.example.ui.theme.GbaScrollPaper
import com.example.ui.theme.RetroGold
import com.example.ui.theme.RetroRed
import com.example.ui.theme.RetroWindowBevel
import com.example.ui.theme.RetroWindowHighlight

private data class CachedDistrictPath(
    val district: DistrictSvgItem,
    val composePath: Path,
    val bounds: RectF
)

sealed class MythLegend(
    val title: String,
    val subtitle: String,
    val regionNode: String,
    val imageRes: Int,
    val story: String,
    val foodConnection: String
) {
    data object LionRock : MythLegend(
        title = "獅子山・鎮龍神獅",
        subtitle = "九龍半島山脈節點 (The Guardian Lion of Lion Rock)",
        regionNode = "九龍半島山脊 (黃大仙 / 沙田交界)",
        imageRes = R.drawable.img_lion_rock,
        story = "相傳九龍半島原有九條惡龍出沒為禍人間，天帝憐憫眾生，特遣守護神獅落凡鎮壓。神獅威嚴俯臥於山巔，前爪將第九條惡龍之龍尾與龍脊長鎮於山嶽之下，雙雙化作橫跨九龍之巍峨山脈。這座鎮龍巨獅亦化為今日香港人堅毅開拓、同舟共濟之『獅子山精神』。",
        foodConnection = "【獅子山下美食巡禮】：九龍城潮汕功夫牛肉火鍋、黃大仙大排檔鑊氣小炒、深水埗老字號豆品麵食，承載世代相傳的庶民經典滋味！"
    )

    data object LoTing : MythLegend(
        title = "大嶼海域・盧亭魚人",
        subtitle = "大嶼山/大澳海洋節點 (Lo Ting - Merfolk of Lantau)",
        regionNode = "大嶼山西南海域 (大澳 / 萬山海域)",
        imageRes = R.drawable.img_lo_ting,
        story = "古代大航海羊皮紙地圖常標註『Here Be Monsters』（此處有海怪）。香港自古流傳大奚山（即今日大嶼山）深海之中居有一支半人半魚之古老海洋族群——『盧亭』。傳聞盧亭族人身披魚鱗、黃髮赤眼、入水如飛，乘風破浪，在大澳水鄉與萬山群島之碧波中繁衍至今，是大嶼山悠久海神神話之化身。",
        foodConnection = "【大嶼海味珍饈】：大澳傳統石磨蝦膏蝦醬、避風塘炭火烤海鮮、南丫島水產鮮魷，融合大自然與海洋風韻的地道港味！"
    )
}

/**
 * 16-bit Antique Parchment Map with Hong Kong Mythological Cartographic Monsters:
 * 1. 獅子山鎮龍巨獅 (The Guardian Lion of Lion Rock) pinned at Kowloon mountain ridge.
 * 2. 大嶼山盧亭魚人 (Lo Ting - Merfolk of Lantau) emerging from ocean waves.
 * 3. 16-bit retro parchment aesthetics (isAntiAlias = false, weathered burnt edges, wave lines).
 * 4. Tap either creature to inspect its legendary lore and culinary connection!
 */
@Composable
fun AncientMapScrollScreen(
    cards: List<RestaurantCardEntity>,
    modifier: Modifier = Modifier
) {
    val totalDistricts = HongKongDistricts.ALL_DISTRICTS.size
    val visitedCount = HongKongDistricts.ALL_DISTRICTS.count { district ->
        cards.any { it.district == district.name || district.name.startsWith(it.district) }
    }

    var selectedLegend by remember { mutableStateOf<MythLegend?>(null) }

    // Pre-cache parsed vector paths for performance
    val cachedPaths = remember {
        HKSvgMapData.districts.map { item ->
            val androidPath = android.graphics.Path()
            item.paths.forEach { spec ->
                try {
                    val p = PathParser.createPathFromPathData(spec.d)
                    if (spec.dx != 0f || spec.dy != 0f) {
                        val m = Matrix()
                        m.postTranslate(spec.dx, spec.dy)
                        p.transform(m)
                    }
                    androidPath.addPath(p)
                } catch (_: Exception) {
                }
            }
            val rect = RectF()
            androidPath.computeBounds(rect, true)
            CachedDistrictPath(
                district = item,
                composePath = androidPath.asComposePath(),
                bounds = rect
            )
        }
    }

    fun getCount(district: DistrictSvgItem): Int {
        return cards.count { card ->
            card.district == district.name ||
                    card.district == district.shortName ||
                    district.name.startsWith(card.district) ||
                    card.district.startsWith(district.shortName)
        }
    }

    fun getParchmentHeatColor(count: Int): Color {
        return when (count) {
            0 -> Color(0xFFD7D9DB) // Unvisited aged slate-parchment
            1 -> Color(0xFFF9E79F) // Parchment warm yellow (1 restaurant)
            2 -> Color(0xFFF39C12) // Golden amber (2 restaurants)
            3 -> Color(0xFFE65100) // Deep dark orange (3 restaurants)
            else -> Color(0xFFC0392B) // Intense vermilion red (4+ restaurants)
        }
    }

    // Full-page ancient parchment canvas (centered Hong Kong map + mythological creatures)
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 76.dp)
            .background(Color(0xFF2C241E)), // Dark desk behind ancient scroll
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            // Scale map to cover the full screen generously and center it exactly
            val usableWidth = (canvasWidth - 16f).coerceAtLeast(100f)
            val usableHeight = (canvasHeight - 16f).coerceAtLeast(100f)
            val scale = minOf(
                usableWidth / HKSvgMapData.VIEWBOX_WIDTH,
                usableHeight / HKSvgMapData.VIEWBOX_HEIGHT
            ) * 0.98f

            val offsetX = (canvasWidth - HKSvgMapData.VIEWBOX_WIDTH * scale) / 2f
            val offsetY = (canvasHeight - HKSvgMapData.VIEWBOX_HEIGHT * scale) / 2f

            // 1. Draw Map Canvas with antique parchment, ocean wave lines, and districts
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Aged Weathered Parchment Paper filling full screen
                drawRect(
                    color = GbaScrollPaper,
                    topLeft = Offset(4f, 4f),
                    size = Size(w - 8f, h - 8f)
                )

                // Weathered ragged, tattered, burnt paper edges (MS Paint stepped pixel cuts)
                val burntCharcoal = Color(0xFF3E2723)
                val burntDarkBrown = Color(0xFF5D4037)
                val burntLightBrown = Color(0xFF8D6E63)
                val pixelTile = 6f

                // Top & Bottom ragged edges
                for (x in 0 until w.toInt() step pixelTile.toInt()) {
                    val noiseTop = ((x * 19 + 7) % 11).toFloat()
                    val noiseBot = ((x * 23 + 13) % 11).toFloat()

                    drawRect(
                        color = if (x % 18 == 0) burntCharcoal else if (x % 12 == 0) burntDarkBrown else burntLightBrown,
                        topLeft = Offset(x.toFloat(), 0f),
                        size = Size(pixelTile, 4f + noiseTop)
                    )
                    drawRect(
                        color = if (x % 18 == 0) burntCharcoal else if (x % 12 == 0) burntDarkBrown else burntLightBrown,
                        topLeft = Offset(x.toFloat(), h - (4f + noiseBot)),
                        size = Size(pixelTile, 4f + noiseBot)
                    )
                }

                // Left & Right ragged edges
                for (y in 0 until h.toInt() step pixelTile.toInt()) {
                    val noiseLeft = ((y * 17 + 5) % 11).toFloat()
                    val noiseRight = ((y * 29 + 11) % 11).toFloat()

                    drawRect(
                        color = if (y % 18 == 0) burntCharcoal else if (y % 12 == 0) burntDarkBrown else burntLightBrown,
                        topLeft = Offset(0f, y.toFloat()),
                        size = Size(4f + noiseLeft, pixelTile)
                    )
                    drawRect(
                        color = if (y % 18 == 0) burntCharcoal else if (y % 12 == 0) burntDarkBrown else burntLightBrown,
                        topLeft = Offset(w - (4f + noiseRight), y.toFloat()),
                        size = Size(4f + noiseRight, pixelTile)
                    )
                }

                // Vintage Coordinate Grid in sepia ink
                val gridColor = Color(0x184A2E18)
                val gridStep = 44.dp.toPx()
                var gx = 16.dp.toPx()
                while (gx < w - 16.dp.toPx()) {
                    drawLine(color = gridColor, start = Offset(gx, 10f), end = Offset(gx, h - 10f), strokeWidth = 1.2f)
                    gx += gridStep
                }
                var gy = 16.dp.toPx()
                while (gy < h - 16.dp.toPx()) {
                    drawLine(color = gridColor, start = Offset(10f, gy), end = Offset(w - 10f, gy), strokeWidth = 1.2f)
                    gy += gridStep
                }

                // Cartographic Ocean Waves around South/West waters (Here Be Monsters zone)
                drawIntoCanvas { canvas ->
                    val wavePaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#4A6B82")
                        textSize = 22f * (scale / 0.5f).coerceIn(0.6f, 1.4f)
                        isFakeBoldText = true
                        isAntiAlias = false
                    }
                    val seaLabelPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#3B596A")
                        textSize = 18f * (scale / 0.5f).coerceIn(0.6f, 1.4f)
                        isFakeBoldText = true
                        isAntiAlias = false
                    }

                    // Stepped ocean ripple symbols: ~ ~ ~
                    val waveBaseX = offsetX + 160f * scale
                    val waveBaseY = offsetY + 680f * scale
                    canvas.nativeCanvas.drawText("~ ~ ~ ~ ~", waveBaseX, waveBaseY, wavePaint)
                    canvas.nativeCanvas.drawText("~ ~ ~ ~ ~", waveBaseX + 60f * scale, waveBaseY + 36f * scale, wavePaint)
                    canvas.nativeCanvas.drawText("~ ~ ~ ~ ~", waveBaseX - 30f * scale, waveBaseY + 72f * scale, wavePaint)
                    canvas.nativeCanvas.drawText("[HERE BE MONSTERS]", waveBaseX - 10f * scale, waveBaseY + 110f * scale, seaLabelPaint)

                    // Mountain ridge marks near Lion Rock
                    val mtnPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#5D4037")
                        textSize = 20f * (scale / 0.5f).coerceIn(0.6f, 1.4f)
                        isFakeBoldText = true
                        isAntiAlias = false
                    }
                    val mtnBaseX = offsetX + 820f * scale
                    val mtnBaseY = offsetY + 465f * scale
                    canvas.nativeCanvas.drawText("▲ ▲ ▲", mtnBaseX, mtnBaseY, mtnPaint)
                }

                // Top Seal Banner: Exploration progress stamp
                drawIntoCanvas { canvas ->
                    val stampWidth = 180.dp.toPx()
                    val stampHeight = 44.dp.toPx()
                    val stampLeft = 14.dp.toPx()
                    val stampTop = 14.dp.toPx()
                    val stampRight = stampLeft + stampWidth
                    val stampBottom = stampTop + stampHeight

                    val bgPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#E4D4BC")
                        style = Paint.Style.FILL
                        isAntiAlias = false
                    }
                    val borderPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#4E342E")
                        style = Paint.Style.STROKE
                        strokeWidth = 2.dp.toPx()
                        isAntiAlias = false
                    }
                    canvas.nativeCanvas.drawRect(stampLeft, stampTop, stampRight, stampBottom, bgPaint)
                    canvas.nativeCanvas.drawRect(stampLeft, stampTop, stampRight, stampBottom, borderPaint)

                    val sealPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#4E342E")
                        textSize = 13.sp.toPx()
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = false
                    }
                    canvas.nativeCanvas.drawText("香港全境食足跡", stampRight - stampWidth / 2f, stampTop + 18.dp.toPx(), sealPaint)

                    val subSealPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#8D6E63")
                        textSize = 10.sp.toPx()
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = false
                    }
                    canvas.nativeCanvas.drawText("已探索：$visitedCount / $totalDistricts 區", stampRight - stampWidth / 2f, stampTop + 34.dp.toPx(), subSealPaint)
                }

                // Draw the Centered Hong Kong 18 Districts Map
                drawIntoCanvas { canvas ->
                    canvas.save()
                    canvas.translate(offsetX, offsetY)
                    canvas.scale(scale, scale)

                    val strokePaint = Paint().apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 2.8f / scale
                        color = android.graphics.Color.parseColor("#3A281E")
                        isAntiAlias = false
                    }

                    val fillPaint = Paint().apply {
                        style = Paint.Style.FILL
                        isAntiAlias = false
                    }

                    val labelPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#2A1B14")
                        textSize = 46f
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = false
                    }

                    val countBadgePaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#B71C1C")
                        textSize = 34f
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = false
                    }

                    cachedPaths.forEach { cached ->
                        val count = getCount(cached.district)
                        val heatColor = getParchmentHeatColor(count)

                        fillPaint.color = android.graphics.Color.argb(
                            (heatColor.alpha * 255).toInt(),
                            (heatColor.red * 255).toInt(),
                            (heatColor.green * 255).toInt(),
                            (heatColor.blue * 255).toInt()
                        )

                        val nativeP = (cached.composePath as androidx.compose.ui.graphics.AndroidPath).internalPath
                        canvas.nativeCanvas.drawPath(nativeP, fillPaint)
                        canvas.nativeCanvas.drawPath(nativeP, strokePaint)

                        val lx = cached.district.labelX
                        val ly = cached.district.labelY

                        if (count > 0) {
                            canvas.nativeCanvas.drawText(cached.district.name, lx, ly - 12f, labelPaint)
                            canvas.nativeCanvas.drawText("$count 間", lx, ly + 28f, countBadgePaint)
                        } else {
                            canvas.nativeCanvas.drawText(cached.district.name, lx, ly + 10f, labelPaint)
                        }
                    }

                    canvas.restore()
                }
            }

            // 2. Mythical Creature Icon A: 獅子山鎮龍巨獅 (The Guardian Lion of Lion Rock)
            // Positioned near Kowloon mountain ridge: (840f, 490f) in SVG coordinates
            val lionX = (offsetX + 820f * scale).toInt()
            val lionY = (offsetY + 450f * scale).toInt()
            val creatureSizeDp = (54f * (scale / 0.35f).coerceIn(0.85f, 1.4f)).dp

            Box(
                modifier = Modifier
                    .offset { IntOffset(lionX - 45, lionY - 45) }
                    .clickable { selectedLegend = MythLegend.LionRock }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(creatureSizeDp)
                            .clip(PixelStepShape(3.dp))
                            .background(Color(0xFFE8DCC8))
                            .border(2.dp, Color(0xFF4A3425), PixelStepShape(3.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lion_rock),
                            contentDescription = "獅子山鎮龍巨獅",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .clip(PixelStepShape(2.dp))
                            .background(Color(0xFF4A3425))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "【獅子山・鎮龍獅】",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFF7DB)
                        )
                    }
                }
            }

            // 3. Mythical Creature Icon B: 大嶼海域盧亭魚人 (Lo Ting Merfolk)
            // Positioned in Lantau Sea / Tai O waters: (230f, 750f) in SVG coordinates
            val loTingX = (offsetX + 220f * scale).toInt()
            val loTingY = (offsetY + 710f * scale).toInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(loTingX - 45, loTingY - 45) }
                    .clickable { selectedLegend = MythLegend.LoTing }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(creatureSizeDp)
                            .clip(PixelStepShape(3.dp))
                            .background(Color(0xFFD4E6F1))
                            .border(2.dp, Color(0xFF1B4F72), PixelStepShape(3.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lo_ting),
                            contentDescription = "大嶼海域盧亭魚人",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .clip(PixelStepShape(2.dp))
                            .background(Color(0xFF1B4F72))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "【大嶼・盧亭魚人】",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEBF5FB)
                        )
                    }
                }
            }
        }
    }

    // 4. Mythical Lore Detail Modal Dialog
    selectedLegend?.let { legend ->
        Dialog(
            onDismissRequest = { selectedLegend = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(PixelStepShape(4.dp))
                    .background(Color(0xFFF4ECE1))
                    .pixelBevel(
                        borderWidth = 2.5.dp,
                        outerColor = GbaCardFrameDark,
                        highlightColor = RetroWindowHighlight,
                        shadowColor = RetroWindowBevel
                    )
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Header Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GbaHeaderBg)
                            .pixelBevel(
                                borderWidth = 2.dp,
                                outerColor = GbaCardFrameDark,
                                highlightColor = Color(0x33FFFFFF),
                                shadowColor = Color(0x66000000)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "香港古地圖神話考察",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            IconButton(
                                onClick = { selectedLegend = null },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "關閉",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Title & Subtitle
                        Text(
                            text = legend.title,
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark
                        )
                        Text(
                            text = legend.subtitle,
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFF6E5D4F)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large 16-bit Mythical Creature Artwork Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(PixelStepShape(4.dp))
                                .background(Color(0xFF2C241E))
                                .border(2.5.dp, GbaCardFrameDark, PixelStepShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = legend.imageRes),
                                contentDescription = legend.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Region Node Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PixelStepShape(2.dp))
                                .background(Color(0xFFE9DFCE))
                                .border(1.dp, GbaCardFrameDark, PixelStepShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "地圖座標節點: ${legend.regionNode}",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Lore Story Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PixelStepShape(3.dp))
                                .background(Color(0xFFFAF6EE))
                                .border(1.5.dp, GbaCardFrameDark, PixelStepShape(3.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "【古地圖神話記載】",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RetroRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = legend.story,
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF2E241E)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = legend.foodConnection,
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Close button
                        PixelButton(
                            onClick = { selectedLegend = null },
                            backgroundColor = RetroGold,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "返回古地圖考察",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GbaCardFrameDark
                            )
                        }
                    }
                }
            }
        }
    }
}
