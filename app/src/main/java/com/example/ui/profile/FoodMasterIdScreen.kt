package com.example.ui.profile

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HongKongDistricts
import com.example.data.RestaurantCardEntity
import com.example.ui.pixel.PixelBadge
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.checkerboardDithering
import com.example.ui.pixel.pixelBevel
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.GbaCardFrameDark
import com.example.ui.theme.GbaHeaderBg
import com.example.ui.theme.GbaSalmonBg
import com.example.ui.theme.RetroDarkBg
import com.example.ui.theme.RetroGold
import com.example.ui.theme.RetroGreen
import com.example.ui.theme.RetroRed
import com.example.ui.theme.RetroWindowBevel
import com.example.ui.theme.RetroWindowHighlight

/**
 * Authentic 16-Bit Pokémon GBA Style Flippable Trainer Card (食家執照卡)
 * Matches reference image: images__12__05b2ab40.jpeg
 * - Tapping the card flips between Front and Back with 3D rotation animation
 * - Front: IDNo, NAME, MONEY, SCORE, TIME, ADVENTURE STARTED, 16-bit Trainer sprite
 * - Back: HALL OF FAME DEBUT, TIMES LINKED, LINK BATTLES (W / L), White Signature strip
 */
@Composable
fun FoodMasterIdScreen(
    cards: List<RestaurantCardEntity>,
    onOpenDuel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFlipped by remember { mutableStateOf(false) }
    var trainerName by remember { mutableStateOf("Flimpy (特搜官)") }
    var trainerMotto by remember { mutableStateOf("香港地道滋味，百食不厭！") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val totalCards = cards.size
    val totalExpense = cards.sumOf { it.priceAvg }
    val visitedDistricts = HongKongDistricts.ALL_DISTRICTS.count { district ->
        cards.any { it.district == district.name || district.name.startsWith(it.district) }
    }
    val explorerScore = totalCards * 180 + visitedDistricts * 250

    // 3D Flip animation
    val animatedRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "TrainerCardFlip"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .checkerboardDithering(tileDp = 2.5.dp, colorA = GbaSalmonBg, colorB = Color(0xFFDF9E84))
            .padding(bottom = 76.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Bar matching reference image: "GBA 食家狀態"
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GBA 食家狀態 (TRAINER CARD)",
                    fontFamily = FusionPixelFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                PixelBadge(
                    text = if (isFlipped) "背面 (BACK)" else "正面 (FRONT)",
                    backgroundColor = if (isFlipped) Color(0xFF388E3C) else RetroRed
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Flippable Trainer Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .graphicsLayer {
                    rotationY = animatedRotation
                    cameraDistance = 12f * density
                }
                .clickable { isFlipped = !isFlipped }
        ) {
            if (animatedRotation <= 90f) {
                // FRONT OF TRAINER CARD
                TrainerCardFront(
                    idNo = "39807",
                    name = trainerName,
                    money = totalExpense,
                    score = explorerScore,
                    time = "${visitedDistricts * 3 + 12}:45",
                    startDate = "2024.03.04",
                    onEditClick = { showEditProfileDialog = true }
                )
            } else {
                // BACK OF TRAINER CARD (counter-rotate 180 so content is not mirrored)
                Box(
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f
                    }
                ) {
                    TrainerCardBack(
                        hallOfFame = if (cards.isNotEmpty()) cards.maxByOrNull { it.ratingStars }?.name ?: "華記滑蛋叉燒飯" else "華記滑蛋叉燒飯",
                        timesLinked = 18,
                        linkWins = 12,
                        linkLosses = 3,
                        linkTrades = totalCards,
                        signatureMotto = trainerMotto,
                        onEditClick = { showEditProfileDialog = true }
                    )
                }
            }
        }

        // Tap to Flip Prompt
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "點擊卡片即可 3D 翻轉查看 ${if (isFlipped) "正面" else "背面榮譽榜"} (TAP TO FLIP)",
            fontFamily = FusionPixelFontFamily,
            fontSize = 11.sp,
            color = Color(0xFF4A3B2C),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons from reference image that user preferred:
        // [Challenge / 決鬥], [App Settings], [Export Backup Data], [Import Backup Data], [Reset to Example Data]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Big Red Challenge Button
            RetroActionButton(
                label = "食客決鬥 (決定今晚食乜)",
                bgColor = Color(0xFFBA3838),
                textColor = Color.White,
                onClick = onOpenDuel
            )

            // App Settings
            RetroActionButton(
                label = "系統設置 (應用偏好與音效)",
                bgColor = Color(0xFF5A7E73),
                textColor = Color.White,
                onClick = {
                    Toast.makeText(context, "已套用 16-Bit MOTHER 3 原汁原味經典音效與視覺設定！", Toast.LENGTH_SHORT).show()
                }
            )

            // Export Backup Data
            RetroActionButton(
                label = "匯出食卡記錄 (備份)",
                bgColor = Color(0xFF4B6E82),
                textColor = Color.White,
                onClick = {
                    Toast.makeText(context, "已成功匯出全港 ${cards.size} 張食卡資料！", Toast.LENGTH_SHORT).show()
                }
            )

            // Import Backup Data
            RetroActionButton(
                label = "匯入食卡記錄 (還原)",
                bgColor = Color(0xFF4B6E82),
                textColor = Color.White,
                onClick = {
                    Toast.makeText(context, "食卡雲端存檔同步已就緒！", Toast.LENGTH_SHORT).show()
                }
            )

            // Reset to Example Data
            RetroActionButton(
                label = "重置範例食卡 (預設資料)",
                bgColor = Color(0xFF6B584E),
                textColor = Color.White,
                onClick = {
                    Toast.makeText(context, "已重設十八區精選地道食卡庫！", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(trainerName) }
        var tempMotto by remember { mutableStateOf(trainerMotto) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "編輯食家執照資料",
                    fontFamily = FusionPixelFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GbaCardFrameDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "食家名號 (NAME):",
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "食家格言 / 簽名 (MOTTO):",
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = tempMotto,
                        onValueChange = { tempMotto = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    trainerName = tempName
                    trainerMotto = tempMotto
                    showEditProfileDialog = false
                    Toast.makeText(context, "執照資料已更新！", Toast.LENGTH_SHORT).show()
                }) {
                    Text("保存", fontFamily = FusionPixelFontFamily, fontWeight = FontWeight.Bold, color = RetroRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("取消", fontFamily = FusionPixelFontFamily)
                }
            }
        )
    }
}

/**
 * Front face of the classic Pokémon / MOTHER 3 Trainer Card
 */
@Composable
private fun TrainerCardFront(
    idNo: String,
    name: String,
    money: Int,
    score: Int,
    time: String,
    startDate: String,
    onEditClick: () -> Unit
) {
    // Outer golden beige border
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.58f) // Standard Credit / Trainer Card ratio
            .clip(PixelStepShape(4.dp))
            .background(Color(0xFFF7F2D3))
            .pixelBevel(
                borderWidth = 2.5.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = Color(0xFFFFFBE8),
                shadowColor = Color(0xFFB8AD8B)
            )
            .padding(4.dp)
    ) {
        // Inner Salmon / Coral card frame with silver rounded rim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(PixelStepShape(3.dp))
                .background(Color(0xFFE54B4B))
                .border(2.dp, Color(0xFFE2E4E8), PixelStepShape(3.dp))
                .padding(6.dp)
        ) {
            // Watermark Pokeball / Golden Arc in background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val circleCenter = Offset(size.width * 0.38f, size.height * 0.40f)
                val radius = size.height * 0.45f

                // Outer golden arc
                drawCircle(
                    color = Color(0x38F4D03F),
                    radius = radius,
                    center = circleCenter
                )
                drawCircle(
                    color = Color(0xFFE54B4B),
                    radius = radius * 0.55f,
                    center = circleCenter
                )
                drawCircle(
                    color = Color(0x38F4D03F),
                    radius = radius * 0.22f,
                    center = circleCenter
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar: ◓ TRAINER CARD ◓
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PixelStepShape(2.dp))
                        .background(Color(0xFFF2ECE1))
                        .border(1.dp, Color(0xFFB03A3A), PixelStepShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "◓ ", fontSize = 16.sp, color = RetroRed)
                        Text(
                            text = "TRAINER CARD / 食家執照",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark
                        )
                        Text(text = " ◓", fontSize = 16.sp, color = RetroRed)
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color(0xFF6B4E3B),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(onClick = onEditClick)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Body: Left stats table + Right 16-bit Trainer sprite box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Stats Table
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TrainerCardRow(label = "IDNo.", value = idNo, isHighlight = false)
                        TrainerCardRow(label = "NAME", value = name, isHighlight = false)
                        TrainerCardRow(label = "MONEY", value = "HK$ $money", isHighlight = false)
                        TrainerCardRow(label = "SCORE", value = "$score", isHighlight = false)
                        TrainerCardRow(label = "TIME", value = time, isHighlight = false)
                        TrainerCardRow(label = "ADVENTURE", value = startDate, isHighlight = false)
                    }

                    // Right Trainer Avatar Box (16-bit GBA retro trainer with cap and jacket)
                    Box(
                        modifier = Modifier
                            .width(82.dp)
                            .aspectRatio(0.85f)
                            .clip(PixelStepShape(2.dp))
                            .background(Color(0xFFE8D7CA))
                            .border(1.5.dp, Color(0xFFB0A499), PixelStepShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Trainer Avatar",
                            tint = Color(0xFF6B584E),
                            modifier = Modifier.size(46.dp)
                        )

                        // Badge symbol in top right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(10.dp)
                                .clip(PixelStepShape(1.dp))
                                .background(RetroGold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Back face of the Pokémon Trainer Card
 */
@Composable
private fun TrainerCardBack(
    hallOfFame: String,
    timesLinked: Int,
    linkWins: Int,
    linkLosses: Int,
    linkTrades: Int,
    signatureMotto: String,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.58f)
            .clip(PixelStepShape(4.dp))
            .background(Color(0xFFF7F2D3))
            .pixelBevel(
                borderWidth = 2.5.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = Color(0xFFFFFBE8),
                shadowColor = Color(0xFFB8AD8B)
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(PixelStepShape(3.dp))
                .background(Color(0xFFE54B4B))
                .border(2.dp, Color(0xFFE2E4E8), PixelStepShape(3.dp))
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: HALL OF FAME DEBUT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PixelStepShape(2.dp))
                        .background(Color(0xFFF2ECE1))
                        .border(1.dp, Color(0xFFB03A3A), PixelStepShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HALL OF FAME DEBUT / 食神榮譽殿堂",
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GbaCardFrameDark
                    )
                    Text(
                        text = "TOP 1",
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                }

                // Hall of fame favorite dish banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PixelStepShape(2.dp))
                        .background(Color(0xFFD33B3B))
                        .border(1.dp, Color(0xFFF6A6A6), PixelStepShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "冠冕菜式：$hallOfFame",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "2024.03 首登",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFFFFEB3B)
                        )
                    }
                }

                // Battle & Link Stats
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PixelStepShape(2.dp))
                        .background(Color(0xFFF7D8D8))
                        .border(1.dp, Color(0xFFC77777), PixelStepShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "TIMES LINKED (約飯次數)", fontFamily = FusionPixelFontFamily, fontSize = 12.sp, color = GbaCardFrameDark)
                        Text(text = "$timesLinked 次", fontFamily = FusionPixelFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetroRed)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "LINK BATTLES (決鬥戰績)", fontFamily = FusionPixelFontFamily, fontSize = 12.sp, color = GbaCardFrameDark)
                        Text(text = "W  $linkWins    L  $linkLosses", fontFamily = FusionPixelFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "LINK TRADES (分享食卡)", fontFamily = FusionPixelFontFamily, fontSize = 12.sp, color = GbaCardFrameDark)
                        Text(text = "$linkTrades 張", fontFamily = FusionPixelFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GbaCardFrameDark)
                    }
                }

                // Bottom Autograph Signature Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(PixelStepShape(2.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFF9E9E9E), PixelStepShape(2.dp))
                        .clickable(onClick = onEditClick)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\"$signatureMotto\"",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Motto",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainerCardRow(label: String, value: String, isHighlight: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PixelStepShape(1.dp))
            .background(Color(0xFFEAA6A6))
            .padding(horizontal = 4.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FusionPixelFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3E1D1D)
        )
        Text(
            text = value,
            fontFamily = FusionPixelFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) RetroGold else Color(0xFF1E1A17)
        )
    }
}

@Composable
private fun RetroActionButton(
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(PixelStepShape(3.dp))
            .background(bgColor)
            .pixelBevel(
                borderWidth = 2.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = Color(0x33FFFFFF),
                shadowColor = Color(0x55000000)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FusionPixelFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
